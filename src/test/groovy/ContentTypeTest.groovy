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
 * $Id: ContentTypeTest.groovy,v 1.4 2013/08/27 16:39:17 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class ContentTypeTest {
 
    @Test
    void testContentType1() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain')

        assert 'text/plain' == header.getType()

    }

    @Test
    void testContentType2() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain ; charset=us-ascii')

        assert 'text/plain' == header.getType()
        assert 'us-ascii' == header.getCharset()

    }


    @Test
    void testContentType3() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain ; charset="us-ascii"')

        assert 'text/plain' == header.getType()
        assert 'us-ascii' == header.getCharset()

    }

    @Test
    void testContentType4() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("text/plain ; charset='us-ascii'")

        assert 'text/plain' == header.getType()
        assert 'us-ascii' == header.getCharset()

    }

    @Test
    void testContentType5() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain ; boundary = "blah"')

        assert 'text/plain' == header.getType()
        assert 'blah' == header.getBoundary()

    }

    @Test
    void testContentType6() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain ; ')

        assert 'text/plain' == header.getType()

    }

    @Test
    void testContentType7() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain ; bogus;')

        assert 'text/plain' == header.getType()

    }

    @Test
    void testContentType8() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('text/plain ; bogus ')

        assert 'text/plain' == header.getType()

    }

    /* JavaMail crashes on this one
     * javax.mail.internet.ParseException: Expected '=', got "null"
     */
    @Test
    void testContentType9() throws Exception {

        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/alternative;\n boundary="=_NextPart_2rfkindysadvnqw3nerasdf";ISO-8859-1')


        assert 'multipart/alternative' == header.getType()
        assert '=_NextPart_2rfkindysadvnqw3nerasdf' == header.getBoundary()

    }

    @Test
    void testContentType10() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/alternative; boundary=----=_NextPart_000_384C_0B5FCC3E.CEDCD8F')

        assert 'multipart/alternative' == header.getType()
        assert '----=_NextPart_000_384C_0B5FCC3E.CEDCD8F' == header.getBoundary()
    }

    @Test
    void testContentType11() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')

        header.setValue('multipart/alternative;\n boundary="=_NextPart_2rfkindysadvnqw3nerasdf";ISO-8859-1')

        assert 'multipart/alternative' == header.getType()
        assert '=_NextPart_2rfkindysadvnqw3nerasdf' == header.getBoundary()
    }

    @Test
    void testContentType12() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/related;\n boundary="----=_NextPart_000_00E0_01C34495.FA087520"\n type=multipart/alterntive')

        assert '----=_NextPart_000_00E0_01C34495.FA087520' == header.getBoundary()
    }

    @Test
    void testContentType13() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/alternative;\n boundary="--74743358621870236863"%RND_SPACE')

        assert '--74743358621870236863' == header.getBoundary()
    }

    @Test
    void testContentType14() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/mixed  boundary="--1396158232235973"')

        assert '--1396158232235973' == header.getBoundary()
    }

    @Test
    void testContentType15() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/alternative;\n boundary="_000_49D9ABAA8ACCE9438F0C33BF1529340D20AA5Axmbalnx14ciscocom_"')

        assert '_000_49D9ABAA8ACCE9438F0C33BF1529340D20AA5Axmbalnx14ciscocom_' == header.getBoundary()

    }

    @Test
    void testContentType16() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/alternative;  boundary="_000_49D9ABAA8ACCE9438F0C33BF1529340D20AA5Axmbalnx14ciscocom_"')

        assert '_000_49D9ABAA8ACCE9438F0C33BF1529340D20AA5Axmbalnx14ciscocom_' == header.getBoundary()

    }

    @Test
    void testContentType17() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative;\r\n boundary=\"busboyshouldn'tdeleterious\"")

        assertEquals("busboyshouldn'tdeleterious", header.getBoundary())
    }

    @Test
    void testContentType18() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue('multipart/alternative  boundary=gFNSO2Hq1')
        assertEquals('gFNSO2Hq1', header.getBoundary())
    }

    @Test
    void testContentType19() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative; boundary=\"--25browbeaten(1modem!1insolent\$526bolshevist\\\"")
        assertEquals("--25browbeaten(1modem!1insolent\$526bolshevist\\", header.getBoundary())
    }

    @Test
    void testContentType20() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative; boundary=\"--2bluebill`98sling?873educable_84emphases\\")
        assertEquals("--2bluebill`98sling?873educable_84emphases\\", header.getBoundary())
    }

    @Test
    void testContentType21() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative; boundary=\"\"");
        assertEquals("", header.getBoundary())
    }

    @Test
    void testContentType22() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/mixed boundary=b245b93524411bfb7388f21758e1eed1")

        assertEquals("b245b93524411bfb7388f21758e1eed1", header.getBoundary())

    }

    @Test
    void testContentType23() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/mixed;\r\nboundary=\"----------ETlGUPlPu8l1pSfkjsLgij")
        assertEquals("----------ETlGUPlPu8l1pSfkjsLgij", header.getBoundary())

    }

    @Test
    void testContentType24() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative; boundary=\\\"===============0538697490==\\\"")
        assertEquals("\\===============0538697490==\\", header.getBoundary())

    }

    @Test
    void testContentType25() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative;\n boundary=\"=_MoreStuf_2zzz1234sadvnqw3nerasdf\"; MIME-Version: 1.0");
        assertEquals("=_MoreStuf_2zzz1234sadvnqw3nerasdf", header.getBoundary())

    }

    @Test
    void testContentType26() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative;\n boundary=----=_NextPart_000_0023_11_4B737088.8CD69090");
        assertEquals("----=_NextPart_000_0023_11_4B737088.8CD69090",
            header.getBoundary())

    }

    @Test
    void testContentType27() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative; boundary=1374091607=_?:");
        assertEquals("1374091607=_?:", header.getBoundary());
    }

    @Test
    void testContentType28() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/mixed;\n boundary=\"=_NextPart_2rfkindysadvnqw3nerasdf\";iso-8859-1");
        assertEquals("=_NextPart_2rfkindysadvnqw3nerasdf", header.getBoundary());
    }

    @Test
    void testContentType29() throws Exception {
        def header = new ContentType()
        header.setName('Content-Type')
        header.setValue("multipart/alternative;\n boundary\"----=_NextPart_000_0006_00000123.0000F48B\"")
        assert null ==  header.getBoundary();
    }

}

