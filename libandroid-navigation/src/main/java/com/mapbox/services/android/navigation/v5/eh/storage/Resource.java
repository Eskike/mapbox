package com.mapbox.services.android.navigation.v5.eh.storage;

/**
 * Resource for request to {@link FileSource}.
 */
public class Resource {

    /**
     * The resource's kind.
     */
    public enum Kind {
        /**
         * Other.
         */
        Unknown,

        /**
         * Tile.
         */
        Tile
    }

    /**
     * The resource's necessity.
     */
    enum Necessity {
        /**
         * Not required for operation.
         */
        Optional,

        /**
         * Required.
         */
        Required,
    }


    private Kind kind;
    private Necessity necessity;
    private String url;

    /**
     * Create a resource.
     *
     * @param kind      the kind of resource
     * @param url       the url
     * @param necessity the necessity
     */
    public Resource(final Kind kind, final String url, final Necessity necessity) {
        this.kind = kind;
        this.necessity = necessity;
        this.url = url;
    }

    /**
     * Create a resource with default necessity.
     *
     * @param kind the kind of resource
     * @param url  the url
     */
    public Resource(final Kind kind, final String url) {
        this.kind = kind;
        this.necessity = Necessity.Required;
        this.url = url;
    }

    /**
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * @return the necessity
     */
    public Necessity getNecessity() {
        return necessity;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return String.format("[kind: %s, url: %s]", kind, url);
    }
}
