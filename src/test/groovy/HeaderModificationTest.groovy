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
 * $Id: HeaderModificationTest.groovy,v 1.6 2015/05/07 14:43:22 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class HeaderModificationTest {

    @Test
    void test0000() throws Exception {

        def message = new FileMessageSource("data/0000.eml").load()

        assert 1 == message.getHeaders('x-sieve').size()
        message.removeHeader('x-sieve')
        assert 0 == message.getHeaders('x-sieve').size()

        assert 1 == message.getHeaders('status').size()
        message.addHeader('status', 'rw')
        assert 2 == message.getHeaders('status').size()

        assert 2 == message.getHeaders('received').size()
        message.setHeader('received', 'got it')
        assert 1 == message.getHeaders('received').size()
        assert 'got it'  == message.getHeaders('received')[0].getValue()

    }

    @Test
    void testMessage() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"))

        def message = new FileMessageSource("data/0000.eml").load()

        message.setFrom('dijon@bravo-cat.net')
        message.setSubject('Rules of the house')

        def cal = Calendar.getInstance()

        cal.set(Calendar.YEAR, 1977)
        cal.set(Calendar.MONTH, 5)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        cal.set(Calendar.HOUR_OF_DAY, 15)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        message.setDate(cal);

        assert 'dijon@bravo-cat.net' == message.getSender().toString()
        assert 'Rules of the house' == message.getSubject()
        assert 'Wed Jun 22 15:00:00 EDT 1977' == message.getDate().getTime().toString();
    }

    @Test
    void testMessageToRecipients() throws Exception {
        def message = new FileMessageSource("data/0000.eml").load()

        def recipients = message.getToRecipients()

        assert 1 == recipients.size()
        assert '["barbee" <barbee@darkfog.org>]' == recipients.toString()

        message.addToRecipientStrings(['dijon@bravo-cat.net', 'marty@bravo-cat.net'])

        recipients = message.getToRecipients()
        assert 3 == recipients.size()
        assert '["barbee" <barbee@darkfog.org>, dijon@bravo-cat.net, marty@bravo-cat.net]' == recipients.toString()

        message.removeToRecipient('barbee@darkfog.org')

        recipients = message.getToRecipients()
        assert 2 == recipients.size()
        assert '[dijon@bravo-cat.net, marty@bravo-cat.net]' == recipients.toString()

        message.setToRecipientStrings(['marty@bravo-cat.net', 'nore@bravo-cat.net'])
        recipients = message.getToRecipients()
        assert 2 == recipients.size()
        assert '[marty@bravo-cat.net, nore@bravo-cat.net]' == recipients.toString()

    }

    @Test
    void testMessageCcRecipients() throws Exception {
        def message = new FileMessageSource("data/0000.eml").load()

        def recipients = message.getCcRecipients()

        assert 0 == recipients.size()

        message.addCcRecipientStrings(['dijon@bravo-cat.net', 'marty@bravo-cat.net'])

        recipients = message.getCcRecipients()
        assert 2 == recipients.size()
        assert '[dijon@bravo-cat.net, marty@bravo-cat.net]' == recipients.toString()

        message.removeCcRecipient('dijon@bravo-cat.net')

        recipients = message.getCcRecipients()
        assert 1 == recipients.size()
        assert '[marty@bravo-cat.net]' == recipients.toString()

        message.setCcRecipientStrings(['marty@bravo-cat.net', 'nore@bravo-cat.net'])
        recipients = message.getCcRecipients()
        assert 2 == recipients.size()
        assert '[marty@bravo-cat.net, nore@bravo-cat.net]' == recipients.toString()

    }


    @Test
    void testMessageBccRecipients() throws Exception {
        def message = new FileMessageSource("data/0000.eml").load()

        def recipients = message.getBccRecipients()

        assert 0 == recipients.size()

        message.addBccRecipientStrings(['dijon@bravo-cat.net', 'marty@bravo-cat.net'])

        recipients = message.getBccRecipients()
        assert 2 == recipients.size()
        assert '[dijon@bravo-cat.net, marty@bravo-cat.net]' == recipients.toString()

        message.removeBccRecipient('dijon@bravo-cat.net')

        recipients = message.getBccRecipients()
        assert 1 == recipients.size()
        assert '[marty@bravo-cat.net]' == recipients.toString()

        message.setBccRecipientStrings(['marty@bravo-cat.net', 'nore@bravo-cat.net'])
        recipients = message.getBccRecipients()
        assert 2 == recipients.size()
        assert '[marty@bravo-cat.net, nore@bravo-cat.net]' == recipients.toString()

    }

    @Test
    void testSetRecipients() throws Exception {
        def message = new Message()

        def dijon = new Address('dijon@bravo-cat.net')
        def marty = new Address('marty@bravo-cat.net')

        def list = new ArrayList<Address>()

        list.add(dijon)
        list.add(marty)

        message.setToRecipients(list)
        message.setCcRecipients(list)
        message.setBccRecipients(list)
    }

    @Test
    void testSetRecipientStrings() throws Exception {
        def message = new Message()

        def dijon = 'dijon@bravo-cat.net'
        def marty = 'marty@bravo-cat.net'

        def list = new ArrayList<Address>()

        list.add(dijon)
        list.add(marty)

        message.setToRecipientStrings(list)
        message.setCcRecipientStrings(list)
        message.setBccRecipientStrings(list)
    }

    @Test
    void testaddRecipients() throws Exception {
        def message = new Message()

        def dijon = new Address('dijon@bravo-cat.net')
        def marty = new Address('marty@bravo-cat.net')

        def list = new ArrayList<Address>()

        list.add(dijon)
        list.add(marty)

        message.addToRecipients(list)
        message.addCcRecipients(list)
        message.addBccRecipients(list)
    }

    @Test
    void testaddRecipientStrings() throws Exception {
        def message = new Message()

        def dijon = 'dijon@bravo-cat.net'
        def marty = 'marty@bravo-cat.net'

        def list = new ArrayList<Address>()

        list.add(dijon)
        list.add(marty)

        message.addToRecipientStrings(list)
        message.addCcRecipientStrings(list)
        message.addBccRecipientStrings(list)
    }


}
