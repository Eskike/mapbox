package com.mapbox.services.android.navigation.v5.eh.logging;

import org.slf4j.LoggerFactory;

/**
 * Simple logging abstraction for now.
 */
public class Logger {
    private final org.slf4j.Logger delegate;

    /**
     * Create a Logger for the given context.
     *
     * @param context the name of the context
     */
    public Logger(final String context) {
        delegate = LoggerFactory.getLogger(context);
    }

    /**
     * Create a Logger for the given context.
     *
     * @param context the class of the context
     */
    public Logger(final Class context) {
        delegate = LoggerFactory.getLogger(context);
    }

    /**
     * Log this on info level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void info(final String format, final Object... params) {
        delegate.info(String.format(format, params));
    }

    /**
     * Log this on warning level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void warn(final String format, final Object... params) {
        delegate.warn(String.format(format, params));
    }

    /**
     * Log this on error level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void error(final String format, final Object... params) {
        delegate.error(String.format(format, params));
    }

    /**
     * Log this on error level.
     *
     * @param error  the exception
     * @param format the format string
     * @param params the params if any
     */
    public void error(final Exception error, final String format, final Object... params) {
        delegate.error(String.format(format, params), error);
    }

    /**
     * Log this on debug level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void debug(final String format, final Object... params) {
        delegate.debug(String.format(format, params));
    }

    /**
     * Log this on trace level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void trace(final String format, final Object... params) {
        delegate.trace(String.format(format, params));
    }

    /**
     * @return true if trace level is enabled for this logger
     */
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    /**
     * @return true if debug level is enabled for this logger
     */
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

}
