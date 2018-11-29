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

package com.liferay.apio.architect.uri.mapper;

import aQute.bnd.annotation.ConsumerType;

/**
 * Converts between an identifier and its string version.
 *
 * <p>Instances of this interface will be used when reading an identifier value
 * from the HTTP request URL or writing a resource URL.
 *
 * <p>A component of this class must be present for every identifier class used
 * across your APIs.
 *
 * @author Alejandro Hernández
 * @param  <T> the identifier type to map
 * @review
 */
@ConsumerType
public interface IdentifierMapper<T> {

	/**
	 * Converts an identifier in string version to the object one.
	 *
	 * @review
	 */
	public T map(String string);

	/**
	 * Converts an identifier in object version to the string one.
	 *
	 * @review
	 */
	public String map(T t);

}