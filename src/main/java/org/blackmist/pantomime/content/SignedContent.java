package org.blackmist.pantomime.content;

/**
 * This is a digitally signed message.
 *
 * There is a signature part and a second part for the actualy content.
 */
public class SignedContent {

    private SimpleContent content;
    private String signature;
    private String signatureType;
 
    // getters
    
    /**
     * Gets the signature type (e.g., application/pgp-signature).
     */
    public String getSignatureType() {
     return signatureType;
     }
    
    /**
     * Gets the signature.
     */
    public String getSignature() {
     return signature;
     }
    
    /**
     * Gets the content which was signed.
     */
    public SimpleContent getContent() {
     return content;
     }

    // setters
    
    /**
     * Sets the signature type (e.g., application/pgp-signature).
     */
    public void setSignatureType(String  signatureType) {
     this.signatureType=signatureType;
     }
    
    /**
     * Sets the signature.
     */
    public void setSignature(String  signature) {
     this.signature=signature;
     }
    
    /**
     * Sets the content which was signed.
     */
    public void setContent(SimpleContent  content) {
     this.content=content;
     }

}

