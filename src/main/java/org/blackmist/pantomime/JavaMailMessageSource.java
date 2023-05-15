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
 * $Id: JavaMailMessageSource.java,v 1.18 2015/05/27 10:47:18 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.activation.UnsupportedDataTypeException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.blackmist.pantomime.ContentTransferEncoding.*;

/**
 * JavaMail storage adapter for an email message.
 */
public class JavaMailMessageSource implements MessageSource {
 
    MimeMessage mime;
    Session session;

    private static Logger log =
        LoggerFactory.getLogger(JavaMailMessageSource.class.getName());

    /**
     * Constructs a new JavaMail message source to create a new message.
     */
    public JavaMailMessageSource(Session session) {
        this.session = session;
    }

    /**
     * Constructs a new JavaMail message source to read an existing message.
     */
    public JavaMailMessageSource(MimeMessage mime) {
        this.mime = mime;
    }

    /**
     * Returns the mime message that backs this pantomime message.
     */
    public MimeMessage getMime() {
        return mime;
    }
 
    /**
     * (Internal use.) Loads the email message from the JavaMail message.
     */
    public SourcedMessage load() throws PantomimeException {
        return (SourcedMessage)getPart(new MimePath("0"));
    }

    /**
     * (Internal Use.) Gets the preamble of the MIME part for the given
     * MimePath.
     */
    public String getPreamble(MimePath path) throws PantomimeException {
        javax.mail.Part part = getJavaPart(path);

        if ( part instanceof Multipart ) {

            try {
                return ((MimeMultipart)part).getPreamble();
            } catch (MessagingException e) {
                throw new PantomimeException(e);
            }

        } else {
            return "";
        }
    }

    /**
     * (Internal Use.) Gets the epilogue of the MIME part for the given MimePath
     */
    public String getEpilogue(MimePath path) {
        return "";
    }


    private InputStream getJavaMailInputStream(javax.mail.Part part)
        throws IOException, javax.mail.MessagingException {

        InputStream stream = new JavaMailInputStream(part);

        int current;
        int previous3 = -1;
        int previous2 = -1;
        int previous1 = -1;

        while ( ( current = stream.read() ) != -1 ) {

            if ( ( previous3 == 13 ) &&
                ( previous2 == 10 ) &&
                ( previous1 == 13 ) &&
                ( current == 10 ) ) {

                break;
            }

            previous3 = previous2;
            previous2 = previous1;
            previous1 = current;

        }

        return stream;

    }

    /**
     * (Internal Use.) Returns the body of the MIME part for the given MimPath.
     */
    public InputStream getBody(MimePath path) throws PantomimeException {

        InputStream stream = null;

        /**
         * If a JavaMail messaage comes from some extant source (e.g.
         * eml file or over the wire), then
         *
         * the transfer encoded version is available through
         *      getDataHandler().getRawInputStream()
         * the unencoded version is available through
         *      getContent() and getInputStream(), back by SharedInputStream
         *
         * If a JavaMail messages is just being created, then
         *
         * the transfer encoded version is not available at all
         * the unencoded version is available through
         *      getContent() and getInputStream() back by FileInputStream
         *      or ByteArrayInputStream or an underlying String.
         *
         * Our method must always return the transfer encoded version.
         */

        try {

            javax.mail.Part part = getJavaPart(path);
            stream = getJavaMailInputStream(part);
            StreamMonitor.opened(this, stream);

        } catch (Exception e) {
            throw new PantomimeException(e);
        }

        return stream;

    }

    /**
     * (Internal Use.) Gets the size of the body of the MIME part for the
     * given MimePath
     */
    public long getTransferEncodedBodySize(MimePath path)
        throws PantomimeException {

        InputStream stream = getBody(path);

        try {
            return StreamUtility.count(stream);
        } catch (IOException e) {
            log.error("Unable to determine size of message.");
            return -1;
        } finally {
            StreamUtility.close(this, stream);
        }
    }

    /**
     * (Internal Use.) Returns the number of subordinate MIME parts in the
     * MIME part addressed by teh given MimePath.
     */
    public int getSubPartCount(MimePath path) throws PantomimeException {
        javax.mail.Part part = getJavaPart(path);

        try {

            Object content = part.getContent();

            if ( content instanceof Multipart ) {
                return ((Multipart)content).getCount();
            } else {
                return 0;
            }
        } catch (Exception e) {
            throw new PantomimeException(e);
        }
    }

    /**
     * (Internal Use.) Returns the MIME part addressed by teh given MimePath.
     */
    public Part getPart(MimePath path) throws PantomimeException {

        return javaToPanto(getJavaPart(path), path);

    }

    private javax.mail.Part getJavaPart(MimePath path) throws PantomimeException {
        if ( path == null ) {
            return null;
        }

        if ( path.toString().equals("0") ) {
            return mime;
        }

        return getJavaPart(path, 1, mime);

    }

    private javax.mail.Part getJavaPart(MimePath path, int pathIndex,
        javax.mail.Part part) throws PantomimeException {

        try {

            Object content = part.getContent();

            if ( content instanceof Multipart ) {

                Multipart multipart = (Multipart)content;
                int index = path.get(pathIndex);
                javax.mail.Part subPart;

                if ( index >= multipart.getCount() ) {
                    log.warn("Fewer parts than expected. " + path + " Index: " +
                        pathIndex);
                    return null;
                }
                
                subPart = multipart.getBodyPart(index);

                if ( path.isChildless(pathIndex) ) {
                    return subPart;
                } else {
                    return getJavaPart(path, pathIndex+1, subPart);
                }

            } else {

                log.warn("Expecting multipart but wasn't. " + path + " Index: "
                    + pathIndex);
                return null;

            }

        } catch (Exception e) {

            throw new PantomimeException(e);

        }

    }

    private List<Line> readHeaders(javax.mail.Part part) {

        Enumeration e = null;

        ArrayList<Line> lines;

        try {

            e = part.getAllHeaders();

        } catch (MessagingException me) {

            log.error("Unable to get headers.", me);
            return null;

        }

        lines = new ArrayList<Line>();

        for ( ; e.hasMoreElements() ; ) {

            javax.mail.Header header = (javax.mail.Header)e.nextElement();

            StringBuilder builder = new StringBuilder();

            Line line = new Line();

            builder.append(header.getName()).append(": ")
                .append(header.getValue());

            line.text = builder.toString();
            line.ending = "\r\n";

            lines.add(line);
        }

        return lines;

    }

    private String getBoundary(List<Line> lines) {

        for ( Line line : lines ) {
            int pos;
            BoundaryMarkers markers;
            String text = line.text;

            if ( text == null ) {
                continue;
            }

            if ( ! text.toLowerCase().startsWith("content-type") ) {
                continue;
            }

            pos = text.indexOf("boundary");

            if ( pos == -1 ) {
                continue;
            }

            markers = getBoundaryMarkers(text, pos);

            if ( markers.end == -1 ) {
                markers.end = text.length();
            }

            return text.substring(markers.start, markers.end);

        }

        return null;
    }

    private static class BoundaryMarkers {
        private int start = -1;
        private int end = -1;
    }

    private BoundaryMarkers getBoundaryMarkers(String text, int pos) {
        int searchState = 0;
        char[] chars = text.toCharArray();
        boolean hasQuotes = false;

        BoundaryMarkers markers = new BoundaryMarkers();

        markers.end = -1;

        for ( int index = pos+1; index < chars.length; index++ ) {

            char c = chars[index];

            if ( searchState == 0 ) {

                if ( c == '=' ) {
                    searchState = 1;
                }

            } else if ( searchState == 1 ) {

                if ( Character.isWhitespace(c) ) {
                    continue;
                }

                if ( c == '"' ) {

                    hasQuotes = true;
                    markers.start = index+1;

                } else {
                    hasQuotes = false;
                    markers.start = index;
                }

                searchState = 2;
            } else {

                if ( hasQuotes ) {

                    if ( c == '"' ) {

                        markers.end = index;
                        break;

                    }
                } else {

                    if ( Character.isWhitespace(c) || ( c == ';' ) ) {
                        markers.end = index;
                    }
                }

            }
        }

        return markers;
    }

    private Part javaToPanto(javax.mail.Part javaPart, MimePath path)
        throws PantomimeException {

        Part part;

        List<Line> headers = readHeaders(javaPart);

        if ( path.getPath().equals("0") ) {
            part = new SourcedMessage();
        } else {

            String disposition = "";

            try {
                if ( javaPart.getDisposition() != null ) {
                    disposition = javaPart.getDisposition();
                }
            } catch (MessagingException e) {
                log.error("Unable to get disposition.", e);
            }


            if ( disposition.equalsIgnoreCase(javax.mail.Part.ATTACHMENT) ) {
                part = new Attachment();
            } else {
                part = new Part();
            }
        }

        part.setHeaders(headers);

        if ( getBoundary(headers) != null ) {
            part.muster(this, path, getBoundary(headers));
        } else {
            part.muster(this, path);
        }


        return part;
    }

    /**
     * Frees resources used by this JavaMailMessageSource.
     */
    public void free() {
        mime = null;
    }

    private static class Size {
        long size;
    }

    /**
     * (Internal Use.) Gets the size of the MIME part for the given MimePath.
     */
    public long getTransferEncodedSize(MimePath path)
        throws PantomimeException {

        OutputStream out = null;
        javax.mail.Part part = getJavaPart(path);
        final Size size = new Size();

        try {

            out = new OutputStream() {
                public void write(int b) {
                    size.size++;
                }
            };
            StreamMonitor.opened(this, out);

            part.writeTo(out);

        } catch (Exception e) {
            throw new PantomimeException(e);
        } finally {
            StreamUtility.close(this, out);
        }

        return size.size;
    }

    /**
     * (Internal use.) Saves message back to the JavaMail message.
     */
    public void save(InputStream stream) throws PantomimeException {

        try {
            this.mime = new MimeMessage(session, stream);
        } catch (MessagingException e) {
            throw new PantomimeException(e);
        }

    }
}

