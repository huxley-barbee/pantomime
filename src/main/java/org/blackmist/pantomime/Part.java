/**
 * Copyright (c) 2013-2015 <JH Barbee>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Initial Developer: JH Barbee
 *
 * For support, please see https://bitbucket.org/barbee/pantomime
 * 
 * $Id: Part.java,v 1.29 2015/06/08 16:13:51 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.FileNameMap;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.blackmist.pantomime.content.*;

import static org.blackmist.pantomime.ContentTransferEncoding.*;
import static org.blackmist.pantomime.ContentDispositionType.*;

/**
 * A MIME Part.
 * <p>
 * A MIME part comprises headers and an optional body.
 * Between the headers and body is a blank line.
 * <p>
 * More details on MIME headers in the API documentation for {@link Header}.
 * <p>
 * A MIME part can either:
 * <ol>
 * <li>contain content, or
 * <li>contain other MIME parts.
 * </ol>
 *
 * In the first case, the body is some content (e.g., your email about smelly
 * guy on the subway or a picture of your cat).
 * <p>
 * In the second case, the body contains headers and bodies of other MIME parts.
 * <p>
 * MIME is inherently recursive!
 * <p>
 * This Part API allows you to manipulate both cases.
 * <p>
 * Your one and only indication that a MIME part is one or the other is
 * {@link #isMultipart},
 * <p>
 * Once you know if a MIME part is a multipart or not, you can access these
 * specific methods.
 * <p>
 * To maninpulate the MIME part as a multipart. call {@link #asMultipart}.
 * See the API docuemntation for {@link Part.Multipart} for more infomration.
 * <p>
 * To maninpulate the MIME part as a single part . call {@link #asSinglePart}.
 * See the API docuemntation for {@link Part.SinglePart} for more infomration.
 * <p>
 * If you want to specify that a MIME part should be one or the other call
 * either {@link #specializeAsSinglePart} or {@link #specializeAsMultipart}.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME">Wikipedia on MIME</a>
 */
public class Part {

    private static Logger log = LoggerFactory.getLogger(Part.class.getName());
    private static int boundaryCounter = -1;

    protected CaseInsensitiveMap<List<Header>> headers =
        new CaseInsensitiveMap<List<Header>>();
    protected List<String> invalidHeaders = new ArrayList<String>();

    protected MessageSource source;

    private SinglePart single = null;
    private Multipart multi = null;
    private MimePath path;
    private boolean isModified = false;

    private String createBoundary() {

        StringBuilder builder;

        builder = new StringBuilder();

        builder.append("Pantomime-");

        builder.append(Pantomime.VERSION);
        builder.append("-");
        builder.append(String.valueOf(System.currentTimeMillis()));
        builder.append("-");
        builder.append(getNewBoundaryCount());

        return builder.toString();
    }


    private void setMimePath(MimePath path) {
        this.path = path;
    }

    private int getNewBoundaryCount() {
        boundaryCounter++;
        if ( boundaryCounter < 0 ) {
            boundaryCounter = 0;
        }

        return boundaryCounter;
    }

    void muster(MessageSource source, MimePath path) {
        this.source = source;
        this.path = path;
        specializeAsSinglePart();
    }

    void muster(MessageSource source, MimePath path, String boundary) {
        this.source = source;
        this.path = path;
        specializeAsMultipart();
        asMultipart().setBoundary(boundary);
    }

    private void updateMimePathPrefix(MimePath prefix)
        throws PantomimeException {

        path.realignLineage(prefix);

        if ( ! isMultipart() ) {
            return;
        }

        for ( Part subPart : multi.getSubParts() ) {
            subPart.updateMimePathPrefix(prefix);
        }

    }

    /**
     * Specialize this MIME part as a single part with content.
     */
    public void specializeAsSinglePart() {
        if ( single != null ) {
            return;
        }

        single = new SinglePart();
        multi = null;
    }

    /**
     * Specialize this MIME part as a multipart that contains other parts.
     */
    private void specializeAsMultipart(boolean saveSinglePart) {
        if ( multi != null ) {
            return;
        }

        if ( ! saveSinglePart ) {
            single = null;
        }
        multi = new Multipart();
    }

    /**
     * Specialize this MIME part as a multipart that contains other parts.
     */
    public void specializeAsMultipart() {
        specializeAsMultipart(false);
    }

    /**
     * Returns the single part aspect of this MIME part.
     */
    public SinglePart asSinglePart() {
        return single;
    }

    /**
     * Returns the multipart aspect of this MIME part.
     */
    public Multipart asMultipart() {
        return multi;
    }

    private boolean isContentType(String is) {

        ContentType type = getContentType();

        if ( type == null ) {
            return false;
        }

        if ( type.getType() == null ) {
            return false;
        }

        return type.getType().trim().toLowerCase().equalsIgnoreCase(is);
    }

    private boolean isContentTypeMultipart() {

        ContentType type = getContentType();

        if ( type == null ) {
            return false;
        }

        if ( type.getType() == null ) {
            return false;
        }

        return type.getType().trim().toLowerCase().startsWith("multipart");

    }

    /**
     * Returns the Content-ID header value of this MIME part, if such exists.
     */
    public String getContentId() throws PantomimeException {

        Header header;

        if ( isMultipart() ) {
            log.error("Attempted single part operation on multi");
            return null;
        }

        header = getFirstHeader("content-id");

        if ( header == null ) {
            return null;
        }

        return header.getValue();

    }

    /**
     * Returns the Content-Disposition header of this MIME part, if it
     * exists.
     */
    public ContentDisposition getContentDisposition() {

        Header header;

        if ( isMultipart() ) {
            log.error("Attempted single part operation on multi");
            return null;
        }

        header = getFirstHeader("content-disposition");

        if ( header == null ) {
            return null;
        }

        if ( ! ( header instanceof ContentDisposition ) ) {
            return null;
        }

        return (ContentDisposition)header;

    }


    /**
     * Returns the MimePath of this MIME 
     */
    public MimePath getMimePath() {
        return path;
    }

    /**
     * Returns true if this is part is a container for subordinate parts.
     */
    public boolean isMultipart() {
        return multi != null;
    }

    void setHeaders(List<Line> lines) throws PantomimeException {

        StringBuilder buffer = null;
        Line previous = null;
        Header header = null;

        for ( Line line : lines ) {

            String text = line.text;

            if ( previous != null && previous.ending.equals("\r") ) {

                buffer.append(previous.ending).append(text);
                header.setTransferEncodedValue(buffer.toString());

            } else if ( text.startsWith(" ") || text.startsWith("\t") ) {

                if ( header != null ) {
                    buffer.append(previous.ending).append(text);
                    header.setTransferEncodedValue(buffer.toString());
                }

            } else {

                int colon = text.indexOf(":");

                if ( colon != -1 ) {

                    String name = text.substring(0, colon).trim();
                    String value = text.substring(colon + 1);

                    if ( name.equalsIgnoreCase("Content-Disposition") ) {
                        header = new ContentDisposition ();
                    } else if ( name.equalsIgnoreCase("Content-Type") ) {
                        header = new ContentType ();
                    } else {
                        header = new Header ();
                    }

                    while ( value.length() > 0 && Character.isWhitespace(value.charAt(0)) ) {
                        value = value.substring(1);
                    }

                    buffer = new StringBuilder (value);
                    header.setName(name);
                    header.setTransferEncodedValue(value);

                    if ( !this.headers.containsKey(name) ) {
                        headers.put(name, new ArrayList<Header> ());
                    }

                    headers.get(name).add(header);

                } else {

                    invalidHeaders.add(text);

                }

            }

            previous = line;

        }

    }

    /**
     * Returns the Content-Transfer-Encoding header of this MIME part,
     * if it exists.
     */
    public ContentTransferEncoding getContentTransferEncoding()
        throws PantomimeException {
        Header header = getFirstHeader("content-transfer-encoding");

        if ( header == null ) {
            return null;
        }

        return ContentTransferEncoding.fromString(header.getValue());

    }

    /**
     * Returns the Content-Type header of this MIME part, if it exists.
     */
    public ContentType getContentType() {

        Header header = getFirstHeader("content-type");

        if ( header == null ) {
            return null;
        }

        if ( header instanceof ContentType ) {
            return (ContentType)header;
        }

        ContentType type = new ContentType();
        type.setName(header.getName());
        type.setValue(header.getValue());
        return type;

    }

    /**
     * Returns an InputSgream of the entire MIME 
     */
    public InputStream serialize() throws PantomimeException {
        InputStream stream = new PartInputStream(this);

        StreamMonitor.opened(this, stream);

        return stream;
    }

    /**
     * Returns true if there is a header with the given name.
     */
    public boolean hasHeader(String headerName) {

        List<Header> headerList = getHeaders(headerName);

        if ( headerList == null ) {
            return false;
        }

        if ( headerList.size() == 0 ) {
            return false;
        }

        return true;

    }

    /**
     * Returns true if there is a header with the given name and value.
     */
    public boolean hasHeaderWithValue(String headerName, String value)
        throws PantomimeException {

        List<Header> headerList = getHeaders(headerName);

        if ( headerList == null ) {
            return false;
        }

        for ( Header header : headerList ) {
            if ( header.getValue() == null ) {
                continue;
            }

            if ( header.getValue().equals(value) ) {
                return true;
            }
        }

        return false;

    }

    /**
     * Returns true if there is a header with the given name and value.
     */
    public boolean hasHeaderWithValue(String headerName, int value)
        throws PantomimeException {
        return hasHeaderWithValue(headerName, String.valueOf(value));
    }

    /**
     * Returns all the headers as a list.
     */
    public List<Header> getHeaderList() {

        ArrayList<Header> asList = new ArrayList<Header>();

        for ( String name : headers.keySet() ) {

            asList.addAll(headers.get(name));

        }

        return asList;

    }

    /**
     * Returns all the headers in a hash of lists, where the hash key is
     * the header name.
     */
    public Map<String, List<Header>> getHeaders() {
        return headers;
    }

    /**
     * Returns a list of headers for the given name.
     */
    public List<Header> getHeaders(String headerName) {
        if ( headerName == null ) {
            return null;
        }

        if ( headers.get(headerName) != null ) {
            return headers.get(headerName);
        } else {
            return new ArrayList<Header>();
        }
    }

    /**
     * Returns the first header for a given name.
     */
    public Header getFirstHeader(String headerName) {

        List<Header> result = getHeaders(headerName);

        if ( result == null ) {
            return null;
        }

        if ( result.size() == 0 ) {
            return null;
        }

        return result.get(0);

    }

    public String toString() {

        String disposition = "";
        String id = "";

        if ( hasHeader("content-disposition") ) {
            disposition = "[ Disposition: " +
                getFirstHeader("content-disposition").getValue() + " ]";
        }

        if ( hasHeader("content-id") ) {
            id = "[ ID: " + getFirstHeader("content-id").getValue() + " ]";
        }

        if ( path == null ) {

            return "Part: Not Attached [ Valid Headers: " +
                getHeaderList().size() + " ] [ Type: " + getContentType() +
                " ] " + disposition + " " + id;

        } else {

            return "Part: " + path.toString() + "[ Valid Headers: " +
                getHeaderList().size() + " ] [ Type: " + getContentType() +
                " ] " + disposition + " " + id;

        }
    }

    /**
     * Returns the list of headers in this MIME part that are not RFC
     * compliant.
     */
    public List<String> getInvalidHeaders() {
        return invalidHeaders;
    }

    MessageSource getSource() {
        return source;
    }

    /**
     * Returns the size of the whole MIME part encoded for trasnport.
     */
    public long getTransferEncodedSize() throws PantomimeException {

        if ( isModified ) {

            InputStream stream = null;

            try {
                stream = serialize();

                return StreamUtility.count(stream);

            } catch (IOException e) {
                log.error("Unable to get size of stream", e);
                return -1;
            } finally {
                StreamUtility.close(this, stream);
            }
        } else if ( source != null ) {
            return source.getTransferEncodedSize(path);
        }

        return -1;

    }

    /**
     * Sets the Content-Encoding header for this MIME part.
     */
    public void setContentEncoding(ContentTransferEncoding encoding) {

        Header header = new Header();
        header.setName("Content-Transfer-Encoding");
        header.setValue(encoding.getText());
    }

    /**
     * Sets the Content-Type header for this MIME 
     */
    public void setContentType(ContentType type) {

        setHeader(type);
    }

    /**
     * Sets the Content-Type header for this MIME 
     */
    public void setContentType(String type) {

        Header header = new Header();
        header.setName("Content-Type");
        header.setValue(type);

        setHeader(header);
    }

    private void _addHeader(Header header) {
        List<Header> newHeaderList;

        isModified = true;

        for ( String name : headers.keySet() ) {
            if ( name.trim().equalsIgnoreCase(header.getName()) ) {
                headers.get(name).add(header);
                return;
            }
        }

        newHeaderList = new ArrayList<Header>();
        newHeaderList.add(header);
        headers.put(header.getName(), newHeaderList);


    }

    /**
     * Add a new header.
     */
    public void addHeader(Header header) {
        _addHeader(header);
    }

    /**
     * Add a new header.
     */
    public void addHeader(String headerName, String headerValue) {
        Header newHeader = new Header();
        newHeader.setName(headerName);
        newHeader.setValue(headerValue);
        _addHeader(newHeader);
    }

    /**
     * Add a new header.
     */
    public void addHeader(String headerName, int headerValue) {
        addHeader(headerName, String.valueOf(headerValue));
    }

    /**
     * Remove a header.
     */
    public void removeHeader(String headerName) {

        headers.remove(headerName.trim().toLowerCase());
        isModified = true;

    }

    /**
     * Sets a header.
     */
    public void setHeader(Header header) {
        removeHeader(header.getName());
        addHeader(header);
    }

    /**
     * Sets a header.
     */
    public void setHeader(String headerName, String headerValue) {
        removeHeader(headerName);
        addHeader(headerName, headerValue);
    }

    /**
     * Sets a header.
     */
    public void setHeader(String headerName, int headerValue) {
        setHeader(headerName, String.valueOf(headerValue));
    }

    /**
     * Converts a non-multipart MIME part into a multipart MIME 
     *
     * The current part retains the headers. However, the Content-Type of the
     * current part becomes multipart/mixed.
     * <p>
     * The body of the current body is reassigned to the new child 
     */
    public void filialize() throws PantomimeException {

        Part newPart;
        List<Part> newSubParts;
        String boundary;
        boolean wasSinglePart = false;

        if ( isMultipart() && asMultipart().isMultipartMixed() ) {
            return;
        }

        newPart = new Part();

        if ( isMultipart() ) {
            /**
             * Let's say we're a multipart/alternative.
             * Content moves to the new part.
             * Boundary moves to the new part.
             * MimePath is now orig.0
             */
            newPart.muster(null, new MimePath(path, 0), multi.getBoundary());
            newPart.asMultipart().setSubParts(multi.getSubParts());
            newPart.setContentType(getContentType());

        } else {

            /**
             * We are a single part.
             * Content moves to the new part.
             * MimePath is now orig.0
             */

            String type = null;
            String charset = null;

            newPart.muster(null, new MimePath(path, 0));

            if ( getContentType() != null ) {
                type = getContentType().getType();
                charset = getContentType().getCharset();
            }


            newPart.asSinglePart().set(new InputStreamSource() {
                public InputStream getInputStream() throws PantomimeException {
                    return asSinglePart().getBody();
                }
            }, type, charset);

            /* The current part is single. It needs to be converted to
             * a multipart now.
             */
            specializeAsMultipart(true);

        }

        /* This part becomes a multipart/mixed with a new boundar.
         * The MimePath stays the same.
         */

        multi.setBoundary(multi.setMultipartContentType("mixed"));
        newSubParts = new ArrayList<Part>();
        newSubParts.add(newPart);

        multi.setSubParts(newSubParts);

        isModified = true;
    }

    protected Part searchForInlinePart(String type)
        throws PantomimeException {

        /* Test case 0056
         * This message has a multipart/mixed content type but no boundary
         * defined. Use isContentTypeMultipart() rather than isMultipart().
         */
        if ( isContentTypeMultipart() ) {

            int count = 0;

            if ( isMultipart() ) {
                count = multi.getSubPartCount();
            }

            if ( count > 0 ) {

                List<Part> parts = multi.getSubParts();

                for ( Part part : parts ) {

                    Part found = part.searchForInlinePart(type);

                    if ( found != null ) {
                        return found;
                    }
                }

            } else {

                /* Test case 0027 is a message that claims to be multipart
                 * that is really just a single 
                 *
                 * It claims to have a boundary.
                 * Content-Type: multipart/mixed; boundary="MimeMultipartBoundary"
                 * but the boundary never shows up.
                 *
                 * JavaMail fails here with a MessagingException saying
                 * missing start boundary.
                 */

                return this;
            }

        } else { 

            if ( ! ( this instanceof Attachment ) ) {

                ContentType contentType = getContentType();
                /* If part has no content type and no disposition,
                 * then that works. */

                if ( contentType == null ) {
                    return this;
                }

                if ( contentType.getType() == null ) {
                    return this;
                }

                if ( contentType.getType().toLowerCase().equals(type) ) {
                    return this;
                }

            }

        }

        return null;
    }

    static ContentTransferEncoding determineTransferEncoding(InputStream stream, String charset) {

        ContentTransferEncoding encoding = SEVEN_BIT;

        try {

            int bytesRead = 0;
            byte[] data = new byte[6*3000];
            ContentTransferEncoding newEncoding = SEVEN_BIT;

            while ( ( bytesRead = stream.read(data) ) > 0 ) {

                String s;
                char[] chars;

                try {

                    if ( charset != null ) {
                        s = new String(data, 0, bytesRead, charset);
                    } else {
                        s = new String(data, 0, bytesRead);
                    }
                } catch (UnsupportedEncodingException e) {
                    s = new String(data, 0, bytesRead);
                }

                newEncoding = determineTransferEncoding(s);

                if ( newEncoding == BASE64 ) {
                    encoding = newEncoding;
                    break;
                }

                if ( newEncoding == QUOTED_PRINTABLE ) {
                    encoding = newEncoding;
                }
            }

        } catch (IOException e) {
            log.error("Unable to read whole stream to determine transfer " +
                "encoding.");
        }

        return encoding;
    }
 
    private static ContentTransferEncoding determineTransferEncoding(String s) {
        ContentTransferEncoding encoding = SEVEN_BIT;
        char[] chars = s.toCharArray();

        for ( int index = 0; index < chars.length; index++ ) {
            int codePoint = Character.codePointAt(chars, index);

            if ( ( encoding == SEVEN_BIT ) && ( codePoint > 127 ) ) {
                encoding = QUOTED_PRINTABLE;
            }

            /* 976 only because that about where Outlook switches
             * from QP to Base64.
             */
            if ( ( encoding != BASE64 ) && ( codePoint > 976 ) ) {
                encoding = BASE64;
                return encoding;
            }
        }

        return encoding;
    }

    /**
     * This is a MIME part that is specialized as a single part, with its
     * own content.
     * <p>
     * You cannot instantiate a single part directly. Instead you must call
     * {@link Part#asSinglePart}.
     * <p>
     * <b>Working with single parts</b>
     * <br/>
     * You can read the content with the following:
     * <ul>
     * <li>getContent() - Returns the content.
     * <li>getTransferEncodedContent() - Returns the content ready for transport.
     * Anything outside of ASCII must be
     * encoded for <b>transport</b>. If the body has characters with
     * code points above 976, Pantomime will use Base64 encoding. If the
     * code points are between 127 and 976, Pantomime will use Quoted Printable
     * encoding.
     * </ul>
     *
     * @see <a href="http://en.wikipedia.org/wiki/MIME">Wikipedia on MIME</a>
      * @see <a href="http://en.wikipedia.org/wiki/List_of_Unicode_characters#Greek_and_Coptic">Unicode Codepoints</a>
     */
    public class SinglePart {

        private File newContentFromFile = null;
        private String newContent = null;
        private InputStreamSource newContentSource = null;

        private SinglePart() { }

        /**
         * Returns an InputStream of the body.
         */
        public InputStream getBody() throws PantomimeException {

            InputStream newContent = single.getNewBody();

            if ( newContent != null ) {
                return newContent;

            } else {

                ContentTransferEncoding encoding = getContentTransferEncoding();

                if ( encoding == null ) {
                    return source.getBody(path);
                }

                if ( encoding == ContentTransferEncoding.BASE64 ) {
                    InputStream stream =
                        new Base64DecodeInputStream(source.getBody(path));
                    StreamMonitor.opened(this, stream);
                    return stream;
                } else if ( encoding == ContentTransferEncoding.QUOTED_PRINTABLE ) {
                    InputStream stream =
                        new QuotedPrintableDecodeInputStream(source.getBody(path));
                    StreamMonitor.opened(this, stream);
                    return stream;
                } else {
                    return source.getBody(path);
                }
            }

        }

        /**
         * Returns an body as a String. Note that this method is not
         * suitable for large message since this method will load the
         * entire body into memory.
         */
        public String getBodyAsString() throws PantomimeException {
            ContentType type = getContentType();
            InputStream stream = getBody();

            try {

                if ( type != null ) {
                    return StreamUtility.asString(stream, type.getCharset());
                } else {
                    return StreamUtility.asString(stream, null);
                }


            } catch (IOException e) {
                log.error("Unable to stringify.", e);
                return "";
            } finally {

                StreamUtility.close(this, stream);

            }
        }

        private String getCharset() {
            ContentType type = getContentType();
            String charset = null;
            if ( type != null ) {
                charset = type.getCharset();
            }
            return charset;
        }

        private ContentTransferEncoding determineTransferEncoding(InputStream stream) {
            return Part.determineTransferEncoding(stream, getCharset());
        }

        /**
         * Returns the body of this MIME part ready for transport.
         *
         * If the body has characters with
         * code points above 976, Pantomime will use Base64 encoding. If the
         * code points are between 127 and 976, Pantomime will use Quoted Printable
         * encoding.
         */
        public InputStream getTransferEncodedBody()
            throws PantomimeException {

            if ( hasNewBody() ) {

                ContentTransferEncoding encoding;
                InputStream stream = null;

                try {
                    stream = single.getNewBody();
                    encoding = determineTransferEncoding(stream);
                } finally {
                    StreamUtility.close(this, stream);
                }

                if ( encoding == BASE64 ) {
                    stream = new Base64EncodeInputStream(single.getNewBody());

                    StreamMonitor.opened(this, stream);

                    return stream;

                } else if ( encoding == QUOTED_PRINTABLE ) {
                    stream = new QuotedPrintableEncodeInputStream(single.getNewBody());
                    StreamMonitor.opened(this, stream);

                    return stream;
                } else {
                    return single.getNewBody();

                }
            } else if ( source != null ) {

                /* Here, we're expecting getBody() to return the transfer
                 * encoded body.
                 *
                 * One way or another we expect that the body was previously
                 * encoced.
                 */
                return source.getBody(path);
            } else {
                return null;
            }


        }

        /**
         * Returns the size of the body encoded for trasnport.
         */
        public long getTransferEncodedBodySize() throws PantomimeException {
            if ( hasNewBody() ) {
                InputStream stream =  null;

                try {
                    stream = getTransferEncodedBody();
                    return StreamUtility.count(stream);
                } catch (IOException e) {
                    log.error("Unable to get size of stream", e);
                    return -1;
                } finally {
                    StreamUtility.close(this, stream);
                }
            } else if ( source != null ) {
                return source.getTransferEncodedBodySize(path);
            }

            return -1;

        }


        /**
         * Returns true if this MIME part is text/plain content.
         */
        public boolean isPlainText() {

            return isContentType("text/plain");
        }

        /**
         * Returns true if this MIME part is text/html content.
         */
        public boolean isHtml() {
            return isContentType("text/html");
        }

        /**
         * Returns true if this MIME part is an attachment message.
         */
        public boolean isRfc822Message() {
            return isContentType("message/rfc822");
        }

        /**
         * If this MIME part is a mesage/rfc822 attachment, return it as a 
         * full-fledged {@link Message}.
         */
        public Message asRfc822Message() throws PantomimeException {

            PartMessageSource partSource;

            if ( ! isRfc822Message() ) {
                return null;
            }

            partSource = new PartMessageSource(Part.this);

            return partSource.load();

        }

        /**
         * Sets the Content-Disposition header for this MIME 
         */
        public void setContentDisposition(ContentDisposition disposition) {
            setHeader(disposition);
        }

        /**
         * Sets the Content-Disposition header for this MIME part as an
         * attachment for the given filename.
         */
        public void setContentDisposition(String filename) {
            setHeader(new ContentDisposition(ATTACHMENT, filename));
        }

        /**
         * Sets the Content-Disposition header for this MIME part as an
         * attachment for the given filename.
         */
        public void setContentDisposition(String filename, long size) {
            setHeader(new ContentDisposition(ATTACHMENT, filename, size));
        }

        /**
         * Sets the Content-Type header for this MIME 
         */
        public void setContentType(String type, String charset) {

            ContentType header;
            StringBuilder builder;

            header = new ContentType();
            builder = new StringBuilder();

            header.setName("Content-Type");

            builder.append(type);

            if ( charset != null ) {
                builder.append("; charset=\"")
                    .append(charset)
                    .append("\"");
            }
            header.setValue(builder.toString());

            setHeader(header);
        }

        void clearNewBody() {

            newContent = null;
            newContentFromFile = null;
            newContentSource = null;
        }

        /**
         * Sets the body of this MimePart to the given stream.
         */
        public void set(InputStreamSource contentSource, String type,
            String charset) throws PantomimeException {

            ContentTransferEncoding encoding;
            InputStream stream = null;

            if ( contentSource == null ) {
                return;
            }

            newContent = null;
            newContentFromFile = null;
            newContentSource = contentSource;
            setContentType(type, charset);

            try {

                stream = contentSource.getInputStream();
                StreamMonitor.opened(this, stream);

                encoding = determineTransferEncoding(stream);
            } finally {
                StreamUtility.close(this, stream);
            }

            setTransferEncoding(encoding);

            isModified = true;

        }

        private void setTransferEncoding(ContentTransferEncoding encoding) {
            Header header = new Header();
            header.setName("Content-Transfer-Encoding");
            header.setValue(encoding.getText());
            setHeader(header);
        }


        /**
         * Sets the body of this MimePart to the given file.
         */
        public void set(File content, String type, String charset)
            throws PantomimeException {

            ContentTransferEncoding encoding;
            FileInputStream stream = null;

            newContent = null;
            newContentSource = null;
            newContentFromFile = content;
            setContentType(type, charset);


            try {

                stream = new FileInputStream(content);
                StreamMonitor.opened(this, stream);

                encoding = determineTransferEncoding(stream);

                setTransferEncoding(encoding);

            } catch (IOException e) {

                throw new PantomimeException(e);

            } finally {
                StreamUtility.close(this, stream);
            }

            isModified = true;
        }

        /**
         * Sets the body of this MimePart to the given string.
         */
        public void set(String content, String type, String charset) {

            ContentTransferEncoding encoding;

            if ( isMultipart() ) {
                log.error("Attempted single part operation on multi");
                return;
            }


            newContentFromFile = null;
            newContentSource = null;
            newContent = content;
            setContentType(type, charset);

            encoding = Part.determineTransferEncoding(content);
            setTransferEncoding(encoding);

            isModified = true;
        }

        void saveRfc822Message(final InputStream stream)
            throws PantomimeException {

            newContentSource = new InputStreamSource() {
                public InputStream getInputStream() throws PantomimeException {

                    try {
                        stream.reset();
                    } catch (IOException e) {
                        log.error("Cannot save RFC822 MIME Part.", e);
                        throw new PantomimeException(e);
                    }

                    return stream;
                }
            };

        }

        private InputStream getNewBody() throws PantomimeException {

            InputStream stream = null;

            if ( newContentFromFile != null ) {

                try {
                    stream = new FileInputStream(newContentFromFile);
                } catch (FileNotFoundException e) {
                    throw new PantomimeException(e);
                }
            } else if ( newContent != null ) {

                String charset = getCharset();

                if ( charset != null ) {

                    try {
                        byte[] data = newContent.getBytes(charset);
                        stream =new ByteArrayInputStream(data);
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Unable to getbytes for charset.", e);
                    }

                } else {
                    stream = new ByteArrayInputStream(newContent.getBytes());
                }

            } else if ( newContentSource != null ) {
                stream = newContentSource.getInputStream();
            } else {
                log.info("Invalid new content source.");
            }

            StreamMonitor.opened(this, stream);

            return stream;
        }

        private boolean hasNewBody() {
            if ( isMultipart() ) {
                log.error("Attempted single part operation on multi");
                return false;
            }

            return ( newContent != null ) ||
                ( newContentFromFile != null ) ||
                ( newContentSource != null );
        }

        /**
         * Sets the Content-ID header of this MIME 
         */
        public void setContentId(String contentId) {
            Header header;

            if ( isMultipart() ) {
                log.error("Attempted single part operation on multi");
                return;
            }

            header  = new Header();

            header.setName("Content-ID");
            header.setValue("<" + contentId + ">");
            setHeader(header);
        }

    }

    /**
     * This is a MIME part that is specialized as a multipart.
     * <p>
     * You cannot instantiate a multipart directly. Instead you must call
     * {@link Part#asMultipart}.
     * <p>
     * <b>Working with multiparts</b>
     * <br/>
     * Here are some of the methods you can use to profile and manipulate subordinate MIME parts:
     * <ul>
     * <li>getSubParts() - Returns a list of subordinate MIME parts.
     * <li>getSubPartCount() - Returns the number of subordinate MIME parts.
     * <li>setSubPartCount() - Sets the number of subordinate MIME parts.
     * <li>addSubPart() - Adds a new subordinate MIME 
     * <li>insertSubPart() - Inserts a new subordinate MIME 
     * <li>filialize() - Converts a single part into a multi
     * <li>addAttachment() - Convenience method to add a new subordinate MIME
     * part as an attachment.
     * <li>isMultipartAlternative - Returns true if content type is multipart/alternative.
     * <li>isMultipartEncrypted - Returns true if content type is multipart/encrypted.
     * <li>isMultipartMixed - Returns true if content type is multipart/mixed.
     * <li>isMultipartRelated - Returns true if content type is multipart/related.
     * <li>isMultipartReport - Returns true if content type is multipart/report.
     * <li>isMultipartSigned - Returns true if content type is multipart/signed.
     * </ul>
     *
     * There are also a number of set() methods that automatically set multiple
     * layers of multipart content all at once.
     * <p>
     * @see <a href="http://en.wikipedia.org/wiki/MIME">Wikipedia on MIME</a>
     */
    public class Multipart {

        private String newPreamble;
        private String newEpilogue;
        private String boundary;
        private List<Part> proxiedSubParts = null;

        private Multipart() { }

        String getBoundary() {
            return boundary;
        }
     
        private void setBoundary(String boundary) {
            this.boundary = boundary;
        }

        private void setSimple(Part part, SimpleContent content, String type)
            throws PantomimeException {

            SinglePart single;

            single = part.asSinglePart();

            if ( content.getInputStreamSource() != null ) {
                single.set(content.getInputStreamSource(), type,
                    content.getCharset());
            } else if ( content.getFile() != null ) {
                single.set(content.getFile(), type, content.getCharset());
            } else {
                single.set(content.getString(), type, content.getCharset());
            }

        }

        /**
         * Returns the multipart preamble of this MIME 
         */
        public String getPreamble() throws PantomimeException {

            if ( getSubPartCount() == 0 ) {
                return "";
            }

            if ( newPreamble != null ) {
                return newPreamble;
            } else if ( source != null ) {
                return source.getPreamble(path);
            } else {
                return "";
            }

        }

        /**
         * Returns the multipart epilogue of this MIME 
         */
        public String getEpilogue() throws PantomimeException {

            if ( getSubPartCount() == 0 ) {
                return "";
            }

            if ( getSubPartCount() == 0 ) {
                return "";
            }

            if ( newEpilogue != null ) {
                return newEpilogue;
            } else if ( source != null ) {
                return source.getEpilogue(path);
            } else {
                return "";
            }


        }

        /**
         * Sets the preamble.
         */
        public void setPreamble(String preamble) {
            newPreamble = preamble;
        }

        /**
         * Sets the epilogue.
         */
        public void setEpilogue(String epilogue) {
            newEpilogue = epilogue;
        }

        /**
         * Returns all the subparts of this MIME part that are attachments.
         */
        public List<Attachment> getAttachments() throws PantomimeException {

            ArrayList<Attachment> attachments;
            List<Part> parts;

            attachments = new ArrayList<Attachment>();

            parts = getSubParts();

            for ( Part part : parts ) {

                if ( part instanceof Attachment ) {
                    attachments.add((Attachment)part);
                }

            }

            return attachments;

        }

        /**
         * Returns a subordinate MIME part for the given Content ID.
         */
        public Part getSubPart(String contentId) throws PantomimeException {

            if ( contentId == null ) {
                return null;
            }

            for ( Part part : getSubParts() ) {

                String partId = getContentId();

                if ( partId == null ) {
                    continue;
                }

                if ( partId.trim().equalsIgnoreCase(contentId) ) {
                    return part;
                }
            }

            return null;
        }

        /**
         * Sets teh body of this MIME part to the given multipart/alternative
         * content.
         */
        public void set(AlternativeContent plainAndHtml)
            throws PantomimeException {

            this.boundary = setMultipartContentType("alternative");

            Part plain = addSubPart();
            Part html = addSubPart();

            SimpleContent plainContent = plainAndHtml.getPlain();
            SimpleContent htmlContent = plainAndHtml.getHtml();

            setSimple(plain, plainContent, "text/plain");
            setSimple(html, htmlContent, "text/html");

        }

        /**
         * Sets teh body of this MIME part to the given multipart/related
         * content.
         */
        public void set(RelatedContent plainAndHtmlWithImages)
            throws PantomimeException {

            this.boundary = setMultipartContentType("related");

            Part alternative = addSubPart();
            alternative.specializeAsMultipart();
            alternative.asMultipart().set((AlternativeContent)plainAndHtmlWithImages);

            for ( InlineImage image : plainAndHtmlWithImages.getImages() ) {

                Part imagePart = addSubPart();
                ContentDisposition disposition;

                disposition = new ContentDisposition(INLINE,
                    image.getFilename(), image.getLength());

                imagePart.asSinglePart().setContentDisposition(disposition);
                imagePart.asSinglePart().setContentId(image.getContentId());
                imagePart.asSinglePart().set(image.getInputStream(), image.getType(), null);
                
            }
        }

        /**
         * Sets the body of this MIME part to the given digitally signed
         * content.
         */
        public void set(SignedContent signed) throws PantomimeException {

            this.boundary = setMultipartContentType("signed");

            SimpleContent content = signed.getContent();

            Part contentPart = addSubPart();
            Part signaturePart = addSubPart();
            SinglePart signatureSingle = signaturePart.asSinglePart();

            setSimple(contentPart, content, "text/plain");

            signaturePart.setContentType(signed.getSignatureType());

            signatureSingle.set(signed.getSignature(),
                signed.getSignatureType(), null);

        }

        /**
         * Sets the body of this MIME part to the given encrypted content.
         */
        public void set(EncryptedContent encrypted) throws PantomimeException {

            this.boundary = setMultipartContentType("signed");

            SimpleContent data = encrypted.getEncrypted();

            Part controlPart = addSubPart();
            Part encryptedPart = addSubPart();
            SinglePart controlSingle = controlPart.asSinglePart();

            controlSingle.set(encrypted.getControl(),
                encrypted.getEncryptionType(), null);

            setSimple(encryptedPart, data, "application/octet-stream");

        }

        /**
         * Sets the body of this MIME part to the given digest content.
         */
        public void set(DigestContent digest) throws PantomimeException {

            boundary = setMultipartContentType("digest");

            for ( final Message message : digest.getMessages() ) {

                Part newPart = addSubPart();

                newPart.asSinglePart().set(new InputStreamSource() {
                    public InputStream getInputStream() throws PantomimeException {

                        return message.serialize();
                    }
                }, "message/rfc822", null);

            }
        }

        /**
         * Sets the body of this MIME part to the given bounce message.
         */
        public void set(final ReportContent report) throws PantomimeException {

            this.boundary = setMultipartContentType("report");

            Part notice = addSubPart();
            Part status = addSubPart();
            Part original = addSubPart();
            SinglePart originalSingle = original.asSinglePart();

            notice.asSinglePart().set(report.getNotice(), "text/plain",
                report.getNoticeCharset());

            status.asSinglePart().set(report.getStatus(),
                "message/delivery-status", null);

            originalSingle.set(new InputStreamSource() {
                public InputStream getInputStream() throws PantomimeException {

                    return report.getOriginal().serialize();
                }
            }, "message/rfc822", null);

            originalSingle.setContentDisposition(new ContentDisposition(ATTACHMENT));
        }

        private void setSubParts(List<Part> parts) {

            proxiedSubParts = parts;
        }

        /**
         * Returns the number of subordinate parts contained in this MIME 
         * Returns 0 if this is not a container.
         */
        public int getSubPartCount() throws PantomimeException {


            return getSubParts().size();

        }

        /**
         * Returns the list of subordinate parts contained in this MIME 
         *
         * Returns null if this is not a container.
         */
        public List<Part> getSubParts() throws PantomimeException {

            if ( proxiedSubParts == null ) { 

                if ( source != null ) {

                    ArrayList<Part> parts = new ArrayList<Part>();


                    int count = source.getSubPartCount(path);

                    for ( int index = 0; index < count; index++) {
                        parts.add(source.getPart(new MimePath(path, index)));
                    }

                    proxiedSubParts = parts;
                } else {
                    proxiedSubParts = new ArrayList<Part>();
                }

            }

            return proxiedSubParts;

        }

        /**
         * Sets the Content-Type header for this MIME part assuming it's a
         * multi
         */
        public String setMultipartContentType(String multipartType) {

            String boundary;

            boundary = createBoundary();

            setHeader("Content-Type", "multipart/" + multipartType +
                "; boundary=\"" + boundary + "\"");
            return boundary;
        }

        /**
         * Removes a subordinate part from this multi
         *
         * The index is the index of the MIME part in this level.
         */
        public Part removeSubPart(int index) throws PantomimeException {

            List<Part> subParts;
            Part removed = null;

            subParts = getSubParts();

            for ( int jndex = index; jndex < subParts.size(); jndex++ ) {

                if ( index == jndex ) {
                    removed = subParts.remove(jndex);
                } else {

                    MimePath oldPath = subParts.get(jndex).path;
                    MimePath newPath = oldPath.decrement(oldPath.length()-1);

                    subParts.get(jndex).updateMimePathPrefix(newPath);

                }

            }
            isModified = true;

            return removed;
        }

        /**
         * Inserts a new subordinate part into this multi
         *
         * The index is where you want the Part to be in this level.
         */
        public Part insertSubPart(int index) throws PantomimeException {

            List<Part> subParts = getSubParts();
            Part part = null;
            MimePath originalPath = null;

            for ( int jndex = subParts.size()-1; jndex >= index; jndex++ ) {

                Part subPart = subParts.get(jndex);
                originalPath = subPart.path;

                subPart.path = originalPath.increment();
            }


            muster(null, originalPath);
            subParts.add(index, part);
            isModified = true;

            return part;
        }

        private Part addAttachment() throws PantomimeException {
            return (Attachment)addSubPart(new Attachment());
        }

        /**
         * Adds a new subordinate part into this multi
         */
        public Part addSubPart() throws PantomimeException {
            return addSubPart(new Part());
        }

        /**
         * Adds a new subordinate part into this multi
         */
        private Part addSubPart(Part part) throws PantomimeException {

            List<Part> subParts = getSubParts();
            MimePath newPath;

            if ( subParts.size() == 0 ) {
                newPath = new MimePath(path, 0);
            } else {
                Part last = subParts.get(subParts.size()-1);
                newPath = last.getMimePath().increment();
            }

            part.muster(null, newPath);

            subParts.add(part);

            isModified = true;

            return part;
        }

        /**
         * Returns true if this is part is a container for subordinate parts
         * and is multipart/alternative.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartAlternative() throws PantomimeException {
            return isContentType("multipart/alternative");
        }

        /**
         * Returns true if this is part is a container for subordinate parts
         * and is multipart/mixed.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartMixed() throws PantomimeException {
            return isContentType("multipart/mixed");
        }

        /**
         * Returns true if this is part is a container for subordinate parts
         * and is multipart/related.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartRelated() throws PantomimeException {
            return isContentType("multipart/related");
        }

        /**
         * Returns true if this is part is a container for subordinate parts
         * and is multipart/digest.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartDigest() throws PantomimeException {
            return isContentType("multipart/digest");
        }

        /**
         * Returns true if this is part is a bounce message.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartReport() throws PantomimeException {
            return isContentType("multipart/report");
        }

        /**
         * Returns true if this is a digitally signed message.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartSigned() throws PantomimeException {
            return isContentType("multipart/signed");
        }

        /**
         * Returns true if this is an encrypted message.
         *
         * @see <a href="http://en.wikipedia.org/wiki/MIME#Multipart_messages">Wikipedia on multipart messages</a>
         */
        public boolean isMultipartEncrypted() throws PantomimeException {
            return isContentType("multipart/encrypted");
        }

        /**
         * Returns an attachment for the given Content ID.
         */
        public Attachment getAttachment(String contentId)
            throws PantomimeException {

            if ( contentId == null ) {
                return null;
            }

            for ( Part part : getSubParts() ) {

                String partId = getContentId();

                if ( ! ( part instanceof Attachment ) ) {
                    continue;
                }

                if ( partId == null ) {
                    continue;
                }

                if ( partId.trim().equalsIgnoreCase(contentId) ) {
                    return (Attachment)part;
                }
            }

            return null;
        }

        private Part addAttachment(String filename, long size, String mimeType)
            throws PantomimeException {
            ContentDisposition disposition;
            Part newPart;

            if ( mimeType == null ) {
                mimeType = "application/octet-stream";
            }

            filialize();

            if ( size > -1 ) {

                disposition = new ContentDisposition(ATTACHMENT, filename, size);
            } else {
                disposition = new ContentDisposition(ATTACHMENT, filename);
            }

            newPart = addAttachment();
            newPart.asSinglePart().setContentDisposition(disposition);

            return newPart;
        }

        /**
         * Adds a new subordinate MIME part.
         */
        public void addAttachment(Attachment attachment)
            throws PantomimeException {

            List<Part> subParts;
            MimePath newPath;

            filialize();

            subParts = getSubParts();

            if ( subParts.size() == 0 ) {
                newPath = new MimePath(path, 0);
            } else {
                Part last = subParts.get(subParts.size()-1);
                newPath = last.getMimePath().increment();
            }

            ((Part)attachment).setMimePath(newPath);

            subParts.add(attachment);

            isModified = true;


        }

        /**
         * Adds a new subordinate MIME part that is a file attachment.
         */
        public Part addAttachment(File file, String mimeType)
            throws PantomimeException {

            Part part;

            part = addAttachment(file.getName(), file.length(), mimeType);
            part.asSinglePart().set(file, mimeType, null);
            return part;
        }

        /**
         * Adds a new subordinate MIME part that is a file attachment.
         */
        public Part addAttachment(InputStreamSource stream, String filename,
            String mimeType) throws PantomimeException {

            Part part;

            part = addAttachment(filename, -1, mimeType);
            part.asSinglePart().set(stream, mimeType, null);
            return part;
        }

        /**
         * Adds a new subordinate MIME part that is a file attachment.
         */
        public Part addAttachment(String content, String filename,
            String mimeType) throws PantomimeException {

            Part part;

            part = addAttachment(filename, content.length(), mimeType);
            part.asSinglePart().set(content, mimeType, null);
            return part;
        }

        /**
         * Adds a new subordinate MIME part that is a file attachment.
         */
        public Part addAttachment(InputStreamSource stream, String filename)
            throws PantomimeException {

            return addAttachment(stream, filename, getMimeType(filename));
        }

        /**
         * Adds a new subordinate MIME part that is a file attachment.
         */
        public Part addAttachment(String content, String filename)
            throws PantomimeException {

            return addAttachment(content, filename, getMimeType(filename));
        }

        /**
         * Adds a new subordinate MIME part that is a file attachment.
         */
        public Part addAttachment(File file) throws PantomimeException {
            return addAttachment(file, getMimeType(file.getName()));
        }

        private String getMimeType(String filename) throws PantomimeException {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            return fileNameMap.getContentTypeFor(filename);
        }

    }
}

