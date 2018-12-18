package com.mapbox.services.android.navigation.v5.eh.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple Debouncer implementation that uses a dedicated thread to schedule the calls.
 * <p>
 * Debouncers differ from Throttlers in that they coalesce and only use the last provided
 * arguments to call the wrapped {@link Callback}
 *
 * @param <T> the Function argument type
 */
public class Debouncer<T> {

    /**
     * Callback interface.
     *
     * @param <T> the argument type
     */
    public interface Callback<T> {

        /**
         * Call the callback.
         *
         * @param t the arguments
         */
        void call(T t);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicReference<T> arguments = new AtomicReference<>();
    private final Callback<T> callback;
    private ScheduledFuture future;
    private int interval;

    /**
     * Create a new debouncer.
     *
     * @param callback the Function to call when debounced
     * @param interval the interval between calls (trailing)
     */
    public Debouncer(final Callback<T> callback, final int interval) {
        this.callback = callback;
        this.interval = interval;
    }

    /**
     * Call the debouncer.
     *
     * @param arguments the function arguments
     */
    public synchronized void call(final T arguments) {
        this.arguments.set(arguments);


        if (future == null || future.isDone()) {
            future = scheduler.schedule(() -> {
                callback.call(this.arguments.get());
            }, interval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * @param interval the new update frequency
     */
    public void interval(final int interval) {
        this.interval = interval;
    }

    /**
     * @return the interval in ms
     */
    public int interval() {
        return interval;
    }

    /**
     * Shutdown the debouncer explicitly.
     */
    public void terminate() {
        scheduler.shutdownNow();
    }
}
