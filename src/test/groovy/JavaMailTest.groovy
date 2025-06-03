/**
 * Copyright (c) 2013 <JH Barbee>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Initial Developer: JH Barbee
 *
 *
 * For support, please see https://bitbucket.org/barbee/pantomime
 *
 * $Id: JavaMailTest.groovy,v 1.11 2015/05/27 10:47:30 barbee Exp $
**/

import java.util.Properties

import jakarta.activation.FileTypeMap
import jakarta.activation.MimetypesFileTypeMap
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class JavaMailTest {

    @BeforeClass
    static void beforeClass () {
        def types = FileTypeMap.getDefaultFileTypeMap() as MimetypesFileTypeMap
        types.addMimeTypes("image/gif gif")
        types.addMimeTypes("image/jpeg jpg")
        types.addMimeTypes("image/x-png png")
        types.addMimeTypes("application/pdf pdf")
        types.addMimeTypes("text/csv csv")
        FileTypeMap.setDefaultFileTypeMap(types)
    }

    @Before
    void setup() {
        StreamMonitor.reset()
    }

    private MimeMessage makeMail(String subject) {
        return makeMail(subject, null)
    }

    private MimeMessage makeMail(String subject, String attachmentType) {
        def text = new MimeBodyPart()

        text.setText("test message", "utf-8", "plain")

        def html = new MimeBodyPart()

        html.setText("<b>test message</b>", "utf-8", "html")

        def alternative = new MimeMultipart()

        alternative.addBodyPart(text)
        alternative.addBodyPart(html)
        alternative.setSubType('alternative')

        def container = new MimeBodyPart()
        container.setContent(alternative)

        def attachment = new MimeBodyPart();

        if ( attachmentType != null ) {
            attachment.setContent("1,2,3", attachmentType)
        } else {
            attachment.setText("1,2,3", "utf-8", "csv")
        }

        attachment.setDisposition(jakarta.mail.Part.ATTACHMENT)

        def mixed = new MimeMultipart()
        mixed.addBodyPart(container)
        mixed.addBodyPart(attachment)
        mixed.setSubType('mixed')

        def session = Session.getDefaultInstance(new Properties())
        def mime = new MimeMessage(session)

        mime.addHeader("A", "B");
        mime.addHeader("A", "C");
        mime.addHeader("D", "E");
        mime.addFrom(new InternetAddress("dijon@bravo-cat.com"))
        mime.setRecipients(jakarta.mail.Message.RecipientType.TO,
            'marty@bravo-cat.com')
        mime.setSubject(subject)
        mime.setContent(mixed)

        mime.saveChanges()

        return mime

//        mime.writeTo(new java.io.FileOutputStream(new java.io.File('javatestout')))


    }

    /* Test single part text/plain message from mutt. */
    @Test
    void test0000() throws Exception {

        def mime = makeMail('Stop Chasing Me!!!')

        def message = new JavaMailMessageSource(mime).load()

        assert 'dijon@bravo-cat.com' == message.getSender().toString()

        assert 1 == message.getToRecipients().size()
        assert 'marty@bravo-cat.com' == message.getToRecipients()[0].toString()
        assert 1 == message.getAllRecipients().size()
        assert 'marty@bravo-cat.com' == message.getAllRecipients()[0].toString()

        assert 2 == message.getHeaders('A').size()
        assert 'B' == message.getFirstHeader('A').getValue()
        assert 'C' == message.getHeaders('A')[1].getValue()
        assert 1 == message.getHeaders('D').size()

        assert 'Stop Chasing Me!!!'  == message.getSubject()

        assert message.asMultipart().isMultipartMixed();

        assert 2 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]

        assert part1.asMultipart().isMultipartAlternative()

        assert 2 == part1.asMultipart().getSubPartCount()

        def part1_1 = part1.asMultipart().getSubParts()[0]
        def part1_2 = part1.asMultipart().getSubParts()[1]

        assertEquals('test message', Util.streamToString(part1_1.asSinglePart().getBody()))
        assertEquals('<b>test message</b>',
            Util.streamToString(part1_2.asSinglePart().getBody()))

        assert part2 instanceof Attachment

        assertEquals('1,2,3', Util.streamToString(part2.asSinglePart().getBody()))

        assert 1 == message.getAttachments().size()
        def att = message.getAttachments()[0]
        assertEquals('1,2,3', Util.streamToString(att.asSinglePart().getBody()))

        assertEquals('test message', message.getPlainBodyAsString())
        assertEquals('<b>test message</b>', message.getHtmlBodyAsString())

        assert 900 <= message.getTransferEncodedSize()
        assert 950 >= message.getTransferEncodedSize()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0001() {

        def mime = makeMail('Personal Space')

        def message = new JavaMailMessageSource(mime).load()

        message.setSubject()

        message.save()

        def baos = new ByteArrayOutputStream()

        mime.writeTo(baos)

        assert baos.toString().indexOf("Subject: Personal Space") > -1

        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0002() {

        def message = new Message()

        message.asSinglePart().set('You stink', 'text/plain', 'utf-8')

        message.setSubject('You stink.')
        message.setFrom('dijon@bravo-cat.net')
        message.addToRecipient('marty@bravo-cat.net')

        def session = Session.getDefaultInstance(new Properties())
        def source = new JavaMailMessageSource(session)

        def sourcedMessage = message.saveAs(source)

        assert sourcedMessage instanceof SourcedMessage

        def baos = new ByteArrayOutputStream()

        def mime = source.getMime()
        mime.writeTo(baos)

        assert 'You stink.' == sourcedMessage.getSubject()
        assert 'You stink.' == mime.getSubject()
        assert 'text/plain; charset="utf-8"' == mime.getHeader('Content-Type')[0]
        assert '7bit' == mime.getHeader('Content-Transfer-Encoding')[0]
        assert 'You stink' == mime.getContent()

        assert 244 <= baos.toString().length()
        assert 246 >= baos.toString().length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0003() {
        def session = Session.getDefaultInstance(new Properties())
        def mime = new MimeMessage(session, new FileInputStream(new File('data/attachments.eml')))

        checkMessage(mime)
    }

    private checkMessage(MimeMessage mime) {
        def message = new JavaMailMessageSource(mime).load()

        assert 6 ==  message.getAllAttachmentCount()

        def attachments = message.getAllAttachments()

        assert '1px.gif' == attachments.get(0).getFilename()
        assert 'image/gif' == attachments.get(0).getFileType()

        /* Newline accounts for the difference. */
        assert 183 <= attachments.get(0).getTransferEncodedSize()
        assert 195 >= attachments.get(0).getTransferEncodedSize()
        assert 49 == Util.getSize(attachments.get(0).asSinglePart().getBody())

        assert '1px.jpg' == attachments.get(1).getFilename()
        assert 'image/jpeg' == attachments.get(1).getFileType()
        assert 15203 <= attachments.get(1).getTransferEncodedSize()
        assert 15398 >= attachments.get(1).getTransferEncodedSize()
        assert 11159 == Util.getSize(attachments.get(1).asSinglePart().getBody())

        assert '1px.png' == attachments.get(2).getFilename()
        assert 'image/x-png' == attachments.get(2).getFileType()
        assert 3893 <= attachments.get(2).getTransferEncodedSize()
        assert 3951 >= attachments.get(2).getTransferEncodedSize()
        assert 2792 == Util.getSize(attachments.get(2).asSinglePart().getBody())

        /* JavaMail choose quoted printable for PD.
         * Pantomime chooses base 64.
         */
        assert 'blank.pdf' == attachments.get(3).getFilename()
        assert 'application/pdf' == attachments.get(3).getFileType()
        assert 1569 <= attachments.get(3).getTransferEncodedSize()
        assert 1889 >= attachments.get(3).getTransferEncodedSize()

        /* This is really bizarre. JavaMail will sometimes switch to \r\n
         * even though the file is only \n. This accounts for eh extra 77 bytes.
         */
        assert ( 1357 == Util.getSize(attachments.get(3).asSinglePart().getBody()) ) || ( 1280 == Util.getSize(attachments.get(3).asSinglePart().getBody()) )

        assert 'blank.xlsx' == attachments.get(4).getFilename()
        assert 'application/octet-stream' == attachments.get(4).getFileType()
        assert 35871 <= attachments.get(4).getTransferEncodedSize()
        assert 36322 >= attachments.get(4).getTransferEncodedSize()
        assert 26435 == Util.getSize(attachments.get(4).asSinglePart().getBody())

        assert 'blank.csv' == attachments.get(5).getFilename()
        assert 'text/csv' == attachments.get(5).getFileType()
        assert 101 <= attachments.get(5).getTransferEncodedSize()
        assert 148 >= attachments.get(5).getTransferEncodedSize()
        assert 2 == Util.getSize(attachments.get(5).asSinglePart().getBody())

    }

    @Test
    void test0004() {
        def text = new MimeBodyPart()

        text.setText("test message", "utf-8", "plain")

        def html = new MimeBodyPart()

        html.setText("<b>test message</b>", "utf-8", "html")

        def alternative = new MimeMultipart()

        alternative.addBodyPart(text)
        alternative.addBodyPart(html)
        alternative.setSubType('alternative')

        def container = new MimeBodyPart()
        container.setContent(alternative)

        def attachment1 = new MimeBodyPart();
        attachment1.attachFile(new File('data/1px.gif'))
        def attachment2 = new MimeBodyPart();
        attachment2.attachFile(new File('data/1px.jpg'))
        def attachment3 = new MimeBodyPart();
        attachment3.attachFile(new File('data/1px.png'))
        def attachment4 = new MimeBodyPart();
        attachment4.attachFile(new File('data/blank.pdf'))
        def attachment5 = new MimeBodyPart();
        attachment5.attachFile(new File('data/blank.xlsx'))
        def attachment6 = new MimeBodyPart();
        attachment6.attachFile(new File('data/blank.csv'))

        def mixed = new MimeMultipart()
        mixed.addBodyPart(container)
        mixed.addBodyPart(attachment1)
        mixed.addBodyPart(attachment2)
        mixed.addBodyPart(attachment3)
        mixed.addBodyPart(attachment4)
        mixed.addBodyPart(attachment5)
        mixed.addBodyPart(attachment6)
        mixed.setSubType('mixed')

        def session = Session.getDefaultInstance(new Properties())
        def mime = new MimeMessage(session)

        mime.addFrom(new InternetAddress("dijon@bravo-cat.com"))
        mime.setRecipients(jakarta.mail.Message.RecipientType.TO,
            'marty@bravo-cat.com')
        mime.setSubject('abc')
        mime.setContent(mixed)

        mime.saveChanges()

        checkMessage(mime)

    }



    @Test
    void test0005() {

        def session = Session.getDefaultInstance(new Properties())

        def mime = makeMail('Stop Chasing Me!!!', 'application/octet-stream')

        def message = new JavaMailMessageSource(mime).load()

        Util.streamToString(message.serialize())

        def attachments = message.getAllAttachments()

        assert '1,2,3' == Util.streamToString(attachments[0].asSinglePart().getBody())


    }

//    /* Test single part text/plain message from mutt. */
//    @Test
//    void test0006() throws Exception {
//
//        def session = Session.getDefaultInstance(new Properties())
//
//        //def mime = makeMail('Stop Chasing Me!!!')
//        def mime = new MimeMessage(session, new FileInputStream(new File('data/attachments.eml')))
//
//        def message = new JavaMailMessageSource(mime).load()
//
//        assert Util.getSize(message.serialize()) == message.getTransferEncodedSize()
//
//    }
//
//    /* Test single part text/plain message from mutt. */
//    @Test
//    void test0007() throws Exception {
//
//        def session = Session.getDefaultInstance(new Properties())
//
//        //def mime = makeMail('Stop Chasing Me!!!')
//        def mime = new MimeMessage(session, new FileInputStream(new File('data/chinese.eml')))
//
//        def message = new JavaMailMessageSource(mime).load()
//
//        println Util.streamToString(message.serialize())
//
//        println Util.streamToString(message.getPlainBody())
//
//        println message.getPlainBodyAsString()
//
//        assert false
//
//    }
//

    @Test
    void test0006() {

        def session = Session.getDefaultInstance(new Properties())

        def mime = new MimeMessage(session, new FileInputStream(new File('data/abars.eml')))

        def message = new JavaMailMessageSource(mime).load()

        println Util.streamToString(message.serialize())

        assert Util.streamToString(message.serialize()).endsWith("--_004_37A42E28A0884D449AAB89C9DE358EDB3BE890F9bcmsg106abarsco_--")

    }

}
