/**
 * Copyright (c) 2013-2015 <JH Barbee>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Initial Developer: JH Barbee
 *
 * For support, please see https://bitbucket.org/barbee/pantomime
 * 
 * $Id: Address.java,v 1.8 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import jakarta.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an email address.
 * <p>
 * An address comprises the following parts:
 * <ul>
 *      <li>personal
 *      <li>local
 *      <li>domain
 *      <li>comment
 * </ul>
 *
 * This is an example of a full email address.
 * <p>
 * <code>&quot;jh barbee&quot; <barbee@pantomime.org> (Developer)</code>
 * <p>
 * In this example the parts are the following:
 * <p>
 * <table>
 * <tr><td>personal</td><td>=&gt; jh barbee</td></tr>
 * <tr><td>local</td><td> =&gt; barbee</td></tr>
 * <tr><td>domain</td><td> =&gt; pantomime.org</td></tr>
 * <tr><td>comment</td><td>=&gt; Developer</td></tr>
 * </table>
 * <p>
 * By domain, we simply mean the stuff after the @ symbol. It could
 * be a fully-qualified hostname or IP address. We still call it a domain name.
 * <p>
 * The only required component is the local part.
 * <p>
 * This class will make a best-effort attempt at parsing any string provided,
 * constructing the address based on the component it can find.
 * <p>
 * Use {@link #isValid} to check if the address is RFC compliant.
 * <p>
 * @see <a href="http://en.wikipedia.org/wiki/Email_address">Wikipedia on Email Address</a>
 */
public class Address {

    private String personal;
    private String local;
    private String domain;
    private String comment;
    private String original;

    private String stringAndClear(StringBuilder builder) {

        String s;

        if ( builder.length() == 0 ) {
            return null;
        }

        s = builder.toString();
        builder.delete(0, builder.length());

        s = s.trim();

        if ( s.length() > 0 ) {
            return s;
        } else {
            return null;
        }
    }

    private static class Profile {
        private boolean inQuotes = false;
        private int angleStart = -1;
        private int angleEnd = -1;
        private int at = -1;
        private int parenStart = -1;
        private int parenEnd = -1;
        private char previousChar = '\0';
    }

    private void perChar(Profile profile, char c, int index) {

        if ( c == '"' ) {
            if ( profile.previousChar != '\\' ) {
                profile.inQuotes = ! profile.inQuotes;
            }
        }

        if ( c == '<' ) {
            if ( ! profile.inQuotes ) {
                profile.angleStart = index;
            }
        }

        if ( c == '@' ) {
            if ( index > profile.angleStart ) {

                /* Test case address 57,
                 * don't read @ that's in comment.
                 */
                if ( profile.parenStart == -1 ) {
                    profile.at = index;
                }
            }
        }

        if ( c == '>' ) {
            if ( profile.angleStart > -1 ) {
                profile.angleEnd = index;
            }
        }

        if ( c == '(' ) {
            if ( ! profile.inQuotes ) {
                if ( index > profile.angleEnd ) {
                    profile.parenStart = index;
                }
            }
        }

        if ( c == ')' ) {
            if ( index > profile.parenStart ) {
                profile.parenEnd = index;
            }
        }
    }

    /**
     * Constructs a new email address.
     */
    public Address(String address) {

        Profile profile = new Profile();
        char[] chars;

        if ( address == null ) {
            return;
        }

        if ( address.length() == 0 ) {
            return;
        }

        address = address.trim();

        original = address;

        chars = address.toCharArray();

        for ( int index = 0; index < chars.length; index++ ) {

            char c = chars[index];

            perChar(profile, c, index);
        }

        if ( profile.angleStart > -1 ) {
            assignPartsForAddressWithAngleBrackets(address, profile);
        } else {
            assignPartsForAddressWithoutAngleBrackets(address, profile);
        }

        cleanupParts();

    }

    private void assignPartsForAddressWithoutAngleBrackets(String address,
        Profile profile) {

        if ( profile.at > -1 ) {

            local = address.substring(0, profile.at);

            if ( profile.parenStart > -1 ) {

                domain = address.substring(profile.at+1, profile.parenStart);

                if ( profile.parenEnd > -1 ) {

                    /* address is blah@blah.com (blah) */
                    comment = address.substring(profile.parenStart+1,
                        profile.parenEnd);
                } else {
                    /* address is blah@blah.com (blah */
                    comment = address.substring(profile.parenStart+1);
                }

            } else {

                /* address is blah@blah.com */
                domain = address.substring(profile.at+1);
            }

        } else {

            if ( profile.parenStart > -1 ) {

                local = address.substring(0, profile.parenStart);

                if ( profile.parenEnd > -1 ) {
                    /* address is blah (blah) */
                    comment = address.substring(profile.parenStart+1,
                        profile.parenEnd);

                } else {
                    /* address is blah (blah */
                    comment = address.substring(profile.parenStart+1);
                }

            } else {

                /* address is blah */
                local = address;
            }

        }

    }

    private void assignPartsForAddressWithAngleBrackets(String address,
        Profile profile) {

        personal = address.substring(0, profile.angleStart);

        if ( profile.angleEnd > -1 ) {

            if ( ( profile.at > profile.angleStart ) &&
                ( profile.at < profile.angleEnd) ) {

                /* address is blah <blah@blah.com> */

                local = address.substring(profile.angleStart+1, profile.at);
                domain = address.substring(profile.at+1, profile.angleEnd);

            } else {

                /* address is blah <blah> */
                local = address.substring(profile.angleStart+1,
                    profile.angleEnd);
            }


            if ( profile.parenStart > -1 ) {
                if ( profile.parenEnd > -1 ) {
                    
                    /* with a comment of (blah) */
                    comment = address.substring(profile.parenStart+1,
                        profile.parenEnd);
                } else {
                    /* with a comment of (blah */
                    comment = address.substring(profile.parenStart+1);
                }
            }

        } else {

            /* no closing > */
            if ( profile.at > -1 ) {

                /* address is blah <blah@blah.com */

                if ( profile.angleStart < profile.at ) {
                    local = address.substring(profile.angleStart+1, profile.at);
                    domain = address.substring(profile.at+1);
                }

            } else {

                /* address is blah <blah */
                local = address.substring(profile.angleStart+1);
            }
        }
    }

    private void cleanupParts() {

        if ( personal != null ) {

            personal = personal.trim();

            if ( personal.length() == 0 ) {
                personal = null;
            } else {

                if ( personal.charAt(0) == '"' ) {
                    personal = personal.substring(1);
                }

                if ( personal.charAt(personal.length()-1) == '"' ) {
                    personal = personal.substring(0, personal.length()-1);
                }

                personal = personal.trim();

                if ( personal.length() == 0 ) {
                    personal = null;
                }
            }

        }

        if ( local != null ) {
            local = local.trim();

            if ( local.length() == 0 ) {
                local = null;
            }
        }

        if ( domain != null ) {
            domain = domain.trim();

            if ( domain.length() == 0 ) {
                domain = null;
            }
        }

        if ( comment != null ) {
            comment = comment.trim();

            if ( comment.length() == 0 ) {
                comment = null;
            }
        }

    }

    private void _setDomain(StringBuilder buffer) {
        if ( domain != null ) {
            return;
        }
        domain = stringAndClear(buffer);
    }
    
    private void _setLocal(StringBuilder buffer) {
        if ( local != null ) {
            return;
        }
        local = stringAndClear(buffer);
    }
 
    /**
     * Gets personal part of an email address.
     */
    public String getPersonal() {
        return personal;
    }

    /**
     * Gets local part of an email address.
     */
    public String getLocal() {
        return local;
    }

    /**
     * Gets domain part of an email address.
     */
    public String getDomain() {
        return domain;
    }
 
    /**
     * Gets comment part of an email address.
     */
    public String getComment() {
        return comment ;
    }

    /**
     * Returns the original string passed into the constructor.
     */
    public String getRaw() {
        return original;
    }

    /**
     * Returns the local part + @ + domain part.
     */
    public String getEmail() {
        return getLocal() + "@" + getDomain();
    }
 
    /**
     * Returns a full email address with 
     * &quot; personal &quot; + &lt; local + @ + domain &gt; + ( comment )
     */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        if ( personal != null ) {
            builder.append("\"").append(personal).append("\" <");
        }

        if ( local != null ) {
            builder.append(local);
        }

        if ( domain != null ) {
            builder.append("@").append(domain);
        }

        if ( personal != null ) {
            builder.append(">");
        }

        if ( comment != null ) {
            builder.append(" (").append(comment).append(")");
        }

        return builder.toString();

    }

    /**
     * Returns true if this email address is RFC compliant.
     */
    public boolean isValid() {
        try {
            new InternetAddress(original);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equals(Object o) {

        if ( o == null ) {
            return false;
        }

        return toString().equals(o.toString());
    }
}

