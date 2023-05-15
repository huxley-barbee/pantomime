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
 * $Id: QuotedPrintableEncodeInputStream.java,v 1.5 2013/10/10 14:28:26 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import java.util.LinkedList;

class QuotedPrintableEncodeInputStream extends InputStream {

    private InputStream input;
    private LinkedList<Byte> buffer = new LinkedList<Byte>();
    private LinkedList<Byte> encodedBuffer = new LinkedList<Byte>();
    private byte[] readBuffer = new byte[8192];
    private boolean header = false;
    private int charCountInLine = 0;

    QuotedPrintableEncodeInputStream(InputStream input) {
        this.input = input;
    }

    QuotedPrintableEncodeInputStream(InputStream input, boolean header) {
        this.input = input;
        this.header = header;
    }

    public int available() throws IOException {
        return input.available();
    }

    public void close() throws IOException {
        StreamUtility.close(this, input);
    }

    public void mark(int readLimit) {
        return;
    }

    public boolean markSupported() {
        return false;
    }


    private int readMore() throws IOException {

        int bytesRead = input.read(readBuffer);

        for ( int index = 0; index < bytesRead; index++ ) {

            buffer.add(readBuffer[index]);

        }

        return bytesRead;

    }

    private static class Result {
        private int data;
        private boolean state;
        private Result() {
            data = -1;
            state = false;
        }
        private Result(int data) {
            this.data = data;
            state = true;
        }
    }

    public int read() throws IOException {

        return _read().data;

    }

    private Result _read() throws IOException {

        Byte b = encodedBuffer.poll();

        if ( b == null ) {
            encode();
            b = encodedBuffer.poll();
        }

        if ( b == null ) {
            return new Result();
        }

        return new Result(b);

    }

    private boolean isPrintable(Byte b) {

        if ( b == 61 ) {
            return false;
        }

        if ( b == 9 ) {
            return true;
        }

        if ( ( b >= 32 ) && ( b <= 126 ) ) {
            return true;
        }

        return false;
    }

    private void encode() throws IOException {

        Byte b;

        /* make sure we have data */
        if ( buffer.size() <= 0 ) {
            int bytesRead = readMore();

            if ( bytesRead == 0 ) {
                return;
            }
        }

        /* grab a byte */
        b = buffer.poll();

        if ( b == null ) {
            return;
        }

        if ( isPrintable(b) ) {

            /* plain character. no change */

            if ( charCountInLine >= 75 ) {
                /* eol = \r \n, then b */
                encodedBuffer.add((byte)61);
                encodedBuffer.add((byte)13);
                encodedBuffer.add((byte)10);
                charCountInLine = 0;
            }

            if ( header && ( b == 32 ) ) {
                encodedBuffer.add((byte)95);
            } else {
                encodedBuffer.add(b);
            }

            charCountInLine++;

        } else {

            String hex = String.format("%02X", b);


            if ( charCountInLine >= 73 ) {
                /* eol = \r \n, then b */
                encodedBuffer.add((byte)61);
                encodedBuffer.add((byte)13);
                encodedBuffer.add((byte)10);
                charCountInLine = 0;
            }

            encodedBuffer.add((byte)61);
            encodedBuffer.add((byte)hex.charAt(0));
            encodedBuffer.add((byte)hex.charAt(1));

            charCountInLine += 3;
            

        }

    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {

        int bytesRead = 0;

        for ( int index = 0; index < len; index++ ) {
            Result result = _read();

            if ( ! result.state ) {
                break;
            }

            b[off+index] = (byte)result.data;
            bytesRead++;
        }

        return bytesRead;

    }

    public void reset() {
        return;
    }

    public long skip(long n) throws IOException {
        int skipped = 0;

        for ( int index = 0; index < n; index++ ) {
            if ( read() == -1 ) {
                break;
            } else {
                skipped++;
            }
        }

        return skipped;

    }
}

