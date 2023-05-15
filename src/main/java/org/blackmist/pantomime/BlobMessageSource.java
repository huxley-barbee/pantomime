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
 * $Id: BlobMessageSource.java,v 1.8 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.sql.Blob;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database storage adapter for an email message.
 */
public class BlobMessageSource extends StreamMessageSource {

    private static final Logger log =
        LoggerFactory.getLogger(BlobMessageSource.class.getName());

    private Blob blob;
    private long positionOffset = 1;

    /**
     * Constructs a new BlobMessageSource with the given blob.
     */
    public BlobMessageSource(Blob blob) throws PantomimeException {

        this.blob = blob;

    }

    /**
     * (Internal use.) Loads the email message from the blob.
     */
    public SourcedMessage load() throws PantomimeException {

        return super.init();

    }

    protected void seek(long newPosition) throws PantomimeException {
        super.seek(newPosition);
        positionOffset = 1;
    }

    int read(byte[] data) throws PantomimeException {

        try {
            /* blobs are index from 1, not 0 */
            long position = getPosition() + positionOffset;

            int max = data.length;

            byte[] transfer;

            if ( (position+max) > getLength() ) {
                max = (int)(getLength() - position);
            }

            if ( max == 0 ) {
                return 0;
            }

            transfer = blob.getBytes(position, max);

            System.arraycopy(transfer, 0, data, 0, transfer.length);

            positionOffset += transfer.length;

            return transfer.length;

        } catch (SQLException e) {
            throw new PantomimeException(e);
        }
    }

    int read() throws PantomimeException {
        byte[] b = new byte[1];
        int bytesRead = read(b);
        if ( bytesRead == 0 ) {
            return -1;
        } else {
            return b[0];
        }
    }

    long getLength() {
        try {
            return blob.length();
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * (Internal Use.) Returns the body of the MIME part for the given MimPath.
     */
    public InputStream getBody(MimePath path) throws PantomimeException {
        long bodyStart = getBodyStart(path);
        long bodyEnd = getBodyEnd(path);

        InputStream stream =
            new BlobMimePartInputStream(blob, bodyStart, bodyEnd);

        StreamMonitor.opened(this, stream);
        return stream;
    }

    /**
     * Frees resources used by this BlobMessageSource.
     */
    public void free() {

        try {
            blob.free();
        } catch (SQLException e) {
            log.error("Unable to free blob.", e);
        }
    }

    /**
     * (Internal use.) Saves message back to database.
     */
    public void save(InputStream stream) throws PantomimeException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        OutputStream blobOutput = null;
        byte[] buffer;
        int bytesRead;
        File temp;

        temp = getTempFile();

        try {

            fos = new FileOutputStream(temp);
            StreamMonitor.opened(this, fos);

            buffer = new byte[16384];

            while ( ( bytesRead = stream.read(buffer) ) > 0 ) {
                fos.write(buffer, 0, bytesRead);
            }

            fis = new FileInputStream(temp);
            StreamMonitor.opened(this, fis);

            blob.truncate(0);

            blobOutput = blob.setBinaryStream(1);
            StreamMonitor.opened(this, blobOutput);

            while ( ( bytesRead = fis.read(buffer) ) > 0 ) {

                blobOutput.write(buffer, 0, bytesRead);

            }

            if ( ! temp.delete() ) {
                log.warn("Unable to delete temporary file: " + temp + ".");
            }

        } catch (SQLException e) {
            throw new PantomimeException(e);
        } catch (IOException e) {
            throw new PantomimeException(e);
        } finally {

            StreamUtility.close(this, fis);
            StreamUtility.close(this, fos);
            StreamUtility.close(this, blobOutput);

        }
        
    }

}
