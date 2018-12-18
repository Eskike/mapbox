package com.mapbox.services.android.navigation.v5.eh.storage;

import com.mapbox.services.android.navigation.v5.eh.io.IOUtils;
import com.mapbox.services.android.navigation.v5.eh.logging.Logger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;


/**
 * Http implementation of {@link AsyncHttpRequest}.
 */
class AsyncHttpRequest implements AsyncRequest {
    private static final Logger LOGGER = new Logger(AsyncHttpRequest.class);

    private Future<org.apache.http.HttpResponse> execution;
    private final CompletableFuture<HttpResponse> future = new CompletableFuture<>();

    /**
     * Http response implementation.
     */
    private static class HttpResponse implements Response {

        private org.apache.http.HttpResponse httpResponse;
        private boolean noContent;
        private Error error;

        HttpResponse(final org.apache.http.HttpResponse httpResponse) {
            this.httpResponse = httpResponse;

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            //CHECKSTYLE:OFF
            if (statusCode == 304) {
                // OK, but no content
                noContent = true;
            } else if (statusCode == 404) {
                this.error = new Error(Error.Reason.NotFound, httpResponse.getStatusLine().getReasonPhrase());
            } else if (statusCode == 429) {
                this.error = new Error(Error.Reason.RateLimit, httpResponse.getStatusLine().getReasonPhrase());
            } else if (statusCode >= 500 && statusCode < 600) {
                this.error = new Error(Error.Reason.Server, httpResponse.getStatusLine().getReasonPhrase());
            } else if (!(statusCode >= 200 && statusCode < 300)) {
                this.error = new Error(Error.Reason.Other, httpResponse.getStatusLine().getReasonPhrase());
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
        public InputStream getInputStream() throws IOException {
            if (Stream.of(httpResponse.getHeaders("Content-Encoding"))
                    .anyMatch(h -> h.getValue().toUpperCase().equals("GZIP"))) {
                // Gzip entity does not work...
                return new GZIPInputStream(httpResponse.getEntity().getContent());
            } else {
                return httpResponse.getEntity().getContent();
            }
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
    AsyncHttpRequest(final HttpAsyncClient httpClient, final Resource resource) {
        this(httpClient, resource, null);
    }

    /**
     * Creates a new async http request.
     *
     * @param resource the resource to load
     * @param callback the callback to use
     */
    AsyncHttpRequest(final HttpAsyncClient httpClient, final Resource resource, final FileSource.Callback callback) {
        LOGGER.debug("Loading resource %s", resource);

        HttpGet request = new HttpGet(resource.getUrl());
        httpClient.execute(request, new FutureCallback<org.apache.http.HttpResponse>() {

            @Override
            public void completed(final org.apache.http.HttpResponse httpResponse) {
                if (callback != null) {
                    callback.onResponse(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(httpResponse));
                }

                future.complete(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(httpResponse));
            }

            @Override
            public void failed(final Exception e) {
                if (callback != null) {
                    callback.onResponse(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(e));
                }

                future.complete(new com.mapbox.services.android.navigation.v5.eh.storage.AsyncHttpRequest.HttpResponse(e));
            }

            @Override
            public void cancelled() {
                LOGGER.debug("Canceled: %s", resource.getUrl());
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
