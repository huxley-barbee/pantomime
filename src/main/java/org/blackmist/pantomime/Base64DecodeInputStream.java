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
 * $Id: Base64DecodeInputStream.java,v 1.10 2013/10/25 13:47:15 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import java.util.BitSet;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs Base64 decoding.
 * <p>
 * Does not support {@link java.io.InputStream#mark} or
 * {@link java.io.InputStream#reset}.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Base64">Wikipedia on Base64</a>
 * @see <a href="http://tools.ietf.org/html/rfc2045#section-6.8">RFC 2045 on use of Base64 in email.</a>
 */
class Base64DecodeInputStream extends InputStream {

    LinkedList<Integer> decodeBuffer = new LinkedList<Integer>();
    LinkedList<Byte> buffer = new LinkedList<Byte>();
    byte[] readBuffer = new byte[8192];
    InputStream input;

    /**
     * We turn this on if we encounter the equal sign.
     * RFC 2045 states we may consider any equal sign as the
     * end of content.
     */
    boolean stopDecode = false;

    private static int EQUAL = 61;

    private static final Logger log =
        LoggerFactory.getLogger(Base64DecodeInputStream.class.getName());
 
    Base64DecodeInputStream(InputStream input) {
        this.input = input;
    }
 
    /**
     * Returns an approximation of available bytes left in this stream.
     */
    public int available() throws IOException {
        return (int)(input.available() * 3 / 4);
    }

    /**
     * Closes this decode stream.
     */
    public void close() throws IOException {
        StreamUtility.close(this, input);
    }

    private int readMore() throws IOException {

        int bytesRead = input.read(readBuffer);

        if ( bytesRead == -1 ) {
            stopDecode = true;
            return -1;
        }

        for ( int index = 0; index < bytesRead; index++ ) {

            if ( isValidBase64(readBuffer[index]) ) {
                buffer.add(readBuffer[index]);
            }

        }

        return bytesRead;

    }

    private boolean isValidBase64(byte value ) {
        if ( ( value >= 65 ) && ( value <=90 ) ) {
            return true;
        }

        if ( ( value >= 97 ) && ( value <= 122 ) ) {
            return true;
        }

        if ( ( value >= 48 ) && ( value <= 57 ) ) {
            return true;
        }
        if ( value == 43 ) {
            return true;
        }

        if ( value == 47 ) {
            return true;
        }

        if ( value == EQUAL ) {
            return true;
        }

        return false;
    }

    private void convertToBase64IndexValues(byte[] data) {
        for ( int index = 0; index < data.length; index++ ) {
            byte value = data[index];

            if ( ( value >= 65 ) && ( value <=90 ) ) {
                data[index] -=65;
                continue;
            }

            if ( ( value >= 97 ) && ( value <= 122 ) ) {
                data[index] -= 71;
                continue;
            }

            if ( ( value >= 48 ) && ( value <= 57 ) ) {
                data[index] += 4;
                continue;
            }
            if ( value == 43 ) {
                data[index] = 62;
                continue;
            }

            if ( value == 47 ) {
                data[index] = 63;
                continue;
            }

        }
    }

    private void bytesToBitset(byte[] data, BitSet bits) {
        for ( int index = 0; index < data.length; index++ ) {

            byte currentByte = data[index];

            if ( ( currentByte & 32 ) == 32 ) {
                bits.set((index*6)+0);
            }

            if ( ( currentByte & 16 ) == 16 ) {
                bits.set((index*6)+1);
            }

            if ( ( currentByte & 8 ) == 8 ) {
                bits.set((index*6)+2);
            }

            if ( ( currentByte & 4 ) == 4 ) {
                bits.set((index*6)+3);
            }

            if ( ( currentByte & 2 ) == 2 ) {
                bits.set((index*6)+4);
            }

            if ( ( currentByte & 1 ) == 1 ) {
                bits.set((index*6)+5);
            }

        }

    }

    private void bitsetToBytes(BitSet bits, byte[] decodedData) {
        for ( int index = 0; index < decodedData.length; index++ ) {

            if ( bits.get( (index*8)+0 ) ) {
                decodedData[index] += 128;
            }
            if ( bits.get( (index*8)+1 ) ) {
                decodedData[index] += 64;
            }
            if ( bits.get( (index*8)+2 ) ) {
                decodedData[index] += 32;
            }
            if ( bits.get( (index*8)+3 ) ) {
                decodedData[index] += 16;
            }
            if ( bits.get( (index*8)+4 ) ) {
                decodedData[index] += 8;
            }
            if ( bits.get( (index*8)+5 ) ) {
                decodedData[index] += 4;
            }
            if ( bits.get( (index*8)+6 ) ) {
                decodedData[index] += 2;
            }
            if ( bits.get( (index*8)+7 ) ) {
                decodedData[index] += 1;
            }


        }

    }

    private int pullBytesForConversion(byte[] data) {
        int equalSignBytes = 0;

        data[0] = buffer.poll();
        data[1] = buffer.poll();
        data[2] = buffer.poll();
        data[3] = buffer.poll();

        if ( EQUAL == (int)(data[2]) ) {
            equalSignBytes++;
        }

        if ( EQUAL == (int)(data[3]) ) {
            equalSignBytes++;
        }

        return equalSignBytes;
    }

    private void fillBuffer() throws IOException {
        int bytesRead = readMore();

        if ( bytesRead < 4 ) {

            if ( buffer.size() == 0 ) {
                /* There is nothing here at all. */
                return;
            }  else {

                /* Test case 35 has a base64 encoded string with 23 chars.
                 * This is not a multiple of 4.
                 * mutt and an online decode is able to decode this,
                 * probably by append = where needed.
                 */

                for ( int index = buffer.size(); index < 4; index++ ) {
                    buffer.addLast((byte)EQUAL);
                }
            }

        }
    }

    private void decode() throws IOException {

        BitSet bits = new BitSet();
        byte[] decodedData = new byte[3];
        byte[] data = new byte[4];
        Byte b;

        int equalSignBytes = 0;

        /* first make sure we have enough data in the buffer */
        if ( buffer.size() < 4 ) {
            fillBuffer();
        }

        b = buffer.peek();

        if ( b == null ) {
            /* no data. we're done */
            return;
        }

        /* pull 4 bytes from the buffer into our 4-member local array */
        equalSignBytes = pullBytesForConversion(data);

        convertToBase64IndexValues(data);

        /* convert local 4-member array into bitset */
        bytesToBitset(data, bits);

        /* convert bit set to local 3-member array */
        bitsetToBytes(bits, decodedData);

        /* add decoded data to decode buffer */
        for ( int index = 0; index < (decodedData.length-equalSignBytes);
            index++ ) {
            int number = decodedData[index] & 0xff;
            decodeBuffer.add(number);
        }

        /**
         * We turn this on if we encounter the equal sign.
         * RFC 2045 states we may consider any equal sign as the
         * end of content.
         */
        if ( equalSignBytes > 0 ) {
            stopDecode = true;
        }
    }


    /**
     * Reads a decoded byte.
     */
    public int read() throws IOException {

        Integer b = decodeBuffer.peek();

        if ( b == null ) {

            /**
             * If the underlying stream has returned -1, then stopDecode
             * will be on.
             *
             * This is on if we encounter the equal sign.
             * RFC 2045 states we may consider any equal sign as the
             * end of content.
             */
            if ( stopDecode ) {
                return -1;
            }
            decode();
        }

        b = decodeBuffer.poll();

        if ( b == null ) {
            return -1;
        } else {
            return b;
        }

    }

    /**
     * Reads up to b.length decoded bytes into the given array and returns the
     * bytes read.
     */
    public int read(byte[] b) throws IOException {

        return read(b, 0, b.length);

    }

    /**
     * Reads up to len decoded bytes into the given array starting at offset,
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

        if ( ( bytesRead == 0 ) && stopDecode ) {
            return -1;
        }
        return bytesRead;
    }

    /**
     * Skips the given number of bytes in the decoded stream and returns
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

