package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartMessageSource extends StreamMessageSource {

    private static Logger log = LoggerFactory.getLogger(PartMessageSource.class.getName());

    long positionOffset = 0;
    Part part;

    PartMessageSource(Part part) {
        this.part = part;
    }

    public SourcedMessage load() throws PantomimeException {

        return super.init();

    }

    protected void seek(long newPosition) throws PantomimeException {
        super.seek(newPosition);
        positionOffset = 0;
    }

    int read(byte[] data) throws PantomimeException {

        int bytesRead;
        InputStream stream = null;

        try {

            long wantToSkip = getPosition() + positionOffset;

            long skipped = 0;

            stream = part.asSinglePart().getBody();
            
            skipped = stream.skip(getPosition() + positionOffset);

            if ( skipped < wantToSkip ) {
                return 0;
            }


            bytesRead = stream.read(data);

            positionOffset += bytesRead;

            return bytesRead;
        } catch (IOException e) {
            throw new PantomimeException(e);

        } finally {
            StreamUtility.close(this, stream);
        }


    }

    int read() throws PantomimeException {

        byte[] b = new byte[1];

        int bytesRead = read(b);

        if ( bytesRead == 0 ) {
            return -1;
        } else {
            return b[0];
        }

    }

    long getLength() throws PantomimeException {

        byte[] data = new byte[1684];

        int bytesRead = 0;

        long totalBytes = 0;

        InputStream stream = null;
        
        try {
            stream = part.asSinglePart().getBody();

            while ( ( bytesRead = stream.read(data) ) > 0 ) {
                totalBytes += bytesRead;
            }
        } catch (IOException e) {
            throw new PantomimeException(e);
        } finally {
            StreamUtility.close(this, stream);
        }
        
        return totalBytes;

    }


    public InputStream getBody(MimePath path) throws PantomimeException {
        long bodyStart = getBodyStart(path);
        long bodyEnd = getBodyEnd(path);

        InputStream stream =
            new PartMimePartInputStream(part, bodyStart, bodyEnd);

        StreamMonitor.opened(this, stream);

        return stream;
    }

    public void free() { }

    /* Closed by SourcedMessage.save() */
    public void save(InputStream stream) throws PantomimeException {
        part.asSinglePart().saveRfc822Message(stream);
    }

}
