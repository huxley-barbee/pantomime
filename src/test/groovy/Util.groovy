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
 * $Id: Util.groovy,v 1.6 2013/10/10 14:28:27 barbee Exp $
**/

import org.blackmist.pantomime.StreamUtility

class Util {

    static def streamToString(InputStream stream) {
        return streamToString(stream, null)
    }

    static def streamToString(InputStream stream, String charset) {

        byte[] data = new byte[8192];

        int bytesRead = stream.read(data);

        StreamUtility.close(this, stream);

        if ( bytesRead >= 0 ) {

            if ( charset == null ) {
                return new String(data, 0, bytesRead)
            } else {
                return new String(data, 0, bytesRead, charset)
            }


        } else {

            return null;

        }

    }

    static def streamToBytes(InputStream stream) {
        byte[] data = new byte[8192];
        int bytesRead = 0;
        def baos = new ByteArrayOutputStream()

        while ( ( bytesRead = stream.read(data) ) > 0 ) {
            baos.write(data, 0, bytesRead)
        }

        return baos.toByteArray()

    }
    
    static def getSize(part) {

        if ( part.isMultipart() ) {
            part.specializeAsSinglePart();
        }

        def stream = part.asSinglePart().getBody()
        long size = streamToBytes(stream).length

        StreamUtility.close(this, stream)

        return size
    }
 
    static def getSize(InputStream stream) {
        int size = streamToBytes(stream).length
        StreamUtility.close(this, stream)
        return size
    }
 
}
