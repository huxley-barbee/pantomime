package org.blackmist.pantomime.content;

/**
 * Email content that is encrypted.
 * <p>
 * There is one part that contains control information and the other part
 * that is the encrypted data.
 */
public class EncryptedContent {

    private SimpleContent content;
    private String control;
    private String encryptionType;
 
    // getters
    
    /**
     * The type of encryption (e.g. application/pgp-encrypted).
     */
    public String getEncryptionType() {
     return encryptionType;
     }
    
    /**
     * Gets the control information.
     */
    public String getControl() {
     return control;
     }
    
    /**
     * Gets the encrypted data.
     */
    public SimpleContent getEncrypted() {
     return content;
     }

    // setters
    
    /**
     * Set teh type of encryption (e.g. application/pgp-encrypted).
     */
    public void setEncryptionType(String  encryptionType) {
     this.encryptionType=encryptionType;
     }
    
    /**
     * Sets the control information.
     */
    public void setControl(String  control) {
     this.control=control;
     }
    
    /**
     * Sets the encrypted data.
     */
    public void setEncrypted(SimpleContent  content) {
     this.content=content;
     }

}

