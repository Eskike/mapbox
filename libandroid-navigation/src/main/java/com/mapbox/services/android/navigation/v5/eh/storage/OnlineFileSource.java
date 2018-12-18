package com.mapbox.services.android.navigation.v5.eh.storage;

import com.mapbox.services.android.navigation.v5.eh.logging.Logger;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;

import java.io.IOException;

/**
 * The online {@link FileSource} requests resources over HTTP.
 */
public class OnlineFileSource implements FileSource {
    private static final Logger LOGGER = new Logger(OnlineFileSource.class);

    private static final int MAX_CONNECTIONS = 10;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;

    private final CloseableHttpAsyncClient httpclient;

    /**
     * Create a default instance of the {@link OnlineFileSource}.
     */
    public OnlineFileSource() {
        try {
            httpclient = HttpAsyncClientBuilder.create()
                    .setConnectionManager(createPoolingConnManager()).build();
            httpclient.start();
        } catch (IOReactorException e) {
            LOGGER.error(e, "Cannot create http client");
            throw new RuntimeException(e);
        }
    }

    @Override
    public AsyncRequest request(final Resource resource) {
        return new AsyncHttpRequest(httpclient, resource);
    }

    @Override
    public AsyncRequest request(final Resource resource, final Callback callback) {
        return new AsyncHttpRequest(httpclient, resource, callback);
    }

    PoolingNHttpClientConnectionManager createPoolingConnManager() throws IOReactorException {
        PoolingNHttpClientConnectionManager manager =
                new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
        manager.setMaxTotal(MAX_CONNECTIONS);
        manager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        return manager;
    }

    void close() throws IOException {
        httpclient.close();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } catch (IOException e) {
            LOGGER.error(e, "Could not close http client");
            throw e;
        }
    }
}
