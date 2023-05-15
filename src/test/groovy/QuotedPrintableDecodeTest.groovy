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
 * $Id: QuotedPrintableDecodeTest.groovy,v 1.4 2013/10/08 17:33:15 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class QuotedPrintableDecodeTest {
 
    void testDecode(String crypt, String plain) throws Exception {
        testDecode(crypt, plain, false)
    }

    void testDecode(String crypt, String plain, boolean header)
        throws Exception {

        def bais = new ByteArrayInputStream(crypt.getBytes())

        def QuotedPrintable = new QuotedPrintableDecodeInputStream(bais, header);

        byte[] bytes = new byte[8192];

        int bytesRead = QuotedPrintable.read(bytes);

        assertEquals(plain, new String(bytes, 0, bytesRead));
        assert plain == new String(bytes, 0, bytesRead);

    }

    @Test
    void testSimple() throws Exception {

        testDecode("If you believe that truth=3Dbeauty, then surely =\nmathematics is the most beautiful branch of philosophy.", "If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.");

    }

    @Test
    void testLineEnding() throws Exception {

        testDecode("font-family: Arial, sans-serif;font-size:10pt;text-align:left;'>=\n\n\n\t<tr>", "font-family: Arial, sans-serif;font-size:10pt;text-align:left;'>\n\n\t<tr>");

    }

    @Test
    void testUnderscore() throws Exception {

        testDecode("_", "_");

        testDecode("_", " ", true);

    }

    @Test
    void testLongLine() throws Exception {

        testDecode("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx=\nx", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        testDecode("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx=\r\nx", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        testDecode("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx=\r\n", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    }
/*
    @Test
    void testBalh() throws Exception {
        testDecode("=A1=CD=C6=D0=D0KPI=B2=BB=D6=BB=CA=C7=C8=CB=C1=A6=D7=CA=D4=B4=B2=BF=C3=C5=", 'blah');
    }
    */
}
