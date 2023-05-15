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
 * $Id: ContentTransferEncoding.java,v 1.4 2013/09/19 19:17:22 barbee Exp $
**/

package org.blackmist.pantomime;

/**
 * There are five types of Content-Transfer-Encoding: 7bit, quoted-printable,
 * base64, 8bit, and binary.
 */
public enum ContentTransferEncoding {
 
    /**
     * This is what most English, plain text MIME parts use.
     */
    SEVEN_BIT("7bit"),
    /**
     * HTML MIME parts and plain text parts in non-English Latin and Cyrillic
     * alphabets use this. For the most part.
     */
    QUOTED_PRINTABLE("quoted-printable"),
    /**
     * This used for any binary or complex character sets like Chinese. 
     */
    BASE64("base64"),
    /**
     * Not used much.
     */
    EIGHT_BIT("8bit"),
    /**
     * Not used much.
     */
    BINARY("binary");

    private String text;

    ContentTransferEncoding(String text) {
        this.text = text;
    }

    /**
     * String representative of this Content Transfer Encoding.
     */
    public String getText() {
        return text;
    }

    /**
     * Case-insensitive conversion from string.
     */
    public static ContentTransferEncoding fromString(String text) {
        if ( text == null ) {
            text = "7bit";
        }

        text = text.toLowerCase();

        for ( ContentTransferEncoding encoding : ContentTransferEncoding.values() ) {
            if ( text.equalsIgnoreCase(encoding.text) ) {
                return encoding;
            }
        }

        return null;

    }
 
}

