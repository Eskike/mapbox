package com.mapbox.services.android.navigation.v5.eh.loop;

import com.mapbox.services.android.navigation.v5.eh.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link Looper} enables a message loop on the current thread or a main run loop that can
 * be used application wide.
 */
public class Looper {
    private static final Logger LOGGER = new Logger(Looper.class);
    private static final int MAX_BACKLOG = 5;

    private static ThreadLocal<Looper> current = new ThreadLocal<>();
    private static ThreadLocal<Long> threadId = new ThreadLocal<>();
    private static AtomicReference<Looper> main = new AtomicReference<>();

    private boolean running = true;

    /**
     * @return the current looper for the current thread if {@link Looper#prepare()} was called.
     */
    public static Looper get() {
        return current.get();
    }

    /**
     * Prepares a {@link Looper} for the current thread.
     *
     * @return the {@link Looper}
     */
    public static synchronized Looper prepare() {
        if (current.get() != null) {
            throw new IllegalStateException("Looper already prepared for thread");
        }

        Looper looper = new Looper();
        current.set(looper);
        threadId.set(Thread.currentThread().getId());
        return looper;
    }

    /**
     * @return the Main {@link Looper} if set
     */
    public static Looper getMainLooper() {
        return main.get();
    }

    /**
     * Prepares a {@link Looper} on the current thread and sets it as the main {@link Looper}.
     *
     * @return the {@link Looper}
     */
    public static synchronized Looper prepareMainLooper() {
        if (main.get() != null) {
            throw new IllegalStateException("Main looper already prepared");
        }

        Looper looper = prepare();
        main.set(looper);
        return looper;
    }


    private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();

    /**
     * Blocks until either {@link Looper#shutdown()} is called or the {@link Thread} is
     * interrupted.
     * <p>
     * May only be called on the Thread where it was created.
     */
    public void run() {
        assertCorrectThread();

        try {
            Message message;
            while (running) {
                // Blocking wait on a message
                message = messages.take();

                try {
                    message.execute();
                } catch (Exception e) {
                    LOGGER.error(e, "Could not execute message");
                    halt();
                }

                if (messages.size() > MAX_BACKLOG) {
                    LOGGER.warn("Loop backlogged. %s messages in queue", messages.size());
                }
            }
        } catch (InterruptedException e) {
            LOGGER.info("Loop interrupted");
        }
        LOGGER.info("Loop stopping");
    }

    /**
     * Processes at most 1 message (if any messages are queued) and returns.
     * May only be called on the Thread where it was created.
     */
    public void runOnce() {
        assertCorrectThread();

        Message message = messages.poll();
        if (message != null) {
            message.execute();
        }
    }

    /**
     * Post a message to be processed on this {@link Looper}
     * <p>
     * May be called from any thread.
     *
     * @param runnable the runnable to process
     */
    public void post(final Runnable runnable) {
        messages.add(new RunnableMessage(runnable));
    }

    /**
     * Post a message to be processed and get a Future to deal with the result.
     *
     * @param callable the callable to process
     * @param <T>      the result type
     * @return the future that will hold the result
     */
    public <T> CompletableFuture<T> ask(final Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        messages.add(new CallableMessage<T>(callable, future));
        return future;
    }

    /**
     * Shuts down the looper (if running).
     * <p>
     * May be called from any thread.
     */
    public void shutdown() {
        messages.add(new ShutdownMessage(this));
    }

    /**
     * Shuts down the looper immediately.
     * <p>
     * Only callable from {@link Message}
     */
    void halt() {
        assertCorrectThread();
        running = false;
    }

    // For testing purposes
    static void reset() {
        main.set(null);
        current.set(null);
    }

    private void assertCorrectThread() {
        if (Thread.currentThread().getId() != threadId.get()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Loop was prepared for Thread %s and cannot be started on Thread %s",
                            threadId.get(),
                            Thread.currentThread().getId()
                    )
            );
        }
    }
}
