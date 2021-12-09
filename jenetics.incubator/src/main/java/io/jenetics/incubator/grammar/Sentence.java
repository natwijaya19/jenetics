/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.incubator.grammar;

import static java.lang.Integer.MAX_VALUE;
import static io.jenetics.incubator.grammar.Sentence.Expansion.LEFT_FIRST;
import static io.jenetics.incubator.grammar.Sentence.Expansion.LEFT_TO_RIGHT;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.Gene;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Codec;
import io.jenetics.incubator.grammar.Cfg.NonTerminal;
import io.jenetics.incubator.grammar.Cfg.Rule;
import io.jenetics.incubator.grammar.Cfg.Symbol;
import io.jenetics.incubator.grammar.Cfg.Terminal;
import io.jenetics.util.BaseSeq;
import io.jenetics.util.Factory;
import io.jenetics.util.IntRange;

/**
 * Standard implementation for creating sentences from a given grammar.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__!
 */
public final class Sentence {
	private Sentence() {}

	/**
	 * Enum of the used sentence expansion strategy.
	 */
	public enum Expansion {

		/**
		 * The symbol replacement always starting from the leftmost nonterminal
		 * as described in
		 * <a href="https://www.brinckerhoff.org/tmp/grammatica_evolution_ieee_tec_2001.pdf">
		 * Grammatical Evolution</a>.
		 */
		LEFT_FIRST {
			@Override
			boolean expand(List<Symbol> symbols, Cfg cfg, SymbolIndex index) {
				boolean expanded = false;

				final ListIterator<Symbol> sit = symbols.listIterator();
				while (sit.hasNext()) {
					if (sit.next() instanceof NonTerminal nt) {
						sit.remove();
						final List<Symbol> exp = Expansion.expand(cfg, nt, index);
						exp.forEach(sit::add);
						expanded = true;
					}
				}

				return expanded;
			}
		},

		/**
		 * The symbol replacement is performed from left to right and is repeated
		 * until all non-terminal symbol has been expanded.
		 */
		LEFT_TO_RIGHT {
			@Override
			boolean expand(List<Symbol> symbols, Cfg cfg, SymbolIndex index) {
				boolean expanded = false;

				final ListIterator<Symbol> sit = symbols.listIterator();
				while (sit.hasNext()) {
					if (sit.next() instanceof NonTerminal nt) {
						sit.remove();
						final List<Symbol> exp = Expansion.expand(cfg, nt, index);
						exp.forEach(sit::add);
						expanded = true;
					}
				}

				return expanded;
			}
		};


		void expand(
			final Cfg cfg,
			final SymbolIndex index,
			final List<Symbol> symbols,
			final int limit
		) {
			symbols.add(cfg.start());
			while (symbols.size() <= limit && expand(symbols, cfg, index));
		}

		abstract boolean expand(List<Symbol> symbols, Cfg cfg, SymbolIndex index);

		private static List<Symbol> expand(
			final Cfg cfg,
			final NonTerminal symbol,
			final SymbolIndex index
		) {
			return cfg.rule(symbol)
				.map(rule -> rule.alternatives()
					.get(index.next(rule.alternatives().size()))
					.symbols())
				.orElse(List.of(symbol));
		}
	}

	/**
	 * Generates a new sentence from the given grammar, <em>cfg</em>.
	 * <p>
	 * The following code snippet shows how to create a random sentence from a
	 * given grammar:
	 * <pre>{@code
	 * final Cfg cfg = Bnf.parse("""
	 *     <expr> ::= ( <expr> <op> <expr> ) | <num> | <var> |  <fun> ( <arg>, <arg> )
	 *     <fun>  ::= FUN1 | FUN2
	 *     <arg>  ::= <expr> | <var> | <num>
	 *     <op>   ::= + | - | * | /
	 *     <var>  ::= x | y
	 *     <num>  ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
	 *     """
	 * );
	 *
	 * final RandomGenerator random = RandomGenerator.of("L64X256MixRandom");
	 * final List<Terminal> sentence = Sentences.generate(
	 *     cfg, random::nextInt, LEFT_TO_RIGHT
	 * );
	 * final String string = sentence.stream()
	 *     .map(Symbol::value)
	 *     .collect(Collectors.joining());
	 *
	 * System.out.println(string);
	 * }</pre>
	 * <em>Some sample output:</em>
	 * <pre>{@code
	 * > ((x-FUN1(5,5))+8)
	 * > (FUN2(y,5)-FUN2(0,x))
	 * > x
	 * > FUN2(x,x)
	 * > 5
	 * > FUN2(y,FUN2((FUN1(5,FUN1(y,2))*9),y))
	 * > ((FUN1(x,5)*9)*(x/(y*FUN2(x,y))))
	 * > (9-(y*(x+x)))
	 * > }</pre>
	 *
	 * @see #generate(Cfg, SymbolIndex)
	 *
	 * @param cfg the generating grammar
	 * @param index the symbol index strategy
	 * @param expansion the expansion strategy to use for building the sentence
	 * @param limit the maximal number of symbols
	 * @return a newly created terminal list (sentence)
	 */
	public static List<Terminal> generate(
		final Cfg cfg,
		final SymbolIndex index,
		final Expansion expansion,
		final int limit
	) {
		final var sentence = new LinkedList<Symbol>();
		expand(cfg, index, sentence, expansion, limit);

		return sentence.stream()
			.map(Terminal.class::cast)
			.toList();
	}

	/**
	 * Generates a new sentence from the given grammar, <em>cfg</em>.
	 *
	 * @param cfg the generating grammar
	 * @param index the symbol index strategy
	 * @param expansion the expansion strategy to use for building the sentence
	 * @return a newly created terminal list (sentence)
	 */
	public static List<Terminal> generate(
		final Cfg cfg,
		final SymbolIndex index,
		final Expansion expansion
	) {
		return generate(cfg, index, expansion, MAX_VALUE);
	}

	/**
	 * Generates a new sentence from the given grammar, <em>cfg</em>.
	 *
	 * @param cfg the generating grammar
	 * @param index the symbol index strategy
	 * @return a newly created terminal list (sentence)
	 */
	public static List<Terminal> generate(
		final Cfg cfg,
		final SymbolIndex index
	) {
		return generate(cfg, index, LEFT_FIRST, MAX_VALUE);
	}

	/**
	 * Generates a new sentence from the given grammar, <em>cfg</em>.
	 *
	 * @param cfg the generating grammar
	 * @param index the symbol index strategy
	 * @return a newly created terminal list (sentence)
	 */
	public static List<Terminal> generate(
		final Cfg cfg,
		final SymbolIndex index,
		final int limit
	) {
		return generate(cfg, index, LEFT_FIRST, limit);
	}

	static void expand(
		final Cfg cfg,
		final SymbolIndex index,
		final List<Symbol> symbols,
		final Expansion expansion,
		final int limit
	) {
		symbols.add(cfg.start());

		boolean proceed = true;
		while (proceed) {
			proceed = false;

			final ListIterator<Symbol> sit = symbols.listIterator();
			while (sit.hasNext() && (expansion == LEFT_TO_RIGHT || !proceed)) {
				if (sit.next() instanceof NonTerminal nt) {
					sit.remove();
					final List<Symbol> exp = expand(cfg, nt, index);
					exp.forEach(sit::add);

					proceed = true;
				}
			}

			if (symbols.size() > limit) {
				symbols.clear();
				proceed = false;
			}
		}
	}



	static List<Symbol> expand(
		final Cfg cfg,
		final NonTerminal symbol,
		final SymbolIndex index
	) {
		return cfg.rule(symbol)
			.map(rule -> rule.alternatives()
				.get(index.next(rule.alternatives().size()))
				.symbols())
			.orElse(List.of(symbol));
	}
















	/**
	 * Codec for sentences, generated by a grammar.
	 *
	 * @param cfg the creating grammar
	 * @param factory the chromosome factory
	 * @param codons generation functions for codons of a gene sequence
	 * @return sentence codec
	 */
	public static <G extends Gene<?, G>> Codec<List<Terminal>, G> codec(
		final Cfg cfg,
		final Factory<? extends Chromosome<G>> factory,
		final Function<? super BaseSeq<G>, ? extends SymbolIndex> codons,
		final int limit
	) {
		return Codec.of(
			Genotype.of(factory, 1),
			gt -> generate(cfg, codons.apply(gt.chromosome()), limit)
		);
	}

	/**
	 * Codec for sentences, generated by a grammar.
	 *
	 * @param cfg the creating grammar
	 * @param domain the integer domain for the symbol index
	 * @param length the length of the chromosomes
	 * @return sentence codec
	 */
	public static Codec<List<Terminal>, IntegerGene> codec(
		final Cfg cfg,
		final IntRange domain,
		final IntRange length,
		final int limit
	) {
		return codec(
			cfg,
			IntegerChromosome.of(domain, length),
			Codons::ofIntegerGenes,
			limit
		);
	}

}
