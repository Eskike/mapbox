package com.mapbox.services.android.navigation.v5.eh.storage;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * The online {@link FileSource} requests resources over HTTP.
 */
public class OnlineFileSource implements FileSource {

    private static final int MAX_CONNECTIONS = 10;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;

    private final OkHttpClient okHttpClient;

    /**
     * Create a default instance of the {@link OnlineFileSource}.
     */
    public OnlineFileSource() {
              okHttpClient = new OkHttpClient.Builder()
                      .connectionPool(new ConnectionPool(MAX_CONNECTIONS, MAX_CONNECTIONS_PER_ROUTE, TimeUnit.MINUTES))
                      .build();
    }

    @Override
    public AsyncRequest request(final Resource resource) {
        return new AsyncHttpRequest(okHttpClient, resource);
    }

    @Override
    public AsyncRequest request(final Resource resource, final Callback callback) {
        return new AsyncHttpRequest(okHttpClient, resource, callback);
    }

    void close() {
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }

    @Override
    protected void finalize() {
        close();
    }
}
