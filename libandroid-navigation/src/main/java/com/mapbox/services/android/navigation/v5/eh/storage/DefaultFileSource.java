package com.mapbox.services.android.navigation.v5.eh.storage;

/**
 * Common {@link FileSource} for client consumption.
 */
public class DefaultFileSource implements FileSource {

    private FileSource onlineFileSource;

    /**
     * Creates the Default {@link FileSource}.
     */
    public DefaultFileSource() {
        this(new OnlineFileSource());
    }

    /**
     * For testing.
     *
     * @param onlineFileSource the online {@link FileSource} implementation
     */
    DefaultFileSource(final FileSource onlineFileSource) {
        this.onlineFileSource = onlineFileSource;
    }

    @Override
    public AsyncRequest request(final Resource resource) {
        return onlineFileSource.request(resource);
    }

    @Override
    public AsyncRequest request(final Resource resource, final Callback callback) {
        return onlineFileSource.request(resource, callback);
    }

}
