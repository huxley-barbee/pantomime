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
 * $Id: ContentDisposition.java,v 1.10 2015/06/07 13:23:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.UnsupportedEncodingException;

import static org.blackmist.pantomime.ContentDispositionType.*;

/**
 * A Content-Disposition header.
 *
 * This is what determines if a MIME part is <code>inline</code> or an <code>attachment</code>.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME#Content-Disposition">Wikipedia on Content-Disposition</a>
 */
public class ContentDisposition extends Header {

    /**
     * Creates an inline disposition.
     */
    public ContentDisposition() {
        this(INLINE, null);
    }

    /**
     * Creates an disposition with the given paramter.
     */
    public ContentDisposition(ContentDispositionType disposition) {
        this(disposition, null);
    }

    /**
     * Creates an disposition with the given disposition and filename.
     */
    public ContentDisposition(ContentDispositionType disposition,
        String filename) {
        this(disposition, filename, -1);
    }

    /**
     * Creates an disposition with the given disposition and filename,
     * and file size.
     */
    public ContentDisposition(ContentDispositionType disposition,
        String filename, long size) {

        super();
        setName("Content-Disposition");

        if ( disposition == ATTACHMENT ) {

            setValue(makeAttachmentValue(filename, size));

        } else {

            setValue("inline");

        }

    }

    private String makeAttachmentValue(String filename, long size) {

        StringBuilder builder = new StringBuilder();

        builder.append("attachment");

        if ( filename != null ) {

            builder.append("; ");

            builder.append(encodeFilename(filename));
        }

        if ( size > -1 ) {
            builder.append("; size=\"")
                .append(String.valueOf(size))
                .append("\"");
        }

        return builder.toString();

    }

    private String encodeFilename(String filename) {

        /* According to RFC 5987, there are three ways this is done.
         *
         * 1. If it's just English with no space, then
         *
         *      filename=myfile
         *
         * 2. If it's English with a space, then 
         *      
         *      filename="my file"
         *
         * 3. If it's not English, then 
         *
         *      filename*=encoding'country'%encodedFilename
         *
         *      The country and be blank.
         */

        int rfcCase = 1;

        int index = 0;

        byte[] chars;
        
        
        try {
            
            chars = filename.getBytes("UTF-8");

        } catch ( UnsupportedEncodingException e ) {

            chars = filename.getBytes();

        }

        while ( index < chars.length ) {

            if ( ! isPrintable(chars[index]) ) {

                rfcCase = 3;
                break;

            } else if ( chars[index] == 32 ) {

                rfcCase = 2;

            }

            index++;

        }

        if ( rfcCase == 1 ) {

            return "filename=" + filename;

        } else if ( rfcCase == 2 ) {

            return "filename=\"" + filename + "\"";

        } else {

            return "filename*=UTF-8''" + java.net.URLEncoder.encode(filename);

        }

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

    /**
     * Returns true if the disposition is NOT an ATTACHMENT.
     */
    public boolean isInline() {

        String main = getMainField();

        return ATTACHMENT != ContentDispositionType.fromString(main);
    }

    /**
     * Returns true if the disposition is ATTACHMENT.
     */
    public boolean isAttachment() {
        String main = getMainField();
        return ATTACHMENT == ContentDispositionType.fromString(main);
    }

    private static int hexToBin( char ch ) {
        if( '0'<=ch && ch<='9' )    return ch-'0';
        if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
        if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
        return -1;
    }

    private String getRfc5987Filename() {

        int start;
        int end;
        String value = getValue();

        start = value.indexOf("filename*=");

        start += 10;

        if ( start >= value.length() ) {
            return "";
        }

        end = value.indexOf(";", start);

        if ( end == -1 ) {
            end = value.length();
        }

        return value.substring(start, end);
    }

    /**
     * Returns filename in this Content Disposition header, if the
     * filename exists.
     */
    public String getFilename() {
        String encoding;
        String encodedString;
        String[] parts;
        char[] chars;
        String value =  getSubField("filename*");
        int index = 0;
        byte[] bytes;
        int length = 0;

        if ( value == null ) {
            return getSubField("filename");
        }

        /* We have a filename*, need to read it directly since
         * Header.java will strip the ' chars.
         **/

        value = getRfc5987Filename();

        parts = value.split("'");

        if ( parts.length < 3 ) {
            /* Bad formatting. */
            return null;
        }

        encoding = parts[0];

        encodedString = parts[2];

        chars = encodedString.toCharArray();

        bytes = new byte[chars.length];

        while ( index < chars.length ) {

            if ( chars[index] != '%' ) {

                bytes[length] = (byte)chars[index];

            } else {

                if ( (index+2) < chars.length ) {

                    int hexOne;
                    int hexTwo;

                    index++;
                    hexOne = hexToBin(chars[index]);

                    index++;
                    hexTwo = hexToBin(chars[index]);

                    bytes[length] = (byte) (( hexOne * 16 ) + hexTwo ) ;
                }

            }

            length++;
            index++;

        }

        try {

            return new String(bytes, 0, length, encoding);

        } catch (UnsupportedEncodingException e) {
            return new String(bytes, 0, length);
        }

    }

    /**
     * Returns file size in this Content Disposition header, if it
     * was recorded in the header.
     */
    public long getStatedSize() {
        String size = getSubField("size");

        if ( size != null ) {
            try {
                return Long.parseLong(size);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }

    /**
     * Returns file creation date in this Content Disposition header, if it
     * was recorded in the header.
     */
    public String getCreationDateString() {
        return getSubField("creation-date");
    }

    /**
     * Returns file modification date in this Content Disposition header, if it
     * was recorded in the header.
     */
    public String getModificationDateString() {
        return getSubField("modification-date");
    }

}

