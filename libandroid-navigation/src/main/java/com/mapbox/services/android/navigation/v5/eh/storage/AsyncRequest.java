package com.mapbox.services.android.navigation.v5.eh.storage;

import java.util.concurrent.CompletableFuture;

/**
 * Handle to a {@link FileSource} request.
 */
public interface AsyncRequest {

    /**
     * Abort the request.
     */
    void cancel();

    /**
     * Get a {@link CompletableFuture} to await the result.
     *
     * @return the future if set
     */
    CompletableFuture<? extends Response> future();

}
