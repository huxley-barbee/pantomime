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
 * $Id: FileMessageSource.java,v 1.12 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File storage adapter for an email message.
 */
public class FileMessageSource extends StreamMessageSource {

    private static final Logger log =
        LoggerFactory.getLogger(FileMessageSource.class.getName());

    private RandomAccessFile file;
    private File f;
    private String filename;

    /**
     * Constructs a new message source based on the given filename.
     */
    public FileMessageSource(String filename) {

        this.filename = filename;

    }

    /**
     * Constructs a new message source based on the given file.
     */
    public FileMessageSource(File file) {

        this.f = file;
    }

    /**
     * (Internal use.) Loads the email message from the file.
     */
    public SourcedMessage load() throws PantomimeException {

        if ( f == null ) {
            if ( filename == null ) {
                throw new NullPointerException("Null filename");
            }

            f = new File(filename);
        }

        try {

            this.file = new RandomAccessFile(f, "rwd");
        } catch (IOException e) {
            throw new PantomimeException(e);
        }

        return super.init();

    }

    protected void seek(long newPosition) throws PantomimeException {
        super.seek(newPosition);

        try {
            file.seek(newPosition);
        } catch (IOException e) {
            throw new PantomimeException(e);
        }
    }

    int read(byte[] data) throws PantomimeException {

        try {
            return file.read(data);
        } catch (IOException e) {
            throw new PantomimeException(e);
        }
    }

    int read() throws PantomimeException {
        try {
            return file.read();
        } catch (IOException e) {
            throw new PantomimeException(e);
        }
    }

    long getLength() throws PantomimeException {
        try {
            return file.length();
        } catch (IOException e) {
            throw new PantomimeException(e);
        }
    }

    /**
     * (Internal Use.) Returns the body of the MIME part for the given MimPath.
     */
    public InputStream getBody(MimePath path) throws PantomimeException {
        long bodyStart = getBodyStart(path);
        long bodyEnd = getBodyEnd(path);
        InputStream stream = new RandomAccessFileMimePartInputStream(file,
            bodyStart, bodyEnd);
        StreamMonitor.opened(this, stream);
        return stream;
    }

    /**
     * Frees resources used by this FileMessageSource.
     */
    public void free() {

        if ( file == null ) {
            return;
        }

        try {
            file.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * (Internal use.) Saves message back to file.
     */
    public void save(InputStream stream) throws PantomimeException {

        FileOutputStream fos = null;
        byte[] buffer;
        int bytesRead;
        File temp;

        if ( f == null ) {
            this.f = new File(filename);
        }

        temp = getTempFile();

        try {

            fos = new FileOutputStream(temp);
            StreamMonitor.opened(this, fos);

            buffer = new byte[16384];

            while ( ( bytesRead = stream.read(buffer) ) > 0 ) {
                fos.write(buffer, 0, bytesRead);
            }

            if ( ! temp.renameTo(this.f) ) {
                log.warn("Unable to rename file " + temp + " to " +
                    this.f + ".");
            }

        } catch (IOException e) {
            throw new PantomimeException(e);
        } finally {
            StreamUtility.close(this, fos);
        }
        
    }

}
