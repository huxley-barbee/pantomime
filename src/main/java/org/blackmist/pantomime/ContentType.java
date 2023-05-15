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
 * $Id: ContentType.java,v 1.5 2013/10/09 14:06:13 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.IOException;

/**
 * A Content-Type header.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME#Content-Type">Wikipedia on Content-Type header</a>
 */
public class ContentType extends Header {

    /**
     * Constructs a new Content-Type header with no value. */
    public ContentType() {
        super();
        setName("Content-Type");
    }
 
    /**
     * Returns the main field of this Content-Type header.
     */
    public String getType() {

        String type = getMainField();

        if ( type != null ) {
            return type.toLowerCase();
        } else {
            return null;
        }

    }

    /**
     * Returns the boundary defined in this Content-Type header,
     * if defined.
     */
    public String getBoundary() {
        return getSubField("boundary");
    }

    /**
     * Returns the character set  defined in this Content-Type header,
     * if defined.
     */
    public String getCharset() {
        return getSubField("charset");
    }

}
