/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.apio.architect.internal.test.registrar;

import static ch.qos.logback.classic.Level.WARN;

import static com.liferay.apio.architect.internal.test.base.LogsMatchers.contains;
import static com.liferay.apio.architect.internal.test.base.LogsMatchers.warnMessage;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.liferay.apio.architect.internal.test.base.BaseTest;
import com.liferay.apio.architect.uri.Path;
import com.liferay.apio.architect.uri.mapper.IdentifierMapper;
import com.liferay.apio.architect.uri.mapper.PathIdentifierMapper;

import org.junit.Test;

/**
 * Test suite for {@link
 * com.liferay.apio.architect.internal.registrar.PathIdentifierMapperRegistrar}
 * class.
 *
 * @author Alejandro Hernández
 */
public class PathIdentifierMapperRegistrarTest extends BaseTest {

	@Test
	public void testInvalidMapperIsNotRegisteredAsIdentifierMapper() {
		beforeTestAttachLoggerWithLevel(WARN);

		int numberOfIdentifierMapperBeforeRegistering =
			getNumberOfServicesImplementing(IdentifierMapper.class);

		beforeTestRegisterAs(
			PathIdentifierMapper.class, new InvalidPathIdentifierMapper(),
			noProperties);

		int numberOfIdentifierMapperAfterRegistering =
			getNumberOfServicesImplementing(IdentifierMapper.class);

		assertThat(
			numberOfIdentifierMapperAfterRegistering,
			is(equalTo(numberOfIdentifierMapperBeforeRegistering)));

		String message =
			"Unable to get identifier class from " +
				InvalidPathIdentifierMapper.class;

		assertThat(testLogs, contains(warnMessage(equalTo(message))));
	}

	@Test
	public void testValidMapperIsRegisteredAsIdentifierMapper() {
		beforeTestRegisterAs(
			PathIdentifierMapper.class, new PojoPathIdentifierMapper(),
			noProperties);

		IdentifierMapper<Pojo> pojoIdentifierMapper = getService(
			IdentifierMapper.class, "apio.architect.principal.type.argument",
			Pojo.class);

		assertNotNull(pojoIdentifierMapper);

		String stringVersion = pojoIdentifierMapper.map(() -> 42L);

		assertThat(stringVersion, is("42"));

		Pojo pojo = pojoIdentifierMapper.map("42");

		assertThat(pojo.getId(), is(42L));
	}

	public static class InvalidPathIdentifierMapper
		implements PathIdentifierMapper {

		@Override
		public Object map(Path path) {
			return path;
		}

		@Override
		public Path map(String name, Object o) {
			return new Path(name, o.toString());
		}

	}

	public static class PojoPathIdentifierMapper
		implements PathIdentifierMapper<Pojo> {

		@Override
		public Pojo map(Path path) {
			return () -> Long.valueOf(path.getId() + path.getName());
		}

		@Override
		public Path map(String name, Pojo pojo) {
			return new Path(name, String.valueOf(pojo.getId()) + name);
		}

	}

	@FunctionalInterface
	public interface Pojo {

		public Long getId();

	}

}