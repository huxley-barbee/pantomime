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
 * $Id: AddressTest.groovy,v 1.4 2015/06/09 15:36:00 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class AddressTest {
 
    @Test
    void testAddresses() throws Exception {

        def a001 = new Address('niceandsimple@example.com')

        assert 'niceandsimple' == a001.getLocal()
        assert 'example.com' == a001.getDomain()
        assert a001.isValid();

        def a002 = new Address('very.common@example.com')
        assert 'very.common' == a002.getLocal()
        assert 'example.com' == a002.getDomain()
        assert a002.isValid();

        def a003 = new Address('a.little.lengthy.but.fine@dept.example.com')
        assert 'a.little.lengthy.but.fine' == a003.getLocal()
        assert 'dept.example.com' == a003.getDomain()
        assert a003.isValid();

        def a004 =
            new Address('disposable.style.email.with+symbol@example.com')
        assert 'disposable.style.email.with+symbol' == a004.getLocal()
        assert 'example.com' == a004.getDomain()
        assert a004.isValid();

        def a005 = new Address('user@[IPv6:2001:db8:1ff::a0b:dbd0]')
        assert 'user' == a005.getLocal()
        assert '[IPv6:2001:db8:1ff::a0b:dbd0]' == a005.getDomain()
        assert a005.isValid();

        def a006 = new Address("\"much.more.unusual\"@example.com")
        assert '"much.more.unusual"' == a006.getLocal()
        assert 'example.com' == a006.getDomain()
        assert a006.isValid();

        def a007 = new Address("\"very.unusual.@.unusual.com\"@example.com")
        assert '"very.unusual.@.unusual.com"' == a007.getLocal()
        assert 'example.com' == a007.getDomain()
        assert a007.isValid();

        def a008 = new Address('"very.(),:;<>[]\".VERY.\"very@\\ \"very\".unusual\"@strange.example.com')
        assert '"very.(),:;<>[]\".VERY.\"very@\\ \"very\".unusual"' ==
            a008.getLocal()
        assert 'strange.example.com' == a008.getDomain()
        assert a008.isValid();

        def a009 = new Address("postbox@com")
        assert 'postbox' == a009.getLocal()
        assert 'com' == a009.getDomain()
        assert a009.isValid();

        def a010 = new Address("admin@mailserver1")
        assert 'admin' == a010.getLocal()
        assert 'mailserver1' == a010.getDomain()
        assert a010.isValid();

        def a011 = new Address('!#$%&\'*+-/=?^_`{}|~@example.org')
        assert '!#$%&\'*+-/=?^_`{}|~' == a011.getLocal()
        assert 'example.org' == a011.getDomain()
        assert a011.isValid();

        def a012 =
            new Address('"()<>[]:,;@\\\"!#$%&\'*+-/=?^_`{}| ~.a"@example.org')
        assert '"()<>[]:,;@\\\"!#$%&\'*+-/=?^_`{}| ~.a"' == a012.getLocal()
        assert 'example.org' == a012.getDomain()
        assert a012.isValid();

        def a013 = new Address("\" \"@example.org")
        assert '" "' == a013.getLocal()
        assert 'example.org' == a013.getDomain()
        assert a013.isValid();

        def a014 = new Address('robert <bob@domain.com>')
        assert 'bob' == a014.getLocal()
        assert 'domain.com' == a014.getDomain()
        assert 'robert' == a014.getPersonal()
        assert a014.isValid();

        def a015 = new Address('"robert smith" <bob@domain.com>')
        assert 'bob' == a015.getLocal()
        assert 'domain.com' == a015.getDomain()
        assert 'robert smith' == a015.getPersonal()
        assert a015.isValid();


        def a016 = new Address('"smith, robert" <bob@domain.com>')
        assert 'bob' == a016.getLocal()
        assert 'domain.com' == a016.getDomain()
        assert 'smith, robert' == a016.getPersonal()
        assert a016.isValid();

        def a017 = new Address('bob@domain.com (a comment)')
        assert 'bob' == a017.getLocal()
        assert 'domain.com' == a017.getDomain()
        assert 'a comment' == a017.getComment()
        assert a017.isValid();

        def a018 = new Address('"(bob)"@domain.com (a comment)')
        assert '"(bob)"' == a018.getLocal()
        assert 'domain.com' == a018.getDomain()
        assert 'a comment' == a018.getComment()
        assert a018.isValid();

        def a019 = new Address('<bob@domain.com> (a comment)')
        assert 'bob' == a019.getLocal()
        assert 'domain.com' == a019.getDomain()
        assert 'a comment' == a019.getComment()
        assert a019.isValid();

        def a020 = new Address('bob (a comment)')
        assert 'bob' == a020.getLocal()
        assert null == a020.getDomain()
        assert 'a comment' == a020.getComment()
        assert a020.isValid();

        def a021 = new Address('(Dear Friend)')
        assert null == a021.getLocal()
        assert null == a021.getDomain()
        assert 'Dear Friend' == a021.getComment()
        assert ! a021.isValid();

        def a023 = new Address('unlisted-recipients: ;')
        assert 'unlisted-recipients: ;' == a023.getLocal()
        assert null == a023.getDomain()
        assert a023.isValid();

        def a024 = new Address('Wankstuff Subscribers <>')
        assert 'Wankstuff Subscribers' == a024.getPersonal()
        assert null == a024.getLocal()
        assert null == a024.getDomain()
        assert ! a024.isValid();

        def a025 = new Address('Merchant account')
        assert 'Merchant account' == a025.getLocal()
        assert null == a025.getDomain()
        assert ! a025.isValid();

        def a026 = new Address('fatarch1@yahoo.com.')
        assert 'fatarch1' == a026.getLocal()
        assert 'yahoo.com.'== a026.getDomain()
        assert ! a026.isValid();
        
        def a027 = new Address('Subscriber@')
        assert 'Subscriber' == a027.getLocal()
        assert null == a027.getDomain()
        assert ! a027.isValid();
        
        def a028 = new Address('<Undisclosed Recipients@global-one.ru>')
        assert 'Undisclosed Recipients' == a028.getLocal()
        assert 'global-one.ru' == a028.getDomain()
        assert ! a028.isValid();
        
        def a029 = new Address('Procmail@informatik.rwth-aachen.de;')
        assert 'Procmail' == a029.getLocal()
        assert 'informatik.rwth-aachen.de;' == a029.getDomain()
        assert ! a029.isValid();
        
        def a030 = new Address('<To all our friends.>')
        assert 'To all our friends.' == a030.getLocal()
        assert null == a030.getDomain()
        assert ! a030.isValid()
         
        def a031 = new Address('<[10.4.21.1]@info1.domainserver.de>')
        assert '[10.4.21.1]' == a031.getLocal()
        assert 'info1.domainserver.de' == a031.getDomain()
        assert ! a031.isValid()

        def a032 = new Address('<Undisclosed Recipients>')
        assert 'Undisclosed Recipients' == a032.getLocal()
        assert null == a032.getDomain()
        assert ! a032.isValid()

        def a033 = new Address('<Undisclosed-Recipient: @smtp1.cluster1.telinco.net;>')
        assert 'Undisclosed-Recipient:' == a033.getLocal()
        assert 'smtp1.cluster1.telinco.net;' == a033.getDomain()
        assert ! a033.isValid()

        def a034 = new Address('wzoguzxbpf@p.d.vanderlinden@stud.far.ruu.nl')
        assert 'wzoguzxbpf@p.d.vanderlinden' == a034.getLocal()
        assert 'stud.far.ruu.nl' == a034.getDomain()
        assert 'wzoguzxbpf@p.d.vanderlinden@stud.far.ruu.nl' == a034.getRaw()
        assert ! a034.isValid()

        def a035 = new Address('    @vger.rutgers.edu')
        assert null == a035.getLocal()
        assert 'vger.rutgers.edu' == a035.getDomain()
        assert ! a035.isValid()

        def a036 = new Address('Undisclosed Recipients@daedalus.bfsmedia.com')
        assert 'Undisclosed Recipients' == a036.getLocal()
        assert 'daedalus.bfsmedia.com' == a036.getDomain()
        assert ! a036.isValid()

        def a037 = new Address('Undisclosed-Recipient: ;;@bruce-guenter.dyndns.org')
        assert 'Undisclosed-Recipient: ;;' == a037.getLocal()
        assert 'bruce-guenter.dyndns.org' == a037.getDomain()
        assert ! a037.isValid()

        def a038 = new Address('@bfsmedia.com')
        assert null == a038.getLocal()
        assert 'bfsmedia.com' == a038.getDomain()
        assert ! a038.isValid()

        def a039 = new Address('')
        assert null == a039.getLocal()
        assert null == a039.getDomain()
        assert ! a039.isValid()

        def a040 = new Address('brucebar_5@hotmail..com')
        assert 'brucebar_5' == a040.getLocal()
        assert 'hotmail..com' == a040.getDomain()
        assert ! a040.isValid()

        def a041 = new Address('"CoIectivos De Compras" CoIectivosDeCompras@hotmail.com')
        assert '"CoIectivos De Compras" CoIectivosDeCompras' == a041.getLocal()
        assert 'hotmail.com' == a041.getDomain()
        assert ! a041.isValid()

        def a042 = new Address('ªxÂÅ­x¤ä«ùªÌ')
        assert 'ªxÂÅ­x¤ä«ùªÌ' == a042.getLocal()
        assert null == a042.getDomain()
        assert a042.isValid()
        
        def a043 = new Address(':travis@vergewebdesign.com')
        assert ':travis' == a043.getLocal()
        assert 'vergewebdesign.com' == a043.getDomain()
        assert ! a043.isValid()

        def a044 = new Address('<Various@www.nakama.com.tw, nakama.com.tw>')
        assert 'Various' == a044.getLocal()
        assert 'www.nakama.com.tw, nakama.com.tw' == a044.getDomain()
        assert ! a044.isValid()

        def a045 = new Address('TTOOffrriieenndd<>')
        assert 'TTOOffrriieenndd' == a045.getPersonal()
        assert null == a045.getLocal()
        assert null == a045.getDomain()
        assert ! a045.isValid()

        def a046 = new Address('loper0180@msn.com<>')
        assert 'loper0180@msn.com' == a046.getPersonal()
        assert null == a046.getLocal()
        assert null == a046.getDomain()
        assert ! a046.isValid()

        def a047 = new Address('""<>')
        assert null == a047.getPersonal()
        assert null == a047.getLocal()
        assert null == a047.getDomain()
        assert ! a047.isValid()

        def a048 = new Address('HOMEBUSINESS@')
        assert null == a048.getPersonal()
        assert 'HOMEBUSINESS' == a048.getLocal()
        assert null == a048.getDomain()
        assert ! a048.isValid()

        def a049 = new Address('moved@.met')
        assert null == a049.getPersonal()
        assert 'moved' == a049.getLocal()
        assert '.met' == a049.getDomain()
        assert ! a049.isValid()

        def a050 = new Address('" <jyodey@bfsmedia.com>')
        assert null == a050.getPersonal()
        assert '" <jyodey' == a050.getLocal()
        assert 'bfsmedia.com>' == a050.getDomain()
        assert ! a050.isValid()

        def a051 = new Address('blah@Air Breather')
        assert null == a051.getPersonal()
        assert 'blah' == a051.getLocal()
        assert 'Air Breather' == a051.getDomain()
        assert ! a051.isValid()

        def a052 = new Address('bmbmg@biovax.leeds.ac.uk..leeds.ac.uk')
        assert null == a052.getPersonal()
        assert 'bmbmg' == a052.getLocal()
        assert 'biovax.leeds.ac.uk..leeds.ac.uk' == a052.getDomain()
        assert ! a052.isValid()

        def a053 = new Address('giciyfhpdyterzak@msn>com')
        assert null == a053.getPersonal()
        assert 'giciyfhpdyterzak' == a053.getLocal()
        assert 'msn>com' == a053.getDomain()
        assert ! a053.isValid()

        def a054 = new Address('bruceg@em.ca  ;')
        assert null == a054.getPersonal()
        assert 'bruceg' == a054.getLocal()
        assert 'em.ca  ;' == a054.getDomain()
        assert ! a054.isValid()

        def a055 = new Address('<design@bfsmedia.com')
        assert null == a055.getPersonal()
        assert 'design' == a055.getLocal()
        assert 'bfsmedia.com' == a055.getDomain()
        assert ! a055.isValid()

        def a056 = new Address('"bonsai@hatch.co.jp"<')
        assert 'bonsai@hatch.co.jp' == a056.getPersonal()
        assert null == a056.getLocal()
        assert null == a056.getDomain()
        assert ! a056.isValid()

        def a057 = new Address('bruce@bruce-guenter.dyndns.org (bruce@bruce-guenter.dyndns.org)')
        assert 'bruce' == a057.getLocal()
        assert 'bruce-guenter.dyndns.org' == a057.getDomain()
        assert 'bruce@bruce-guenter.dyndns.org' == a057.getComment()
        assert a057.isValid()

    }

    @Test
    void testLongList() throws Exception {

        def list = new ArrayList<String>()

        list.add('commander@cobra.com')
        list.add('president@gijoe.com')
        list.add('vice_president@gijoe.com')
        list.add('secretary@gijoe.com')

        def message = Pantomime.plain("from@from.com",
            "hi", "hi", list, null)

        assert "commander@cobra.com, president@gijoe.com, vice_president@gijoe.com,\r\n secretary@gijoe.com" == message.getFirstHeader('to').getTransferEncodedValue()

        def message2 = message.saveAs(new FileMessageSource("build/long.eml"))

        def addresses = message.getToRecipients()

        assert 4 == addresses.size()

        assert 'secretary@gijoe.com' == addresses[3]['email']

    }

}
