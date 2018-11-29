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

package com.liferay.apio.architect.internal.registrar;

import static com.liferay.apio.architect.internal.wiring.osgi.manager.TypeArgumentProperties.KEY_PRINCIPAL_TYPE_ARGUMENT;

import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;

import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import static org.slf4j.LoggerFactory.getLogger;

import com.liferay.apio.architect.uri.Path;
import com.liferay.apio.architect.uri.mapper.IdentifierMapper;
import com.liferay.apio.architect.uri.mapper.PathIdentifierMapper;

import java.lang.reflect.Type;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;

/**
 * Allow developers to still use the deprecated {@link PathIdentifierMapper}, by
 * transforming them to the new {@link IdentifierMapper}.
 *
 * @author Alejandro Hernández
 * @review
 */
@Component(immediate = true, service = {})
public class PathIdentifierMapperRegistrar {

	@Reference(cardinality = MULTIPLE, policy = DYNAMIC, policyOption = GREEDY)
	public <T> void setPathIdentifierMapper(
		Map<String, Object> properties,
		PathIdentifierMapper<T> pathIdentifierMapper) {

		Dictionary<String, Object> newProperties = new Hashtable<>(properties);

		Type identifierType = getTypeParameter(
			pathIdentifierMapper.getClass(),
			PathIdentifierMapper.class.getTypeParameters()[0]);

		if (identifierType == null) {
			_logger.warn(
				"Unable to get identifier class from {}",
				pathIdentifierMapper.getClass());

			return;
		}

		newProperties.put(KEY_PRINCIPAL_TYPE_ARGUMENT, identifierType);

		ServiceRegistration<?> serviceRegistration =
			_bundleContext.registerService(
				IdentifierMapper.class,
				_toIdentifierMapper(pathIdentifierMapper), newProperties);

		_map.put(pathIdentifierMapper, serviceRegistration);
	}

	public void unsetPathIdentifierMapper(
		PathIdentifierMapper pathIdentifierMapper) {

		ServiceRegistration<?> serviceRegistration = _map.remove(
			pathIdentifierMapper);

		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private <T> IdentifierMapper<T> _toIdentifierMapper(
		PathIdentifierMapper<T> pathIdentifierMapper) {

		return new IdentifierMapper<T>() {

			@Override
			public T map(String string) {
				return pathIdentifierMapper.map(new Path("", string));
			}

			@Override
			public String map(T t) {
				Path path = pathIdentifierMapper.map("", t);

				return path.getId();
			}

		};
	}

	private BundleContext _bundleContext;
	private final Logger _logger = getLogger(getClass());
	private final Map<Object, ServiceRegistration<?>> _map = new HashMap<>();

}