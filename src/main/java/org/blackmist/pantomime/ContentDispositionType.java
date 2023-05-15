package org.blackmist.pantomime;

/**
 * There are two types of Content Disposition: inline and attachment.
 */
public enum ContentDispositionType {
 

    /**
     * Inline means this MIME part should be displayed inline
     * with the rest of the message.
     */
    INLINE("inline"),

    /**
     * Attachment means this MIME part should NOT be displayed inline
     * with the rest of the message.
     */
    ATTACHMENT("attachment");
 
    private String text;

    ContentDispositionType(String text) {
        this.text = text;
    }

    /**
     * String representative of this Content Disposition.
     */
    public String getText() {
        return text;
    }

    /**
     * Case-insensitive conversion from string.
     */
    public static ContentDispositionType fromString(String text) {
        if ( text == null ) {
            text = "7bit";
        }

        text = text.toLowerCase();

        for ( ContentDispositionType type : ContentDispositionType.values() ) {
            if ( text.equalsIgnoreCase(type.text) ) {
                return type;
            }
        }

        return null;

    }
 
}

