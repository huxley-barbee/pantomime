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
 * $Id: Attachment.java,v 1.4 2013/10/25 13:47:15 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.File;
import java.io.IOException;

/**
 * This is an attachment.
 *
 * An attachment is defined as any MIME part where the
 * {@link ContentDisposition} header is <code>attachment</code>.
 *
 * No other definition of attachment applies.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME#Content-Disposition">Wikipedia on Content-Disposition</a>
 * @see <a href="http://tools.ietf.org/html/rfc2183">RFC 2183 on Content-Disposition</a>
 */
public class Attachment extends Part {

    public Attachment() {
        super();
        specializeAsSinglePart();
    }

    public Attachment(String s, String filename, String type, String charset)
        throws PantomimeException {

        super();
        specializeAsSinglePart();
        asSinglePart().set(s, type, charset);
        asSinglePart().setContentDisposition(filename, s.length());

    }

    public Attachment(File file, String type, String charset)
        throws PantomimeException {

        super();
        specializeAsSinglePart();
        asSinglePart().set(file, type, charset);
        asSinglePart().setContentDisposition(file.getName(), file.length());

    }

    /**
     * Returns true if this attachment has a filename.
     */
    public boolean hasFilename() {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getFilename() != null;
    }

    /**
     * Returns true the size of the message was recorded in the email.
     */
    public boolean hasStatedSize() throws IOException {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getStatedSize() > -1;
    }

    /**
     * Returns true if this attachment was a file and its creation
     * date was recorded in the email.
     */
    public boolean hasCreationDateString() {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getCreationDateString() != null;
    }

    /**
     * Returns true if this attachment was a file and its modification
     * date was recorded in the email.
     */
    public boolean hasModificationDateString() {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getModificationDateString() != null;
    }

    /**
     * Returns the filename of the attachment, if there was one.
     */
    public String getFilename() {

        String filename = null;

        ContentDisposition disposition = getContentDisposition();

        if ( disposition != null ) {

            filename = disposition.getFilename();

        }

        if ( filename == null ) {

            ContentType type = getContentType();

            if ( type != null ) {

                filename = type.getSubField("name");

            }

        }

        return filename;

    }

    /**
     * Returns the size of the attachment, if it was recorded in the email.
     */
    public long getStatedSize() {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getStatedSize();
    }

    /**
     * Returns the creation date attachment, if it was a file and the date
     * was recorded in the email.
     */
    public String getCreationDateString() {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getCreationDateString();
    }

    /**
     * Returns the modification date attachment, if it was a file and the date
     * was recorded in the email.
     */
    public String getModificationDateString() throws IOException {
        ContentDisposition disposition = getContentDisposition();
        return disposition.getModificationDateString();
    }

    /**
     * Returns the type of the attachment.
     */
    public String getFileType() {
        ContentType type = getContentType();
        if ( type == null ) {
            return null;
        }

        return type.getType();
    }
}

