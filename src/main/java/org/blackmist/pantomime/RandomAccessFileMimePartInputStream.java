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
 * $Id: RandomAccessFileMimePartInputStream.java,v 1.6 2013/10/09 14:06:13 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

class RandomAccessFileMimePartInputStream extends InputStream {

    long end;
    long position;
    long mark;
    RandomAccessFile file;

    RandomAccessFileMimePartInputStream(RandomAccessFile file,
        long start, long end) {

        this.file = file;
        this.position = this.mark = start;
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
        int b;

        if ( position > end ) {
            return -1;
        }

        file.seek(position);

        b = file.read();
        position = file.getFilePointer();
        return b;

    }

    public int read(byte[] b) throws IOException {

        int maxRead = b.length;
        int read;

        if ( position >= end ) {
            return -1;
        }

        if ( (position + b.length) > end ) {

            /* length is 1 + ending index */
            maxRead = (int)(end - position + 1);
        }


        file.seek(position);

        read  = file.read(b, 0, maxRead);

        position = file.getFilePointer();


        return read;

    }

    public int read(byte[] b, int off, int len) throws IOException {

        int maxRead = len;
        int read;

        if ( position >= end ) {
            return -1;
        }

        if ( (position + len) > end ) {
            maxRead = (int)(end - position);
        }

        file.seek(position);

        read  = file.read(b, off, maxRead);

        position = file.getFilePointer();

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

