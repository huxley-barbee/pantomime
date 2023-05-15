package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.blackmist.pantomime.content.InputStreamSource;

class InputStreamMimePartInputStream extends InputStream {
    private static final Logger log =
        LoggerFactory.getLogger(InputStreamMimePartInputStream.class.getName());

    private InputStreamSource source;
    private long bodyStart;
    private long bodyEnd;
    private InputStream currentStream;
    private long position;
 
    public InputStreamMimePartInputStream(InputStreamSource source,
        long bodyStart, long bodyEnd) {
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
        this.source = source;
    }

    public int read() throws IOException {

        if ( position > bodyEnd ) {
            return -1;
        }

        if ( currentStream == null ) {

            long skipped = 0;

            try {
                currentStream = source.getInputStream();
                StreamMonitor.opened(this, currentStream);
            } catch (PantomimeException e) {
                throw new IOException(e);
            }

            skipped = skip(bodyStart);

            if ( skipped != bodyStart ) {
                log.error("Skip only " + skipped + " of "  + bodyStart +
                    " bytes as requested.");
            }

            position = bodyStart;
        }

        position++;
        return currentStream.read();
    }

    public long skip(long count) throws IOException {

        int bufferSize = 5*1024*1024;
        byte[] bytes = new byte[bufferSize];
        long total = 0;
        long skipped = 0;

        while ( count > 0 ) {
            int bytesRead = 0;
            int max = bufferSize;

            if ( count < bufferSize) {
                max = (int)count;
            }

            bytesRead = currentStream.read(bytes, 0, max);

            count -= bytesRead;

            skipped += bytesRead;
        }

        return skipped;

    }

    public void close() {
        StreamUtility.close(this, currentStream);
    }
}

