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
 * $Id: HeaderTest.groovy,v 1.3 2013/09/12 13:56:14 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class HeaderTest {
 
    @Test
    void testDateWithoutDayOfWeek() throws Exception {

        def header = new Header()
        header.setName('Date')
        header.setValue('18 Oct 2015 01:01:16 -0400')

        assert 'Sun Oct 18 01:01:16 EDT 2015' == header.getValueAsDate().getTime().toString()

    }

    @Test
    void testBrokenDate() throws Exception {

        def header = new Header()
        header.setName('Date')
        header.setValue('Not a date string at all.')

        def cal = Calendar.getInstance()


        assert cal.getTime().toString() == header.getValueAsDate().getTime().toString()

    }

    @Test
    void testBasic() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setValue('blah')

        assert 'blah' == header.getName()
        assert 'blah' == header.getValue()

    }

    @Test
    void testTransferEncoding1() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setValue('\u4F60\u597D')

        assert 'blah' == header.getName()
        assert '\u4F60\u597D' == header.getValue()
        assert '=?utf-8?B?5L2g5aW9?=' == header.getTransferEncodedValue()

    }

    @Test
    void testTransferEncoding2() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setTransferEncodedValue('=?utf-8?B?5L2g5aW9?=')

        assert 'blah' == header.getName()
        assert '\u4F60\u597D' == header.getValue()
        assert '=?utf-8?B?5L2g5aW9?=' == header.getTransferEncodedValue()

    }

    @Test
    void testTransferEncoding3() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setValue('\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D')

        assert 'blah' == header.getName()
        assert 115 == header.getTransferEncodedValue().length()
        assert '\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D' == header.getValue()
        assertEquals("=?utf-8?B?5L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW9?=\r\n =?utf-8?B?5L2g5aW95L2g5aW95L2g5aW95L2g5aW9?=", header.getTransferEncodedValue())

    }

    @Test
    void testTransferEncoding4() throws Exception {

        def header = new Header()
        header.setName('blah')

        header.setTransferEncodedValue("=?utf-8?B?5L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW9?=\r\n =?utf-8?B?5L2g5aW95L2g5aW95L2g5aW9?=")

        assert 'blah' == header.getName()
        assert 22 == header.getValue().length()
        assertEquals("=?utf-8?B?5L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW95L2g5aW9?=\r\n =?utf-8?B?5L2g5aW95L2g5aW95L2g5aW95L2g5aW9?=", header.getTransferEncodedValue())
        assert '\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D\u4F60\u597D' == header.getValue()

    }

    void testTransferEncoding5() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setValue('\u00A1Buenos D\u00EDas!')

        assert 'blah' == header.getName()
        assert '\u00A1Buenos D\u00EDas!' == header.getValue()
        assert '=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!' == header.getTransferEncodedValue()

    }

    @Test
    void testTransferEncoding6() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setTransferEncodedValue('=?utf-8?Q?=C2=A1Buenos D=C3=ADas!?=')

        assert 'blah' == header.getName()
        assert '\u00A1Buenos D\u00EDas!' == header.getValue()
        assert '=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!' == header.getTransferEncodedValue()

    }

    @Test
    void testTransferEncoding7() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setValue('\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!')

        assert 'blah' == header.getName()
        assert '\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!' == header.getValue()
        assert 241 == header.getTransferEncodedValue().length()
        assertEquals("=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!", header.getTransferEncodedValue());

    }

    @Test
    void testTransferEncoding8() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setTransferEncodedValue("=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!");

        assert 'blah' == header.getName()
        assert 65 == header.getValue().length()
        assertEquals('\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!\u00A1Buenos D\u00EDas!', header.getValue())
        assertEquals("=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos\r\n D=?utf-8?Q?=C3=AD?=as!=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!", header.getTransferEncodedValue())

    }

    @Test
    void testTransferEncoding9() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setValue('\u4F60\u597D; filename="\u00A1Buenos D\u00EDas!"')

        assert 'blah' == header.getName()
        assert '\u4F60\u597D; filename="\u00A1Buenos D\u00EDas!"' == header.getValue()
        assert "=?utf-8?B?5L2g5aW9?=;\r\n filename=\"=?utf-8?Q?=C2=A1?=Buenos D=?utf-8?Q?=C3=AD?=as!\"" == header.getTransferEncodedValue()

        assert '\u4F60\u597D' == header.getMainField();

        assert '\u00A1Buenos D\u00EDas!' == header.getSubField('filename')

    }

    @Test
    void testSubFields1() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setTransferEncodedValue("attachment; filename=\"test.txt\"; size=5;\n\tcreation-date=\"Fri, 26 Jul 2013 12:05:26 GMT\";\n\tmodification-date=\"Fri, 26 Jul 2013 12:05:26 GMT\"")

        assertEquals("attachment; filename=\"test.txt\"; size=5;\tcreation-date=\"Fri, 26 Jul 2013 12:05:26 GMT\";\tmodification-date=\"Fri, 26 Jul 2013 12:05:26 GMT\"", header.getValue())

        assertEquals("attachment", header.getMainField())
        assertEquals("test.txt", header.getSubField("filename"))
        assertEquals("5", header.getSubField("size"))
        assertEquals("Fri, 26 Jul 2013 12:05:26 GMT", header.getSubField("creation-date"))
        assertEquals("Fri, 26 Jul 2013 12:05:26 GMT", header.getSubField("modification-date"))

        assertEquals("attachment; filename=\"test.txt\"; size=5;\r\n\tcreation-date=\"Fri, 26 Jul 2013 12:05:26 GMT\";\r\n\tmodification-date=\"Fri, 26 Jul 2013 12:05:26 GMT\"", header.getTransferEncodedValue())

    }

    @Test
    void testSubFields2() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setTransferEncodedValue("DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple;\r\n d=oliveoil.com; i=@oliveoil.com; l=1445; q=dns/txt; s=iport;\r\n t=1374840345; x=1376049945;\r\n h=from:to:subject:date:message-id:mime-version;\r\n bh=5Ov/R6NMnVpyEr0Sa4zwSX2uF9Kk6FSjxJ8ziDlfhwQ=;\r\n b=ZYNTC701QPgVwEchNfZRXS9xfcqidtaoJQYLx3YcwpAsWHMffKryJ4pi\r\n HMTgTlfbmm+GvBKKDB3WMOEBd45LsBcB+MQplpuVBmoikb90Lb/K5EcZ1\r\n 1jSW2O9Te3DccbC7DQt2+BrntyJbJm6dMjUwOjbFAYW4QGkLNi3XLySvv\r\n o=;");

        assertEquals("DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple; d=oliveoil.com; i=@oliveoil.com; l=1445; q=dns/txt; s=iport; t=1374840345; x=1376049945; h=from:to:subject:date:message-id:mime-version; bh=5Ov/R6NMnVpyEr0Sa4zwSX2uF9Kk6FSjxJ8ziDlfhwQ=; b=ZYNTC701QPgVwEchNfZRXS9xfcqidtaoJQYLx3YcwpAsWHMffKryJ4pi HMTgTlfbmm+GvBKKDB3WMOEBd45LsBcB+MQplpuVBmoikb90Lb/K5EcZ1 1jSW2O9Te3DccbC7DQt2+BrntyJbJm6dMjUwOjbFAYW4QGkLNi3XLySvv o=;", header.getValue());

        assertEquals("DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple; d=oliveoil.com;\r\n i=@oliveoil.com; l=1445; q=dns/txt; s=iport; t=1374840345; x=1376049945;\r\n h=from:to:subject:date:message-id:mime-version;\r\n bh=5Ov/R6NMnVpyEr0Sa4zwSX2uF9Kk6FSjxJ8ziDlfhwQ=;\r\n b=ZYNTC701QPgVwEchNfZRXS9xfcqidtaoJQYLx3YcwpAsWHMffKryJ4pi\r\n HMTgTlfbmm+GvBKKDB3WMOEBd45LsBcB+MQplpuVBmoikb90Lb/K5EcZ1\r\n 1jSW2O9Te3DccbC7DQt2+BrntyJbJm6dMjUwOjbFAYW4QGkLNi3XLySvv o=;", header.getTransferEncodedValue());
    }

    @Test
    void testMixedQuotedPrintable() throws Exception {

        def header = new Header()
        header.setName('blah')
        header.setTransferEncodedValue("=?utf-8?B?5pe25Yib572R57uc6K+a6YKA5Luj55CG?=,=?utf-8?B?6LWa5Y+W?=40%=?utf-8?\r\n B?5Yip5ram?=,\r\n  =?GB2312?B?u7m/ydO109DSu7j2yKvQwrXEVkNQz/rK28a9zKgu?=")

        assert header.getName() == 'blah'
        assert header.getValue().length() > 0

    }
    
}
