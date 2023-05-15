package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StreamUtility {

    private static final Logger log =
        LoggerFactory.getLogger(StreamUtility.class.getName());
 
    static long count(InputStream stream) throws IOException {

        long total = 0;
        int bytesRead;
        byte[] buffer;

        if ( stream == null ) {
            return total;
        }

        buffer = new byte[16384];

        while ( ( bytesRead = stream.read(buffer) ) > 0 ) {
            total += bytesRead;
        }

        return total;
    }
 
    static String asString(InputStream stream, String charset)
        throws IOException {

        int bytesRead;
        byte[] buffer;
        StringBuilder builder = new StringBuilder();

        if ( stream == null ) {
            return null;
        }

        buffer = new byte[16384];

        while ( ( bytesRead = stream.read(buffer) ) > 0 ) {

            String s;

            try {

                if ( charset != null ) {
                    s = new String(buffer, 0, bytesRead, charset);
                } else {
                    s = new String(buffer, 0, bytesRead);
                }
            } catch (UnsupportedEncodingException e) {
                s = new String(buffer, 0, bytesRead);
            }

            builder.append(s);

        }

        return builder.toString();
    }
 
    static void close(Object o, OutputStream stream) {

        if ( stream == null ) {
            return;
        }

        try {
            stream.close();
            StreamMonitor.closed(o, stream);
        } catch (IOException e) {
            log.error("Unable to close stream.", e);
        }
    }

    static void close(Object o, InputStream stream) {

        if ( stream == null ) {
            return;
        }

        try {
            stream.close();
            StreamMonitor.closed(o, stream);
        } catch (IOException e) {
            log.error("Unable to close stream.", e);
        }
    }

}

