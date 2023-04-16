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
package io.jenetics.incubator.property;

import java.io.IOException;

import org.testng.annotations.Test;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.GPX.Reader;

public class RecursivePropertyExtractorTest {

	@Test
	public void extract() throws IOException {
		final GPX gpx = Reader.DEFAULT.read(
			RecursivePropertyExtractorTest.class
				.getResourceAsStream("/Austria.gpx")
		);

		Properties.walk(new PathObject(gpx), "io.jenetics.*")
			.forEach(System.out::println);
	}

}
