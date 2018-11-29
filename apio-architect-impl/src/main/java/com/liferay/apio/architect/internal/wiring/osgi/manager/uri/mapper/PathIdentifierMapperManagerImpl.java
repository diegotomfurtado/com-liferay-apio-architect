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

package com.liferay.apio.architect.internal.wiring.osgi.manager.uri.mapper;

import static com.liferay.apio.architect.internal.wiring.osgi.manager.cache.ManagerCache.INSTANCE;
import static com.liferay.apio.architect.internal.wiring.osgi.util.GenericUtil.getGenericTypeArgumentTry;

import com.liferay.apio.architect.functional.Try;
import com.liferay.apio.architect.identifier.Identifier;
import com.liferay.apio.architect.internal.wiring.osgi.error.ApioDeveloperError.MustHaveIdentifierMapper;
import com.liferay.apio.architect.internal.wiring.osgi.manager.base.ClassNameBaseManager;
import com.liferay.apio.architect.internal.wiring.osgi.manager.representable.IdentifierClassManager;
import com.liferay.apio.architect.uri.Path;
import com.liferay.apio.architect.uri.mapper.IdentifierMapper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Hernández
 */
@Component(service = PathIdentifierMapperManager.class)
public class PathIdentifierMapperManagerImpl
	extends ClassNameBaseManager<IdentifierMapper>
	implements PathIdentifierMapperManager {

	public PathIdentifierMapperManagerImpl() {
		super(IdentifierMapper.class, 0);
	}

	@Override
	public boolean hasPathIdentifierMapper(String name) {
		return _getIdentifierMapperTry(name).isSuccess();
	}

	@Override
	public <T> T mapToIdentifierOrFail(Path path) {
		Try<IdentifierMapper<T>> pathIdentifierMapperTry =
			_getIdentifierMapperTry(path.getName());

		return pathIdentifierMapperTry.map(
			service -> service.map(path.getId())
		).orElseGet(
			() -> _getReusableIdentifierMapperOptional(path)
		);
	}

	@Override
	public <T> Optional<Path> mapToPath(String name, T identifier) {
		Try<IdentifierMapper<T>> pathIdentifierMapperTry =
			_getIdentifierMapperTry(name);

		Path path = pathIdentifierMapperTry.map(
			service -> service.map(identifier)
		).map(
			id -> new Path(name, id)
		).orElseGet(
			() -> _getReusablePath(name, identifier)
		);

		return Optional.ofNullable(path);
	}

	private <T> Try<IdentifierMapper<T>> _getIdentifierMapperTry(String name) {
		return Try.success(
			name
		).mapOptional(
			_identifierClassManager::getIdentifierClassOptional
		).flatMap(
			clazz -> getGenericTypeArgumentTry(clazz, Identifier.class, 0)
		).mapOptional(
			this::getServiceOptional
		).map(
			identifierMapper -> (IdentifierMapper<T>)identifierMapper
		);
	}

	private <T> T _getReusableIdentifierMapperOptional(Path path) {
		Optional<IdentifierMapper<T>> identifierMapperOptional =
			_getReusableIdentifierMapperOptional(path.getName());

		return identifierMapperOptional.filter(
			__ -> path.getId() != null && !"__".equals(path.getId())
		).map(
			service -> service.map(path.getId())
		).orElseThrow(
			() -> new MustHaveIdentifierMapper(path)
		);
	}

	private <T> Optional<IdentifierMapper<T>>
		_getReusableIdentifierMapperOptional(String name) {

		Optional<Class<?>> reusableIdentifierClassOptional =
			INSTANCE.getReusableIdentifierClassOptional(name);

		return reusableIdentifierClassOptional.flatMap(
			this::getServiceOptional
		).map(
			identifierMapper -> (IdentifierMapper<T>)identifierMapper
		);
	}

	private <T> Path _getReusablePath(String name, T identifier) {
		Optional<IdentifierMapper<T>> identifierMapperOptional =
			_getReusableIdentifierMapperOptional(name);

		return identifierMapperOptional.map(
			identifierMapper -> identifierMapper.map(identifier)
		).map(
			id -> new Path(name, id)
		).orElse(
			null
		);
	}

	@Reference
	private IdentifierClassManager _identifierClassManager;

}