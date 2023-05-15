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
 * $Id: Line.java,v 1.2 2013/08/27 16:39:17 barbee Exp $
**/

package org.blackmist.pantomime;

class Line {
    long position;
    String text;
    String ending;

    public long getEndOfLinePosition() {
        long number = position;
        if ( text != null ) {
            number += text.length();
        }
        if ( ending != null ) {
            number += ending.length();
        }
        return number;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("{ Position: ").append(position)
            .append(" Text: ");
        
        if ( text == null ) {
            builder.append("Null ");
        } else {
            int len = text.length();
            if ( len > 10 ) {
                len = 10;
            }
            builder.append("\"");
            builder.append(text.substring(0, len));
            builder.append("\" ");
            builder.append(text.length());
            builder.append(" chars ");
        }

        builder.append(" Ending: ");

        if ( ending == null ) {
            builder.append("Null");
        } else if ( ending.length() == 0 ) {
            builder.append("None");
        } else {
            for ( int index = 0; index < ending.length(); index++ ) {
                int num = ending.charAt(index);
                builder.append(num);
                builder.append(" ");
            }
        }

        builder.append(" }");

        return builder.toString();
    }
}

