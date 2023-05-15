package org.blackmist.pantomime.content;

/**
 * This is just like {@link AlternativeContent} with the addition
 * that your text/html MIME part can include images that are delivered
 * with the email rather than externally linked.
 * <p>
 * There are two problems with external images.
 * <ol>
 * <li>Many email client will block loading images over the network.
 * <li>If the recipient reads their email offline, then the image wouldn't
 * load.
 * </ol>
 *
 * So use multipart/related to avoid these problems.
 */
public class RelatedContent extends AlternativeContent {

    private InlineImage[] images;
 
    // getters
    
    /**
     * Gets the inline images for this message.
     */
    public InlineImage[] getImages() {
     return images;
     }

    // setters
    
    /**
     * Sets the inline images for this message.
     */
    public void setImages(InlineImage...  images) {
     this.images=images;
     }


}

