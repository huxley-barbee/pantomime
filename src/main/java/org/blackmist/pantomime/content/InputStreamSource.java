package org.blackmist.pantomime.content;

import java.io.InputStream;

import org.blackmist.pantomime.PantomimeException;

/**
 * Some place from which we can repeatedly get an {@link java.io.InputStream}
 * to add to a MIME part as the content body.
 */
public interface InputStreamSource {

    /**
     * Returns an {@link java.io.InputStream}.
     *
     * Must support returning a new or rewound stream on successive
     * invocations.
     */
    public InputStream getInputStream() throws PantomimeException;
 
}

