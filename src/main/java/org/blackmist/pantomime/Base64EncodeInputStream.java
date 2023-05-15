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
 * $Id: Base64EncodeInputStream.java,v 1.6 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import java.util.BitSet;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs Base64 encoding.
 * <p>
 * Pantomime will use Base64 for any content or header that has characters
 * with code points of 976 or higher.
 * <p>
 * Does not support {@link java.io.InputStream#mark} or
 * {@link java.io.InputStream#reset}.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Base64">Wikipedia on Base64</a>
 * @see <a href="http://tools.ietf.org/html/rfc2045#section-6.8">RFC 2045 on use of Base64 in email.</a>
 * @see <a href="http://en.wikipedia.org/wiki/List_of_Unicode_characters#Greek_and_Coptic">Unicode Code Point 976</a>
 */
class Base64EncodeInputStream extends InputStream {

    private LinkedList<Integer> encodeBuffer = new LinkedList<Integer>();
    private LinkedList<Byte> buffer = new LinkedList<Byte>();
    private byte[] readBuffer = new byte[8192];
    private InputStream input;
    private boolean stopEncode = false;

    private int charCountInLine = 0;

    private static final Logger log =
        LoggerFactory.getLogger(Base64EncodeInputStream.class.getName());
 
    Base64EncodeInputStream(InputStream input) {
        this.input = input;
    }
 
    /**
     * Returns an approximation of available bytes left in this stream.
     */
    public int available() throws IOException {
        return (int)(input.available() * 4 / 3);
    }

    /**
     * Closes this encode stream.
     */
    public void close() throws IOException {
        StreamUtility.close(this, input);
    }

    private int readMore() throws IOException {

        int bytesRead = input.read(readBuffer);

        for ( int index = 0; index < bytesRead; index++ ) {

            buffer.add(readBuffer[index]);

        }

        return bytesRead;

    }

    private void pullBytesForConversion(byte[] data, int size) {
        for ( int index = 0; index < 3; index++ ) {

            if ( index < size ) {
                data[index] = buffer.poll();
            } else {
                data[index] = 0;
            }
        }
    }

    private void encode() throws IOException {

        BitSet bits = new BitSet();
        byte[] encodedData = new byte[4];
        byte[] data = new byte[3];
        Byte b;
        int size = 3;

        /* first make sure we have enough data in the buffer */
        if ( buffer.size() < 3 ) {

            readMore();

            if ( buffer.size() == 0 ) {
                /* There is nothing here at all. */
                stopEncode = true;
                return;
            }

            if ( buffer.size() < 3 ) {
                size = buffer.size();
            }
        }

        b = buffer.peek();

        if ( b == null ) {
            /* no data. we're done */
            stopEncode = true;
            return;
        }

        /* pull 3 bytes from the buffer into our 3-member local array */
        pullBytesForConversion(data, size);

        /* convert local 3-member array into bitset */
        bytesToBitSet(data, bits, size);

        /* convert bit set to local 4-member array */
        bitSetToBytes(bits, encodedData, size);

        for ( int index = 0; index < (size+1); index++ ) {

            if ( charCountInLine >= 76 ) {
                charCountInLine = 0;
                encodeBuffer.add(13);
                encodeBuffer.add(10);
            }
            encodeBuffer.add(convertIntToBase64(encodedData[index]));
            charCountInLine++;
        }

        for ( int index = 0; index < ( 3 - size ); index++ ) {
            stopEncode = true;
            encodeBuffer.add(61);
        }

    }

    private void bytesToBitSet(byte[] data, BitSet bits, int size) {
        for ( int index = 0; index < size; index++ ) {

            byte currentByte = data[index];

            if ( ( currentByte & 128 ) == 128  ) {
                bits.set((index*8)+0);
            }

            if ( ( currentByte & 64 ) == 64  ) {
                bits.set((index*8)+1);
            }

            if ( ( currentByte & 32 ) == 32 ) {
                bits.set((index*8)+2);
            }

            if ( ( currentByte & 16 ) == 16 ) {
                bits.set((index*8)+3);
            }

            if ( ( currentByte & 8 ) == 8  ) {
                bits.set((index*8)+4);
            }

            if ( ( currentByte & 4 ) == 4  ) {
                bits.set((index*8)+5);
            }

            if ( ( currentByte & 2 ) == 2  ) {
                bits.set((index*8)+6);
            }

            if ( ( currentByte & 1 ) == 1  ) {
                bits.set((index*8)+7);
            }

        }

    }

    private void bitSetToBytes(BitSet bits, byte[] encodedData, int size) {
        /*
         * The size of the encoded data is 1 + the size of the plain data.
         *
         * sur => c3Vy
         * su  => c3U=
         * s   => cw==
         *
         * By the same token the number of = is (3 - plain size)
         */
        for ( int index = 0; index < (size+1); index++ ) {

            if ( bits.get((index*6)+0) ) {
                encodedData[index] += 32;
            }

            if ( bits.get((index*6)+1) ) {
                encodedData[index] += 16;
            }

            if ( bits.get((index*6)+2) ) {
                encodedData[index] += 8;
            }

            if ( bits.get((index*6)+3) ) {
                encodedData[index] += 4;
            }

            if ( bits.get((index*6)+4) ) {
                encodedData[index] += 2;
            }

            if ( bits.get((index*6)+5) ) {
                encodedData[index] += 1;
            }

        }

    }

    private int convertIntToBase64(byte value) {

        /* A - Z */
        if ( value < 26 ) {
            return value + 65;
        }

        /* a - z */
        if ( value < 52 ) {
            return value - 26 + 97;
        }

        /* 0 - 9 */
        if ( value < 62 ) {
            return value - 52 + 48;
        }

        if ( value < 63 ) {
            return 43;
        }

        return 47;
    }

    /**
     * Reads an encoded byte.
     */
    public int read() throws IOException {

        Integer b = encodeBuffer.peek();

        if ( b == null ) {

            /**
             * This is on if we encounter the equal sign.
             * RFC 2045 states we may consider any equal sign as the
             * end of content.
             */
            if ( stopEncode ) {
                return -1;
            }
            encode();
        }

        b = encodeBuffer.poll();

        if ( b == null ) {
            return -1;
        } else {
            return b;
        }

    }

    /**
     * Reads up to b.length encoded bytes into the given array and returns the
     * bytes read.
     */
    public int read(byte[] b) throws IOException {

        return read(b, 0, b.length);

    }

    /**
     * Reads up to len encoded bytes into the given array starting at offset,
     * and returns the bytes read.
     */
    public int read(byte[] b, int off, int len) throws IOException {

        int bytesRead = 0;

        for ( int index = 0; index < len; index++ ) {
            int read = read();

            if ( read == -1 ) {
                break;
            }

            b[off+index] = (byte)read;
            bytesRead++;
        }

        return bytesRead;
    }

    /**
     * Skips the given number of bytes in the encoded stream and returns
     * the number of bytes actually skipped.
     */
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

