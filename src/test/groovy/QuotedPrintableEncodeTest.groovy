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
 * $Id: QuotedPrintableEncodeTest.groovy,v 1.1 2013/09/04 15:07:12 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class QuotedPrintableEncodeTest {
 
    void testEncode(String plain, String crypt) throws Exception {
        testEncode(plain, crypt, false)
    }

    void testEncode(String plain, String crypt, boolean header)
        throws Exception {

        def bais = new ByteArrayInputStream(plain.getBytes())

        def quotedPrintable = new QuotedPrintableEncodeInputStream(bais, header);

        byte[] bytes = new byte[8192];

        int bytesRead = quotedPrintable.read(bytes);

        assertEquals(crypt, new String(bytes, 0, bytesRead));

    }

    @Test
    void testSimple() throws Exception {

        testEncode("If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.", "If you believe that truth=3Dbeauty, then surely mathematics is the most bea=\r\nutiful branch of philosophy.");


    }

    @Test
    void testUnderscore() throws Exception {

        testEncode(" ", " ");

        testEncode(" ", "_", true);

    }

    @Test
    void testEol() throws Exception {

        testEncode("abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwx 1234567890", "abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwx =\r\n1234567890");
        testEncode("abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwxy 1234567890", "abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwx abcdefghijklmnopqrstuvwxy=\r\n 1234567890");

    }

}
