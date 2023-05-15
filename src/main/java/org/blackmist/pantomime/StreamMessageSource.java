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
 * $Id: StreamMessageSource.java,v 1.12 2013/10/11 15:45:00 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class StreamMessageSource implements MessageSource {

    private static final byte CARRIAGE_RETURN = (byte)13;
    private static final byte LINE_FEED = (byte)10;

    private static final Logger log =
        LoggerFactory.getLogger(StreamMessageSource.class.getName());

    private long position = 0;
    private ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private byte[] readBuffer = new byte[80];
    private Hashtable<MimePath, MetaDatum> metaData =
        new Hashtable<MimePath, MetaDatum>();

    abstract int read(byte[] data) throws PantomimeException ;
    abstract int read() throws PantomimeException;
    abstract long getLength() throws PantomimeException;

    private File tempdir;

    /**
     * Directory where this message source can store a message
     * temporarily before writing back to storage.
     */
    public void setTempDir(File temp) {
        tempdir = temp;
    }

    protected File getTempFile() {
        File temp;

        if ( tempdir == null ) {
            tempdir = new File(System.getProperty("java.io.tmpdir"));
        }
        
        temp = new File(tempdir, "Pantomime-" +
            UUID.randomUUID().toString() + ".eml");

        return temp;
    }

    protected SourcedMessage init() throws PantomimeException {

        SourcedMessage message;

        MimePath root = new MimePath("0");
        setPosition(root, PositionType.PART_START, 0L);

        message = (SourcedMessage)getPart(root);

        return message;

    }

    /**
     * (Internal Use.) Gets the preamble of the MIME part for the given
     * MimePath.
     */
    public String getPreamble(MimePath path) throws PantomimeException {
        /* Preamble is from where the header ends to the first boundary. */
        long headerEnd = getHeaderEnd(path);

        String boundaryString = getBoundary(path);

        Boundary firstBoundary;
        
        seek(headerEnd);
        
        firstBoundary = getNextBoundary(boundaryString);

        int length = (int)(firstBoundary.line.position - headerEnd);

        byte[] data = new byte[length];

        int bytesRead = 0;

        seek(headerEnd);

        bytesRead = read(data);

        return new String(data, 0, bytesRead);
    }

    /**
     * (Internal Use.) Gets the epilogue of the MIME part for the given MimePath
     */
    public String getEpilogue(MimePath path) throws PantomimeException {
        /* Epilogue is from last boundary to end of part. */
        long headerEnd = getHeaderEnd(path);

        long bodyEnd = getBodyEnd(path);

        String boundaryString = getBoundary(path);

        Boundary boundary;
        Boundary previousBoundary = null;

        int length;

        byte[] data;

        int bytesRead = 0;

        long endOfBoundary;
        
        seek(headerEnd);

        while ( ( boundary = getNextBoundary(boundaryString) ) != null ) {
            previousBoundary = boundary;
        }

        if ( previousBoundary == null ) {
            return "";
        }
        
        endOfBoundary = previousBoundary.line.getEndOfLinePosition();

        length = (int)(bodyEnd - endOfBoundary);

        if ( length <= 0 ) {
            return "";
        }

        data = new byte[length];

        seek(endOfBoundary);

        bytesRead = read(data);

        return new String(data, 0, bytesRead);
    }

    protected long getPosition() {
        return position;
    }

    private boolean hasPosition(MimePath path, PositionType type) {

        MetaDatum datum;

        if ( path == null ) {
            return false;
        }

        datum = metaData.get(path);

        if ( datum == null ) {
            return false;
        }

        if ( type == PositionType.PART_START ) {
            if ( datum.partStart == -1 ) {
                return false;
            } else {
                return true;
            }
        } else if ( type == PositionType.BODY_START ) {
            if ( datum.bodyStart == -1 ) {
                return false;
            } else {
                return true;
            }
        } else if ( type == PositionType.HEADER_END ) {
            if ( datum.headerEnd == -1 ) {
                return false;
            } else {
                return true;
            }
        } else if ( type == PositionType.BODY_END ) {
            if ( datum.bodyEnd == -1 ) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void setPosition(MimePath path, PositionType type, long pos) {

        MetaDatum datum = metaData.get(path);

        if ( datum == null ) {
            datum = new MetaDatum();
            metaData.put(path, datum);
        }

        if ( type == PositionType.PART_START ) {
            datum.partStart = pos;
        } else if ( type == PositionType.BODY_START ) {
            datum.bodyStart = pos;
        } else if ( type == PositionType.HEADER_END ) {
            datum.headerEnd = pos;
        } else if ( type == PositionType.BODY_END ) {
            datum.bodyEnd = pos;
        }

    }
 
    private long getPosition(MimePath path, PositionType type) {

        MetaDatum datum = metaData.get(path);

        if ( datum == null ) {
            return -1;
        }

        if ( type == PositionType.PART_START ) {
            return datum.partStart;
        } else if ( type == PositionType.BODY_START ) {
            return datum.bodyStart;
        } else if ( type == PositionType.BODY_END ) {
            return datum.bodyEnd;
        } else if ( type == PositionType.HEADER_END ) {
            return datum.headerEnd;
        }

        return -1;

    }

    private void setBoundary(MimePath path, Line line) {

        MetaDatum datum = metaData.get(path);

        int boundaryStart;

        if ( line == null ) {
            return;
        }

        if ( datum == null ) {
            datum = new MetaDatum();
            metaData.put(path, datum);
        }

        ContentType contentType = new ContentType();
        
        boundaryStart = line.text.toLowerCase().indexOf("boundary");

        if ( boundaryStart == -1 ) {
            return;
        }

        contentType.setValue("placeholder; " +
            line.text.substring(boundaryStart));

        datum.boundary = contentType.getBoundary();

        if ( datum.boundary != null ) {
            datum.boundary = datum.boundary.replaceAll("\"", "");

//            /* Test Case 0081. Boundary set to "" */
//            if ( datum.boundary.length() == 0 ) {
//                datum.boundary = null;
//            }
        }

        log.info("Boundary for " + path.toString() + " is " + line + ".");

    }

    private String getBoundary(MimePath path) {

        MetaDatum datum = metaData.get(path);

        if ( datum == null ) {
            return null;
        }

        return datum.boundary;
    }

    private boolean boundaryIsAllDashes(String boundary) {

        for ( char c : boundary.toCharArray() ) {

            if ( c != '-' ) {
                return false;
            }

        }

        return true;

    }

    private Boundary getNextBoundary(String boundary)
        throws PantomimeException {

        /* The code is very liberal with boundary parsing.
         * It feels rather anything-goes.
         *
         * However, when the boundary is all dashes, things
         * go wrong. So we'll treat all-dash boundary more
         * strictly.
         */

        if ( boundary.length() == 0 ) {
            return getNextDashesBoundary(boundary);

        } else if ( boundaryIsAllDashes(boundary ) ) {

            return getNextDashesBoundary(boundary);

        } else {

            return getNextNonDashBoundary(boundary);

        }
    }

    private Boundary getNextDashesBoundary(String boundary)
        throws PantomimeException {

        Line line;

        String startBoundary = boundary + "--";
        String endBoundary = startBoundary + "--";

        while ( ( line = getLine() ) != null ) {

            if ( line.text == null ) {
                return null;
            }

            if ( line.text.equals(startBoundary) ) {
                Boundary result = new Boundary();
                result.line = line;
                result.isEndBoundary = false;

                return result;
            } else if ( line.text.equals(endBoundary) ) {
                Boundary result = new Boundary();
                result.line = line;
                result.isEndBoundary = true;

                return result;
            }

        }

        return null;
    }
 
    private Boundary getNextNonDashBoundary(String boundary)
        throws PantomimeException {

        Line line;
        Boundary result;

        if ( boundary == null ) {
            throw new IllegalArgumentException("Cannot skip to boundary " +
                "if there is no boundary.");
        }

        log.debug("boundary is supposed to be \"" + boundary + "\"");

        int kndex = 0;

        while ( ( line = getLine() ) != null ) {

            result = findBoundaryInLine(line, boundary);

            if ( result != null ) {
                return result;
            }

        }

        return null;
    }

    private String getStringAfterBoundary(Line line, int begin,
        String boundary, String beforeBoundary) {

        String afterBoundary;

        if ( line.text.length() ==
                ( beforeBoundary.length() + boundary.length() ) ) {

            afterBoundary = "";

        } else {
            afterBoundary = line.text.substring(begin+boundary.length());
            /* Test case 103, trailing space */
            afterBoundary = afterBoundary.trim();
        }

        return afterBoundary;

    }

    private Boundary findBoundaryInLine(Line line, String boundary) {
        boolean hasNonDashes = false;
        int begin = -1;
        Boundary result;
        String beforeBoundary;
        String afterBoundary;
        boolean contiguousDash = true;
        int contiguousDashCount = 0;

        begin = line.text.indexOf(boundary);

        if ( begin == -1 ) {
            return null;
        }

        /* Test case 102 */
        if ( ( begin == 0 ) && ( ! boundary.startsWith("-") ) ) {
            return null;
        }

        beforeBoundary = line.text.substring(0, begin);

        afterBoundary = getStringAfterBoundary(line, begin, boundary,
            beforeBoundary);

        for ( char c : beforeBoundary.toCharArray() ) {
            if ( c != '-' ) {
                hasNonDashes = true;
                break;
            }
        }

        if ( hasNonDashes ) {
            return null;
        }

        for ( char c : afterBoundary.toCharArray() ) {

            if ( c  == '-' ) {
                if ( contiguousDash ) {
                    contiguousDashCount++;
                }
            } else {
                contiguousDash = false;
                hasNonDashes = true;
                break;
            }

        }

        if ( hasNonDashes ) {
            if ( ! hasContiguousDashCount(contiguousDashCount) ) {
                return null;
            }
        }

        result = new Boundary();
        result.line = line;
        result.isEndBoundary = (contiguousDashCount > 0);

        log.info("found boundary at " + result);

        return result;
    }

    private boolean hasContiguousDashCount(int contiguousDashCount) {
        /* We're here for two cases:
         *
         * One is if a message has two boundaries and
         * some is a superset of the other.
         * e.g.,  --blah
         *        --blahblah
         *
         * The other is if there is no new newline between
         * the boundary and the header.
         *
         * According RFC 2046 this might be OK.
         * NOTE TO IMPLEMENTORS:  Boundary string comparisons must
         * compare the boundary value with the beginning of each
         * candidate line.  An exact match of the entire candidate
         * line is not required; it is sufficient that the boundary
         * appear in its entirety following the CRLF.
         *
         * Earlier in the RFC it states that what follows the boundary
         * may be whitespace.
         *
         * We cannot support both scenarios.
         *
         * We will do the following:
         * 1) Allow erroenous text after an end boundary.
         * 2) Prohibit everything else.
         */

        if ( contiguousDashCount == 0 ) {
            /* If there is no dash after the boundary,
             * this is not considered a boundary.
             */
            return false;
        } else {
            /* We recognized this as an end boundary.
             * The rest of the line will be ignored.
             */
            return true;
        }
    }

    private long getHeaderEnd(MimePath path) throws PantomimeException {

        if ( hasPosition(path, PositionType.HEADER_END) ) {
            return getPosition(path, PositionType.HEADER_END);
        }

        readHeaders(path);

        setPosition(path, PositionType.HEADER_END, position);

        log.info("Header end for " + path + " is " + position + ".");

        return getPosition(path, PositionType.HEADER_END);
    }

    private int getBoundarySize(MimePath path) {
        String boundary = getBoundary(path);
        if ( boundary == null ) {
            return 0;
        } else {
            return boundary.length()+4;
        }
    }

    private long getAntePartStart(MimePath path) throws PantomimeException {
        Boundary boundary = _getPartStart(path);

        if ( boundary == null ) {
            return -1;
        }

        return boundary.line.position - 1;
    }

    private Boundary _getPartStart(MimePath path) throws PantomimeException {
        MimePath parent;

        String boundaryString;
        Boundary boundary = null;
        int partIndex;

        if ( path == null ) {
            return boundary;
        }


        if ( path.equals("0") ) {
            return boundary;
        }

        partIndex = path.getChild();

        /* get the body start of the parent */
        parent = path.getParent();

        if ( parent == null ) {
            return boundary;
        }

        seek(getHeaderEnd(parent));

        boundaryString  = getBoundary(parent);

        if ( boundaryString == null ) {
            return boundary;
        }


        for ( int index = 0; index <= partIndex; index++ ) {

            boundary = getNextBoundary(boundaryString);

            if ( boundary == null ) {
                break;
            }

            if ( index == partIndex ) {
                break;
            }
        }

        if ( boundary != null ) {
            /* Take case of case 50 and 109 , consecutive boundaries. */
            Boundary nextBoundary = getNextBoundary(boundaryString);

            if ( nextBoundary != null ) {
                long eol = boundary.line.getEndOfLinePosition();
                if ( eol >= nextBoundary.line.position ) {
                    boundary = nextBoundary;
                }
            }
        }

        return boundary;
    }

    private long getPartStart(MimePath path) throws PantomimeException {
        long start = -1;

        if ( path == null ) {
            return -1;
        }

        if ( hasPosition(path, PositionType.PART_START) ) {
            return getPosition(path, PositionType.PART_START);
        }

        if ( path.equals("0") ) {
            start = 0;
        } else {

            Boundary boundary = _getPartStart(path);

            if ( boundary != null ) {
                start = boundary.line.getEndOfLinePosition();
            }
        }

        setPosition(path, PositionType.PART_START, start);

        log.info("Part start for " + path + " is " +
            getPosition(path, PositionType.PART_START) + ".");

        return getPosition(path, PositionType.PART_START);
    }

    protected long getBodyStart(MimePath path) throws PantomimeException {

        long headerEnd;
        String boundary;

        if ( hasPosition(path, PositionType.BODY_START) ) {
            return getPosition(path, PositionType.BODY_START);
        }

        headerEnd = getHeaderEnd(path);

        boundary = getBoundary(path);

        if ( boundary != null ) {

            Boundary nextBoundary = getNextBoundary(boundary);

            /* Test case 0027 is a message that claims to be multipart
             * that is really just a single part.
             *
             * It claims to have a boundary.
             * Content-Type: multipart/mixed; boundary="MimeMultipartBoundary"
             * but the boundary never shows up.
             *
             * JavaMail fails here with a MessagingException saying
             * missing start boundary.
             */
            if ( nextBoundary == null ) {
                setPosition(path, PositionType.BODY_START, headerEnd);

            } else {

                setPosition(path, PositionType.BODY_START,
                    nextBoundary.line.getEndOfLinePosition());

            }

        } else {

            setPosition(path, PositionType.BODY_START, headerEnd);

        }

        log.info("Body start for " + path + " is " +
            getPosition(path, PositionType.BODY_START) + ".");

        return getPosition(path, PositionType.BODY_START);

    }

    protected void seek(long newPosition) throws PantomimeException {
        position = newPosition;
    }

    protected long getBodyEnd(MimePath path) throws PantomimeException {

        Boundary boundary = null;
        MimePath parent = path.getParent();
        long uncleAntePartStart;
        
        /* Test case 25 there is no end boundary marker.
         * Let's default to eof.
         */
        long end = getLength();

        if ( hasPosition(path, PositionType.BODY_END) ) {
            return getPosition(path, PositionType.BODY_END);
        }

        if ( parent == null ) {
            end = getLength();
        } else {

            String boundaryString;
            int boundaryIndex = path.getChild() + 1;

            seek(getHeaderEnd(parent));

            boundaryString  = getBoundary(parent);

            if ( boundaryString == null ) {
                return getLength();
            }


            boundary = getNextBoundarySkipConsecutives(boundaryString,
                boundaryIndex);

            if ( boundary != null ) {
                end = getContentBefore(boundary);
            }

        }

        uncleAntePartStart = handleReusedBoundary(path, end);

        if ( uncleAntePartStart > -1 ) {
            end = uncleAntePartStart;
        }


        log.info("Body end for " + path + " is " + end + ".");

        setPosition(path, PositionType.BODY_END, end);

        return getPosition(path, PositionType.BODY_END);

    }

    private Boundary getNextBoundarySkipConsecutives(String boundaryString,
        int boundaryIndex) throws PantomimeException {

        Boundary previousBoundary = null;
        Boundary boundary = null;

        for ( int index = 0; index <= boundaryIndex; index++ ) {

            boundary = getNextBoundary(boundaryString);

            if ( boundary == null ) {
                break;
            }

            /* Take care of case 50 and 109 , consecutive boundaries. */
            if ( previousBoundary != null ) {
                long eol = previousBoundary.line.getEndOfLinePosition();
                if ( eol >= boundary.line.position ) {
                    index--;
                }
            }

            if ( index == boundaryIndex ) {
                break;
            }

            previousBoundary = boundary;
        }

        return boundary;

    }

    private long getContentBefore(Boundary boundary) throws PantomimeException {
        long end = -1;
        byte[] data = new byte[4];
        int bytesRead;

        /* index of start of boundary is boundary.line.position, */

        /* if there is a blank line before the boundary,
         * it is a delimiter between the body and the boundary.
         */

        seek(boundary.line.position-4);

        bytesRead = read(data);

        if ( bytesRead != 4 ) {
            /* XXX big problem */
        }

        if ( ( data[2] == LINE_FEED ) && ( data[3] == LINE_FEED ) ){

            /* \n\n--bound */

            end = boundary.line.position - 3;

        } else if (
            ( data[0] == CARRIAGE_RETURN ) &&
            ( data[2] == CARRIAGE_RETURN ) &&
            ( data[1] == LINE_FEED ) &&
            ( data[3] == LINE_FEED ) ) {
            /* \r\n\r\n--bound */


            end = boundary.line.position - 5;

        } else if (
            ( data[2] == CARRIAGE_RETURN ) &&
            ( data[3] == LINE_FEED ) ) {
            /* \r\n\r\n--bound */

            end = boundary.line.position - 3;

        } else {

            /* no new line at all */
            /* blah--bound */
            end = boundary.line.position - 1;

        }
    
        return end;
    }

    private long handleReusedBoundary(MimePath path, long bodyEnd)
        throws PantomimeException {

        MimePath parent = path.getParent();
        long uncleAntePartStart = -1;
        MimePath uncle = null;
        long bodyStart;

        if ( parent == null ) {
            return -1;
        }

        uncle = parent.getNextSibling();
        uncleAntePartStart = getAntePartStart(uncle);

        if ( uncleAntePartStart < 0 ) {
            return -1;
        }

        bodyStart = getPosition(path, PositionType.BODY_START);

        if ( uncleAntePartStart < bodyStart ) {

            /* For some reason, the part start of our next uncle
             * is earlier in the file than the start of our body.
             *
             * This should never happen. Unless! We have a case
             * as in Test 0051 where nested are reusing multiparts,
             * as prohibited by RFC 2046.
             *
             * The message is fairly broken, so it hardly matters
             * what we do to try to recover from this.
             */
            return -1;

        }

        if ( uncleAntePartStart < bodyEnd ) {
            return uncleAntePartStart - 1;
        }

        return -1;
    }

    /**
     * (Internal Use.) Gets the size of the body of the MIME part for the
     * given MimePath
     */
    public long getTransferEncodedBodySize(MimePath path)
        throws PantomimeException {

        long bodyStart = getBodyStart(path);
        long bodyEnd = getBodyEnd(path);

        return bodyEnd - bodyStart;
    }

    /**
     * (Internal Use.) Gets the size of the MIME part for the given MimePath.
     */
    public long getTransferEncodedSize(MimePath path)
        throws PantomimeException {

        if ( path.equals("0") ) {
            return getLength();
        } else {
            long partStart = getPartStart(path);
            long bodyEnd = getBodyEnd(path);

            return (bodyEnd - partStart);
        }
    }

    private int tallyBoundaries(String boundaryString, int partCount,
        int endBoundaryCount, Boundary previousBoundary)
        throws PantomimeException {

        /* If the boundary is all dashes, we just count the number of
         * start boundaries, RFC compliance.
         */
        if ( boundaryIsAllDashes(boundaryString) ) {
            return partCount - endBoundaryCount;
        }

        /* If there is 0 boundary, the part is the entire message.
         *
         * If there is 1 boundary,
         *
         *      and it's an end boundary, we don't know where the
         *      part starts, so we consider that 0.
         *
         *      and it's a start boundary, then the part start from
         *      there to the end of message as 1 part.
         *
         * If there are 2 or more boundaries
         *
         *      and there is one end boundary, this is a proper message
         *      where the end boundary marks the end of parts.
         *
         *      and there is no end boundary, this is an improper message
         *
         *          and the last boundary is the end of the file, we
         *          can safely assume that last boundary should be
         *          considered an end boundary.
         *
         *          and the last boundary is NOT the end of the file,
         *          we have no choice to count it as a valid sub part.
         */

        if ( partCount > 1 ) {

            if ( endBoundaryCount > 0 ) {
                partCount--;
            } else {

                if ( previousBoundary.line.getEndOfLinePosition() >= getLength() ) {
                    partCount--;
                }

            }

        } else if ( partCount == 1 ) {

            if ( endBoundaryCount == 1 ) {
                partCount = 0;
            }
        }

        return partCount;
    }

    /**
     * (Internal Use.) Returns the number of subordinate MIME parts in the
     * MIME part addressed by teh given MimePath.
     */
    public int getSubPartCount(MimePath path) throws PantomimeException {
        long headerEnd = getHeaderEnd(path);
        int partCount = 0;
        String boundaryString = getBoundary(path);
        int endBoundaryCount = 0;
        Boundary boundary = null;
        Boundary previousBoundary = null;

        if ( boundaryString == null ) {
            return partCount;
        }

        seek(headerEnd);

        /* We have cases like File Test 0025 where there is no end boundary.
         * We have cases like File TEst 0071 where the end boundary is used
         * instead of the start boundary.
         * We have cases like File Test 0062 where the start boundary is
         * used instead of the end boundary.
         */

        while ( ( boundary = getNextBoundary(boundaryString) ) != null ) {

            log.info("next boundary " + boundary);

            partCount++;

            if ( previousBoundary != null ) {
                /* Take case of case 50 and 109 , consecutive boundaries. */
                if ( previousBoundary.line.getEndOfLinePosition() >= boundary.line.position ) {
                    partCount--;
                }
            }

            previousBoundary = boundary;

            if ( boundary.isEndBoundary ) {
                endBoundaryCount++;
            }
        }

        log.info("partCount is " + partCount);
        log.info("endBoundary Count is " + endBoundaryCount);

        partCount = tallyBoundaries(boundaryString, partCount,
            endBoundaryCount, previousBoundary);

        log.info("Returning part count " + partCount + ".");

        return partCount;
    }

    private boolean isBoundaryDefinition(Line line, Line previousHeaderStart) {

        String previous = previousHeaderStart.text.toLowerCase();
        String current = line.text.toLowerCase().replaceAll(" ", "");


        if ( current.indexOf("boundary=") > -1 ) {

            if ( current.startsWith("content-type") ) {
                return true;
            }

            if ( previous.startsWith("content-type") ) {
                return true;
            }
        }

        return false;
    }

    private List<Line> readHeaders(MimePath path) throws PantomimeException {

        List<Line> headers = new ArrayList<Line>();
        boolean keepGoing = true;

        long partStart = getPartStart(path);

        int lastHeaderStart = 0;

        Line previousLine = new Line();
        Line previousHeaderStart = new Line();

        previousLine.ending = previousLine.text = "";
        previousHeaderStart.ending = previousHeaderStart.text = "";

        seek(partStart);

        /* read until blank line */

        while (keepGoing) {

            Line line = getLine();

            if ( line != null ) {
                log.debug(line.toString());
            } else {
                log.debug("Line null.");
            }

            if ( ( line == null ) || ( line.text == null ) ) {
                keepGoing = false;
                break;
            }

            if ( line.text.equals("") &&
                ( line.ending.equals("\r\n") || line.ending.equals("\n") ) &&
                ( ! previousLine.ending.equals("\r") ) ) {
                keepGoing = false;
                break;
            }

            if ( isBoundaryDefinition(line, previousHeaderStart) ) {
                setBoundary(path, line);
            }

            if ( line.text.toLowerCase().startsWith("content-disposition: ") ) {
                setIsAttachment(path, line);
            }

            headers.add(line);

            previousLine = line;

            if ( line.text.length() > 0 ) {
                if ( ! Character.isWhitespace(line.text.charAt(0) ) ) {
                    previousHeaderStart = line;
                }
            }
        }

        return headers;

    }

    private void setIsAttachment(MimePath path, Line header) {

        MetaDatum datum = metaData.get(path);

        if ( ( header == null ) || ( header.text == null ) ) {
            return;
        }

        if ( datum == null ) {
            datum = new MetaDatum();
            metaData.put(path, datum);
        }

        if ( header.text.matches("(?i)content-disposition:\\s*attachment.*") ) {
            datum.isAttachment = true;
        }
    }

    /**
     * (Internal Use.) Returns the MIME part addressed by the given MimePath.
     */
    public Part getPart(MimePath path) throws PantomimeException {

        Part part;
        String boundary = null;
        long partStart = getPartStart(path);

        List<Line> headers = readHeaders(path);

        if ( path.equals("0") ) {
            part = new SourcedMessage();
        } else {

            if ( metaData.get(path).isAttachment ) {
                part = new Attachment();
            } else {
                part = new Part();
            }
        }

        seek(partStart);

        part.setHeaders(headers);

        if ( metaData.containsKey(path) ) {
            boundary = metaData.get(path).boundary;
        }

        if ( boundary != null ) {
            part.muster(this, path, boundary);
        } else {
            part.muster(this, path);
        }

        return part;

    }

    private Line stringify() {
        Line line;
        int lineEndingSize = 0;
        byte[] data = lineBuffer.toByteArray();
        int length = 0;

        line = new Line();

        length = data.length;

        if ( length == 0 ) {
            line = new Line();
            line.text = "";
            line.ending = "";
            return line;
        }

        if ( data[length-1] == LINE_FEED ) {

            lineEndingSize++;

            if ( length == 1 ) {
                line.text = "";
                line.ending = "\n";
                return line;
            }


            if ( data[length-2] == CARRIAGE_RETURN ) {

                lineEndingSize++;

                if ( length == 2 ) {
                    line.text = "";
                    line.ending = "\r\n";
                    return line;
                }

            }

        }
        if ( data[length-1] == CARRIAGE_RETURN ) {
            lineEndingSize++;

            if ( length == 1 ) {
                line.text = "";
                line.ending = "\r";
                return line;
            }
        }

        line.position = position;
        line.text = new String(data, 0, length-lineEndingSize);
        line.ending = new String(data, length-lineEndingSize, lineEndingSize);

        /* need to deal with unicode here */

        return line;

    }

    private boolean isNextCharLineFeed(int index) throws PantomimeException {

        int b;

        if ( ( index + 1) < readBuffer.length ) {
            b = readBuffer[index+1];
        } else {
            b = read();
        }

        return b == 10;
    }

    private Line getLine() throws PantomimeException {

        Line line;
        int totalRead = 0;
        int bytesRead = 0;
        boolean readMore = true;

        while ( readMore ) {

            int lineEnding = 0;

            /* read 80 bytes of data */
            bytesRead = read(readBuffer);

            if ( bytesRead > 0 ) {

                /* got some data */
                totalRead += bytesRead;

                /* look for line ending */
                for ( int index = 0; index < bytesRead; index++ ) {

                    /* got line feed. end of the line */
                    if ( readBuffer[index] == LINE_FEED ) {

                        lineBuffer.write(readBuffer, 0, index+1);

                        readMore = false;

                        break;

                    }

                    /* got carriage return */
                    if ( readBuffer[index] == CARRIAGE_RETURN ) {

                        lineBuffer.write(readBuffer, 0, index+1);

                        /* now figure out if there is a line feed
                         * in the next buffer */
                        if ( isNextCharLineFeed(index) ) {
                            lineBuffer.write(LINE_FEED);
                        }

                        readMore = false;
                        break;
                        
                    }

                }

                if ( readMore ) {
                    /* no carriage return or line feed
                     * read and move on 
                     */
                    lineBuffer.write(readBuffer, 0, bytesRead);
                }

            } else {
                /* no more data available. there is no line ending */
                readMore = false;
            }
        }

        return compileLine(totalRead);
    }

    private Line compileLine(long totalRead) throws PantomimeException {
        if ( totalRead == 0 ) {
            return null;
        } else {

            Line line = stringify();

            seek(position + lineBuffer.size());

            /* erase our buffer */
            lineBuffer.reset();

            return line;
        }
    }

    private static class MetaDatum {
        long partStart = -1;
        long headerEnd = -1;
        long bodyStart = -1;
        long bodyEnd = -1;
        String boundary = null;
        boolean isAttachment;
    }

    private static class Boundary {
        /* index of char at start of boundary */
        boolean isEndBoundary;
        Line line;

        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("{ Line: ").append(line.toString())
                .append(" Is End: ").append(isEndBoundary).append(" }");

            return builder.toString();
        }
    }

    private enum PositionType {
        PART_START,
        HEADER_END,
        BODY_START,
        BODY_END 
    }
}
