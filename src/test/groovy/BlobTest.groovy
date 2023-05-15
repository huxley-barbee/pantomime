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
 * $Id: BlobTest.groovy,v 1.9 2015/05/27 10:47:30 barbee Exp $
**/

import java.sql.DriverManager
import java.sql.ResultSet

import java.util.Properties

import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class BlobTest {

    @Before
    void setup() {
        StreamMonitor.reset()
    }


    /* Test single part text/plain message from mutt. */
    @Test
    void test0003() throws Exception {

        Class.forName("com.mysql.jdbc.Driver");

        def connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pantomime","root","");

        def statement = connection.prepareStatement('select data from mail where id = 3')

        def resultSet = statement.executeQuery()

        resultSet.next()

        def blob = resultSet.getBlob(1)

        def message = new BlobMessageSource(blob).load()

        assert message.isMultipart();

        def multipart = message.asMultipart()

        assert multipart.isMultipartMixed();

        assert 2 == multipart.getSubPartCount()

        assert 4 == message.getPlainBodyAsString().length()
        assert 'test' == message.getPlainBodyAsString()
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
        

        resultSet.close()
        statement.close()
        connection.close()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0004() throws Exception {

        Class.forName("com.mysql.jdbc.Driver");

        def connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pantomime","root","");

        def statement = connection.prepareStatement('select data from mail where id = 4')

        def resultSet = statement.executeQuery()

        resultSet.next()

        def blob = resultSet.getBlob(1)

        def message = new BlobMessageSource(blob).load()

        assert message.isMultipart();

        assert message.asMultipart().isMultipartAlternative();

        assert 2 == message.asMultipart().getSubPartCount()

        assert 101 == message.getPlainBodyAsString().length()

        assert 'If you believe that truth=beauty, then surely mathematics is the most beautiful branch of philosophy.' == message.getPlainBodyAsString()

        resultSet.close()
        statement.close()
        connection.close()

        assert 0 == StreamMonitor.unclosedStreams()
    }

    @Test
    void test0005() {
        def message = new Message()

        message.asSinglePart().set('Eating manners', 'text/plain', 'utf-8')

        message.setSubject('Eating manners.')
        message.setFrom('dijon@bravo-cat.net')
        message.addToRecipient('marty@bravo-cat.net')

        Class.forName("com.mysql.jdbc.Driver");

        def connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pantomime","root","");

        def statement = connection.prepareStatement('insert into mail values(5, " ")')
        assert 1 == statement.executeUpdate()

        statement = connection.prepareStatement("select id, data from mail where id = 5", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)

        def resultSet = statement.executeQuery()

        resultSet.next()

        def blob = resultSet.getBlob(2)

        assert 1 == blob.length()

        def source = new BlobMessageSource(blob)

        def sourcedMessage = message.saveAs(source)

        assert 'Eating manners.' == sourcedMessage.getSubject()
        assert 254 <= blob.length()
        assert 256 >= blob.length()

        resultSet.updateBlob(2, blob)
        resultSet.updateRow()
        
        sourcedMessage.setSubject('Your eating manners are apocryphal.')
        sourcedMessage.save()

        assert 'Your eating manners are apocryphal.' == sourcedMessage.getSubject()
        assert 274 <= blob.length()
        assert 276 >= blob.length()

        resultSet.updateBlob(2, blob)
        resultSet.updateRow()

        resultSet.close()
        statement.close()

        statement = connection.prepareStatement("select id, data from mail where id = 5", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)

        resultSet = statement.executeQuery()

        resultSet.next()

        blob = resultSet.getBlob(2)

        assert 274 <= blob.length()
        assert 276 >= blob.length()

        resultSet.close()
        statement.close()

        connection.close()

        assert 0 == StreamMonitor.unclosedStreams()
    }
}
