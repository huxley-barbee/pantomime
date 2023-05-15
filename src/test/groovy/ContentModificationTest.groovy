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
 * $Id: ContentModificationTest.groovy,v 1.6 2013/09/27 18:14:08 barbee Exp $
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

class ContentModificationTest {

    @Test
    void testStringContent() throws Exception {

        def message = new FileMessageSource("data/0000.eml").load()

        assertEquals('test\n\n',  message.getPlainBodyAsString())

        message.asSinglePart().set('My New String Content \u4F60\u597D',
            'text/plain', 'utf-8')

        assertEquals('text/plain; charset="utf-8"',
            message.getContentType().getValue())
        assertEquals('My New String Content \u4F60\u597D',
            message.getPlainBodyAsString())
        assertEquals('TXkgTmV3IFN0cmluZyBDb250ZW50IOS9oOWlvQ==',
            Util.streamToString(message.asSinglePart().getTransferEncodedBody(), 'utf-8'))

        message.setPlainBody('Lorem ipsum dolor sit amet.')
        message.setHtmlBody('<p>Lorem ipsum dolor sit amet.</p>')

        assertEquals('Lorem ipsum dolor sit amet.', message.getPlainBodyAsString())
        assertEquals('<p>Lorem ipsum dolor sit amet.</p>', message.getHtmlBodyAsString())
        assertTrue(message.getTransferEncodedSize() > 0)
    }

    @Test
    void testFileContent() throws Exception {

        def message = new FileMessageSource("data/0000.eml").load()

        assertEquals('test\n\n',  message.getPlainBodyAsString())

        new File("build/fileContent").write('My New File Content \u4F60\u597D', 'utf-8')

        message.asSinglePart().set(new File('build/fileContent'), 'text/plain',
            'utf-8')

        assertEquals('text/plain; charset="utf-8"',
            message.getContentType().getValue())

        assertEquals('My New File Content \u4F60\u597D',
            message.getPlainBodyAsString())

        assertEquals('TXkgTmV3IEZpbGUgQ29udGVudCDkvaDlpb0=',
            Util.streamToString(message.asSinglePart().getTransferEncodedBody(), 'utf-8'))

        message.setPlainBody('Lorem ipsum dolor sit amet.')
        message.setHtmlBody('<p>Lorem ipsum dolor sit amet.</p>')

        assertEquals('Lorem ipsum dolor sit amet.', message.getPlainBodyAsString())
        assertEquals('<p>Lorem ipsum dolor sit amet.</p>', message.getHtmlBodyAsString())
        assertTrue(message.getTransferEncodedSize() > 0)
    }

    @Test
    void testInputStreamContent() throws Exception {

        def message = new FileMessageSource("data/0000.eml").load()

        assertEquals('test\n\n',  message.getPlainBodyAsString())

        message.asSinglePart().set(new InputStreamSource() {

            public InputStream getInputStream() {

                return new InputStream() {
                    private int index = 0
                    private byte[] content = 'My New Stream Content \u4F60\u597D'.getBytes('utf-8')

                    public int read() {
                        if  ( index < content.length ) {
                            int b = content[index];
                            index++;
                            return b;
                        } else {
                            return -1;
                        }
                    }
                }
            };

        }, 'text/plain', 'utf-8');

        assertEquals('text/plain; charset="utf-8"',
            message.getContentType().getValue())

        assertEquals('My New Stream Content \u4F60\u597D',
            message.getPlainBodyAsString())

        assertEquals('TXkgTmV3IFN0cmVhbSBDb250ZW50IOS9oOWlvQ==',
            Util.streamToString(message.asSinglePart().getTransferEncodedBody(), 'utf-8'))

        message.setPlainBody('Lorem ipsum dolor sit amet.')
        message.setHtmlBody('<p>Lorem ipsum dolor sit amet.</p>')

        assertEquals('Lorem ipsum dolor sit amet.', message.getPlainBodyAsString())
        assertEquals('<p>Lorem ipsum dolor sit amet.</p>', message.getHtmlBodyAsString())
        assertTrue(message.getTransferEncodedSize() > 0)
    }

    @Test
    void testAddAttachment() throws Exception {

        def message = Pantomime.alternative('from@from.com',
            'subject', 'plain', 'html', 'to@to.com')

        message.addAttachment('content', 'file.txt', 'text/plain')

        assert 1 == message.getAllAttachmentCount()

        assert 'content' == message.getAllAttachments()[0].asSinglePart().getBodyAsString()

    }
}
