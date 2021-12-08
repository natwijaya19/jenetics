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

import static io.jenetics.incubator.grammar.Sentence.expand;

import java.util.Optional;

import io.jenetics.incubator.grammar.Cfg.NonTerminal;
import io.jenetics.incubator.grammar.Cfg.Symbol;

import io.jenetics.ext.util.TreeNode;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__!
 */
public final class ParseTree {
	private ParseTree() {}

	public static TreeNode<Symbol> generate(
		final Cfg cfg,
		final SymbolIndex index
	) {
		final NonTerminal start = cfg.start();
		final TreeNode<Symbol> symbols = TreeNode.of(start);

		boolean expanded = true;
		while (expanded) {
			final Optional<TreeNode<Symbol>> tree = symbols.leaves()
				.filter(leaf -> leaf.value() instanceof NonTerminal)
				.filter(leaf -> cfg.rule((NonTerminal)leaf.value()).isPresent())
				.findFirst();

			tree.ifPresent(t ->
				expand(cfg, (NonTerminal)t.value(), index).forEach(t::attach)
			);

			expanded = tree.isPresent();
		}

		return symbols;
	}

}
