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
 * $Id: Header.java,v 1.12 2013/10/25 13:47:15 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an email header.
 * <p>
 * A header has the format <code>name: value</code>.
 *  <p>
 * A header ends with a \r\n.
 * <p>
 * If a header is longer than 76 characters, it must be folded.
 * <p>
 * A header name is not unique. There can be more than one value
 * for each name.
 * <p>
 * A header can only be in ASCII. Anything outside of ASCII must be
 * encoded for <b>transport</b>. If the header has characters with
 * code points above 976, Pantomime will use Base64 encoding. If the
 * code points are between 127 and 976, Pantomime will use Quoted Printable
 * encoding.
 * <p>
 * A header may have some subordinate fields.
 * <p>
 * For example, a simple header looks like this:<br/>
 * <code>Content-Disposition: attachment</code>
 * <p>
 * With subordinate fields, it might look like this:<br/>
 * <code>Content-Disposition: attachment; filename=&quot;test.txt&quot;
 * size=47</code>
 *
 * <p>
 * The fields are delimited by a ;. After the first semicolon, there are
 * name-value pairs that comprise the sub-fields.
 *
 * @see <a href="http://en.wikipedia.org/wiki/List_of_Unicode_characters">Unicode Code Points</a>
 * @see <a href="http://tools.ietf.org/html/rfc822#section-3.1">RFC 822 on header folding.
 * @see <a href="http://en.wikipedia.org/wiki/Base64">Wikipedia on Base64</a>
 * @see <a href="http://tools.ietf.org/html/rfc2045#section-6.8">RFC 2045 on use of Base64 in email.</a>
 * @see <a href="http://en.wikipedia.org/wiki/Quoted-printable">Wikipedia on Quoted Printable</a>
 * @see <a href="http://tools.ietf.org/html/rfc2045#section-6.7">RFC 2045 on use of Quote Printable in email.</a>
 */
public class Header {

    private String name;

    /* The folded, encoded value. */
    private String value;

    private HashMap<String, String> subFields =
        new HashMap<String, String>();
 
    private static Logger log = LoggerFactory.getLogger(Header.class.getName());

    private int getFourthQuestionMark(String string, int mark) {

        for ( int index = 0; index < 4; index++ ) {

            mark = string.indexOf("?", mark+1);

            if ( mark == -1 ) {
                return -1;
            }

        }

        return mark;
    }

    protected String decode(String string) {

        int mark = -1;

        if ( string == null ) {
            return null;
        }

        /* If there is an instance where one encoded field follows another
         * e.g.,  =?iso?Q?=AB?= =?iso?Q?=AB?=
         * that space in between was added by the encoding process.
         * It should be removed.
         */
        String previous = null;
        string = string.replaceAll("\\?= =\\?", "\\?==\\?");

        while ( !string.equals(previous) && ( mark = string.indexOf("=?", mark+1) ) > -1 ) {

            int start = mark;
            int end = getFourthQuestionMark(string, mark-1);
            String replacement;
            StringBuilder builder;
            previous = string;

            if ( end == -1 ) {
                return string;
            }

            if ( string.charAt(end+1) != '=' ) {
                continue;
            }

            replacement = _decode(string.substring(start, end+2));

            builder = new StringBuilder();

            builder.append(string.substring(0, start));
            builder.append(replacement);
            builder.append(string.substring(end+2));

            string = builder.toString();

            mark = -1;

        }

        return string;
    }

//      =?gb2312?B?z7XNs83L0MU=?=
    private String _decode(String string) {
        ByteArrayInputStream bais = null;
        InputStream stream = null;
        ByteArrayOutputStream baos = null;


        if ( string == null ) {
            return null;
        }

        if ( ! string.startsWith("=?") ) {
            return string;
        }

        try {
            
            String[] fields = string.split("\\?");
            byte[] buffer = new byte[1024];
            String encoding;
            String charset;
            String text;
            int bytesRead;

            if ( fields.length != 5 ) {
                return string;
            }

            charset = fields[1];
            encoding = fields[2].trim();
            text = fields[3];

            bais = new ByteArrayInputStream(text.getBytes());
            StreamMonitor.opened(this, bais);

            if ( encoding.equalsIgnoreCase("Q") ) {
                stream = new QuotedPrintableDecodeInputStream(bais, true);
            } else if ( encoding.equalsIgnoreCase("B") ) {
                stream = new Base64DecodeInputStream(bais);
            } else {
                return string;
            }

            StreamMonitor.opened(this, stream);

            baos = new ByteArrayOutputStream();
            StreamMonitor.opened(this, baos);

            while ( ( bytesRead = stream.read(buffer) ) > 0 ) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toString(charset);

        } catch (UnsupportedEncodingException e) {
            log.error("Unable to decode header.", e);
            return baos.toString();

        } catch (IOException e) {
            log.error("Unable to decode header.", e);
            return string;

        } finally {
            StreamUtility.close(this, bais);
            StreamUtility.close(this, baos);
            StreamUtility.close(this, stream);
        }

    }

    protected String unfold(String string) {

        if ( string == null ) {
            return null;
        }

        String[] lines = string.split("\n");
        StringBuilder builder = new StringBuilder();

        for ( int index = 0; index < lines.length; index++ ) {

            lines[index] = lines[index].replaceAll("\r", "");

            builder.append(lines[index]);

        }

        return builder.toString();
        
    }

    /**
     * Retrieves the encoded and folded value of this header, ready for
     * transport.
     */
    public String getTransferEncodedValue() throws PantomimeException {

        if ( value == null ) {
            return "";
        }

        return fold(encode(value));
    }

    /**
     * Retrieves the decoded and unfolded value of this header.
     */
    public String getValue() {
        return value;
     }
    
    /**
     * Gets the name of the header.
     */
    public String getName() {
     return name;
     }

    /**
     * Attempts to parse the header value as a number.
     * <p>
     * Returns -1 if value is null or not a integer.
     *
     */
    public int getValueAsInt() {

        if ( value != null ) {

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return -1;
            }

        } else {
            return -1;
        }
    }

    /**
     * Attempts to parse the header value as a date.
     *
     * Returns null if not a date.
     *
     */
    public Calendar getValueAsDate() {

        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzzz");

        try {

            cal.setTime(sdf.parse(value.trim()));

            return cal;

        } catch (ParseException e) {

            try {

                sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzzzz");

                cal.setTime(sdf.parse(value.trim()));

                return cal;

            } catch ( ParseException e2 ) {

                log.warn("Unable to parse date string " + value +
                    ". Defaulting to now.");

                cal.setTimeInMillis(System.currentTimeMillis());

                return cal;

            }

        } 

    }

    private void processEndOfSubFieldValue(String currentSubName,
        StringBuilder buffer) {

        if ( currentSubName == null ) {
            if ( ! subFields.containsKey("main") ) {
                subFields.put("main", buffer.toString());
            }
        } else {
            if ( ! subFields.containsKey(currentSubName) ) {
                subFields.put(currentSubName, buffer.toString());
            }
        }

        buffer.delete(0, buffer.length());

    }

    private static class SubFieldProfile {
        private boolean inQuotes = false;
        private boolean inQuotedValue = false;
        private char lastBreakChar = '\0';
        private char previousQuoteChar = '\0';
        private char previousChar = '\0';
    }

    private boolean tokenizeQuote(SubFieldProfile profile, char c,
        StringBuilder buffer, List<String> tokens) {

        if ( ! profile.inQuotes ) {

            profile.inQuotes = true;

            if ( profile.previousChar == '=' ) {
                profile.inQuotedValue = true;
            }

            profile.previousQuoteChar = profile.previousChar = c;
            return true;

        } else if ( profile.inQuotes  && ( profile.previousQuoteChar == c ) ) {

            profile.inQuotes = false;

            if ( profile.inQuotedValue ) {
                if ( buffer.length() > 0 ) {
                    tokens.add(buffer.toString());
                    buffer.delete(0, buffer.length());
                    profile.lastBreakChar = c;
                }
            }

            profile.previousChar = c;
            return true;
        }

        return false;

    }

    private List<String> tokenize(String value) {
        ArrayList<String> tokens = new ArrayList<String>();
        char[] chars = value.toCharArray();
        StringBuilder buffer = new StringBuilder();
        SubFieldProfile profile = new SubFieldProfile();

        for ( int index = 0; index < chars.length; index++ ) {

            char c = chars[index];
            profile.previousChar = '\0';

            if ( index > 0 ) {
                profile.previousChar = chars[index-1];
            }

            if ( ( c == '"' )  || ( c == '\'' ) ) {

                boolean cont = tokenizeQuote(profile, c, buffer, tokens);

                if ( cont ) {
                    continue;
                }

            }

            if ( Character.isWhitespace(c) || ( c == ';' ) || ( c == '=' ) ) {

                if ( ! profile.inQuotes ) {

                    if ( buffer.length() > 0 ) {

                        if ( c != profile.lastBreakChar ) {
                            tokens.add(buffer.toString());
                            buffer.delete(0, buffer.length());
                            profile.lastBreakChar = c;
                        } else {
                            buffer.append(c);
                        }
                    }

                    profile.previousChar = c;
                    continue;
                }
            }

            buffer.append(c);

            profile.previousChar = c;
        }

        tokens.add(buffer.toString());

        return tokens;
    }

    private void setSubFields(String value) {

        List<String> tokens = tokenize(value);

        for ( int index = 0; index < tokens.size(); index++ ) {

            if ( index == 0 ) {

                subFields.put("main", tokens.get(index).toLowerCase().trim());

            } else if ( (index+1) == tokens.size() ) {

                /* ignore dangling value */

            } else {

                String name = tokens.get(index).toLowerCase().trim();
                String stuff = tokens.get(index+1);
                subFields.put(name, stuff);
                index++;

            }

        }

    }

    /**
     * Returns the main value of this header.
     *
     * If the header is<br/>
     * <code>Content-Disposition: attachment;
     * filename=&quot;test.txt&quot;</code>
     * <p>
     * this will return <code>attachment</code>.
     */
    public String getMainField() {
        return subFields.get("main");
    }

    /**
     * Returns the main value of this header.
     *
     * If the header is<br/>
     * <code>Content-Disposition: attachment;
     * filename=&quot;test.txt&quot;</code>
     * <p>
     * and the given parameter is <code>filename</code>
     * this will return <code>test.txt</code>.
     */
    public String getSubField(String subName) {
        return subFields.get(subName);
    }

    private int indexOfNextAscii(char[] plain, int offset ) {
        int index = offset;

        while ( index < plain.length ) {

            if ( Character.codePointAt(plain, index) < 128 ) {
                return index;
            }

            index++;

        }

        return index;

    }

    private int indexOfNextNonAscii(char[] plain, int offset ) {
        int index = offset;

        while ( index < plain.length ) {

            if ( Character.codePointAt(plain, index) >= 128 ) {
                return index;
            }

            index++;

        }

        return index;

    }

    private int getBase64Size(int length) {

        int size = length * 4 / 3;

        int remainder = size % 3;

        return size + remainder;

    }

    private int getEndOfNonAsciiHeaderValueSegment(char[] chars, int offset,
        int codePoint) {

        int max = 76 - ( getName().length() + 12 + 2 );

        int end = indexOfNextAscii(chars, offset);

        while ( end > offset ) {

            String s = new String(chars, offset, (end - offset));
            int size;
            byte[] input;

            try {
                input = s.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Unable to get bytes.", e);
                input = s.getBytes();
            }

            if ( codePoint > 976 ) {
                size = getBase64Size(input.length);
            } else {
                size = input.length * 3;
            }

            if ( size <= max ) {
                return end;
            }

            end--;

        }

        return -1;

    }

    private int encodedNonAscii(char[] chars, int index, int codePoint,
        StringBuilder builder) throws IOException {

        String nonAsciiString;
        byte[] inputBytes;
        byte[] buffer;
        ByteArrayInputStream bais = null;
        InputStream encodeStream = null;
        int bytesRead;

        int end = getEndOfNonAsciiHeaderValueSegment(chars, index,
            codePoint);

        if ( end == -1 ) {
            end = chars.length;
        }

        nonAsciiString = new String(chars, index, (end-index));

        try {

            inputBytes = nonAsciiString.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to get header value in UTF-8.");
            inputBytes = nonAsciiString.getBytes();
        }

        try {

            bais = new ByteArrayInputStream(inputBytes);

            if ( codePoint > 976 ) {

                encodeStream = new Base64EncodeInputStream(bais);

            } else {

                encodeStream = new QuotedPrintableEncodeInputStream(bais);

            }

            StreamMonitor.opened(this, encodeStream);

            buffer = new byte[inputBytes.length*3];

            bytesRead = encodeStream.read(buffer);

            builder.append("=?utf-8?");

            if ( encodeStream instanceof Base64EncodeInputStream ) {
                builder.append("B");
            } else {
                builder.append("Q");
            }

            builder.append("?");
            builder.append(new String(buffer, 0, bytesRead, "utf-8"));
            builder.append("?=");

            return end;
        } finally {
            StreamUtility.close(this, bais);
            StreamUtility.close(this, encodeStream);
        }

    }

    private String encode(String plain) {


        char[] chars = plain.toCharArray();

        int index = 0;

        StringBuilder builder = new StringBuilder();

        while ( index < chars.length ) {

            int codePoint = Character.codePointAt(chars, index);

            if ( codePoint < 128 ) {

                int end = indexOfNextNonAscii(chars, index);

                builder.append(new String(chars, index, (end-index)));

                index = end;


            } else {

                try {
                    index = encodedNonAscii(chars, index, codePoint, builder);
                } catch (IOException e) {
                    log.error("Unable to encode header.", e);
                    break;
                }

            }

        }

        return builder.toString();

    }

    private void foldAtSemicolon(List<Character> buffer, char[] chars,
        FoldProfile profile, StringBuilder folded) {
        char next;
        List<Character> sub = buffer.subList(profile.lastBreak,
            profile.lastSemicolon+1);

        Character[] subChars = sub.toArray(new Character[sub.size()]);

        folded.append(primitize(subChars));
        profile.lastBreak = profile.lastSemicolon+1;
        /* check if there is a whitespace next */
        if ( (profile.lastSemicolon+1) < chars.length ) {
            next = chars[profile.lastSemicolon+1];
            if ( Character.isWhitespace(next) ) {
                folded.append("\r\n");
                folded.append(next);
                profile.lastBreak++;
            } else {
                folded.append("\r\n ");
            }
        } else {
                folded.append("\r\n ");
        }

        profile.lastSemicolon = profile.lastWhitespace = -1;

    }

    private void foldAtWhitespace(List<Character> buffer, FoldProfile profile,
        StringBuilder folded) {
        List<Character> sub = buffer.subList(profile.lastBreak,
            profile.lastWhitespace);
        Character[] subChars =
            sub.toArray(new Character[sub.size()]);

        folded.append(primitize(subChars));
        folded.append("\r\n");
        folded.append(buffer.get(profile.lastWhitespace));

        profile.lastBreak = profile.lastWhitespace+1;
        profile.lastSemicolon = profile.lastWhitespace = -1;

    }

    private void foldAtEncodingBorder(List<Character> buffer,
        FoldProfile profile, StringBuilder folded) {
        List<Character> sub = buffer.subList(profile.lastBreak, buffer.size());
        Character[] subChars = sub.toArray(new Character[sub.size()]);
        String currentBufferedLine = new String(primitize(subChars));
        int encodeBoundary = currentBufferedLine.indexOf("?==?");

        if ( encodeBoundary > -1 ) {
            encodeBoundary += 2;

            List<Character> sub2 =
                buffer.subList(profile.lastBreak,
                encodeBoundary + profile.lastBreak);

            Character[] subChars2 =
                sub2.toArray(new Character[sub2.size()]);

            folded.append(primitize(subChars2));
            folded.append("\r\n ");
            profile.lastBreak += encodeBoundary;

        }  else {

            /* there is no good place for us to break,
             * so we break at 76 chars and insert a new space.
             */
            folded.append(primitize(subChars)); 
            folded.append("\r\n ");
            profile.lastBreak = buffer.size();

        }

        profile.lastSemicolon = profile.lastWhitespace = -1;
    }

    private static class FoldProfile {
        private int lastSemicolon = -1;
        private int lastWhitespace = -1;
        private int lastBreak = 0;
    }

    private String fold(String value) {

        StringBuilder folded = new StringBuilder();
        ArrayList<Character> buffer = new ArrayList<Character>();

        char[] chars = value.toCharArray();

        boolean inQuotes = false;

        FoldProfile profile = new FoldProfile();

        List<Character> lastSegment;
        Character[] lastSegmentChars;


        for ( int index = 0; index < chars.length; index++ ) {

            char c = chars[index];

            buffer.add(c);

            if ( c == '"' ) {
                inQuotes = ! inQuotes;
            }

            if ( ( c == ';' ) && ( !inQuotes ) ) {
                profile.lastSemicolon = buffer.size()-1;
            }

            if ( Character.isWhitespace(c) && ( ! inQuotes ) ) {
                profile.lastWhitespace = buffer.size()-1;
            }

            if ( (buffer.size()-profile.lastBreak) >= 76 ) {

                if ( profile.lastSemicolon > -1 ) {
                    /* first we try to fold a line between subfields */
                    foldAtSemicolon(buffer, chars, profile, folded);

                } else if ( profile.lastWhitespace > -1 ) {
                    /* next we try to fold a line at a natural space */
                    foldAtWhitespace(buffer, profile, folded);

                } else {

                    /* next we try to fold between encoded parts */
                    foldAtEncodingBorder(buffer, profile, folded);

                }


            }

        }

        lastSegment = buffer.subList(profile.lastBreak, buffer.size());
        lastSegmentChars = lastSegment.toArray(new Character[lastSegment.size()]);
        folded.append(primitize(lastSegmentChars));


        return folded.toString();
    }

    private char[] primitize(Character[] chars) {
        char[] newChars = new char[chars.length];
        for (int index = 0; index < chars.length; index++ ) {
            newChars[index] = chars[index];
        }
        return newChars;
    }

    /**
     * Sets the header value with the given encoded, folded string.
     */
    public void setTransferEncodedValue(String  value) {
        this.value=decode(unfold(value));
        setSubFields(this.value);
    }
    
    /**
     * Sets the header value with the given decoded, un-folded string.
     */
    public void setValue(String  value) {

        if ( value == null ) {
            return;
        }

        this.value=value;
        setSubFields(value);
    }
    
    /**
     * Sets the name of this header.
     */
    public void setName(String  name) {
        this.name=name;
    }

    /**
     * Returns <code>name: value</code> as a string, the value is
     * decoded and un-folded.
     */
    public String toString() {
        return name + ": " + value;
    }
}

