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
 * $Id: FileTest.groovy,v 1.20 2015/05/27 10:47:30 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class FileTest {

    @Before
    void setup() {
        StreamMonitor.reset()
    }

    /* Test single part text/plain message from mutt. */
    @Test
    void test0000() throws Exception {

        TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"))

        def message = new FileMessageSource("data/0000.eml").load()

        assert 831 == message.getTransferEncodedSize()

        assert '"barbee" <camadedijon@pobox.com>' ==
            message.getSender().toString()

        assert message.hasHeader('lines')
        assert message.hasHeaderWithValue('lines', 1)

        assert 5 == message.getFirstHeader('content-length').getValueAsInt()

        assert 'RO'  == message.getFirstHeader('STATUS').getValue()
        assert '1.0' == message.getFirstHeader('MIME-Version').getValue()
        assert 'CMU Sieve 2.3' == message.getFirstHeader('x-sieve').getValue()

        assert 1 == message.getToRecipients().size()
        assert '"barbee" <barbee@darkfog.org>' ==
            message.getToRecipients()[0].toString()
        assert 1 == message.getAllRecipients().size()
        assert '"barbee" <barbee@darkfog.org>' ==
            message.getAllRecipients()[0].toString()

        assert 'Test 1007.' == message.getSubject()

        assert message.getContentDisposition().isInline();

        assert 'text/plain' == message.getContentType().getType();
        assert 'us-ascii' == message.getContentType().getCharset();

        assert null == message.getContentTransferEncoding();

        assert 'Eastern Standard Time' ==
            message.getDate().getTimeZone().getDisplayName();

        assert 1374588427000 == message.getDate().getTimeInMillis();

        assert 2 == message.getHeaders('received').size()

        assertEquals("from smtp.darkfog.org ([10.20.10.20])\t by imap.darkfog.org (Cyrus v2.3.14)\r\n with LMTPA;\t Tue, 23 Jul 2013 14:27:24 +0000", message.getFirstHeader('received').getTransferEncodedValue())
        assertEquals("from smtp.darkfog.org ([10.20.10.20])\t by imap.darkfog.org (Cyrus v2.3.14) with LMTPA;\t Tue, 23 Jul 2013 14:27:24 +0000", message.getFirstHeader('received').getValue())

        assert 6 == message.getPlainBodyAsString().length()
        assert 'test\n\n' == message.getPlainBodyAsString()
        assert null == message.getHtmlBodyAsString()

        assert ! message.isMultipart();

        assert null == message.asMultipart()

        assert "0" == message.getMimePath().toString()

        assert 850 == Util.getSize(message.serialize())
        assertEquals("Return-Path: <camadedijon@pobox.com>\r\nRe",
            Util.streamToString(message.serialize()).substring(0, 40))
        assertEquals("O\r\nContent-Length: 5\r\nLines: 1\r\n\r\ntest\n\n",
            Util.streamToString(message.serialize()).substring(810))

        assert 0 == StreamMonitor.unclosedStreams()

    }

    /* Tests multipart/alternative messages. */
    @Test
    void test0001() throws Exception {

        def message = new FileMessageSource("data/0001.eml").load()

        assert 3787 == message.getTransferEncodedSize()

        assert 'multipart/alternative' == message.getContentType().getType()
        assert '_000_49D9ABAA8ACCE9438F0C33BF1529340D20AA5Axmbalnx14oliveoilcom_' == message.getContentType().getBoundary()
        assert message.isMultipart();
        assert message.asMultipart().isMultipartAlternative();

        assert 2 == message.asMultipart().getSubPartCount()

        assert 4 == message.getPlainBodyAsString().length()
        assert 'test' == message.getPlainBodyAsString()
        assert 300 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().startsWith('<html>')
        assert message.getHtmlBodyAsString().endsWith('test</div>\n</body>\n</html>')

        assert "0.0" == message.asMultipart().getSubParts()[0].getMimePath().toString()
        assert "0.1" == message.asMultipart().getSubParts()[1].getMimePath().toString()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Tests single part text/plain message from Outlook. */
    @Test
    void test0002() throws Exception {

        def message = new FileMessageSource("data/0002.eml").load()

        assert ! message.isMultipart();

        assert null == message.asMultipart()

        assert 7 == message.getPlainBodyAsString().length()
        assert 'test\n\n\n' == message.getPlainBodyAsString()
        assert null == message.getHtmlBodyAsString()

        assert "0" == message.getMimePath().toString()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Tests message with base64-encoded attachment. */
    @Test
    void test0003() throws Exception {

        def message = new FileMessageSource("data/0003.eml").load()

        assert message.isMultipart();

        assert message.asMultipart().isMultipartMixed();

        assert 2 == message.asMultipart().getSubPartCount()

        assert 4 == message.getPlainBodyAsString().length()
        assert 'test' == message.getPlainBodyAsString()
        assert 300 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().startsWith('<html>')
        assert message.getHtmlBodyAsString().endsWith('test</div>\n</body>\n</html>')

        assert "0.1" == message.getAllAttachments()[0].getMimePath().toString()
        assert 1 == message.getAllAttachmentCount()

        assert "text/plain" == message.getPart(new MimePath("0.0.0")).getContentType().getType()

        assert message.asMultipart().isMultipartMixed();

        Part alternative = message.asMultipart().getSubParts()[0]

        assert "0.0" == alternative.getMimePath().toString()

        assert alternative.asMultipart().isMultipartAlternative()

        assert 2 == alternative.asMultipart().getSubPartCount()

        assert "text/plain" == alternative.asMultipart().getSubParts()[0].getContentType().getType()
        assert "text/html" == alternative.asMultipart().getSubParts()[1].getContentType().getType()

        assert "0.0.0" == alternative.asMultipart().getSubParts()[0].getMimePath().toString()
        assert "0.0.1" == alternative.asMultipart().getSubParts()[1].getMimePath().toString()

        assert 1 == message.getAttachments().size()

        Attachment attachment = message.getAttachments()[0]

        assert "0.1" == attachment.getMimePath().toString()
        assert 'test.txt' == attachment.getFilename()
        assert 'Fri, 26 Jul 2013 12:05:26 GMT' == attachment.getCreationDateString()
        assert 'Fri, 26 Jul 2013 12:05:26 GMT' == attachment.getModificationDateString()

        assert 5 == attachment.getStatedSize()
        assert 7 == attachment.asSinglePart().getTransferEncodedBodySize()
        assert 'test\n' == Util.streamToString(attachment.asSinglePart().getBody());

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Tests message with quoted-printable part */
    @Test
    void test0004() throws Exception {

        def message = new FileMessageSource("data/0004.eml").load()

        assert message.isMultipart();

        assert message.asMultipart().isMultipartAlternative();

        assert 2 == message.asMultipart().getSubPartCount()

        assert 101 == message.getPlainBodyAsString().length()

        assert 'If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.' == message.getPlainBodyAsString()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Test message with only html part */
    @Test
    void test0005() {
        def message = new FileMessageSource("data/0005.eml").load()

        assert ! message.isMultipart()

        assert null == message.asMultipart()

        assert null == message.getPlainBodyAsString()

        assert 7221 == message.getHtmlBodyAsString().length()

        assert message.getHtmlBodyAsString().startsWith('<!DOCTYPE HTML>')

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Test DFR in Outlook from Exchange */
    @Test
    void test0006() throws Exception {

        def message = new FileMessageSource("data/0006.eml").load()

        assert 'Delivery Status Notification (Failure)' == message.getSubject()

        assert message.isMultipart();

        assert 3 == message.asMultipart().getSubPartCount()

        assertEquals('> This message is in MIME format. Since your mail reader does not understand\r\nthis format, some or all of this message may not be legible.\r\n\r\n', message.asMultipart().getPreamble())

        assertEquals('\r\nThis is an epilogue.\r\n\r\n',
            message.asMultipart().getEpilogue())

        assertEquals "The following message to <nobody@nowhere.com> was undeliverable.\r\nThe reason for the problem:\r\n5.1.2 - Bad destination host 'DNS Hard Error looking up nowhere.com (A):\r\ndomain has no A record'\r\n\r\n", message.getPlainBodyAsString()

        assert 196 == message.getPlainBodyAsString().length()

        def notice = message.asMultipart().getSubParts()[0]

        assert "0.0" == notice.getMimePath().toString()
        assert 196 == Util.streamToString(notice.asSinglePart().getBody()).length()

        def status = message.asMultipart().getSubParts()[1]

        assert "0.1" == status.getMimePath().toString()

        assert 'message/delivery-status' == status.getContentType().getType()

        def disposition = status.getContentDisposition()

        assert disposition.isAttachment()
        assert 'ATT00001' == disposition.getFilename()
        assert status instanceof Attachment

        assert 289 == Util.streamToString(status.asSinglePart().getBody()).length()
        assertEquals("Reporting-MTA: dns; rcdn-iport-7.oliveoil.com\r\n\r\nFinal-Recipient: rfc822;nobody@nowhere.com\r\nAction: failed\r\nStatus: 5.0.0 (permanent failure)\r\nDiagnostic-Code: smtp; 5.1.2 - Bad destination host 'DNS Hard Error looking up nowhere.com (A):  domain has no A record' (delivery attempts: 0)\r\n", status.asSinglePart().getBodyAsString())

        def original = message.asMultipart().getSubParts()[2]

        assert "0.2" == original.getMimePath().toString()
        assert 'message/rfc822' == original.getContentType().getType()

        def disposition2 = original.getContentDisposition()

        assert disposition2.isAttachment()
        assert original instanceof Attachment

        assert ! original.isMultipart()
        assert original.asSinglePart().isRfc822Message()

        def rfc822 = original.asSinglePart().asRfc822Message()

        assert 22 == rfc822.getHeaderList().size()
        assert 'test 0950.' == rfc822.getSubject()

        assert rfc822.asMultipart().isMultipartAlternative()

        assert 2 == rfc822.asMultipart().getSubPartCount()

        assertEquals('test', rfc822.asMultipart().getSubParts()[0].asSinglePart().getBodyAsString())
        assert rfc822.asMultipart().getSubParts()[1].asSinglePart().getBodyAsString().startsWith('<html><head>')

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Test DFR in mutt from Postfix */
    @Test
    void test0007() throws Exception {

        def message = new FileMessageSource("data/0007.eml").load()

        assert 'Returned mail: see transcript for details' ==
            message.getSubject()

        assert message.isMultipart();

        assert 3 == message.asMultipart().getSubPartCount()

        assert 345 == message.getPlainBodyAsString().length()

        assertEquals("The original message was received at Mon, 29 Jul 2013 13:01:14 -0400\nfrom cpe-98-14-128-182.nyc.res.rr.com [98.14.128.182]\n\n   ----- The following addresses had permanent fatal errors -----\n<nobody@nowhere.com>\n\n   ----- Transcript of session follows -----\n550 5.1.2 <nobody@nowhere.com>... Host unknown (Name server: nowhere.com: no data known)", message.getPlainBodyAsString())

        def notice = message.asMultipart().getSubParts()[0]

        assert "0.0" == notice.getMimePath().toString()
        assert 345 == Util.streamToString(notice.asSinglePart().getBody()).length()

        def status = message.asMultipart().getSubParts()[1]

        assert "0.1" == status.getMimePath().toString()

        assert 'message/delivery-status' == status.getContentType().getType()

        assert null == status.getContentDisposition()

        assert 336 == Util.streamToString(status.asSinglePart().getBody()).length()

        assertEquals("Reporting-MTA: dns; eidos.os5.com\nReceived-From-MTA: DNS; cpe-98-14-128-182.nyc.res.rr.com\nArrival-Date: Mon, 29 Jul 2013 13:01:14 -0400\n\nOriginal-Recipient: rfc822;nobody@nowhere.com\nFinal-Recipient: RFC822; nobody@nowhere.com\nAction: failed\nStatus: 5.1.2\nRemote-MTA: DNS; nowhere.com\nLast-Attempt-Date: Mon, 29 Jul 2013 13:01:14 -0400", Util.streamToString(status.asSinglePart().getBody()));

        def original = message.asMultipart().getSubParts()[2]

        assert "0.2" == original.getMimePath().toString()
        assert 'message/rfc822' == original.getContentType().getType()

        assert null == original.getContentDisposition()

        assert 0 == StreamMonitor.unclosedStreams()

    }

    private String gb2312ToUtf8 (String input) {
        return convert(input, "GB2312")
    }

    private String iso2022jpToUtf8 (String input) {
        return convert(input, "ISO-2022-JP")
    }

    private String convert(String input, String charset) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes())
        InputStreamReader reader = new InputStreamReader(bais, charset)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8")
        char[] data = new char[8192]
        int charsRead;

        while ( ( charsRead = reader.read(data, 0, data.length) ) > 0 ) {
            writer.write(data, 0, charsRead)
        }

        writer.flush()
        writer.close()
        reader.close()

        return baos.toString("UTF-8")
    }

    public void writeToFile(def filename, def string, def charset ) {
        File file = new File(filename).setText(string, charset);
    }

    /* Test Foreign Langauge Body and Header Encoded in Base 64 */
    @Test
    void test0008() throws Exception {

        def message = new FileMessageSource("data/0008.eml").load()

        assert '系统退信' == message.getSubject()

        assert message.isMultipart();

        assert 3 == message.asMultipart().getSubPartCount()

        assert 404 == message.getPlainBodyAsString().length()

        assertEquals("抱歉，您的邮件被退回来了……\r\n      原邮件信息：\r\n            时  间 2013-07-25 14:33:40  \r\n            主  题 采c购管u理目e标  \r\n            收件人 krzhaoshi@163.com  \r\n     \r\n      退信原因：\r\n            垃圾邮件让邮箱小易很烦心，现在您发送的邮件被怀疑为是垃圾邮件，拒绝接收。\r\n            英文说明:rejected by system \r\n\r\n      建议解决方案：\r\n            邮差小易温馨提示：建议您绿色地使用邮箱，请适当修改标题和内容，再尝试发送。\r\n\r\n----------------\r\nThis message is generated by Coremail.\r\n您收到的是来自 Coremail 专业邮件系统的信件.\r\n\r\n", message.getPlainBodyAsString())

       // gb2312ToUtf8(message.getPlainBodyAsString()));

        assert 4742 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().startsWith("<!-- saved from url")

        def notice = message.asMultipart().getSubParts()[0]

        assert "0.0" == notice.getMimePath().toString()
        assert notice.isMultipart()
        assert notice.asMultipart().isMultipartAlternative()

        assert 2 == notice.asMultipart().getSubParts().size()

        assert "0.0.0" == notice.asMultipart().getSubParts()[0].getMimePath().toString()
        assert 'text/plain' == notice.asMultipart().getSubParts()[0].getContentType().getType()
        assert 'gb2312' == notice.asMultipart().getSubParts()[0].getContentType().getCharset()

        assert 542 == Util.getSize(notice.asMultipart().getSubParts()[0].asSinglePart().getBody())

        assert "0.0.1" == notice.asMultipart().getSubParts()[1].getMimePath().toString()
        assert 'text/html' == notice.asMultipart().getSubParts()[1].getContentType().getType()
        assert 'gb2312' == notice.asMultipart().getSubParts()[1].getContentType().getCharset()
        assert 4742 == notice.asMultipart().getSubParts()[1].asSinglePart().getBodyAsString().length()

        def status = message.asMultipart().getSubParts()[1]
        assert "0.1" == status.getMimePath().toString()
        assert 'message/delivery-status' == status.getContentType().getType()
        assertEquals("Final-Recipient: rfc822; krzhaoshi@163.com\nAction: failed\nStatus: 5.0.0\nDiagnostic-Code: SMTP; rejected by system.(rejected&nbsp;by&nbsp;system)\n", Util.streamToString(status.asSinglePart().getBody()))


        def original = message.asMultipart().getSubParts()[2]
        assert "0.2" == original.getMimePath().toString()
        assert 'message/rfc822' == original.getContentType().getType()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Test Foreign Language Attachment name */
    @Test
    void test0009() throws Exception {

        def message = new FileMessageSource("data/0009.eml").load()

        assert '餃子' == message.getSubject()

        def attachments = message.getAttachments()

        assert 1 == attachments.size()

        assert '餃子.txt' == attachments.get(0).getFilename();

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Test Overlong Subject in a foreign language */
    @Test
    void test0010() throws Exception {

        def message = new FileMessageSource("data/0010.eml").load()

        assertEquals("很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長很長", message.getSubject())

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This has a multipart/mixed that has only one sub part */
    /* Also, BOUNDARY is upper case */
    @Test
    void test0011() {

        def message = new FileMessageSource("data/untroubled/1998/03/890956849.27937.txt").load()

        assert message.asMultipart().isMultipartMixed()

        assert 1 == message.asMultipart().getSubPartCount()

        def sub = message.asMultipart().getSubParts()[0]

        assert 'text/plain' == sub.getContentType().getType()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Subject has a trailing space */
    @Test
    void test0012() {
        def message = new FileMessageSource("data/untroubled/1998/03/891285049.11748.txt").load()

        assert 'Worth Reading - Ask Netscape ' == message.getSubject()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Javamail cannot handle this. Invalid recipient in list. */
    /* Multiple recipients */
    @Test
    void test0013() {
        def message = new FileMessageSource("data/untroubled/1998/03/890929492.24871.txt").load()

        def recipients = message.getAllRecipients()

        assert 357 == recipients.size()
        assert 'bajis@asu.edu balaji srinivasa' == recipients[309].toString()

        assert recipients[0].isValid()
        assert ! recipients[309].isValid()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Undisclosed recipients */
    /* Email address comment */
    @Test
    void test0014() {
        def message = new FileMessageSource('data/untroubled/1998/04/891987072.7172.txt').load()

        def recipients = message.getAllRecipients()

        assert 1 == recipients.size()

        assert recipients[0].isValid()

        assert 'unlisted-recipients:;' == recipients[0].getLocal()
        assert 'no To-header on input' == recipients[0].getComment()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* To address is just a comment and nothing else */
    @Test
    void test0015() {
        def message = new FileMessageSource('data/untroubled/1998/04/892278005.30906.txt').load()

        def recipients = message.getAllRecipients()

        assert 1 == recipients.size()

        assert null == recipients[0].getLocal()
        assert null == recipients[0].getDomain()
        assert 'Dear Friend' == recipients[0].getComment()
        assert ! recipients[0].isValid()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* From and To is just a bunch of spaces */
    @Test
    void test0016() {
        def message = new FileMessageSource('data/untroubled/1998/04/892278009.30906.txt').load()


        assert '' == message.getSender().toString()
        assert 1 == message.getAllRecipients().size()
        assert ! message.getAllRecipients()[0].isValid()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Bogus charset "unknown-8bit". Javamail cannot handle this */
    @Test
    void test0017() {
        def message = new FileMessageSource('data/untroubled/1998/05/895785806.25204.txt').load()

        assert 'unknown-8bit' == message.getContentType().getCharset()

        assert 4921 == message.getPlainBodyAsString().length()
        assert message.getPlainBodyAsString().startsWith('Dear Future Associate')

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Weird subject. It has a second line of all spaces. Javamail inteprets
     * this as a single trailing space, which isn't right.
     */
    @Test
    void test0018() {
        def message = new FileMessageSource('data/untroubled/1998/05/895849497.735.txt').load()

        assertEquals("whats up?                                                           ", message.getSubject())
        assert 68 == message.getSubject().length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* has a base 64 encoded attachment with a lot of bytes > 127 */
    @Test
    void test0019() {
        def message = new FileMessageSource('data/untroubled/1998/09/905279315.22686.txt').load()

        assert message.asMultipart().isMultipartMixed()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 246 == message.getPlainBodyAsString().length()

        def image = message.asMultipart().getSubParts()[1]

        assert 25438 == Util.getSize(image)

        assert 0 == StreamMonitor.unclosedStreams()

    }

    /* The subject is broken up into two lines. The second line only has
     * a space and nothing else.
     */
    @Test
    void test0020() {

        def message = new FileMessageSource('data/untroubled/1998/11/911948009.3721.txt').load()

        assertEquals("Accept Credit Cards, Guaranteed Apr ", message.getSubject())

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /**
     * The subject is folded into two lines.
     */
    @Test
    void test0021() {
        def message = new FileMessageSource('data/untroubled/1998/12/913591761.31088.txt').load()

        assertEquals('Guaranteed Approval-Accept Credit C Guaranteed Approval! - Low Monthly Fee!', message.getSubject())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Subject header is encoded with quoted printable instead of base64.
     * Subject header is only partially encoded.
     */
    @Test
    void test0022() {
        def message = new FileMessageSource('data/untroubled/1999/02/918223354.29294.txt').load()

        assert '¿¿¿Esta libre su dominio???' == message.getSubject()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0023() {
        def message = new FileMessageSource('data/untroubled/1999/03/922727951.11411.txt').load()

        assert message.getPlainBodyAsString().endsWith('\n\n\n\n')
        assert 1140 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Non ascii in from and to addresses */
    @Test
    void test0024() {
        def message = new FileMessageSource('data/untroubled/1999/04/924484628.30197.txt').load()

        def sender = message.getSender()
        def recipients = message.getToRecipients()

        assert 4 == sender.getLocal().length()
        assert 'www.silla.ac.kr' == sender.getDomain()
        assert 4 == recipients[0].getLocal().length()
        assert 'www.silla.ac.kr' == recipients[0].getDomain()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has no end boundary */
    @Test
    void test0025() {
        def message = new FileMessageSource('data/untroubled/1999/05/927651121.3408.txt').load()

        assert message.asMultipart().isMultipartMixed()

        assert 1617 == message.getHtmlBodyAsString().length()

        assert 1 == message.asMultipart().getSubPartCount()

        def html = message.asMultipart().getSubParts()[0]

        assert 'text/html' == html.getContentType().getType()

        assert 1617 == Util.getSize(html)

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Bogus charset "unknown-8bit". Javamail cannot handle this */
    @Test
    void test0026() {
        def message = new FileMessageSource('data/untroubled/1999/07/932709953.469.txt').load()

        assert ! message.asMultipart()

        assert 1600 == message.getPlainBodyAsString().length()

        assert 'text/plain' == message.getContentType().getType()

        assert 'unknown-8bit' == message.getContentType().getCharset()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* No Start boundary. Javamail cannot handle this. */
    @Test
    void test0027() {
        def message = new FileMessageSource('data/untroubled/1999/08/935004235.32576.txt').load()

        assert message.asMultipart().isMultipartMixed()

        assert 0 == message.asMultipart().getSubPartCount()

        def text = message.getPlainBodyAsString()

        assert 1614 == text.length()

        assert 'multipart/mixed' == message.getContentType().getType()

        assert text.startsWith("Now any man, regardless of age, can easily")
        assert 0 == StreamMonitor.unclosedStreams()

    }

    /* Quote printable header that is the entire subject line */
    @Test
    void test0028() {
        def message = new FileMessageSource('data/untroubled/1999/09/936763063.31554.txt').load()

        assert '»¶Ó­¹âÁÙÍøÉÏÓÊ' == message.getSubject()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Multiple quoted printable parts in a subject header */
    @Test
    void test0029() {
        def message = new FileMessageSource('data/untroubled/1999/09/937321067.8872.txt').load()

        assertEquals("linux-kernel´Ô¾È³çÇÏ¼¼¿ä - Ãß¼®¼±¹° -",
            message.getSubject())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Quoted printable recipient */
    /* =?ISO-8859-1?Q?@qcc.sk.ca (=C9=CC=D2=B5=BF=CD=BB=A7?=) */
    @Test
    void test0030() {
        def message = new FileMessageSource('data/untroubled/1999/11/941774384.12068.txt').load()

        assert 2 == message.getAllRecipients().size()

        def recipient = message.getAllRecipients()[1]

        assert 'qcc.sk.ca' == recipient.getDomain()
        assert '\u00C9\u00CC\u00D2\u00B5\u00BF\u00CD\u00BB\u00A7' == recipient.getComment()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* KR-ASCII charset. Javamail does not handle this */
    @Test
    void test0031() {
        def message = new FileMessageSource('data/untroubled/1999/12/945185368.18242.txt').load()

        def html = message.getHtmlBodyAsString()

        assert 5073 == html.length()

        assert html.startsWith('<html>\n<head>\n<title>Untitled Document</title>\n<meta http-equiv="')
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This test has a quoted printable encoded subject with an _,
     * which must be converted to space.
     */
    @Test
    void test0032() {
        def message = new FileMessageSource('data/untroubled/1999/09/938745698.26996.txt').load()

        assert '[alsa-user] Votre campagne de publicité ciblée.' == message.getSubject()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Header encoding with lower b instead of the usual B for base64. */
    @Test
    void test0033() {
        def message = new FileMessageSource("data/untroubled/2000/12/976632400.14597_10.txt").load()

        assertEquals("傳送失敗: 傳送至 mx1.IBM.NET; SMTP Protocol Returned a Permanent Error 551 not our customer 發生錯誤", message.getSubject())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Japanese header */
    @Test
    void test0034() {
        def message = new FileMessageSource('data/untroubled/2000/02/951808811.6936.txt').load()

        assertEquals("        ようこそ、あゆみのホームページへ", message.getSubject())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This has a Russian header. Javamaoil couldnt parse it because the
     * base64 string was missig the trailing =. mutt and online decoder
     * can decode just fine, though.
     */
    @Test
    void test0035() {
        def message = new FileMessageSource('data/untroubled/2000/10/970979910.12652_3.txt').load()

        assertEquals("Будьте бдительны!".length(), message.getSubject().length())
        assertEquals("\u0411\u0443\u0434\u044C\u0442\u0435 \u0431\u0434\u0438\u0442\u0435\u043B\u044C\u043D\u044B!", message.getSubject())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This has encoded headers without any intervening space.
     * Javamail only partial decodes these types of headers.
     */
    @Test
    void test0036() {
        def message = new FileMessageSource('data/untroubled/2000/04/954713592.20506.txt').load()

        assertEquals('★アイドル２００１オープン！無料体験もあり★', message.getSubject())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This has a line that's 81 chars long, 1 greater than our read buffer */
    @Test
    void test0037() {
        def message = new FileMessageSource('data/untroubled/2000/07/963726476.6121.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()

        assert 220 == message.getPlainBodyAsString().length();

        assertEquals("BE AMAZED BY OUR GENUINE PSYCHICS!!! Incredible details. LOVE! MONEY! SUCCESS! \r\nImmediate answers. Call right now...1-800-935-3283   1-900-263-2546      24hrs/7 days a week                                              \n", message.getPlainBodyAsString());


        assert 220 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* bogus charset again. */
    @Test
    void test0038() {
        def message = new FileMessageSource('data/untroubled/2001/01/980257548.28163_15.txt').load()

        def text = message.getPlainBodyAsString()

        assert 579 == text.length()

        assert text.startsWith('\nThought you might be interested in the below')

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* There is a mime part. There is some base64 encoded content
     * followed by plain content. Javamail and mutt both stop at the end
     * of the base64 content. We do the same.
     */
    @Test
    void test0039() {
        def message = new FileMessageSource('data/untroubled/2001/01/980784874.6697_20.txt').load()

        def text = message.getHtmlBodyAsString()

        assert 4457 <= text.length()

        assert 4483 >= text.length()

        assert text.endsWith('</HTML>\r\n');

        assert 0 == StreamMonitor.unclosedStreams()
    }

  /* This has a header that has some lone CR.
     * This is not a legal line ending.
     * JavaMail interprets this as a line sometime, but not always.
     */
    @Test
    void test0040() {
        def message = new FileMessageSource('data/untroubled/2001/02/982504495.4858_28.txt').load()

        assert 17 == message.getHeaderList().size()
        assert 0 == message.getInvalidHeaders().size()

        assert 'Message-ID' == message.getHeaderList()[6].getName()

        assertEquals("<00004a1a6420\$00003900\$00001873@mindspring (user-3qt5hn.dialup.mindspring.com[99.174.150.55]) by smtp6.mindspring.com (8.9.3/8.8.5) with SMTP id OAA06398  from 110140321worldnet.att.net ([102.70.21.32]) by mtiwmhc98.worldnet.att.net (InterMail v03.02.07.07 118-134) with SMTP id<20090116195452.ZOMX28505@110940321worldnet.att.net>>", message.getHeaderList()[6].getValue())
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a boundary setting like this
     * boundary = "blah"
     * with the spaces.
     */
    @Test
    void test0041() {
        def message = new FileMessageSource('data/untroubled/2001/05/989360961.25568_194.txt').load()

        assert '=_9c2cf5d9fac82c6bf39d16dd46a39c9c' == message.getContentType().getBoundary()
        assert 1 == message.asMultipart().getSubPartCount()

        assert message.asMultipart().isMultipartMixed()

        def related = message.asMultipart().getSubParts()[0]

        assert 2 == related.asMultipart().getSubPartCount()

        def alternative = related.asMultipart().getSubParts()[0]
        def jpeg = related.asMultipart().getSubParts()[1]

        assert 43 == Util.getSize(jpeg)

        assert 2 == alternative.asMultipart().getSubPartCount()

        def plain = alternative.asMultipart().getSubParts()[0]
        def html = alternative.asMultipart().getSubParts()[1]

        assert 0 == Util.getSize(plain)
        assert 1451 == Util.getSize(html)
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail fails on this one with
     *
     * Error in encoded stream: needed 4 valid base64 characters but only
     * got 1 before EOF
     */
    @Test
    void test0042() {
        def message = new FileMessageSource('data/untroubled/2001/05/990570097.28769_6.txt').load()

        assert 4861 <= message.getHtmlBodyAsString().length()
        assert 4885 >= message.getHtmlBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Subject is
     * YOU =?UNKNOWN?Q?CAN=B4T?= MISS THIS OPPORTUNITY!
     * JavaMail throws an exception.
     */
    @Test
    void test0043() {
        def message = new FileMessageSource('data/untroubled/2001/10/1003635432.1369_243.txt').load()

        assert message.getSubject().startsWith("YOU CAN")
        assert message.getSubject().endsWith("T MISS THIS OPPORTUNITY!")
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* transfer encoding is quotedGMprintable. not valid.
     * Javamail throws an exception.
     */
    @Test
    void test0044() {
        def message = new FileMessageSource('data/untroubled/2001/10/1004484135.24744_160.txt').load()

        assert 12129 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().startsWith('<html')
        assert message.getHtmlBodyAsString().endsWith('</html>')
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /**
     * Message has a multipart boundary but the boundary doesn't show up
     * and there is no content.
     */
    @Test
    void test0045() {
        def message = new FileMessageSource('data/untroubled/2001/12/1008022010.29752_414.txt').load()

        assert message.isMultipart()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 0  == message.getPlainBodyAsString().length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail fails on this one with
     *
     * BASE64Decoder: Error in encoded stream: needed at least 2 valid
     * base64 characters, but only got 0 before padding character (=)
     */
    @Test
    void test0046() {
        def message = new FileMessageSource('data/untroubled/2002/04/1018680553.16381_1.txt').load()

        assert 311  == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail fails on this one with
     *
     * BASE64Decoder: Error in encoded stream: found valid base64 character
     * after a padding character (=)
     */
    @Test
    void test0047() {
        def message = new FileMessageSource('data/untroubled/2002/04/1020230010.25472_958.txt').load()

        def parts = message.asMultipart().getSubParts()

        assert "0.1"== parts[1].getMimePath().toString()

        assert 88367 == Util.getSize(parts[1])

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail fails on this one with  unsupportedencoding exception.
     * the charset is in single quotes instead of double quotes.
     */
    @Test
    void test0048() {

        def message = new FileMessageSource('data/untroubled/2002/02/1012779537.16735_1.txt').load()

        assert 'iso-8859-1' == message.getContentType().getCharset()

        assert 1456 == message.getHtmlBodyAsString().length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* The HTML part is supposed to be quoted printable but the text
     * isn't encoded at all. JavaMail truncates.
     * At "<font size=-1". It stops at that equal
     * sign.
     */
    @Test
    void test0049() {
        def message = new FileMessageSource('data/untroubled/2002/02/1014157107.3682_85.txt').load()

        assert 29358 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().endsWith('</html>\n')
        assert 0 == StreamMonitor.unclosedStreams()
    }


    /* This message has a completely empty mime part
       The message has the following parts
       1) text/html
       2) audio/x-wav
       3) nothing
       4) application/octet-stream

       JavaMail interprets this as 3 parts. The third part will have no content.
       mutt interprets this as 3 parts but the last part has headers in the body.

       pantomime will also interpret this as three parts. The third will
       have content and propery parsing of headers.
    */
    @Test
    void test0050() {
        def message = new FileMessageSource('data/untroubled/2002/04/1020103868.3907_35.txt').load()

        /* how many parts ? */
        assert 3 == message.asMultipart().getSubPartCount()

        assert 'text/html' == message.asMultipart().getSubParts()[0].getContentType().getType()
        assert 'audio/x-wav' == message.asMultipart().getSubParts()[1].getContentType().getType()
        assert 'application/octet-stream' == message.asMultipart().getSubParts()[2].getContentType().getType()

        def part = message.asMultipart().getSubParts()[2]

        /* This was that extra boundary, that we're now ignoring. */
        //assert 1 == part.getInvalidHeaders().size()

        assert 114 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 88471 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 437 == Util.getSize(message.asMultipart().getSubParts()[2])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* In this message, the main message and subparts use the same boundary.
     * The end boundary shows up in random places. There is no end boundary
     * for the message.
     *
     * It looks like this:
     * related
     *      start boundary
     *      alternative
     *          start boundary (same one)
     *          text/html
     *          end boundary
     *          start bounary
     *          audio/x-wav
     *          start bounary
     *
     * JavaMail and mutt both ignore the audio/x-wav part.
     * mutt pretends there is a text/plain with no content, probably
     * because the text/html is wrapped in a multipart/alternative.
     *
     * JavaMail will sometimes fail with no start boundary.
     *
     * pantomime will interpret four parts.
     */
    @Test
    void test0051() {
        def message = new FileMessageSource('data/untroubled/2002/11/1037403391.20261_93.txt').load()

        assert 4 == message.asMultipart().getSubPartCount()

        assert message.asMultipart().isMultipartRelated()

        def part1 = message.asMultipart().getSubParts()[0]

        assert part1.asMultipart().isMultipartAlternative()

        def part2 = message.asMultipart().getSubParts()[1]

        assert 'text/html' == part2.getContentType().getType()
        assert 653 == Util.getSize(part2)
        assert Util.streamToString(part2.asSinglePart().getBody()).startsWith('\n<HTML>')
        def html = message.getHtmlBodyAsString()
        assert 653 == html.length()
        assert html.startsWith('\n<HTML>')

        def part3 = message.asMultipart().getSubParts()[2]

        assert 0 == Util.getSize(part3)

        def part4 = message.asMultipart().getSubParts()[3]
        assert 'audio/x-wav' == part4.getContentType().getType()
        assert 114690 == Util.getSize(part4)

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /*
     * This message uses :^B for the boundary where ^B is ascii 2 (start text).
     * This is considered whitespace, which we trim from the string.
     *
     * RFC 2046, page 21 (If a boundary delimiter line appears to
     * end with white space, the white space must be presumed to have been
     * added by a gateway, and must be deleted.)
     */
    @Test
    void test0052() {

        def message = new FileMessageSource('data/untroubled/2003/01/1043956115.16948_91.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()

        def part = message.asMultipart().getSubParts()[0]

        assert 'text/html' == part.getContentType().getType()

        assert Util.streamToString(part.asSinglePart().getBody()).startsWith('<body>')
        assert Util.streamToString(part.asSinglePart().getBody()).endsWith('</body>')
        assert 3427 == Util.streamToString(part.asSinglePart().getBody()).length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message fails in JavaMail with
     * javax.mail.internet.ParseException: Expected ';', got "="
     *
     * Probably JavaMail cannot unable a boundary like so
         boundary=----=_NextPart_000_384C_0B5FCC3E.CEDCD8F

     * There is an = in the boundary, but the boundary isn't quoted.
     */
    @Test
    void test0053() {
        def message = new FileMessageSource('data/untroubled/2003/01/1042816728.2283_398.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 1 == message.asMultipart().getSubPartCount()

        assert 2477 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().startsWith('<html>')

        assert 2477 == Util.getSize(message.asMultipart().getSubParts()[0])

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Recipient, "bonsai@hatch.co.jp"< */
    @Test
    void test0054() {
        def message = new FileMessageSource('data/untroubled/2003/03/1047667126.1894_1256.txt').load()

        assert 38 == message.getToRecipients().size()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a multipart, but the start boundary is never used.
     * There is an end boundary, though.
     * JavaMail fails.
     */
    @Test
    void test0055() {
        def message = new FileMessageSource('data/untroubled/2003/03/1048257963.32112_55.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 0 == message.asMultipart().getSubPartCount()

        assertEquals('This is a multi-part message in MIME format.\n\n--_DAD7AFBC.F--\n\n', message.getPlainBodyAsString())

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a multipart/mixed content type but no boundary
     * defined.
     *
     * JavaMail fails.
     */
    @Test
    void test0056() {
        def message = new FileMessageSource('data/untroubled/2003/05/1052489163.11780_46.txt').load()

        assert ! message.isMultipart()

        assert "multipart/mixed" == message.getContentType().getType()

        assert 3302 == message.getPlainBodyAsString().length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* The content type has a sub field class-id. The value has a :, but
     * the sub field isn't quoted.
     * JavaMail fails.
     */
    @Test
    void test0057() {

        def message = new FileMessageSource('data/untroubled/2003/05/1052489331.12165_177.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 2 == message.asMultipart().getSubPartCount()

        assert '------------105240472162183' == message.getContentType().getSubField('boundary')

        assert '6:8eLhs9bS78L:238022' == message.getContentType().getSubField('class-id')

        assert 936 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 2589 == Util.getSize(message.asMultipart().getSubParts()[1])

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a multipart without an end boundary. */
    @Test
    void test0058() {

        def message = new FileMessageSource('data/untroubled/2003/06/1055361266.30918_123.txt').load()

        assert message.asMultipart().isMultipartMixed()

        assert 2 == message.asMultipart().getSubPartCount()

        def part = message.asMultipart().getSubParts()[0]

        assert 1 == part.asMultipart().getSubPartCount()

        def subPart = part.asMultipart().getSubParts()[0]

        def text = Util.streamToString(subPart.asSinglePart().getBody())

        assert 1098 == text.length();

        assert text.startsWith('<html>')

        assert text.endsWith('</html>')

        assert 358 == Util.getSize(message.asMultipart().getSubParts()[1])

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a control character in the boundary.
     * JavaMail fails with java.lang.ArrayIndexOutOfBoundsException: -92
     */
    @Test
    void test0059() {
        def message = new FileMessageSource('data/untroubled/2003/06/1055870201.26734_1.lorien').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert message.asMultipart().isMultipartRelated()

        def alternative = message.asMultipart().getSubParts()[0]

        assert alternative.asMultipart().isMultipartAlternative()

        assert 1 == alternative.asMultipart().getSubPartCount()

        def html = alternative.asMultipart().getSubParts()[0]

        assert 420 == Util.streamToString(html.asSinglePart().getBody()).length()

        assert 420 == message.getHtmlBodyAsString().length()

        assert message.getHtmlBodyAsString().startsWith('<html>')

        def jpeg = message.asMultipart().getSubParts()[1]

        assert 36711 == Util.getSize(jpeg)

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a boundary defined but it's not used.
     * JavaMail fails.
     */
    @Test
    void test0060() {
        def message = new FileMessageSource('data/untroubled/2003/07/1057381938.M717213P20471.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 1828 == message.getPlainBodyAsString().length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This has an unsual boundary with control characters and a semicolon */
    @Test
    void test0061() {
        def message = new FileMessageSource('data/untroubled/2003/07/1057522942.M549249P5426.txt').load()

        assert message.asMultipart().isMultipartRelated()

        assert 2 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]

        assert part1.asMultipart().isMultipartAlternative()
        assert 1 == part1.asMultipart().getSubPartCount()

        def part1_1 = part1.asMultipart().getSubParts()[0]

        def html = Util.streamToString(part1_1.asSinglePart().getBody())
        assert 378 == html.length()

        assert html.startsWith('<html>')

        assert 28723 == Util.getSize(part2)

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a start boundary instead of an end boundary
     * at the end of the file. We should take care to not read an
     * extra mime part here.
     */
    @Test
    void test0062() {
        def message = new FileMessageSource('data/untroubled/2003/07/1057523452.M957438P7342.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 0 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 2281 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has an error in the content type header.
     * Javamail says
     * javax.mail.internet.ParseException: Expected '=', got "null"
     */
    @Test
    void test0063() {
        def message = new FileMessageSource('data/untroubled/2003/07/1058902673.M444783P32645.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 'text/plain' == message.asMultipart().getSubParts()[0].getContentType().getType()
        assert 'text/html' == message.asMultipart().getSubParts()[1].getContentType().getType()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message is using a bogus boundary. It doesn't match the boundary
     * defined. JavaMail fails because no start boundary.
     * We won't throw an exception but the message is pretty useless since
     * the only content is base64 encoeded.
     */
    @Test
    void test0064() {
        def message = new FileMessageSource('data/untroubled/2003/09/1063836183.17406_10.txt').load()

        assert message.asMultipart().isMultipartRelated()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 4866 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }


    /* No semicolon after boundary definition in the Content-Type header.
     * This screws up JavaMail bad.
     */
    @Test
    void test0065() {
        def message = new FileMessageSource('data/untroubled/2003/09/1064592170.24759_600.txt').load()

        assert message.asMultipart().isMultipartRelated()

        assert 1 == message.asMultipart().getSubPartCount()

        def part = message.asMultipart().getSubParts()[0]

        assert part.asMultipart().isMultipartAlternative()

        assert 1 == part.asMultipart().getSubPartCount()

        def subPart = part.asMultipart().getSubParts()[0]

        assert 1789 == Util.getSize(subPart)

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* No end boundary in this message. */
    @Test
    void test0066() {

        def message = new FileMessageSource('data/untroubled/2004/01/1074006032.28300_190.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 1 == message.asMultipart().getSubPartCount()

        assert 'text/html' == message.asMultipart().getSubParts()[0].getContentType().getType()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has no boundary defined. */
    @Test
    void test0067() {
        def message = new FileMessageSource('data/untroubled/2004/01/1074116036.14927_77.txt').load()

        assert "multipart/alternative" == message.getContentType().getType()
        assert ! message.isMultipart()
        assert null == message.getContentType().getBoundary()

        assert 620 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a boundary defined but no content */
    @Test
    void test0068() {
        def message = new FileMessageSource('data/untroubled/2004/01/1074547869.6346_428.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 38 == message.getContentType().getBoundary().length()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 0 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a malformed end boundary */
    @Test
    void test0069() {
        def message = new FileMessageSource('data/untroubled/2004/01/1074550396.10453_1.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 1 == message.asMultipart().getSubPartCount()

        def part = message.asMultipart().getSubParts()[0]

        assert 'text/html' == part.getContentType().getType()

        assert 3710 == Util.getSize(part)

        assert 3710 == message.getHtmlBodyAsString().length()

        assert message.getHtmlBodyAsString().endsWith('</HTML>\n\n')

        assert null == message.getPlainBodyAsString()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a boundary marker in the headers, which
     * is effectively no boundary.
     */
    @Test
    void test0070() {
        def message = new FileMessageSource('data/untroubled/2004/01/1075484181.13471_110.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 4353 == message.getHtmlBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message only has end boundary, acting like start boundaries. */
    @Test
    void test0071() {
        def message = new FileMessageSource('data/untroubled/2004/02/1077211756.18390_71.txt').load()

        assert message.asMultipart().isMultipartAlternative()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 143 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 4071 == Util.getSize(message.asMultipart().getSubParts()[1])

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* The boundaries are written wrong. The boundary is --x, so we should
     * see ----x, but we see --x instead.
     */
    @Test
    void test0072() {
        def message = new FileMessageSource('data/untroubled/2004/02/1077636701.21288_9.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 185 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 9615 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a boundary in the headers and no end boundary */
    @Test
    void test0073() {
        def message = new FileMessageSource('data/untroubled/2004/03/1080083302.32497_203.txt').load()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 253 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has boundaries that don't match the defined boundary. */
    @Test
    void test0074() {
        def message = new FileMessageSource('data/untroubled/2004/03/1080603336.28176_74.txt').load()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 657 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* What is supposed to be the null line between the headers and body
     * is actually a space. So part of the body is cropped off.
     */
    @Test
    void test0075() {
        def message = new FileMessageSource('data/untroubled/2004/10/1098466483.3098_242.txt').load()

        assert 0 == message.asMultipart().getSubPartCount()

        assert 292 == message.getPlainBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Two sub parts. */
    @Test
    void test0076() {
        def message = new FileMessageSource('data/untroubled/2004/01/1073060060.32695_58.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 2830 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 'application/octet-stream' == message.asMultipart().getSubParts()[1].getContentType().getType()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Sub part's boundary is an extension of the main multipart's boundary. */
    @Test
    void test0077() {
        def message = new FileMessageSource('data/untroubled/2004/01/1073320546.2690_52.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Quoted printable part ends with a = on the very last line. */
    @Test
    void test0078() {
        def message = new FileMessageSource('data/untroubled/2004/01/1074006032.28300_190.txt').load()

        assert 3111 == message.getHtmlBodyAsString().length()
        assert 0 == StreamMonitor.unclosedStreams()

    }

    /* The Content-Type header is not folded so the boundary is on the
     * same line.
     */
    @Test
    void test0079() {
        def message = new FileMessageSource('data/untroubled/2004/01/1074297485.8356_41.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 1133 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 5624 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* The Content-Type header is not folded so the boundary is on the
     * same line.
     */
    @Test
    void test0080() {
        def message = new FileMessageSource('data/untroubled/2004/01/1072973089.21481_1.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]

        assert 2 == part1.asMultipart().getSubPartCount()

        def part1_1 = part1.asMultipart().getSubParts()[0]
        def part1_2 = part1.asMultipart().getSubParts()[1]

        assert 1548 == Util.getSize(part1_1)
        assert 7233 == Util.getSize(part1_2)

        assert 832 == Util.getSize(part2)
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This message has a boundary defined to be blank.
     */
    @Test
    void test0081() {
        def message = new FileMessageSource('data/untroubled/2004/02/1076879086.7714_57.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]

        assert 264 == Util.getSize(part1)
        assert 2919 == Util.getSize(part2)

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* There are blanks lines to delimit the boundary.
     * JavaMail fails to see these boundaries.
     */
    @Test
    void test0082() {
        def message = new FileMessageSource('data/untroubled/2004/02/1077211756.18390_105.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]

        /* There is some data here, but it's considered part of the headers. */
        assert 0 == Util.getSize(part1)

        assert 2526 == Util.getSize(part2)
        assert 0 == StreamMonitor.unclosedStreams()
    }

  /* Boundary is not a whole line.
   *
   * RFC 2046 states
   *
   *  The boundary may be followed by zero or more characters of
      linear whitespace. It is then terminated by either another CRLF and
      the header fields for the next part, or by two CRLFs, in which case
      there are no header fields for the next part.
   *
   *  Later it states
   *
   *
   * NOTE TO IMPLEMENTORS:  Boundary string comparisons must compare the
     boundary value with the beginning of each candidate line.  An exact
     match of the entire candidate line is not required; it is sufficient
     that the boundary appear in its entirety following the CRLF.

   * So it is OK that boundary isn't the whole line.
   *
   * But, we're going to ignore the rest of the line.
   */
  @Test
  void test0083() {
      def message = new FileMessageSource('data/untroubled/2004/03/1078938294.10433_164.txt').load()

      assert 3 == message.asMultipart().getSubPartCount()

      def part1 = message.asMultipart().getSubParts()[0]
      def part2 = message.asMultipart().getSubParts()[1]
      def part3 = message.asMultipart().getSubParts()[2]

      assert 165 == Util.getSize(part1)

      /* only headers */
      assert 0 == Util.getSize(part2)

      /* has the exact same content has part1 */
      assert 165 == Util.getSize(part3)
        assert 0 == StreamMonitor.unclosedStreams()
  }

    /* This has a header that has some lone CR.
     * This is not a legal line ending.
     * JavaMail interprets this as a line sometime, but not always.
     */
    @Test
    void test0084() {
        def message = new FileMessageSource('data/untroubled/2004/03/1079022409.5645_199.txt').load()

        def headers = message.getHeaderList();

        assert 'Return-Path' == headers[0].getName()
        assert 'From' == headers[6].getName()
        assert 'Reply-To' == headers[7].getName()

        assert 14 == headers.size()

        assert 0 == message.getInvalidHeaders().size()

        assert '"" <info5@ecom-universe.net>' == headers[6].getValue()
        assert '"" <info5@ecom-universe.net>' == headers[7].getValue()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Boundary contains a single quote. */
    @Test
    void test0085() {
        def message = new FileMessageSource('data/untroubled/2004/03/1079478925.406_44.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]

        assert 423 == Util.getSize(part1)
        assert 1532 == Util.getSize(part2)
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* There is only one boundary, which is ---- for --.
     * If there were a real end bounadry, this wouldn't haven't a problem.
     * As it stands now, it's not worth it to change the code just
     * for this case.
     */
    @Test
    void test0086() {
        def message = new FileMessageSource('data/untroubled/2004/03/1080083301.32497_141.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()

        assert 253 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }


    /* The subject header ends with \r\r\n. We should not interpret
     * the \r\n as an empty and, hence, the end of headers.
     */
    @Test
    void test0087() {
        def message = new FileMessageSource('data/untroubled/2004/06/1088571310.8148_1142.txt').load()

        def headers = message.getHeaderList();

        assert 17 == headers.size()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail fails with
     * javax.mail.internet.ParseException: Unbalanced quoted string
     *
     * Problem is with the boundary definition
     * boundary=3D"--0576163=
     *
     * The boundaries used in the message don't even match it.
     */
    @Test
    void test0088() {
        def message = new FileMessageSource('data/untroubled/2004/08/1092524229.29380_4198.txt').load()

        assert '3D--0576163=' == message.getContentType().getBoundary()
    }

    @Test
    void test0089() {
        def message = new FileMessageSource('data/untroubled/2004/09/1095287677.5504_32.txt').load()

        assert 6 == message.asMultipart().getSubPartCount()

        def part1 = message.asMultipart().getSubParts()[0]
        def part2 = message.asMultipart().getSubParts()[1]
        def part3 = message.asMultipart().getSubParts()[2]
        def part4 = message.asMultipart().getSubParts()[3]
        def part5 = message.asMultipart().getSubParts()[4]
        def part6 = message.asMultipart().getSubParts()[5]

        assert 2 == part1.asMultipart().getSubPartCount()

        def part1_1 = part1.asMultipart().getSubParts()[0]
        def part1_2 = part1.asMultipart().getSubParts()[1]

        assert 'text/plain' == part1_1.getContentType().getType()
        assert 'text/html' == part1_2.getContentType().getType()

        assert 5166 == Util.getSize(part1_1)
        assert 7484 == Util.getSize(part1_2)

        assert '<1B_head_8.jpg>' == part2.getContentId()
        assert 9490 == Util.getSize(part2)
        assert '<2B_line_8.jpg>' == part3.getContentId()
        assert 1688 == Util.getSize(part3)
        assert '<3B_z-shop.jpg>' == part4.getContentId()
        assert 5091 == Util.getSize(part4)
        assert '<4B_line_8.jpg>' == part5.getContentId()
        assert 1688 == Util.getSize(part5)
        assert '<5B_impressum_8.jpg>' == part6.getContentId()
        assert 3471 == Util.getSize(part6)
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail can't handle strange characters in the boundary. */
    @Test
    void test0090() {
        def message = new FileMessageSource('data/untroubled/2005/01/1104875560.13615_93.txt').load()

        assertEquals('--1alma?8rosette/282curt\\603altruism~',
            message.getContentType().getBoundary())

        assert 1 == message.asMultipart().getSubPartCount()

        assert 632 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* No blank line between headers and first boundary */
    @Test
    void test0091() {
        def message = new FileMessageSource('data/untroubled/2005/01/1105030540.12574_247.txt').load()

        assertEquals('--6-547010081-2259799023=:04847',
            message.getContentType().getBoundary())

        assert 0 == message.asMultipart().getSubPartCount()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail can't handle strange characters in the boundary. */
    @Test
    void test0092() {
        def message = new FileMessageSource('data/untroubled/2005/01/1105123431.1550_147.txt').load()


        assertEquals('--9cladophora}439irvin_455spiro\\58columbia:',
            message.getContentType().getBoundary())

        assert 1 == message.asMultipart().getSubPartCount()
        assert 633 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail can't handle strange characters in the boundary. */
    @Test
    void test0093() {
        def message = new FileMessageSource('data/untroubled/2005/01/1106780164.25719_634.txt').load()

        assertEquals('--01frost;231embezzle\\19inbreed{3equinoctial<',
            message.getContentType().getBoundary())

        assert 1 == message.asMultipart().getSubPartCount()
        assert 340 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail can't handle strange characters in the boundary. */
    @Test
    void test0094() {
        def message = new FileMessageSource('data/untroubled/2005/02/1109106859.30009_648.txt').load()

        assertEquals('--95chauncey!0bump\\72alex+104exhibit/',
            message.getContentType().getBoundary())

        assert 1 == message.asMultipart().getSubPartCount()
        assert 360 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail can't handle strange characters in the boundary. */
    @Test
    void test0095() {
        def message = new FileMessageSource('data/untroubled/2005/03/1111379045.539_404.txt').load()

        assertEquals('--91belle*6clause\\786deuteron*00prim(',
            message.getContentType().getBoundary())

        assert 1 == message.asMultipart().getSubPartCount()
        assert 769 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Not sure why Javamail can't find the boundary here. */
    @Test
    void test0096() {
        def message = new FileMessageSource('data/untroubled/2005/09/1127593156.3691_523.txt').load()

        assertEquals('--tBr-LSS-7Qxm7-NoO',
            message.getContentType().getBoundary())

        assert 1 == message.asMultipart().getSubPartCount()
        assert 366 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail can't handle strange characters in the boundary. */
    @Test
    void test0097() {
        def message = new FileMessageSource('data/untroubled/2005/10/1130270192.16320_140.txt').load()


        assert 331 == message.getContentType().getBoundary().length()

        assert 1 == message.asMultipart().getSubPartCount()
        assert 901 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Weird boundary with trailing \
       JavaMail fails with unbalanced quote.
     */
    @Test
    void test0098() {
        def message = new FileMessageSource('data/untroubled/2005/01/1106780082.25719_357.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()
        assert 369 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Use of dashes as a boundary */
    @Test
    void test0099() {
        def message = new FileMessageSource('data/untroubled/2005/01/1104641177.1893_3.txt').load()

        assert '--' == message.getContentType().getBoundary()

        assert 1 == message.asMultipart().getSubPartCount()
        assert 452 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Use of dashes as a boundary */
    @Test
    void test0100() {
        def message = new FileMessageSource('data/untroubled/2005/03/1110776445.23371_60.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 3140 == Util.getSize(message.asMultipart().getSubParts()[1])

        def part = message.asMultipart().getSubParts()[0]

        assert '----' == part.getContentType().getBoundary()

        assert 2 == part.asMultipart().getSubPartCount()
        assert 178 == Util.getSize(part.asMultipart().getSubParts()[0])
        assert 2629 == Util.getSize(part.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0101() {
        def message = new FileMessageSource("data/untroubled/2005/07/1121746082.29151_30.txt").load()

        assert 1 == message.asMultipart().getSubPartCount()

        def html = message.asMultipart().getSubParts()[0]

        assert Util.streamToString(html.asSinglePart().getBody()).startsWith('<html>')
        assert 2871 <= Util.streamToString(html.asSinglePart().getBody()).length()
        assert 2886 >= Util.streamToString(html.asSinglePart().getBody()).length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0102() {
        def message = new FileMessageSource('data/untroubled/2006/01/1136929725.12675_2277.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()
        assert 561 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 1317 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0103() {
        def message = new FileMessageSource('data/untroubled/2006/01/1138334688.11095_731.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()
        assert 24208 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }


    /* Word boundary= occurs in the content-type and another line */
    @Test
    void test0104() {
        def message = new FileMessageSource('data/untroubled/2006/01/1138334692.11095_2505.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()
        assert 976 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* And end boundary followed by a start boundary, followed by nothing */
    @Test
    void test0105() {
        def message = new FileMessageSource('data/untroubled/2006/03/1143662824.10536_881.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()

        assertEquals("solicitousinvestorcontestant.\nlyric turnpike!duffel\ncrew, mutilate.", Util.streamToString(message.asMultipart().getSubParts()[0].asSinglePart().getBody()))
        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0106() {
        def message = new FileMessageSource('data/untroubled/2007/01/1168900539.18785_391.txt').load()

        assert 4 == message.asMultipart().getSubPartCount()

        assert 4174 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 10517 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 2536 == Util.getSize(message.asMultipart().getSubParts()[2])
        assert 17928 == Util.getSize(message.asMultipart().getSubParts()[3])

        println message.dumpTree();
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Base 64 encoded subject is not multiple of 4. */
    @Test
    void test0107() {
        def message = new FileMessageSource('data/untroubled/2007/02/1171049986.22972_61.txt').load()

        assert 1 == message.getSubject().length()
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* There is binary in here. */
    @Test
    void test0108() {
        def message = new FileMessageSource('data/untroubled/2007/01/1167685593.16330_39.txt').load()

        assert 1 == message.asMultipart().getSubPartCount()

        assert message.asMultipart().isMultipartRelated()

        def related = message.asMultipart().getSubParts()[0]

        assert 2 == related.asMultipart().getSubPartCount()

        assert 1445 == Util.getSize(related.asMultipart().getSubParts()[0])
        assert 6193 == Util.getSize(related.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* This has boundaries on consecutive lines. We ignore that empty
     * mime part.
     */
    @Test
    void test0109() {
        def message = new FileMessageSource('data/untroubled/2007/11/1195506634.10160_446.txt').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 716 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 1822 == Util.getSize(message.asMultipart().getSubParts()[1])

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* JavaMail cannot parse the base64 part. */
    @Test
    void test0110() {
        def message = new FileMessageSource('data/eidos/1375239953.P19747Q2285.eidos.os5.com:2,').load()

        assert 2 == message.asMultipart().getSubPartCount()

        assert 1020 == Util.getSize(message.asMultipart().getSubParts()[0])
        assert 19544 == Util.getSize(message.asMultipart().getSubParts()[1])
        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0111() {
        def message = new Message()

        message.asSinglePart().set('Eating manners', 'text/plain', 'utf-8')

        message.setSubject('Eating manners.')
        message.setFrom('dijon@bravo-cat.net')
        message.addToRecipient('marty@bravo-cat.net')

        def source = new FileMessageSource('build/fileSaveTest.eml')

        def sourcedMessage = message.saveAs(source)

        assert 'Eating manners.' == sourcedMessage.getSubject()
        assert 254 <= new File('build/fileSaveTest.eml').length()
        assert 256 >= new File('build/fileSaveTest.eml').length()

        sourcedMessage.setSubject('Your eating manners are apocryphal.')
        sourcedMessage.save()

        assert 'Your eating manners are apocryphal.' == sourcedMessage.getSubject()
        assert 274 <= new File('build/fileSaveTest.eml').length()
        assert 276 >= new File('build/fileSaveTest.eml').length()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0112() {
        def message = new FileMessageSource('data/untroubled/2003/10/1066235417.10802_173.txt').load()

        assert message.getSubject().length() > 0
        assert 0 == StreamMonitor.unclosedStreams()
    }

}
