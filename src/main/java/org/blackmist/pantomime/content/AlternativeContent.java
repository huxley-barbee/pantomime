package org.blackmist.pantomime.content;

/**
 * Email content in both plain text and HTML formats.
 * <p>
 * Why?
 * <p>
 * People like sending email in a rich text format with fonts, colors, and
 * layout. HTML has become the <i>de facto</i> standard for such. However,
 * what if the recipient has an email client that cannot display HTML?
 * They need to take your HTML, save it out to a file, and open it in a 
 * browser. It's a pain.
 * <p>
 * If you want rich text and you want to make it easy for recipients to reader
 * your senders' messages, definitely go with alternative content or
 * {@link RelatedContent} content.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME#Alternative">Wikipedia on multipart/alternative</a>
 * @see <a href="http://tools.ietf.org/html/rfc2046#section-5.1.4">RFC 2046 on multipart/alternative</a>
 *
 * 
 *
 */
public class AlternativeContent {

    private SimpleContent plain;
    private SimpleContent html;
 
    // getters
    
    /**
     * Gets the HTML version of the content.
     */
    public SimpleContent getHtml() {
     return html;
     }
    
    /**
     * Gets the plain version of the content.
     */
    public SimpleContent getPlain() {
     return plain;
     }

    // setters
    
    /**
     * Sets the HTML version of the content.
     */
    public void setHtml(SimpleContent  html) {
     this.html=html;
     }
    
    /**
     * Sets the plain version of the content.
     */
    public void setPlain(SimpleContent  plain) {
     this.plain=plain;
     }

}

