package org.blackmist.pantomime.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import org.blackmist.pantomime.PantomimeException;

/**
 * An image delivered with the email that shows up in a text/html part.
 * <p>
 * As opposed to an image that is externally linked from the HTML.
 */
public class InlineImage {

    private String type;
    private String contentId;
    private File file;
    private String string;
    private InputStreamSource source;

    // getters

    /**
     * Gets the Content ID.
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Gets the type of the image.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the length of the image.
     */
    public long getLength() {
        if ( file != null && file.exists() ) {
            return file.length();
        } else if ( string != null ) {
            return string.length();
        } else if ( source != null ) {
            InputStream stream = null;
            try {
                long total = 0;
                stream = source.getInputStream();
                if ( stream != null ) {
                    byte[] buffer = new byte[16384];
                    int bytesRead;
                    while ( ( bytesRead = stream.read(buffer) ) > 0 ) {
                        total += bytesRead;
                    }
                }
                return total;
            } catch (Exception e) {
                return 0;
            } finally {
                if ( stream != null ) {
                    try {
                        stream.close();
                    } catch (IOException e) { }
                }
            }
        } else {
            return 0;
        }
    }

    /**
     * Gets the filename of the image.
     */
    public String getFilename() {
        if ( file != null && file.exists() ) {
            return file.getName();
        } else {
            return contentId;
        }
    }

    /**
     * Gets the input stream for this image.
     */
    public InputStreamSource getInputStream() {
        if ( file != null && file.exists() ) {
            return new InputStreamSource() {
                public InputStream getInputStream() throws PantomimeException {
                    try {
                        return new FileInputStream(file);
                    } catch (IOException e) {
                        throw new PantomimeException(e);
                    }
                }
            };
        } else if ( string != null ) {
            return new InputStreamSource() {
                public InputStream getInputStream() throws PantomimeException {
                    return new ByteArrayInputStream(string.getBytes());
                }
            };
        } else if ( source != null ) {
            return source;
        } else {
            return null;
        }
    }

    // setters

    /**
     * Sets the Content ID of this image.
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * Sets the image type (e.g., gif, jpg, png).
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the file for this image.
     */
    public void setFile(File file) {
        this.file = file;
        this.string = null;
        this.source = null;
    }

    /**
     * Sets the string for this image.
     */
    public void setString(String string) {
        this.file = null;
        this.string = string;
        this.source = null;
    }

    /**
     * Sets the input stream for this image.
     */
    public void setInputStreamSource(InputStreamSource source) {
        this.file = null;
        this.string = null;
        this.source = source;
    }

}