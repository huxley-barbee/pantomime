package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StreamMonitor {

    private static final Logger log =
        LoggerFactory.getLogger(StreamMonitor.class.getName());

    private static ConcurrentHashMap<String, String> streams;

    static { streams = new ConcurrentHashMap<String, String> (); }

    private static String convert (Object object) {

        StringBuilder buffer = new StringBuilder ();

        buffer.append(object.getClass().getSimpleName());

        buffer.append(":");

        buffer.append(object.hashCode());

        return buffer.toString();

    }

    static void opened (Object opener, Object stream) {

        if ( stream != null && opener != null ) {

            String key = convert(stream);

            String value = convert(opener);

            streams.putIfAbsent(key, value);

        }

    }

    static void closed (Object closer, Object stream) {

        if ( stream != null ) {

            String key = convert(stream);

            streams.remove(key);

        }

    }

    public static String dump () {

        StringBuilder buffer = new StringBuilder ();

        for ( Entry<String, String> entry : streams.entrySet() ) {

            String stream = entry.getKey();

            String opener = entry.getValue();

            buffer.append(stream).append(" ").append(opener).append("\n");

        }

        return buffer.toString();

    }

    public static int unclosedStreams () {

        return streams.size();

    }

    public static void reset () {

        streams = new ConcurrentHashMap<String, String> ();

    }

}