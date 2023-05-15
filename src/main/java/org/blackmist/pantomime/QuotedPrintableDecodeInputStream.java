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
 * $Id: QuotedPrintableDecodeInputStream.java,v 1.8 2013/10/10 14:28:26 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import java.util.LinkedList;

class QuotedPrintableDecodeInputStream extends InputStream {

    private InputStream input;
    private LinkedList<Byte> buffer = new LinkedList<Byte>();
    private byte[] readBuffer = new byte[8192];
    private boolean header = false;

    QuotedPrintableDecodeInputStream(InputStream input) {
        this.input = input;
    }

    QuotedPrintableDecodeInputStream(InputStream input, boolean header) {
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

    private static int hexToBin( char ch ) {
        if( '0'<=ch && ch<='9' )    return ch-'0';
        if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
        if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
        return -1;
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

    public Result _read() throws IOException {

        boolean keepGoing = true;

        while ( keepGoing ) {

            Byte b;

            boolean isLineEnding;

            /* make sure we have data */
            if ( buffer.size() <= 0 ) {
                int bytesRead = readMore();

                if ( bytesRead == 0 ) {
                    /* no more data */
                    return new Result();
                }
            }

            /* grab a byte */
            b = buffer.poll();

            if ( b == null ) {
                /* no more data ??? */
                return new Result();
            }

            /* usual case. plain data. return */
            if ( b != 61 ) {

                /* If this is a header, we need to convert _ to <space> */
                if ( header && ( b == 95 ) ) {
                    return new Result(32);
                } else {
                    return new Result(b);
                }
            }


            /* handle the case if the = is following by \r or \n */
            isLineEnding = handleEqualsThenLineEnding();

            /* if this was a line ending, then read another byte */
            if ( isLineEnding ) {
                continue;
            }

            /* there was an = and it wasn't a line ending. read hex value */
            return handleHexValue();

        }

        return new Result();
    }

    private boolean handleEqualsThenLineEnding() throws IOException {

        /* handle the end of line case */

        byte b;

        if ( buffer.size() < 1 ) {

            int bytesRead = readMore();

            if ( bytesRead <= 0 ) {
                return true;
            }

        }

        b = buffer.peek();

        if ( b == 10 ) {
            buffer.poll();
            return true;

        } else if ( b == 13 ) {


            buffer.poll();

            if ( buffer.size() < 1 ) {

                int bytesRead = readMore();

                if ( bytesRead <= 0 ) {
                    return true;
                }

            }

            b = buffer.peek();

            if ( b == 10 ) {
                buffer.poll();
            }

            return true;

        }

        return false;

    }

    private Result handleHexValue() throws IOException {
        char charOne;
        char charTwo;
        int hexOne;
        int hexTwo;

        if ( buffer.size() < 2 ) {

            int bytesRead = readMore();

            if ( bytesRead < 2 ) {
                /* not enough data to decode */
                return new Result(61);
            }

        }

        charOne = (char)(buffer.get(0).byteValue());
        charTwo = (char)(buffer.get(1).byteValue());

        hexOne = hexToBin(charOne);
        hexTwo = hexToBin(charTwo);

        if ( ( hexOne == -1 ) || ( hexTwo == -1 ) ) {
            /* not a valid hex number */
            return new Result(61);
        }

        buffer.poll();
        buffer.poll();

        return new Result( ( hexOne * 16 ) + hexTwo );

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

        if ( bytesRead < 1 ) {

            return -1;
        } else {
            return bytesRead;
        }

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

