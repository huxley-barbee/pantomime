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
 * $Id: BlobMimePartInputStream.java,v 1.4 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import java.sql.Blob;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BlobMimePartInputStream extends InputStream {

    private long end;
    private long position;
    private long mark;
    private Blob blob;

    BlobMimePartInputStream(Blob blob, long start, long end) {

        this.blob = blob;
        this.position = this.mark = start + 1;
        this.end = end + 1;
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

        int maxRead = b.length;
        int read;
        byte[] transfer;

        if ( position >= end ) {
            return -1;
        }

        if ( (position + b.length) > end ) {

            /* length is 1 + ending index */
            maxRead = (int)(end - position + 1);
        }


        try {

            transfer = blob.getBytes(position, maxRead);

            System.arraycopy(transfer, 0, b, 0, maxRead);

            position += maxRead;

            return maxRead;

        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {

        int maxRead = len;
        int read;
        byte[] transfer;

        if ( position > end ) {
            return -1;
        }

        if ( (position + len) > end ) {
            maxRead = (int)(end - position);
        }

        try {

            transfer = blob.getBytes(position, maxRead);

            System.arraycopy(transfer, 0, b, 0, maxRead);

            position += maxRead;

            return maxRead;

        } catch (SQLException e) {
            throw new IOException(e);
        }
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

