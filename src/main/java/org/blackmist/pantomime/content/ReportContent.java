package org.blackmist.pantomime.content;

import org.blackmist.pantomime.Message;

/**
 * This is a bounce message.
 * <p>
 * There is a human readable notice MIME part. There is a computer-friendly
 * MIME part so mail servers can take automated action based on the bounce.
 * Finally, there is the orignal message.
 *
 */
public class ReportContent {

    private String noticeCharset;
    private String notice;
    private String status;
    private Message original;
 
    // getters
    
    /**
     * Gets the character set of the human-readable notice.
     */
    public String getNoticeCharset() {
     return noticeCharset;
     }

    /**
     * Gets the original message.
     */
    public Message getOriginal() {
     return original;
     }
    
    /**
     * Gets the computer-friendly status message.
     */
    public String getStatus() {
     return status;
     }
    
    /**
     * Gets the human-readable notice.
     */
    public String getNotice() {
     return notice;
     }

    // setters
    
    /**
     * Sets the character set of the human-readable notice.
     */
    public void setNoticeCharset(String  noticeCharset) {
     this.noticeCharset=noticeCharset;
     }

    /**
     * Sets the original message.
     */
    public void setOriginal(Message  original) {
     this.original=original;
     }
    
    /**
     * Sets the computer-friendly status message.
     */
    public void setStatus(String  status) {
     this.status=status;
     }
    
    /**
     * Sets the human-readable notice.
     */
    public void setNotice(String  notice) {
     this.notice=notice;
     }

}

