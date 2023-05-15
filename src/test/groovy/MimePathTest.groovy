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
 * $Id: MimePathTest.groovy,v 1.3 2013/09/18 14:37:48 barbee Exp $
**/

import junit.framework.JUnit4TestAdapter

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import org.blackmist.pantomime.*

class MimePathTest {
 
    @Test
    void testBasic() throws Exception {

        MimePath path = new MimePath("0.0")

        assert "0.0" == path.toString()

        assert "0" == path.getParent().toString()

        assert null == path.getParent().getParent()

        MimePath path2 = new MimePath("0.0.0")

        assert "0.0.0" == path2.toString()

        assert "0.0.1" == path2.getNextSibling().toString()

        assert "0.1" == path2.getParent().getNextSibling().toString()

    }

}
