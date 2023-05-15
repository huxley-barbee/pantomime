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
 * $Id: Message.java,v 1.27 2015/06/09 15:36:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.blackmist.pantomime.content.InputStreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Message.
 *
 * A message is a MIME part like any other except it is expected
 * to have certain headers like subject, from, to, cc, and bcc.
 *
 * See the API documentation for {@link Part} for more details.
 */
public class Message extends Part {

    private static Logger log =
        LoggerFactory.getLogger(Message.class.getName());

    Message(boolean defaultHeaders) {
        super();
        muster(null, new MimePath());

        if ( defaultHeaders ) {
            setHeader("MIME-Version", "1.0");
            setHeader("X-Mailer",
                "Pantomime/" + String.valueOf(Pantomime.VERSION));
            setDate(Calendar.getInstance());
        }
    }

    /**
     * Constructs a new, empty message.
     */
    public Message() {
        this(true);
    }

    /**
     * Gets the From header or the Sender header if the first does
     * not exist.
     *
     * @return {@link org.blackmist.pantomime.Address}
     */
    public Address getSender() throws PantomimeException {

        Header header = getFirstHeader("From");

        if ( header == null ) {
            header = getFirstHeader("Sender");
        }

        if ( header == null ) {
            return null;
        } else {
            return new Address(header.getValue());
        }

    }

    private List<Address> splitAddresses(String string) {

        List<Address> list = new ArrayList<Address>();
        StringBuilder buffer = new StringBuilder();
        boolean inQuotes = false;

        for ( char c : string.toCharArray() ) {

            if ( c == '"' ) {

                inQuotes = ! inQuotes;
                buffer.append(c);

            } else if ( c == ',' ) {

                if ( inQuotes ) {

                    buffer.append(c);
                    continue;

                } else {

                    list.add(new Address(buffer.toString()));
                    buffer.delete(0, buffer.length());

                }
            } else {

                buffer.append(c);
            }

        }

        list.add(new Address(buffer.toString()));

        return list;

    }

    private List<Address> getRecipients(String headerName) {
        List<Header> headers = getHeaders(headerName);

        List<Address> addresses;

        if ( headers == null ) {
            return null;
        }

        addresses = new ArrayList<Address>();

        for ( Header header : headers ) {

            addresses.addAll(splitAddresses(header.getValue()));
        }

        return addresses;
    }

    /**
     * Gets the To header recipients.
     *
     * @return List<org.blackmist.pantomime.Address>
     */
    public List<Address> getToRecipients() {
        return getRecipients("to");
    }

    /**
     * Gets the CC header recipients.
     *
     * @return List<org.blackmist.pantomime.Address>
     */
    public List<Address> getCcRecipients() {
        return getRecipients("cc");
    }

    /**
     * Gets the BCC  header recipients.
     *
     * @return List<org.blackmist.pantomime.Address>
     */
    public List<Address> getBccRecipients() {
        return getRecipients("bcc");
    }

    /**
     * Gets all recipients as strings.
     *
     * @return List<String>
     */
    public List<String> getAllRecipientStrings() {

        List<Address> addresses = getAllRecipients();

        ArrayList<String> strings = new ArrayList<String>(addresses.size());

        for ( Address address : addresses ) {
            strings.add(address.toString());
        }

        return strings;

    }

    /**
     * Gets all recipients.
     *
     * @return List<org.blackmist.pantomime.Address>
     */
    public List<Address> getAllRecipients() {

        ArrayList<Address> addresses = new ArrayList<Address>();
        List<Address> tmp = getToRecipients();

        if ( tmp != null ) {
            addresses.addAll(tmp);
        }

        tmp = getCcRecipients();

        if ( tmp != null ) {
            addresses.addAll(tmp);
        }

        tmp = getBccRecipients();

        if ( tmp != null ) {
            addresses.addAll(tmp);
        }

        return addresses;

    }

    /**
     * Gets the Subject header.
     *
     * @return List<org.blackmist.pantomime.Address>
     */
    public String getSubject() throws PantomimeException {
        Header header = getFirstHeader("subject");

        if ( header == null ) {
            return null;
        }

        return header.getValue();
    }

    /**
     * Gets the Date header.
     *
     * @return Calendar
     */
    public Calendar getDate() {
        Header header = getFirstHeader("date");

        if ( header == null ) {
            return null;
        }

        return header.getValueAsDate();
    }

    /**
     * Gets the plain content of the first inlined text/plain part.
     * It could simply be the body of the message, or the first
     * part of a multipart/alternative, or of a multipart/alternative
     * inside a multipart/mixed. The search is depth-first.
     * 
     * @return java.io.InputStream
     */
    public String getPlainBodyAsString() throws PantomimeException {

        /**
         * The plain content is the first inlined text/plain part.
         * It could simply be the body of the message, or the first
         * part of a multipart/alternative, or of a multipart/alternative
         * inside a multipart/mixed.
         *
         * So we'll need to do a depth first search for the first
         * inlined text/plain part.
         */

        Part part = searchForInlinePart("text/plain");

        if ( part != null ) {

            if ( part.isMultipart() ) {
                /* Handle test case 0027 */
                part.specializeAsSinglePart();
            }

            return part.asSinglePart().getBodyAsString();
        } else {
            return null;
        }

    }

    /**
     * Gets the HTML content of the first inlined text/htmlpart.
     * It could simply be the body of the message, or the first
     * part of a multipart/alternative, or of a multipart/alternative
     * inside a multipart/mixed. The search is depth-first.
     * 
     * @return java.io.InputStream
     */
    public String getHtmlBodyAsString() throws PantomimeException {
        Part part = searchForInlinePart("text/html");

        if ( part != null ) {

            if ( part.isMultipart() ) {
                /* Handle test case 0070  */
                part.specializeAsSinglePart();
            }

            return part.asSinglePart().getBodyAsString();
        } else {
            return null;
        }

    }

    /**
     * Gets the plain content of the first inlined text/plain part.
     * It could simply be the body of the message, or the first
     * part of a multipart/alternative, or of a multipart/alternative
     * inside a multipart/mixed. The search is depth-first.
     * 
     * @return java.io.InputStream
     */
    public InputStream getPlainBody() throws PantomimeException {

        /**
         * The plain content is the first inlined text/plain part.
         * It could simply be the body of the message, or the first
         * part of a multipart/alternative, or of a multipart/alternative
         * inside a multipart/mixed.
         *
         * So we'll need to do a depth first search for the first
         * inlined text/plain part.
         */

        Part part = searchForInlinePart("text/plain");

        if ( part != null ) {

            if ( part.isMultipart() ) {
                /* Handle test case 0027 */
                part.specializeAsSinglePart();
            }

            return part.asSinglePart().getBody();
        } else {
            return null;
        }

    }

    /**
     * Gets the HTML content of the first inlined text/html part.
     * It could simply be the body of the message, or the first
     * part of a multipart/alternative, or of a multipart/alternative
     * inside a multipart/mixed. The search is depth-first.
     * 
     * @return java.io.InputStream
     */
    public InputStream getHtmlBody() throws PantomimeException {
        Part part = searchForInlinePart("text/html");

        if ( part != null ) {

            if ( part.isMultipart() ) {
                /* Handle test case 0070  */
                part.specializeAsSinglePart();
            }

            return part.asSinglePart().getBody();
        } else {
            return null;
        }

    }

    /**
     * Sets the plain content of the first inlined text/plain part.
     * It could simply be the body of the message, or the first
     * part of a multipart/alternative, or of a multipart/alternative
     * inside a multipart/mixed. The search is depth-first.
     */
    public void setPlainBody(String plain) throws PantomimeException {

        if ( plain != null ) {

            Part part = searchForInlinePart("text/plain");

            if ( part == null ) {

                filialize();

                Multipart multi = asMultipart();
                multi.setPreamble("");
                multi.setEpilogue("");

                part = multi.addSubPart();

            }

            part.asSinglePart().set(plain, "text/plain", "utf-8");

        }

    }

    /**
     * Sets the HTML content of the first inlined text/html part.
     * It could simply be the body of the message, or the first
     * part of a multipart/alternative, or of a multipart/alternative
     * inside a multipart/mixed. The search is depth-first.
     */
    public void setHtmlBody(String html) throws PantomimeException {

        if ( html != null ) {

            Part part = searchForInlinePart("text/html");

            if ( part == null ) {

                filialize();

                Multipart multi = asMultipart();
                multi.setPreamble("");
                multi.setEpilogue("");

                part = multi.addSubPart();

            }

            part.asSinglePart().set(html, "text/html", "utf-8");

        }

    }

    /**
     * Dumps the mime tree for this message for debugging.
     */
    public String dumpTree() throws PantomimeException {

        StringBuilder builder = new StringBuilder();
        Stack<Part> stack = new Stack<Part>();

        stack.push(this);

        while ( ! stack.empty() ) {

            Part part = stack.pop();

            builder.append(part.toString());

            builder.append("\n");

            if ( part.isMultipart() ) {
                List<Part> subParts = part.asMultipart().getSubParts();

                for ( int index = subParts.size()-1; index >= 0; index-- ) {
                    stack.push(subParts.get(index));
                }
            }

        }

        return builder.toString();
    }

    /**
     * Converts the message into a {@link javax.mail.internet.MimeMessage}
     * suitable for feeding to JavaMail for SMTP delivery.
     *
     * @param session The SMTP session.
     *
     * @return {@link javax.mail.internet.MimeMessage}
     */
    public MimeMessage toJavaMail(Session session) throws PantomimeException {

        JavaMailMessageSource source = new JavaMailMessageSource(session);

        saveAs(source);

        return source.getMime();

    }

    /**
     * Retrieve a specific MIME part addressed by the given mime path.
     *
     * @param path The mime path that points to the MIME part you want.
     *
     * @return {@link org.blackmist.pantomime.Part}
     */
    public Part getPart(MimePath path) throws PantomimeException {
        return getPart(this, path);
    }

    private Part getPart(Part part, MimePath path) throws PantomimeException {

        if ( part.getMimePath().equals(path) ) {
            return part;
        }

        if ( part.isMultipart() ) {

            for ( Part subpart : part.asMultipart().getSubParts() ) {
                Part p = getPart(subpart, path);

                if ( p != null ) {
                    return p;
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the attachment addressed by the given MIME path.
     *
     * @param path The mime path that points to the MIME part you want.
     *
     * @return {@link org.blackmist.pantomime.Attachment}
     */
    public Attachment getAttachment(MimePath path)
        throws PantomimeException {

        Part part = getPart(path);

        if ( part instanceof Attachment ) {
            return (Attachment)part;
        } else {
            return null;
        }
    }

    private void _getAttachments(Part part, List<Attachment> list)
        throws PantomimeException {

        if ( part.isMultipart() ) {

            for ( Part p : part.asMultipart().getSubParts() ) {
                _getAttachments(p, list);
            }
        }

        if ( part instanceof Attachment ) {
            list.add((Attachment)part);
        }
    }

    /**
     * Retrieves all attachments in this message regardless of MIM
     * structure.
     *
     * @return List<Attachment>
     */
    public List<Attachment> getAllAttachments() throws PantomimeException {

        ArrayList<Attachment> list = new ArrayList<Attachment>();

        _getAttachments(this, list);

        return list;
    }

    /**
     * Returns the count of all attachments in this message regardless of MIM
     * structure.
     *
     * @return List<Attachment>
     */
    public int getAllAttachmentCount() throws PantomimeException {
        return getAllAttachments().size();
    }

    /**
     * Set From header.
     *
     * @param newAddress The new from address.
     */
    public void setFrom(String newAddress) {
        Header header = new Header();
        header.setName("From");
        header.setValue(newAddress);

        setHeader(header);
    }

    /**
     * Set Sender header.
     *
     * @param newAddress The new sender address.
     */
    public void setSender(String newAddress) {
        Header header = new Header();
        header.setName("Sender");
        header.setValue(newAddress);

        setHeader(header);
    }

    /**
     * Add a new To recipient.
     *
     * @param newAddress The new to address.
     */
    public void addToRecipient(String newAddress) {
        addToRecipient(new Address(newAddress));
    }

    private void addRecipient(String type, Address newAddress) {
        Header header = getFirstHeader(type);
        StringBuilder builder = new StringBuilder();
        String value;

        if ( header == null ) {
            header = new Header();
            header.setName(type);
            addHeader(header);
        }

        value = header.getValue();

        if ( ( value != null ) && ( value.length() > 0 ) ) {
            builder.append(value);
            builder.append(", ");
        }

        builder.append(newAddress.toString());

        header.setValue(builder.toString());

    }

    /**
     * Add a new To recipient.
     *
     * @param newAddress The new to address.
     */
    public void addToRecipient(Address newAddress) {
        addRecipient("To", newAddress);
    }

    /**
     * Removes a To recipient, if it exists.
     *
     * @param newAddress The To address to remove.
     */
    public void removeToRecipient(String newAddress) {
        removeToRecipient(new Address(newAddress));
    }

    /**
     * Removes a To recipient, if it exists.
     *
     * @param newAddress The To address to remove.
     */
    public void removeToRecipient(Address newAddress) {
        List<Address> addresses = getToRecipients();

        for ( int index = addresses.size()-1; index >= 0; index-- ) {
            Address address = addresses.get(index);

            if ( address.getEmail().equals(newAddress.getEmail()) ) {
                addresses.remove(index);
            }
        }

        setToRecipients(addresses);
    }

    /**
     * Add new To recipients.
     *
     * @param newAddresses The new To address.
     */
    public void addToRecipientStrings(Collection<String> newAddresses) {
        for ( String address : newAddresses) {
            addToRecipient(address);
        }
    }

    /**
     * Add new To recipients.
     *
     * @param newAddresses The new To address.
     */
    public void addToRecipients(Collection<Address> newAddresses) {
        for ( Address address : newAddresses) {
            addToRecipient(address);
        }
    }

    /**
     * Sets the To header to the given addresses.
     *
     * @param newAddresses The new To addresses.
     */
    public void setToRecipientStrings(Collection<String> newAddresses) {
        Header header = getFirstHeader("To");

        if ( newAddresses == null ) {
            return;
        }

        if ( header != null ) {
            header.setValue("");
        }

        for ( String address : newAddresses ) {
            addToRecipient(address);
        }
    }

    /**
     * Sets the To header to the given addresses.
     *
     * @param newAddresses The new To addresses.
     */
    public void setToRecipients(Collection<Address> newAddresses) {
        Header header = getFirstHeader("To");

        if ( newAddresses == null ) {
            return;
        }

        if ( header != null ) {
            header.setValue("");
        }

        addToRecipients(newAddresses);
    }

    /**
     * Add a new CC recipient.
     *
     * @param newAddress The new CC address.
     */
    public void addCcRecipient(String newAddress) {
        addCcRecipient(new Address(newAddress));
    }

    /**
     * Add a new CC recipient.
     *
     * @param newAddress The new CC address.
     */
    public void addCcRecipient(Address newAddress) {
        addRecipient("Cc", newAddress);
    }

    /**
     * Removes a CC recipient, if it exists.
     *
     * @param newAddress The CC address to remove.
     */
    public void removeCcRecipient(String newAddress) {
        removeCcRecipient(new Address(newAddress));
    }

    /**
     * Removes a CC recipient, if it exists.
     *
     * @param newAddress The CC address to remove.
     */
    public void removeCcRecipient(Address newAddress) {
        List<Address> addresses = getCcRecipients();

        for ( int index = addresses.size()-1; index >= 0; index-- ) {
            Address address = addresses.get(index);

            if ( address.getEmail().equals(newAddress.getEmail()) ) {
                addresses.remove(index);
            }
        }

        setCcRecipients(addresses);
    }

    /**
     * Add new CC recipients.
     *
     * @param newAddresses The new CC addresses.
     */
    public void addCcRecipientStrings(Collection<String> newAddresses) {

        if ( newAddresses == null ) {
            return;
        }

        for ( String address : newAddresses) {
            addCcRecipient(address);
        }
    }

    /**
     * Add new CC recipients.
     *
     * @param newAddresses The new CC addresses.
     */
    public void addCcRecipients(Collection<Address> newAddresses) {

        if ( newAddresses == null ) {
            return;
        }

        for ( Address address : newAddresses) {
            addCcRecipient(address);
        }
    }

    /**
     * Sets the CC header to the given addresses.
     *
     * @param newAddresses The new CC addresses.
     */
    public void setCcRecipientStrings(Collection<String> newAddresses) {
        Header header = getFirstHeader("Cc");

        if ( newAddresses == null ) {
            return;
        }

        if ( header != null ) {
            header.setValue("");
        }

        for ( String address : newAddresses ) {
            addCcRecipient(address);
        }
    }

    /**
     * Sets the CC header to the given addresses.
     *
     * @param newAddresses The new CC addresses.
     */
    public void setCcRecipients(Collection<Address> newAddresses) {
        Header header = getFirstHeader("Cc");

        if ( newAddresses == null ) {
            return;
        }

        if ( header != null ) {
            header.setValue("");
        }

        for ( Address address : newAddresses ) {
            addCcRecipient(address);
        }
    }

    /**
     * Add a new BCC recipient.
     *
     * @param newAddress The new BCC address.
     */
    public void addBccRecipient(String newAddress) {
        addBccRecipient(new Address(newAddress));
    }

    /**
     * Add a new BCC recipient.
     *
     * @param newAddress The new BCC address.
     */
    public void addBccRecipient(Address newAddress) {
        addRecipient("Bcc", newAddress);
    }

    /**
     * Removes a BCC recipient, if it exists.
     *
     * @param newAddress The BCC address to remove.
     */
    public void removeBccRecipient(String newAddress) {
        removeBccRecipient(new Address(newAddress));
    }

    /**
     * Removes a BCC recipient, if it exists.
     *
     * @param newAddress The BCC address to remove.
     */
    public void removeBccRecipient(Address newAddress) {
        List<Address> addresses = getBccRecipients();

        for ( int index = addresses.size()-1; index >= 0; index-- ) {
            Address address = addresses.get(index);

            if ( address.getEmail().equals(newAddress.getEmail()) ) {
                addresses.remove(index);
            }
        }

        setBccRecipients(addresses);
    }

    /**
     * Add new BCC recipients.
     *
     * @param newAddresses The new BCC addresses.
     */
    public void addBccRecipientStrings(Collection<String> newAddresses) {

        if ( newAddresses == null ) {
            return;
        }

        for ( String address : newAddresses) {
            addBccRecipient(address);
        }
    }

    /**
     * Add new BCC recipients.
     *
     * @param newAddresses The new BCC addresses.
     */
    public void addBccRecipients(Collection<Address> newAddresses) {

        if ( newAddresses == null ) {
            return;
        }

        for ( Address address : newAddresses) {
            addBccRecipient(address);
        }
    }

    /**
     * Sets the BCC header to the given addresses.
     *
     * @param newAddresses The new BCC addresses.
     */
    public void setBccRecipientStrings(Collection<String> newAddresses) {
        Header header = getFirstHeader("Bcc");

        if ( newAddresses == null ) {
            return;
        }

        if ( header != null ) {
            header.setValue("");
        }

        for ( String address : newAddresses ) {
            addBccRecipient(address);
        }
    }

    /**
     * Sets the BCC header to the given addresses.
     *
     * @param newAddresses The new BCC addresses.
     */
    public void setBccRecipients(Collection<Address> newAddresses) {
        Header header = getFirstHeader("Bcc");

        if ( newAddresses == null ) {
            return;
        }

        if ( header != null ) {
            header.setValue("");
        }

        addBccRecipients(newAddresses);
    }

    /**
     * Sets the Subject header.
     *
     * @param newSubject The new subject.
     */
    public void setSubject(String newSubject) {
        Header subject = new Header();
        subject.setName("Subject");
        subject.setValue(newSubject);

        setHeader(subject);
    }

    private String getDateString(Calendar date) {
        SimpleDateFormat sdf =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        return sdf.format(date.getTime());
    }

    /**
     * Sets the Date header.
     *
     * @param date The new date.
     */
    public void setDate(Calendar date) {
        Header header = new Header();
        header.setName("Date");
        header.setValue(getDateString(date));

        setHeader(header);
    }

    /**
     * Saves this messages to the given storage.
     */
    public SourcedMessage saveAs(MessageSource source)
        throws PantomimeException {

        InputStream stream = null;

        try {
            stream = serialize();

            source.save(stream);
        } finally {
            StreamUtility.close(this, stream);
        }

        return source.load();

    }

    /**
     * Returns all the attachments in the top level of a message.
     */
    public List<Attachment> getAttachments() throws PantomimeException {

        if ( ! isMultipart() ) {
            return null;
        }

        return asMultipart().getAttachments();

    }

    /**
     * Adds a new attachment to this messages.
     */
    public void addAttachment(Attachment attachment) throws PantomimeException {

        filialize();

        asMultipart().addAttachment(attachment);
    }

    /**
     * Adds a new attachment to this messages.
     */
    public Part addAttachment(File file, String mimeType)
        throws PantomimeException {

        filialize();

        return asMultipart().addAttachment(file, mimeType);
    }

    /**
     * Adds a new attachment to this messages.
     */
    public Part addAttachment(InputStreamSource stream, String filename,
        String mimeType) throws PantomimeException {

        filialize();

        return asMultipart().addAttachment(stream, filename, mimeType);
    }

    /**
     * Adds a new attachment to this messages.
     */
    public Part addAttachment(String content, String filename,
        String mimeType) throws PantomimeException {

        filialize();

        return asMultipart().addAttachment(content, filename, mimeType);
    }

    /**
     * Adds a new attachment to this messages.
     */
    public Part addAttachment(File file) throws PantomimeException {

        filialize();

        return asMultipart().addAttachment(file);
    }

    /**
     * Adds a new attachment to this messages.
     */
    public Part addAttachment(InputStreamSource stream, String filename)
        throws PantomimeException {

        filialize();

        return asMultipart().addAttachment(stream, filename);
    }

    /**
     * Adds a new attachment to this messages.
     */
    public Part addAttachment(String content, String filename)
        throws PantomimeException {

        filialize();

        return asMultipart().addAttachment(content, filename);
    }

}

