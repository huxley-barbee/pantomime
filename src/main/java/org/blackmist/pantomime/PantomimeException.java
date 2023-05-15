package org.blackmist.pantomime;

/**
 * An exception wrapper for underlying exceptions.
 *
 * Generally speaking, Pantomime makes best-effort to read or manipulate
 * messages. A number of {@link java.io.UnsupportedEncodingException},
 * {@link java.text.ParseException}, and {@link javax.mail.MessagingException}
 * are handled and dealt with.
 *
 * However, there are some cases where there is no recovery when the
 * storage for the message simply is not behaving properly. These
 * exception are wrapped in a PantomimeException and passed up.
 */
public class PantomimeException extends Exception {

    public PantomimeException(String m) {
        super(m);
    }
 
    public PantomimeException(Exception e) {
        super(e);
    }
 
    public PantomimeException(String m, Exception e) {
        super(m, e);
    }
 
}

