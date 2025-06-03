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
 * $Id: InputStreamTest.groovy,v 1.3 2013/10/10 14:28:26 barbee Exp $
**/

import java.sql.DriverManager
import java.sql.ResultSet

import java.util.Properties

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
import org.blackmist.pantomime.content.InputStreamSource

class InputStreamTest {

    @Before
    void setup() {
        StreamMonitor.reset()
    }

    @Test
    void testBasic() throws Exception {
        def source = new InputStreamSource() {
            public InputStream getInputStream() {
                new FileInputStream(new File("data/0003.eml"));
            }
        };

        def messageSource = new InputStreamMessageSource(source, 100)

        def data = new byte[45]

        int read = messageSource.read(data)

        assert 45 == read

        assert 'From barbee@oliveoil.com Fri Jul 26 08:26:05 ' == new String(data, 0, read)

        /* number 2*/
        assert 50 == messageSource.read()

        read = messageSource.read(data)

        assert 45 == read
        
        assertEquals('013\nReturn-Path: <barbee@oliveoil.com>\nReceiv', new String(data, 0, read))

        messageSource.seek(91)

        data = new byte[4000]

        read = messageSource.read(data)

        assert 3800 == read
        
        def rest = new String(data, 0, read)

        assert rest.startsWith('ed: from smtp.darkfog.org')

        assert rest.endsWith('xmbalnx14oliveoilcom_--\n\n')

        messageSource.free()

        assert 0 == StreamMonitor.unclosedStreams()

    }

    @Test
    void testBasic2() throws Exception {
        def source = new InputStreamSource() {
            public InputStream getInputStream() {
                new FileInputStream(new File("data/0012.eml"));
            }
        };

        def messageSource = new InputStreamMessageSource(source)
        assert 2 == messageSource.getSubPartCount(new MimePath("0"))

        def message = messageSource.load()

        assert 2 == message.asMultipart().getSubPartCount()

        message.free()

        assert 0 == StreamMonitor.unclosedStreams()

    }


    @Test
    void test0003() throws Exception {

        def source = new InputStreamSource() {
            public InputStream getInputStream() {
                new FileInputStream(new File("data/0003.eml"));
            }
        };

        def messageSource = new InputStreamMessageSource(source)

        def message = messageSource.load()

        assert message.isMultipart();

        def multipart = message.asMultipart()

        assert multipart.isMultipartMixed();
        assert 2 == multipart.getSubPartCount()

        assert 'test' == message.getPlainBodyAsString()
        assert 4 == message.getPlainBodyAsString().length()
        assert 300 == message.getHtmlBodyAsString().length()
        assert message.getHtmlBodyAsString().startsWith('<html>')
        assert message.getHtmlBodyAsString().endsWith('test</div>\n</body>\n</html>')

        assert multipart.isMultipartMixed();

        Part alternative = multipart.getSubParts()[0]

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
        assertEquals('test\n', Util.streamToString(attachment.asSinglePart().getBody()))

        assert message.getPart(new MimePath("0.1")) instanceof Attachment
        
        messageSource.free()

        assert 0 == StreamMonitor.unclosedStreams()
    }
}
