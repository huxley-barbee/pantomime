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
 * $Id: MimePath.java,v 1.9 2013/10/09 14:06:13 barbee Exp $
**/

package org.blackmist.pantomime;

import java.util.ArrayList;
import java.util.List;

/**
 * A method for uniquely addressing each MIME part in a message.
 * <p>
 * Each MimePath uniquely identifies a MIME part within an email message.
 * Each <i>level</i> in the MIME structure is delimited by a &quot;.&quot;.
 * The indexing of MIME parts within a level is 0-based, so the first MIME
 * part in that <i>level</i> is 0 and the second is 1, and so on.
 * <p>
 * After a review of the following examples, MimePath will make sense.
 * <p>
 * Here is a simple message made up of a single text/plain part.
 * <p>
 * <pre>
 * a (text/plain)
 * </pre>
 * <table border=1>
 * <tr><th>Letter</th><th>Mime Path</th><th>Content Type</th></tr>
 * <tr><td>a</td><td>0</td><td>text/plain</td></tr>
 * </table>
 * <p>
 * Now let's look at a more complex example. Here is a multipart/alternative 
 * message with a text/plain and text/html part.
 * <p>
 * <pre>
 * a (multipart/alternative)
 *  \
 *  |-b (text/plain)
 *  |-c (text/html)
 * </pre>
 * <table border=1>
 * <tr><th>Letter</th><th>Mime Path</th><th>Content Type</th></tr>
 * <tr><td>a</td><td>0</td><td>multipart/alternative</td></tr>
 * <tr><td>b</td><td>0.0</td><td>text/plain</td></tr>
 * <tr><td>c</td><td>0.1</td><td>text/html</td></tr>
 * </table>
 * <p>
 *
 * Here is a text/plain message with a PDF attachment.
 * <p>
 * <pre>
 * a (multipart/mixed)
 *  \
 *  |-b (text/plain)
 *  |-c (application/pdf)
 * </pre>
 * <table border=1>
 * <tr><th>Letter</th><th>Mime Path</th><th>Content Type</th></tr>
 * <tr><td>a</td><td>0</td><td>multipart/mixed</td></tr>
 * <tr><td>b</td><td>0.0</td><td>text/plain</td></tr>
 * <tr><td>c</td><td>0.1</td><td>application/pdf</td></tr>
 * </table>
 * <p>
 *
 * For our final example we have a alternative plain and HTML content
 * with a PDF attachment.
 * <p>
 * <pre>
 * a (multipart/mixed)
 *  \
 *  |-b (multipart/alternative)
 *  |  \
 *  |  |-c (text/plain)
 *  |  |-d (text/html)
 *  |-e (application/pdf)
 * </pre>
 * <table border=1>
 * <tr><th>Letter</th><th>Mime Path</th><th>Content Type</th></tr>
 * <tr><td>a</td><td>0</td><td>multipart/mixed</td></tr>
 * <tr><td>b</td><td>0.0</td><td>multipart/alternative</td></tr>
 * <tr><td>c</td><td>0.0.0</td><td>text/plain</td></tr>
 * <tr><td>d</td><td>0.0.1</td><td>text/html</td></tr>
 * <tr><td>e</td><td>0.1</td><td>application/pdf</td></tr>
 * </table>
 * <p>
 */
public class MimePath {

    private List<Integer> path = new ArrayList<Integer>();
    private int cursor = 0;

    private ArrayList<Integer> breakup(String s) {
        String[] parts = s.split("\\.");

        ArrayList<Integer> list = new ArrayList<Integer>();

        for ( String part : parts ) {
            list.add(Integer.parseInt(part));
        }

        return list;
    }

    private MimePath(List<Integer> path) {
        this.path = path;
    }
 
    /**
     * Constructs a new MimePath of 0.
     */
    public MimePath() {
        path.add(0);
    }

    /**
     * Constructs a new MimePath with the given parent and child.
     */
    public MimePath(String parent, int child) {
        this(parent + "." + String.valueOf(child));
    }

    /**
     * Constructs a new MimePath with the given parent and child.
     */
    public MimePath(MimePath parent, int child) {
        path.addAll(parent.path);
        path.add(child);
    }

    /**
     * Constructs a new MimePath with the given parent and child.
     */
    public MimePath(MimePath parent, String child) {
        path.addAll(parent.path);
        path.addAll(breakup(child));
    }

    /**
     * Constructs a new MimePath with the given parent and child.
     */
    public MimePath(MimePath parent, MimePath child) {
        path.addAll(parent.path);
        path.addAll(child.path);
    }

    /**
     * Removes the root of the MimePath.
     */
    public MimePath removeProgenitor() {

        MimePath clone = clone();

        List<Integer> path = clone.path;

        if ( path.size() <= 1 ) {
            return null;
        }

        return new MimePath(path.subList(1, path.size()));
    }

    /**
     * Constructs a new MimePath with the given string.
     */
    public MimePath(String path) {
        this.path.addAll(breakup(path));
    }

    /**
     * Constructs a new MimePath with the given parent and child.
     */
    public MimePath(String parent, String child) {
        path.addAll(breakup(parent));
        path.addAll(breakup(child));
    }

    /**
     * Removes the child from the MimePath.
     */
    public MimePath getParent() {

        if ( path.size() <= 1 ) {
            return null;
        }

        return new MimePath(path.subList(0, path.size()-1));
    }

    /**
     * Returns the MimePath for the next MIME part in this level.
     */
    public MimePath getNextSibling() {

        MimePath parent = getParent();
        Integer leaf = path.get(path.size()-1);

        if ( parent == null ) {
            return null;
        }

        return new MimePath(parent, leaf+1);

    }

    protected MimePath clone() {

        ArrayList<Integer> newList = new ArrayList<Integer>(path.size());

        newList.addAll(path);

        return new MimePath(newList);
    }

    MimePath decrement(int index) {
        MimePath newPath = clone();

        newPath.path.set(index, newPath.path.get(index) - 1);

        return newPath;
    }

    MimePath increment(int index) {
        MimePath newPath = clone();

        newPath.path.set(index, newPath.path.get(index) + 1);

        return newPath;
    }

    MimePath decrement() {
        return decrement(path.size()-1);
    }

    MimePath increment() {
        return increment(path.size()-1);
    }

    private String getPath(int max) {

        StringBuilder builder = new StringBuilder();

        boolean first = true;

        for ( int index = 0; index < max; index++ ) {

            if ( first ) {
                first = false;
            } else {
                builder.append(".");
            }

            builder.append(path.get(index));
        }

        return builder.toString();

    }

    String getPath() {

        return getPath(path.size());

    }

    /**
     * Returns the string representative of this MimePath.
     */
    public String toString() {
        return getPath();
    }

    /**
     * Returns the leaf of this MimePath.
     */
    public int getChild() {
        return path.get(path.size()-1);
    }

    public boolean equals(String path) {
        return this.getPath().equals(path);
    }

    public boolean equals(Object two) {

        if ( two == null ) {
            return false;
        }

        if ( ! ( two instanceof MimePath ) ) {
            return false;
        }

        return this.getPath().equals(((MimePath)two).getPath());
    }

    public int hashCode() {

        int sum = 0;

        for ( int num : path ) {
            sum = (sum*10)+num;
        }

        return sum;

    }

    /**
     * Returns the number of levels in this MimePath.
     */
    public int length() {
        return path.size();
    }

    /**
     * Returns true if the given index is a leaf.
     */
    public boolean isChildless(int index) {
        return index >= (path.size()-1);
    }

    int get(int index) {
        return path.get(index);
    }

    void realignLineage(MimePath prefix) {

        for ( int index = 0; index < prefix.length(); index++ ) {

            path.set(index, prefix.path.get(index));

        }

    }

    public void reset() {
        cursor = 0;
    }

    public int get() {
        return path.get(cursor);
    }

    public boolean hasNext() {
        if ( cursor < (path.size()-1) ) {
            return true;
        } else {
            return false;
        }
    }

    public void next() {

        if ( !hasNext() ) {
            throw new IllegalArgumentException("Out of bounds.");
        }

        cursor++;

    }

}


