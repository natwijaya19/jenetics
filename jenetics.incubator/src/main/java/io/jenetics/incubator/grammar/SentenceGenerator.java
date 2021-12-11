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

import static io.jenetics.incubator.grammar.StandardSentenceGenerator.Expansion.LEFT_TO_RIGHT;

import java.util.List;
import java.util.stream.Collectors;

import io.jenetics.incubator.grammar.Cfg.NonTerminal;
import io.jenetics.incubator.grammar.Cfg.Symbol;
import io.jenetics.incubator.grammar.Cfg.Terminal;

/**
 * This interface is used for creating <em>sentences</em> from a context-free
 * grammar ({@link Cfg}). A sentence is defined as list of {@link Terminal}s,
 * {@code List<Cfg.Terminal>}.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__!
 */
@FunctionalInterface
public interface SentenceGenerator {

	/**
	 * Generates a new sentence from the given grammar. If the generation of the
	 * sentence fails, an empty list is returned.
	 *
	 * @param cfg the generating grammar
	 * @return a newly created list of terminal symbols (sentence)
	 */
	List<Terminal> generate(final Cfg cfg);

	static String toString(final List<? extends Symbol> symbols) {
		return symbols.stream()
			.map(symbol -> symbol instanceof NonTerminal nt
				? "<%s>".formatted(nt)
				: symbol.value())
			.collect(Collectors.joining());
	}

	static SentenceGenerator of(final SymbolIndex index, final int limit) {
		return new StandardSentenceGenerator(index, LEFT_TO_RIGHT, limit);
	}

}
