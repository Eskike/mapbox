package com.mapbox.services.android.navigation.v5.eh.loop;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

class CallableMessage<T> implements Message {
    private final Callable<T> callable;
    private final CompletableFuture<T> future;

    CallableMessage(final Callable<T> callable, final CompletableFuture<T> future) {
        this.callable = callable;
        this.future = future;
    }

    public void execute() {
        try {
            T result = callable.call();
            future.complete(result);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }
}
