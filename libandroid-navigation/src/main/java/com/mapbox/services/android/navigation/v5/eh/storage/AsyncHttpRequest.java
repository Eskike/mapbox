package com.mapbox.services.android.navigation.v5.eh.storage;

import com.mapbox.services.android.navigation.v5.eh.io.IOUtils;
import com.mapbox.services.android.navigation.v5.eh.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/**
 * Http implementation of {@link AsyncHttpRequest}.
 */
class AsyncHttpRequest implements AsyncRequest {
    private static final Logger LOGGER = new Logger();

    private Future<okhttp3.Response> execution;
    private final CompletableFuture<HttpResponse> future = new CompletableFuture<>();

    /**
     * Http response implementation.
     */
    private static class HttpResponse implements Response {

        private okhttp3.Response okHttpResponse;
        private boolean noContent;
        private Error error;

        HttpResponse(final okhttp3.Response okHttpResponse) {
            this.okHttpResponse = okHttpResponse;

            int statusCode = okHttpResponse.code();

            //CHECKSTYLE:OFF
            if (statusCode == 304) {
                // OK, but no content
                noContent = true;
            } else if (statusCode == 404) {
                this.error = new Error(Error.Reason.NotFound, okHttpResponse.message());
            } else if (statusCode == 429) {
                this.error = new Error(Error.Reason.RateLimit, okHttpResponse.message());
            } else if (statusCode >= 500 && statusCode < 600) {
                this.error = new Error(Error.Reason.Server, okHttpResponse.message());
            } else if (!(statusCode >= 200 && statusCode < 300)) {
                this.error = new Error(Error.Reason.Other, okHttpResponse.message());
            }
            //CHECKSTYLE:ON
        }

        HttpResponse(final Exception error) {
            this.error = new Error(Error.Reason.Connection, error.getMessage());
        }

        @Override
        public String getData() {
            try {
                return IOUtils.toString(getInputStream());
            } catch (IOException e) {
                LOGGER.error(e, "Could not read response");
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream getInputStream() {
            return okHttpResponse.body().byteStream();
        }

        public Error getError() {
            return error;
        }

        @Override
        public boolean noContent() {
            return noContent;
        }
    }

    /**
     * Creates a new async http request.
     *
     * @param resource the resource to load
     */
    AsyncHttpRequest(final OkHttpClient okHttpClient, final Resource resource) {
        this(okHttpClient, resource, null);
    }

    /**
     * Creates a new async http request.
     *
     * @param resource the resource to load
     * @param callback the callback to use
     */
    AsyncHttpRequest(final OkHttpClient okHttpClient, final Resource resource, final FileSource.Callback callback) {
        LOGGER.debug("Loading resource %s", resource);

        Request okHttpRequest = new Request.Builder()
          .url(resource.getUrl())
          .build();

        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onResponse(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(e));
                }

                future.complete(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(e));
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                if (callback != null) {
                    callback.onResponse(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(response));
                }

                future.complete(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(response));
            }
        });
    }

    @Override
    public void cancel() {
        if (execution != null && (!execution.isCancelled() || execution.isDone())) {
            execution.cancel(true);
        }
    }

    @Override
    public CompletableFuture<? extends Response> future() {
        return future;
    }

    /**
     * Cancel the request on finalizing.
     *
     * @throws Throwable on error
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cancel();
    }
}
