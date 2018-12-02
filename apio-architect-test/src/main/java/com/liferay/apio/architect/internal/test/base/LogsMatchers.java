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

package com.liferay.apio.architect.internal.test.base;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Provides {@code Matcher} objects that can be used for testing the logs
 * contents.
 *
 * <p>This class shouldn't be instantiated.
 *
 * @author Alejandro Hernández
 * @review
 */
@SuppressWarnings("unused")
public final class LogsMatchers {

	/**
	 * This matcher checks if the {@link ListAppender} being tested contains an
	 * {@link ILoggingEvent} that matches the provided {@code matcher}.
	 *
	 * @review
	 */
	public static Matcher<ListAppender<ILoggingEvent>> contains(
		Matcher<ILoggingEvent> matcher) {

		return new TypeSafeMatcher<ListAppender<ILoggingEvent>>() {

			@Override
			public void describeTo(Description description) {
				description.appendText(
					"logs contains "
				).appendDescriptionOf(
					matcher
				);
			}

			@Override
			protected void describeMismatchSafely(
				ListAppender<ILoggingEvent> listAppender,
				Description description) {

				description.appendText("message could not be found in logs");
			}

			@Override
			protected boolean matchesSafely(
				ListAppender<ILoggingEvent> listAppender) {

				for (ILoggingEvent iLoggingEvent : listAppender.list) {
					if (matcher.matches(iLoggingEvent)) {
						return true;
					}
				}

				return false;
			}

		};
	}

	/**
	 * This matcher checks if the {@link ILoggingEvent} being tested was logged
	 * as a {@link Level#DEBUG} and matches the provided {@code matcher}.
	 *
	 * @review
	 */
	public static Matcher<ILoggingEvent> debugMessage(Matcher<String> matcher) {
		return new LogLevelMatcher(DEBUG, matcher);
	}

	/**
	 * This matcher checks if the {@link ILoggingEvent} being tested was logged
	 * as a {@link Level#ERROR} and matches the provided {@code matcher}.
	 *
	 * @review
	 */
	public static Matcher<ILoggingEvent> errorMessage(Matcher<String> matcher) {
		return new LogLevelMatcher(ERROR, matcher);
	}

	/**
	 * This matcher checks if the {@link ILoggingEvent} being tested was logged
	 * as a {@link Level#INFO} and matches the provided {@code matcher}.
	 *
	 * @review
	 */
	public static Matcher<ILoggingEvent> infoMessage(Matcher<String> matcher) {
		return new LogLevelMatcher(INFO, matcher);
	}

	/**
	 * This matcher checks if the {@link ILoggingEvent} being tested was logged
	 * as a {@link Level#TRACE} and matches the provided {@code matcher}.
	 *
	 * @review
	 */
	public static Matcher<ILoggingEvent> traceMessage(Matcher<String> matcher) {
		return new LogLevelMatcher(TRACE, matcher);
	}

	/**
	 * This matcher checks if the {@link ILoggingEvent} being tested was logged
	 * as a {@link Level#WARN} and matches the provided {@code matcher}.
	 *
	 * @review
	 */
	public static Matcher<ILoggingEvent> warnMessage(Matcher<String> matcher) {
		return new LogLevelMatcher(WARN, matcher);
	}

	private LogsMatchers() {
	}

	private static class LogLevelMatcher
		extends TypeSafeMatcher<ILoggingEvent> {

		public LogLevelMatcher(Level level, Matcher<String> matcher) {
			_level = level;
			_matcher = matcher;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(
				_level.levelStr
			).appendText(
				" message "
			).appendDescriptionOf(
				_matcher
			);
		}

		@Override
		protected boolean matchesSafely(ILoggingEvent iLoggingEvent) {
			if (_level.equals(iLoggingEvent.getLevel())) {
				return _matcher.matches(iLoggingEvent.getFormattedMessage());
			}

			return false;
		}

		private final Level _level;
		private final Matcher<String> _matcher;

	}

}