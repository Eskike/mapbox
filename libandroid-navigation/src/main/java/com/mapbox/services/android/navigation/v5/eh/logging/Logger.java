package com.mapbox.services.android.navigation.v5.eh.logging;

import com.mapbox.services.android.navigation.BuildConfig;

import timber.log.Timber;

/**
 * Simple logging abstraction for now.
 */
public class Logger {

    /**
     * Log this on info level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void info(final String format, final Object... params) {
        Timber.i(String.format(format, params));
    }

    /**
     * Log this on warning level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void warn(final String format, final Object... params) {
        Timber.w(String.format(format, params));
    }

    /**
     * Log this on error level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void error(final String format, final Object... params) {
        Timber.e(String.format(format, params));
    }

    /**
     * Log this on error level.
     *
     * @param error  the exception
     * @param format the format string
     * @param params the params if any
     */
    public void error(final Exception error, final String format, final Object... params) {
        Timber.e(String.format(format, params), error);
    }

    /**
     * Log this on debug level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void debug(final String format, final Object... params) {
        Timber.d(String.format(format, params));
    }

    /**
     * Log this on trace level.
     *
     * @param format the format string
     * @param params the params if any
     */
    public void trace(final String format, final Object... params) {
        Timber.v(String.format(format, params));
    }

    /**
     * @return true if trace level is enabled for this logger
     */
    public boolean isTraceEnabled() {
        return BuildConfig.DEBUG;
    }

    /**
     * @return true if debug level is enabled for this logger
     */
    public boolean isDebugEnabled() {
        return BuildConfig.DEBUG;
    }

}
