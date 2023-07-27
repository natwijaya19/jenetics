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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Methods for extracting {@link PropertyDescription} directly accessible from
 * a given data type.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class PropertyDescriptionExtractor {
	private PropertyDescriptionExtractor() {
	}

	static Stream<Description> extract(final Class<?> type) {
		final var descriptions = new ArrayList<Description>();

		if (type.isArray() && !type.getComponentType().isPrimitive()) {
			final var desc = new IndexedPropertyDescription(
				"[*]",
				type.getComponentType(),
				object -> ((Object[])object).length,
				(object, index) -> ((Object[])object)[index],
				(object, index, value) -> {
					((Object[])object)[index] = value;
					return true;
				}
			);
			descriptions.add(desc);
		} else if (List.class.isAssignableFrom(type)) {
			final var desc = new IndexedPropertyDescription(
				"[*]",
				type,
				object -> ((List<?>)object).size(),
				(object, index) -> ((List<?>)object).get(index),
				(object, index, value) -> {
					((List<Object>)object).set(index, value);
					return true;
				}
			);
			descriptions.add(desc);
		} else if (type.isRecord()) {
			for (var component : type.getRecordComponents()) {
				descriptions.add(toDescription(component));
			}
		} else {
			try {
				final PropertyDescriptor[] descriptors = Introspector
					.getBeanInfo(type)
					.getPropertyDescriptors();

				for (var descriptor : descriptors) {
					if (descriptor.getReadMethod() != null &&
						!"class".equals(descriptor.getName()) &&
						!"declaringClass".equals(descriptor.getName()))
					{
						descriptions.add(toDescription(descriptor));
					}
				}
			} catch (IntrospectionException e) {
				throw new IllegalArgumentException(
					"Can't introspect class '%s'.".formatted(type.getName()),
					e
				);
			}
		}

		descriptions.sort(Comparator.naturalOrder());
		return descriptions.stream();
	}

	private static PropertyDescription toDescription(final RecordComponent component) {
		return new PropertyDescription(
			component.getName(),
			component.getType(),
			Methods.toGetter(component.getAccessor()),
			null
		);
	}

	private static PropertyDescription
	toDescription(final PropertyDescriptor descriptor) {
		return new PropertyDescription(
			descriptor.getName(),
			descriptor.getPropertyType(),
			Methods.toGetter(descriptor.getReadMethod()),
			Methods.toSetter(descriptor.getWriteMethod())
		);
	}

//	static Optional<PropertyDescription> extract(final Class<?> type, final String name) {
//		requireNonNull(type);
//		requireNonNull(name);
//
//		Optional<PropertyDescription> description = Optional.empty();
//		if (type.isRecord()) {
//			for (var component : type.getRecordComponents()) {
//				if (component.getName().equals(name)) {
//					description = Optional.of(toDescription(component));
//				}
//			}
//		} else {
//			try {
//				final PropertyDescriptor[] descriptors = Introspector
//					.getBeanInfo(type)
//					.getPropertyDescriptors();
//
//				for (var descriptor : descriptors) {
//					if (descriptor.getReadMethod() != null &&
//						!"class".equals(descriptor.getName()) &&
//						descriptor.getName().equals(name))
//					{
//						description = Optional.of(toDescription(descriptor));
//					}
//				}
//			} catch (IntrospectionException e) {
//				throw new IllegalArgumentException(
//					"Can't introspect class '%s'.".formatted(type.getName()),
//					e
//				);
//			}
//		}
//
//		return description;
//	}

}
