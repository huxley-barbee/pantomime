package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message that is sourced from backend storage like a database, file,
 * or a JavaMail message.
 */
public class SourcedMessage extends Message {

    private static Logger log =
        LoggerFactory.getLogger(SourcedMessage.class.getName());

    SourcedMessage() {
        super(false);
    }

    /**
     * Frees any underlying resoures this Message may be using.
     */
    void free() {

        if ( source != null ) {
            source.free();
        }
    }

    /**
     * Writes any changes back to storage.
     */
    public void save() throws PantomimeException {

        InputStream stream = null;

        if ( source == null ) {
            return;
        }

        try {

            stream = serialize();

            source.save(stream);

        } finally {
            StreamUtility.close(this, stream);
        }

    }
 
}

