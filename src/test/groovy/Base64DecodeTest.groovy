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
 * $Id: Base64DecodeTest.groovy,v 1.3 2013/09/19 19:17:22 barbee Exp $
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

class Base64DecodeTest {
 
    void testDecode(String crypt, String plain) throws Exception {

        ByteArrayInputStream bais =
            new ByteArrayInputStream(crypt.getBytes())

        Base64DecodeInputStream base64 = new Base64DecodeInputStream(bais);

        byte[] bytes = new byte[8192];

        int bytesRead = base64.read(bytes);

        assertEquals(plain, new String(bytes, 0, bytesRead));
        assert plain == new String(bytes, 0, bytesRead);

        assert plain.length() == bytesRead

    }

    @Test
    void testaaa() throws Exception {
        testDecode('YWFh', 'aaa')
    }

    @Test
    void testTest() throws Exception {
        testDecode('dGVzdAo=', 'test\n')
    }

    @Test
    void testTestWithNewLine() throws Exception {
        testDecode('dGVzdAo=\n', 'test\n')
    }

    @Test
    void testaaaaa() throws Exception {
        testDecode('YWFhYWE=', 'aaaaa')
    }

    @Test
    void testaaaa() throws Exception {
        testDecode('YWFhYQ==', 'aaaa')
    }

    @Test
    void testLeviathan() throws Exception {

        testDecode('TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=', 'Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.');
    }

    @Test
    void testDeliveryFailure() throws Exception {
        testDecode('UmVwb3J0aW5nLU1UQTogZG5zOyByY2RuLWlwb3J0LTcuY2lzY28uY29tDQoNCkZpbmFsLVJlY2lwaWVudDogcmZjODIyO25vYm9keUBub3doZXJlLmNvbQ0KQWN0aW9uOiBmYWlsZWQNClN0YXR1czogNS4wLjAgKHBlcm1hbmVudCBmYWlsdXJlKQ0KRGlhZ25vc3RpYy1Db2RlOiBzbXRwOyA1LjEuMiAtIEJhZCBkZXN0aW5hdGlvbiBob3N0ICdETlMgSGFyZCBFcnJvciBsb29raW5nIHVwIG5vd2hlcmUuY29tIChBKTogIGRvbWFpbiBoYXMgbm8gQSByZWNvcmQnIChkZWxpdmVyeSBhdHRlbXB0czogMCkNCg==', "Reporting-MTA: dns; rcdn-iport-7.cisco.com\r\n\r\nFinal-Recipient: rfc822;nobody@nowhere.com\r\nAction: failed\r\nStatus: 5.0.0 (permanent failure)\r\nDiagnostic-Code: smtp; 5.1.2 - Bad destination host 'DNS Hard Error looking up nowhere.com (A):  domain has no A record' (delivery attempts: 0)\r\n")
    }

    @Test
    void testHighBytes() throws Exception {
        ByteArrayInputStream bais =
            new ByteArrayInputStream("/9j/".getBytes());

        Base64DecodeInputStream base64 = new Base64DecodeInputStream(bais);

        byte[] bytes = new byte[8192];

        int bytesRead = base64.read(bytes);

        assert 3 == bytesRead

        assert -1 == bytes[0]
        assert -40 == bytes[1]
        assert -1 == bytes[2]

    }

    @Test
    void testIncompleteEncodedString() {

        ByteArrayInputStream bais =
            new ByteArrayInputStream("4tXE2NTFIMLEydTFzNjO2SE".getBytes());

        Base64DecodeInputStream base64 = new Base64DecodeInputStream(bais);

        byte[] bytes = new byte[8192];

        int bytesRead = base64.read(bytes);

        assertEquals('Будьте бдительны!', new String(bytes, 0, bytesRead, 'koi8-r'))
    }

    /* We have seen a case where a base64 encoded part has base64 encoded
     * text, like it's supposed to. This is followed by plain content,
     * like it's NOT supposed to. We'll ignore the plain content just
     * like JavaMail and mutt.
     */
    @Test
    void testTrailingPlainContent() throws Exception {
        testDecode('YWFhYWE=\r\nHello World', 'aaaaa');
    }

}
