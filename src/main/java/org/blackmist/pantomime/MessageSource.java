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
 * $Id: MessageSource.java,v 1.8 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API for loading messages from storage.
 * <p>
 * Patotmime comes with class for loading from {@link FileMessageSource},
 * {@link BlobMessageSource}, and {@link JavaMailMessageSource}.
 * <p>
 * Implement this interface for a new one.
 */
public interface MessageSource {
 
    /**
     * Loads a message.
     */
    public SourcedMessage load() throws PantomimeException;

    /**
     * Returns the number of subparts (or 0) for the given MimePath.
     */
    public int getSubPartCount(MimePath path) throws PantomimeException;

    /**
     * Returns the MIME Part for the given MIMEPath.
     */
    public Part getPart(MimePath path) throws PantomimeException;

    /**
     * Gets the preamble for the given MIMEPath.
     */
    public String getPreamble(MimePath path) throws PantomimeException;

    /**
     * Gets the epilogue for the given MIMEPath.
     */
    public String getEpilogue(MimePath path) throws PantomimeException;

    /**
     * Gets the body for the given MIMEPath.
     */
    public InputStream getBody(MimePath path) throws PantomimeException;

    /**
     * Gets the body size for the given MIMEPath.
     */
    public long getTransferEncodedBodySize(MimePath path)
        throws PantomimeException;

    /**
     * Gets the size of the MIME Part for the given MIMEPath.
     */
    public long getTransferEncodedSize(MimePath path) throws PantomimeException;

    /**
     * Releases any resources used.
     */
    public void free();

    /**
     * Serializes message back to storage.
     */
    public void save(InputStream stream) throws PantomimeException;

}

