package org.blackmist.pantomime.content;

import java.io.File;

/**
 * This is a non-multipart content, just straight up data.
 */
public class SimpleContent {

    private String s;
    private File f;
    private InputStreamSource i;
    private String charset = "UTF-8";
 
    // getters
    
    /**
     * Gets the character set of the content.
     */
    public String getCharset() {
     return charset ;
     }

    /**
     * Gets the content as a stream.
     */
    public InputStreamSource getInputStreamSource() {
     return i;
     }
    
    /**
     * Gets the content as a file.
     */
    public File getFile() {
     return f;
     }
    
    /**
     * Gets the content as a string.
     */
    public String getString() {
     return s;
     }

    // setters
    
    /**
     * Sets the character set of the content.
     */
    public void setCharset(String  charset ) {
     this.charset =charset ;
     }

    /**
     * Sets the content as a stream.
     */
    public void set(InputStreamSource  i) {
     this.i=i;
     }
    
    /**
     * Sets the content as a file.
     */
    public void set(File  f) {
     this.f=f;
     }
    
    /**
     * Sets the content as a string.
     */
    public void set(String  s) {
     this.s=s;
     }

}

