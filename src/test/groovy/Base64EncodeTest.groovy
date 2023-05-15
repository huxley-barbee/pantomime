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
 * $Id: Base64EncodeTest.groovy,v 1.3 2013/09/19 19:17:22 barbee Exp $
**/

package org.blackmist.pantomime;

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class Base64EncodeTest {
 
    void testEncode(String plain, String crypt) throws Exception {

        ByteArrayInputStream bais =
            new ByteArrayInputStream(plain.getBytes())

        Base64EncodeInputStream base64 = new Base64EncodeInputStream(bais);

        byte[] bytes = new byte[8192];

        int bytesRead = base64.read(bytes);

        assertEquals(crypt, new String(bytes, 0, bytesRead));

        assert crypt.length() == bytesRead

    }

    @Test
    void testaaa() throws Exception {
        testEncode('aaa', 'YWFh')
    }

    @Test
    void testTest() throws Exception {
        testEncode('test\n', 'dGVzdAo=')
    }

    @Test
    void testaaaaa() throws Exception {
        testEncode('aaaaa', 'YWFhYWE=')
    }

    @Test
    void testaaaa() throws Exception {
        testEncode('aaaa', 'YWFhYQ==')
    }

    @Test
    void testEol() throws Exception {
        testEncode('abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz ', 'YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXogYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXogYWJj\r\nZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXog')
    }
    @Test
    void testLeviathan() throws Exception {
        testEncode('Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.', 'TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\r\nIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\r\ndGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\r\ndWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\r\nZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=')
    }

    @Test
    void testDeliveryFailure() throws Exception {
        testEncode("Reporting-MTA: dns; rcdn-iport-7.cisco.com\r\n\r\nFinal-Recipient: rfc822;nobody@nowhere.com\r\nAction: failed\r\nStatus: 5.0.0 (permanent failure)\r\nDiagnostic-Code: smtp; 5.1.2 - Bad destination host 'DNS Hard Error looking up nowhere.com (A):  domain has no A record' (delivery attempts: 0)\r\n", 'UmVwb3J0aW5nLU1UQTogZG5zOyByY2RuLWlwb3J0LTcuY2lzY28uY29tDQoNCkZpbmFsLVJlY2lw\r\naWVudDogcmZjODIyO25vYm9keUBub3doZXJlLmNvbQ0KQWN0aW9uOiBmYWlsZWQNClN0YXR1czog\r\nNS4wLjAgKHBlcm1hbmVudCBmYWlsdXJlKQ0KRGlhZ25vc3RpYy1Db2RlOiBzbXRwOyA1LjEuMiAt\r\nIEJhZCBkZXN0aW5hdGlvbiBob3N0ICdETlMgSGFyZCBFcnJvciBsb29raW5nIHVwIG5vd2hlcmUu\r\nY29tIChBKTogIGRvbWFpbiBoYXMgbm8gQSByZWNvcmQnIChkZWxpdmVyeSBhdHRlbXB0czogMCkN\r\nCg==')
    }

}
