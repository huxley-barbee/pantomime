package org.blackmist.pantomime.content;

import org.blackmist.pantomime.Message;

/**
 * Email content that a list of other messages.
 * <p>
 * This was more popular back in the day when people want their mailing
 * list subscription in digest format.
 *
 */
public class DigestContent {

    private Message[] messages;
 
    // getters
    
    /**
     * Gets the messages in this digest.
     */
    public Message[] getMessages() {
     return messages;
     }

    // setters
    
    /**
     * Sets the messages in this digest.
     */
    public void setMessages(Message...  messages) {
     this.messages=messages;
     }

}

