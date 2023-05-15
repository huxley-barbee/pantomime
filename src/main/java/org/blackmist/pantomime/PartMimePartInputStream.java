/**
 * Copyright (c) 2013-2015 <JH Barbee>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Initial Developer: JH Barbee
 *
 * For support, please see https://bitbucket.org/barbee/pantomime
 * 
 * $Id: PartMimePartInputStream.java,v 1.6 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartMimePartInputStream extends InputStream {

    private static Logger log = LoggerFactory.getLogger(PartMimePartInputStream.class.getName());

    private long start;
    private long end;
    private long position;
    private long mark;
    private Part part;
    private long positionOffset = 0;

    PartMimePartInputStream(Part part, long start, long end) {

        this.part = part;
        this.start = this.position = this.mark = start;
        this.end = end;
    }
 
    public int available() {
        return (int)(end - position);
    }

    public void close() {
    }

    public void mark(int readLimit) {
        mark = position;
    }

    public boolean markSupported() {
        return true;
    }

    public int read() throws IOException {

        byte[] b = new byte[1];
        int bytesRead = read(b);
        if ( bytesRead > 0 ) {
            return b[0];
        } else {
            return -1;
        }

    }

    public int read(byte[] b) throws IOException {

        return read(b, 0, b.length);

    }

    public int read(byte[] b, int off, int len) throws IOException {

        int maxRead = len;
        int read;

        InputStream stream = null;

        if ( (position + positionOffset) > end ) {
            return 0;
        }

        if ( (position + positionOffset + len) > end ) {
            maxRead = 1+(int)(end - (position+positionOffset));
        }

        try {

            long skipped = 0;
            long wantToSkip;

            stream = part.asSinglePart().getBody();

            wantToSkip = position + positionOffset;

            skipped = stream.skip(position + positionOffset);

            if ( skipped != wantToSkip ) {
                log.error("Unable to skip requested " + wantToSkip + " bytes.");
            }

            read = stream.read(b, off, maxRead);

            positionOffset += read;

        } catch (PantomimeException e) {
            throw new IOException(e);
        } finally {
            StreamUtility.close(this, stream);
        }

        return read;
        
    }

    public void reset() {
        position = mark;
    }

    public long skip(long n) {

        long previousPosition = position;

        if ( n < 0 ) {
            return 0;
        }

        if ( ( position  + n ) > end ) {
            position = end;
        } else {
            position += n;
        }

        return position - previousPosition;

    }

}

