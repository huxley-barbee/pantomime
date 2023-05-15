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
 * $Id: PartModificationTest.groovy,v 1.14 2015/06/09 15:36:00 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*
import org.blackmist.pantomime.content.*

class PartModificationTest {

    @Before
    void setup() {
        StreamMonitor.reset()
    }


    /* single part */
    @Test
    void test0000() throws Exception {

        def message = new Message()

        message.asSinglePart().set("test", "text/plain", "utf-8")


        assert 166 <= message.getTransferEncodedSize()
        assert 168 >= message.getTransferEncodedSize()
        assert 4 == message.asSinglePart().getTransferEncodedBodySize()
        assert 'test' == Util.streamToString(message.asSinglePart().getBody())
        assert 'test' == Util.streamToString(message.asSinglePart().getTransferEncodedBody())

        assert 0 == StreamMonitor.unclosedStreams()

        assert message.hasHeader('Date')

        assert '1.0' == message.getFirstHeader('MIME-Version').getValue()

        assert Util.streamToString(message.serialize()) =~ /MIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d+\r\nDate: \w{2,3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest/
    }

    /* alternative */
    @Test
    void test0001() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def html = new SimpleContent()
        def alternative = new AlternativeContent()

        plain.set('test')
        html.set('<b>test</b>')

        alternative.setPlain(plain)
        alternative.setHtml(html)

        message.specializeAsMultipart();
        message.asMultipart().set(alternative)

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert '' == message.asMultipart().getPreamble()
        assert '' == message.asMultipart().getEpilogue()
        assert 450 <= message.getTransferEncodedSize()
        assert 460 >= message.getTransferEncodedSize()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* alternative with attachment */
    @Test
    void test0002() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def html = new SimpleContent()
        def alternative = new AlternativeContent()

        plain.set('test')
        html.set('<b>test</b>')

        alternative.setPlain(plain)
        alternative.setHtml(html)

        message.specializeAsMultipart();
        message.asMultipart().set(alternative)

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        message.addAttachment("test", "test.txt")

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/mixed; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Disposition: attachment; filename=test.txt; size="4"\r\nContent-Type: text\/plain\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }


    /* alternative with 2 attachments */
    @Test
    void test0003() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def html = new SimpleContent()
        def alternative = new AlternativeContent()

        plain.set('test')
        html.set('<b>test</b>')

        alternative.setPlain(plain)
        alternative.setHtml(html)

        message.specializeAsMultipart();
        message.asMultipart().set(alternative)

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        message.addAttachment("test", "test.txt");
        message.addAttachment("test", "test2.txt");

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/mixed; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Disposition: attachment; filename=test.txt; size="4"\r\nContent-Type: text\/plain\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Disposition: attachment; filename=test2.txt; size="4"\r\nContent-Type: text\/plain\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* related */
    @Test
    void test0004() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def html = new SimpleContent()
        def image = new InlineImage()
        def related = new RelatedContent()

        image.setFile(new File("data/1px.gif"))
        image.setType("image/gif")
        image.setContentId("gif123")

        plain.set('test')
        html.set('<b>test</b>')

        related.setPlain(plain)
        related.setHtml(html)
        related.setImages(image);

        message.specializeAsMultipart();
        message.asMultipart().set(related)

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/related; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Disposition: inline\r\nContent-ID: <gif123>\r\nContent-Type: image\/gif\r\nContent-Transfer-Encoding: base64\r\n\r\nR0lGODlhAQABAJEAAAAAAP\/\/\/\/\/\/\/wAAACH5BAEAAAIALAAAAAABAAEAAAICVAEAOw==\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* signed */
    @Test
    void test0005() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def signed = new SignedContent()

        plain.set('test')

        signed.setContent(plain)
        signed.setSignatureType("application/pgp-signature")
        signed.setSignature("test");

        message.specializeAsMultipart();
        message.asMultipart().set(signed)
//        new File("0005.out").write(Util.streamToString(message.serialize()))

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/signed; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: application\/pgp-signature\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* encrypted */
    @Test
    void test0006() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def encrypted = new EncryptedContent()

        plain.set('test')

        encrypted.setEncrypted(plain)
        encrypted.setEncryptionType("application/pgp-encrypted");
        encrypted.setControl("test");

        message.specializeAsMultipart();
        message.asMultipart().set(encrypted)

//        new File("0006.out").write(Util.streamToString(message.serialize()))
        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/signed; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: application\/pgp-encrypted\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: application\/octet-stream; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* digest */
    @Test
    void test0007() throws Exception {

        def message1 = new Message()
        def message2 = new Message()
        def message3 = new Message()

        def digest = new DigestContent()

        message2.asSinglePart().set("test2", "text/plain", "utf-8")
        message3.asSinglePart().set("test3", "text/plain", "utf-8")

        digest.setMessages(message2, message3)

        message1.specializeAsMultipart();
        message1.asMultipart().set(digest)

//        new File("0007.out").write(Util.streamToString(message1.serialize()))

        assert Util.streamToString(message1.serialize()) =~ /MIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: multipart\/digest; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: message\/rfc822\r\nContent-Transfer-Encoding: 7bit\r\n\r\nMIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest2\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: message\/rfc822\r\nContent-Transfer-Encoding: 7bit\r\n\r\nMIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest3\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        def rfc822 = message1.asMultipart().getSubParts()[0].asSinglePart().asRfc822Message()

        assert rfc822 instanceof Message

        rfc822.asSinglePart().set("test4", "text/plain", "utf-8")

        rfc822.save()

        def blah = message1.serialize()

        assert Util.streamToString(blah) =~ /Content-Type: multipart\/digest; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: message\/rfc822\r\nContent-Transfer-Encoding: 7bit\r\n\r\nMIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest4\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: message\/rfc822\r\nContent-Transfer-Encoding: 7bit\r\n\r\nMIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest3\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* report */
    @Test
    void test0008() throws Exception {

        def message = new Message()

        def report = new ReportContent()

        def original = new Message()

        original.asSinglePart().set("test", "text/plain", "utf-8")

        report.setNoticeCharset("utf-8")
        report.setNotice("It bounced");
        report.setStatus("bounce: true")
        report.setOriginal(original)

        message.specializeAsMultipart();
        message.asMultipart().set(report)

//        new File("0008.out").write(Util.streamToString(message.serialize()))

        assert Util.streamToString(message.serialize()) =~ /MIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: multipart\/report; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}"\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\nIt bounced\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: message\/delivery-status\r\nContent-Transfer-Encoding: 7bit\r\n\r\nbounce: true\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}\r\nContent-Type: message\/rfc822\r\nContent-Transfer-Encoding: 7bit\r\nContent-Disposition: attachment\r\n\r\nMIME-Version: 1.0\r\nX-Mailer: Pantomime\/\d\.\d{2,4}\r\nDate: \w{3}, \d{1,2} \w{3} \d{4} \d{2}:\d{2}:\d{2} -\d{4}\r\nContent-Type: text\/plain; charset="utf-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d{1,2}--/

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* preamble epilogue */
    @Test
    void test0009() throws Exception {

        def message = new Message()

        def plain = new SimpleContent()
        def html = new SimpleContent()
        def alternative = new AlternativeContent()

        plain.set('test')
        html.set('<b>test</b>')

        alternative.setPlain(plain)
        alternative.setHtml(html)

        message.specializeAsMultipart();
        message.asMultipart().set(alternative)
        message.asMultipart().setPreamble("my preamble")
        message.asMultipart().setEpilogue("my epilogue")

        assert Util.streamToString(message.serialize()) =~ /Content-Type: multipart\/alternative; boundary="Pantomime-\d.\d{2,4}-\d{13}-\d+"\r\n\r\nmy preamble\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d+\r\nContent-Type: text\/plain; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\ntest\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d+\r\nContent-Type: text\/html; charset="UTF-8"\r\nContent-Transfer-Encoding: 7bit\r\n\r\n<b>test<\/b>\r\n\r\n--Pantomime-\d.\d{2,4}-\d{13}-\d+--\r\n\r\nmy epilogue/

        assert 'my preamble' == message.asMultipart().getPreamble()
        assert 'my epilogue' == message.asMultipart().getEpilogue()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* single part with attachment */
    @Test
    void test0010() throws Exception {

        def message = new Message()

        message.asSinglePart().set("test", "text/plain", "utf-8")

        message.addAttachment('attachment content', 'test.txt', 'text/plain')

        assert 530 <= message.getTransferEncodedSize()
        assert 541 >= message.getTransferEncodedSize()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 'test' == message.asMultipart().getSubParts()[0].asSinglePart().getBodyAsString()
        assert 'attachment content' == message.asMultipart().getSubParts()[1].asSinglePart().getBodyAsString()

        assert 0 == StreamMonitor.unclosedStreams()

        assert '1.0' == message.getFirstHeader('MIME-Version').getValue()

    }
}
