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
 * $Id: ToJavaMailTest.groovy,v 1.5 2013/10/10 14:28:27 barbee Exp $
**/

import java.util.Properties

import jakarta.mail.Multipart
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

class ToJavaMailTest {

    @Before
    void setup() {
        StreamMonitor.reset()
    }


    /* Tests message with base64-encoded attachment. */
    @Test
    void test0003() throws Exception {

        def message = new FileMessageSource("data/0003.eml").load()

        def session = Session.getDefaultInstance(new Properties())

        def mime = message.toJavaMail(session)
        
        assertEquals("\"John Barbee (barbee)\" <barbee@oliveoil.com>",
            mime.getFrom()[0].toString())
        assertEquals("test 0800.", mime.getSubject())

        def content = mime.getContent()

        assert content instanceof Multipart

        def multipart = (Multipart)content

        assert 2 == multipart.getCount()

        def part1 = multipart.getBodyPart(0)
        def part2 = multipart.getBodyPart(1)

        content = part1.getContent()

        assert content instanceof Multipart

        multipart = (Multipart)content

        assert 2 == multipart.getCount()

        def part1_1 = multipart.getBodyPart(0)
        def part1_2 = multipart.getBodyPart(1)

        assertEquals("test\r\n", part1_1.getContent())
        assert part1_2.getContent().startsWith('<html>')

        assertEquals("test\n", part2.getContent())

        assert 0 == StreamMonitor.unclosedStreams()
    }

    /* Tests message with quoted-printable part */
    @Test
    void test0004() throws Exception {

        def message = new FileMessageSource("data/0004.eml").load()

        def session = Session.getDefaultInstance(new Properties())

        def mime = message.toJavaMail(session)

        def content = mime.getContent()

        assert content instanceof Multipart

        def multipart = (Multipart)content

        assert 2 == multipart.getCount()

        def part1 = multipart.getBodyPart(0)

        assertEquals("If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.\r\n", part1.getContent())

        assert 0 == StreamMonitor.unclosedStreams()
    }

}
