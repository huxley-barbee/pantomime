package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.blackmist.pantomime.content.InputStreamSource;

public class InputStreamMessageSource extends StreamMessageSource {

    private static final Logger log =
        LoggerFactory.getLogger(InputStreamMessageSource.class.getName());

    private InputStreamSource source;
    private InputStream currentStream;
    private Window window;

    private class Window {

        private int windowSize = 5*1024*1024;
        private byte[] buffer;
        private int size = 0;
        private long windowStart = 0;
        private int index = 0;

        public String toString() {
            return "Window: [ start: " + windowStart + " index: " + index +
                " size: " + size + " ]";
        }

        private Window() {
            buffer = new byte[windowSize];
        }

        private Window(int size) {
            this.windowSize = size;
            buffer = new byte[windowSize];
        }


        private int get() {
            byte[] data = new byte[1];
            get(data);
            return data[0];
        }

        private int get(byte[] data) {
            return get(data, 0);
        }

        private int get(byte[] data, int start) {


            int bytesCopied = 0;

            for (int jndex = start; jndex < data.length; jndex++ ) {

                if ( index >= size ) {
                    break;
                }

                data[jndex] = buffer[index];
                index++;
                bytesCopied++;

            }

            return bytesCopied;

        }

        
        private boolean inWindow(long position) {


            if ( ( position >= windowStart ) &&
                ( position < (windowStart+size) ) ) {
                return true;
            }

            return false;
        }

        private void skip(int count) throws IOException {

            int bufferSize = 5*1024*1024;
            byte[] bytes = new byte[bufferSize];
            long total = 0;

            while ( count > 0 ) {
                int bytesRead = 0;
                int max = bufferSize;

                if ( count < bufferSize) {
                    max = count;
                }

                bytesRead = currentStream.read(bytes, 0, max);

                count -= bytesRead;

            }

        }

        private void move(long newPosition) throws PantomimeException {


            int previousWindowEnd;
            int windowNum;
            int totalRead = 0;

            /* if seeking to the current window, no need to
             * re-populate the buffer
             */
            if ( inWindow(newPosition) ) {
                index = (int)(newPosition - windowStart);
                return;
            }

            closeStream();
            currentStream = source.getInputStream();
            StreamMonitor.opened(this, currentStream);

            windowNum = (int)(newPosition / windowSize);

            previousWindowEnd = (windowNum * windowSize);

            try {
                skip(previousWindowEnd);
            } catch (IOException e) {
                throw new PantomimeException(e);
            }

            try {

                int bytesRead;

                do {

                    bytesRead = currentStream.read(buffer, totalRead,
                        (buffer.length - totalRead));

                    if ( bytesRead > 0 ) {
                        totalRead += bytesRead;
                    }


                } while ( bytesRead > 0 );


            } catch (IOException e) {
                throw new PantomimeException(e);
            }

            windowStart = previousWindowEnd;
            size = totalRead;

            index = (int)(newPosition - windowStart);

        }

    }
 
    public InputStreamMessageSource(InputStreamSource source, int size) {
        this.source = source;
        this.window = new Window(size);
    }

    public InputStreamMessageSource(InputStreamSource source) {
        this.source = source;
        this.window = new Window();
    }

    protected void seek(long newPosition) throws PantomimeException {

        super.seek(newPosition);

        window.move(newPosition);

    }

    long getLength() throws PantomimeException {
        InputStream stream = null;
        long size = 0;

        try {
            stream = source.getInputStream();
            StreamMonitor.opened(this, stream);

            size = StreamUtility.count(stream);

        } catch (IOException e) {
            log.error("Unable to count.", e);
        } finally {
            StreamUtility.close(this, stream);
        }

        return size;

    }

    private void closeStream() {
        StreamUtility.close(this, currentStream);
        currentStream = null;
    }

    int read() throws PantomimeException {

        if ( ! window.inWindow(getPosition()) ) {
            window.move(getPosition());
        }

        return window.get();
    }

    int read(byte[] data) throws PantomimeException {


        int totalRead = 0;

        while ( totalRead < data.length ) {
            int bytesRead = 0;

            if ( ! window.inWindow(getPosition()+totalRead) ) {
                window.move(getPosition()+totalRead);
            }


            bytesRead += window.get(data, totalRead);

            if ( bytesRead == 0 ) {
                break;
            }

            totalRead += bytesRead;

        }

        return totalRead;

    }

    public void free() {

        closeStream();

    }

    public void save(InputStream stream) {
        return;
    }

    public InputStream getBody(MimePath path) throws PantomimeException {
        long bodyStart = getBodyStart(path);
        long bodyEnd = getBodyEnd(path);
        InputStream stream = new InputStreamMimePartInputStream(source,
            bodyStart, bodyEnd);
        StreamMonitor.opened(this, stream);
        return stream;
    }

    public SourcedMessage load() throws PantomimeException {

        return super.init();

    }

}

