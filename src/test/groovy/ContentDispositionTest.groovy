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
 * $Id: ContentDispositionTest.groovy,v 1.5 2015/06/05 20:52:12 barbee Exp $
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

class ContentDispositionTest {

    @Test
    void testContentDisposition1() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue('inline')

        assert header.isInline()
    }

    @Test
    void testContentDisposition2() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue('attachment')

        assert header.isAttachment()
    }

    @Test
    void testContentDisposition3() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setTransferEncodedValue('attachment; filename="test.txt"; size=5;\n creation-date="Fri, 26 Jul 2013 12:05:26 GMT";\n modification-date="Fri, 26 Jul 2013 12:05:26 GMT"')

        assert header.isAttachment()
        assert 'test.txt' == header.getFilename()
        assert 5 == header.getStatedSize()
        assert 'Fri, 26 Jul 2013 12:05:26 GMT' == header.getCreationDateString()
        assert 'Fri, 26 Jul 2013 12:05:26 GMT' == header.getModificationDateString()

    }

    @Test
    void testContentDisposition4() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setTransferEncodedValue('attachment;\n filename="=?iso-2022-jp?B?GyRCcS07UhsoQi50eHQ=?="; size=7;\n creation-date="Tue, 30 Jul 2013 16:10:08 GMT";\n modification-date="Tue, 30 Jul 2013 16:10:08 GMT"')

        assert header.isAttachment()
        assert '餃子.txt' == header.getFilename()
    }

    @Test
    void testContentDisposition5() throws Exception {
        def header = new ContentDisposition(ContentDispositionType.ATTACHMENT,
            '10777258-22JGCSRMD-2002-2003_Jeep_Grand_Cherokee_Service_Repair_Manual_Download.zip')

        assert 'attachment; filename=10777258-22JGCSRMD-2002-2003_Jeep_Grand_Cherokee_Service_Repair_Manual_Download.zip' == header.getValue()
        assert "attachment;\r\n filename=10777258-22JGCSRMD-2002-2003_Jeep_Grand_Cherokee_Service_Repair_Man\r\n ual_Download.zip" == header.getTransferEncodedValue()
    }

    @Test
    void testContentDisposition6() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue("attachment; filename*=utf-8''%E4%B8%AD%E6%96%87%2Etxt")

        assert '\u4E2D\u6587.txt' == header.getFilename()
    }

    @Test
    void testContentDisposition7() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue("attachment; filename*=ISO-2022-JP''%1B%24BCfJ8%1B%28B.txt")

        assert '\u4E2D\u6587.txt' == header.getFilename()
    }

    @Test
    void testContentDisposition8() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue("attachment; filename*=ISO-2022-JP''%1B%24BCfJ8%1B%28B.txt")

        assert '\u4E2D\u6587.txt' == header.getFilename()
    }

    @Test
    void testContentDisposition9() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue("attachment; filename*=iso-8859-1'en'%A3%20rates")

        assert '£ rates' == header.getFilename()
    }

    @Test
    void testContentDisposition10() throws Exception {
        def header = new ContentDisposition()
        header.setName('Content-Disposition')
        header.setValue("attachment; filename*=UTF-8''%c2%a3%20and%20%e2%82%ac%20rates")

        assert '\u00A3 and \u20AC rates' == header.getFilename()
    }


    @Test
    void testContentDisposition11() throws Exception {
        def header = new ContentDisposition(ContentDispositionType.ATTACHMENT,
            '\u4E2D\u6587.txt')

        assert "attachment; filename*=UTF-8''%E4%B8%AD%E6%96%87.txt" == header.getTransferEncodedValue()
    }

    @Test
    void testContentDisposition12() throws Exception {

        def message = new Message()

        message.asSinglePart().set("test", "text/plain", "utf-8")

        message.addAttachment('attachment content', '\u4E2D\u6587.txt', 'text/plain')

        assert Util.streamToString(message.serialize()).contains("Content-Disposition: attachment; filename*=UTF-8''%E4%B8%AD%E6%96%87.txt; size=\"18\"")

    }

    @Test
    void testContentDisposition13() throws Exception {

        def pantoMessage = Pantomime.alternative("john@doe.com", "Lorem Ipsum",
            null, null, "jane@doe.com")

        pantoMessage.addAttachment(new InputStreamSource () {
            public InputStream getInputStream () {
                def value = "Lorem ipsum dolor sit amet."
                return new ByteArrayInputStream (value.getBytes())
            }
        }, "attachment.txt", "text/plain")

    }

    @Test
    void testAttachmentFilename1() throws Exception {

        def message = new FileMessageSource("data/0013.eml").load()

        assert 1 == message.getAllAttachmentCount()

        def attachment= message.getAllAttachments()[0]

        assert '999999.TXT' == attachment.getFilename()

    }

    @Test
    void testAttachmentFilename2() throws Exception {

        def message = new FileMessageSource("data/0014.eml").load()

        assert 1 == message.getAllAttachmentCount()

        def attachment = message.getAllAttachments()[0]

        assert '888888.TXT' == attachment.getFilename()

    }

    @Test
    void testAttachmentFilename3() throws Exception {

        def message = new FileMessageSource("data/0015.eml").load()

        assert 1 == message.getAllAttachmentCount()

        def attachment = message.getAllAttachments()[0]

        assert null == attachment.getFilename()

    }

    @Test
    void testInlineFilename1() throws Exception {

        def message = new FileMessageSource("data/0016.eml").load()

        assert 0 == message.getAllAttachmentCount()

    }

}
