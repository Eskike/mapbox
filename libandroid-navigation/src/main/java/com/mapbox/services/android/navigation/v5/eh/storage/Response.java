package com.mapbox.services.android.navigation.v5.eh.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Response for AsyncRequest.
 */
public interface Response {

    /**
     * The response error.
     */
    class Error {
        private final Reason reason;
        private final String message;

        public Error(final Reason reason, final String message) {
            this.reason = reason;
            this.message = message;
        }

        enum Reason {
            NotFound,
            Server,
            Connection,
            RateLimit,
            Other,
        }

        /**
         * @return the reason
         */
        public Reason getReason() {
            return reason;
        }

        /**
         * @return the message if present
         */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("Error: %s - %s", reason, message);
        }
    }

    /**
     * @return the response data as a {@link String}
     */
    String getData();

    /**
     * @return the response data {@link InputStream}
     * @throws IOException on io errors
     */
    InputStream getInputStream() throws IOException;

    /**
     * @return the Error, if any
     */
    Error getError();

    /**
     * @return true if the response has no content
     */
    boolean noContent();
}
