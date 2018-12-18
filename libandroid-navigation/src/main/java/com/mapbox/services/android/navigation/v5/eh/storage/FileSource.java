package com.mapbox.services.android.navigation.v5.eh.storage;

/**
 * Main file source abstraction.
 */
public interface FileSource {

    /**
     * Callback to use for the request handling.
     */
    interface Callback {
        /**
         * On response (could be an error).
         *
         * @param response the response
         */
        void onResponse(Response response);
    }

    /**
     * Request a Resource without a callback (Assumes handling through {@link AsyncRequest#future()}.
     *
     * @param resource the resource to load
     * @return the request token
     */
    AsyncRequest request(Resource resource);

    /**
     * Request a Resource without a callback (Handling through either
     * {@link AsyncRequest#future() or callback implementation}.
     *
     * @param resource the resource to load
     * @param callback the callback
     * @return the request token
     */
    AsyncRequest request(Resource resource, Callback callback);

}
