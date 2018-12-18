package com.mapbox.services.android.navigation.v5.eh.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Collection of IO utility methods.
 */
public final class IOUtils {

    /**
     * Not constructable.
     */
    private IOUtils() {
    }

    /**
     * Copy input stream to output stream.
     * Closes both when done.
     *
     * @param input  the input
     * @param output the output
     * @throws IOException on error
     */
    public static void copy(final InputStream input, final OutputStream output) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                }
            }
        }
    }

    /**
     * Read an {@link InputStream} to a string.
     *
     * @param input the input stream to read
     * @return the resulting string
     * @throws IOException when erroring out
     */
    public static String toString(final InputStream input) throws IOException {
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        }
        return out.toString();
    }
}
