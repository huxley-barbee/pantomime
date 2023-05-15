package org.blackmist.pantomime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.List;

import org.blackmist.pantomime.content.AlternativeContent;
import org.blackmist.pantomime.content.InputStreamSource;
import org.blackmist.pantomime.content.SimpleContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of static methods to create a text/plain, text/html, or
 * multipart/alternative message with a single method call and free
 * resources.
 */
public class Pantomime {

    private static final Logger log = LoggerFactory.getLogger(Pantomime.class.getName());

    /**
     * The version of Pantomime.
     *
     * VERSION has the value {@value}.
     */
    public static final String VERSION = "0.9984";
//    public static final String VERSION = "0.99";

    private Pantomime() {}

    /**
     * Frees (null-safe) all releases used by this mesage, including any message
     * sources.
     */
    public static void free(SourcedMessage message) {
        if ( message == null ) {
            return;
        }

        message.free();
    }

    /**
     * Frees (null-safe) all releases used by this MessageSource (Not necessary
     * if you already freed the message sourced therefrom).
     */
    public static void free(MessageSource source ) {
        if ( source == null ) {
            return;
        }

        source.free();
    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        String html, String... to) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        String html, String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        String html, List<String> to, List<String> cc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        String html, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        String html, List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, bccArray);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        File html, String... to) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        File html, String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        File html, List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        File html, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(plain), to, cc, bcc);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, String plain,
        File html, List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, bccArray);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        String plain, InputStreamSource html, String... to)
        throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain), html, to, null,
            null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        String plain, InputStreamSource html, String[] to,
        String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain), html, to, cc,
            null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        String plain, InputStreamSource html, List<String> to,
        List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain), html,
            toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        String plain, InputStreamSource html, String[] to,
        String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain), html, to, cc,
            bcc);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        String plain, InputStreamSource html, List<String> to,
        List<String> cc, List<String> bcc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain), html,
            toArray, ccArray, bccArray);
    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        String html, String... to) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        String html, String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        String html, List<String> to, List<String> cc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        String html, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        String html, List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, bccArray);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        File html, String... to) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        File html, String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        File html, List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        File html, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(plain), to, cc, bcc);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject, File plain,
        File html, List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain),
            getInputStreamSource(html), toArray, ccArray, bccArray);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        File plain, InputStreamSource html, String... to)
        throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain), html, to, null,
            null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        File plain, InputStreamSource html, String[] to,
        String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain), html, to, cc,
            null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        File plain, InputStreamSource html, List<String> to,
        List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain), html,
            toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        File plain, InputStreamSource html, String[] to,
        String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(plain), html, to, cc,
            bcc);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        File plain, InputStreamSource html, List<String> to,
        List<String> cc, List<String> bcc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(plain), html,
            toArray, ccArray, bccArray);
    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        String html, String... to) throws PantomimeException {

        return make(from, subject, plain,
            getInputStreamSource(html), to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        String html, String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, plain,
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        String html, List<String> to, List<String> cc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, plain,
            getInputStreamSource(html), toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        String html, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, plain,
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        String html, List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, plain, getInputStreamSource(html), toArray,
            ccArray, bccArray);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        File html, String... to) throws PantomimeException {

        return make(from, subject, plain,
            getInputStreamSource(html), to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        File html, String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, plain,
            getInputStreamSource(html), to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        File html, List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, plain, getInputStreamSource(html), toArray,
            ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        File html, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, plain,
            getInputStreamSource(html), to, cc, bcc);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain,
        File html, List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, plain, getInputStreamSource(html), toArray,
            ccArray, bccArray);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain, InputStreamSource html, String... to)
        throws PantomimeException {

        return make(from, subject, plain, html, to, null, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain, InputStreamSource html, String[] to,
        String[] cc) throws PantomimeException {

        return make(from, subject, plain, html, to, cc, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain, InputStreamSource html, List<String> to,
        List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, plain, html, toArray, ccArray, null);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain, InputStreamSource html, String[] to,
        String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, plain, html, to, cc, bcc);

    }

    /**
     * Makes a new plain text and HTML messages.
     */
    public static Message alternative(String from, String subject,
        InputStreamSource plain, InputStreamSource html, List<String> to,
        List<String> cc, List<String> bcc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, plain, html, toArray, ccArray, bccArray);
    }

    /********************** plain ******************/


    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, String content,
        String... to) throws PantomimeException {

        return make(from, subject, getInputStreamSource(content), null, to,
            null, null);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, String content,
        String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(content), null,
            to, cc, null);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, String content,
        List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(content), null,
            toArray, ccArray, null);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, String content,
        String[] to, String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(content), null,
            to, cc, bcc);
    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, String content,
        List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(content), null,
            toArray, ccArray, bccArray);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, File content,
        String... to) throws PantomimeException {
        return make(from, subject, getInputStreamSource(content), null,
            to, null, null);
    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, File content,
        String[] to, String[] cc) throws PantomimeException {
        return make(from, subject, getInputStreamSource(content), null,
            to, cc, null);
    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, File content,
        List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, getInputStreamSource(content), null,
            toArray, ccArray, null);
    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, File content,
        String[] to, String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, getInputStreamSource(content), null,
            to, cc, bcc);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject, File content,
        List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, getInputStreamSource(content), null,
            toArray, ccArray, bccArray);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject,
        InputStreamSource content, String... to) throws PantomimeException {

        return make(from, subject, content, null, to, null, null);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject,
        InputStreamSource content, String[] to, String[] cc)
        throws PantomimeException {

        return make(from, subject, content, null, to, cc, null);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject,
        InputStreamSource content, List<String> to, List<String> cc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, content, null, toArray, ccArray, null);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject,
        InputStreamSource content, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, content, null, to, cc, bcc);

    }

    /**
     * Makes a new plain text messages.
     */
    public static Message plain(String from, String subject,
        InputStreamSource content, List<String> to, List<String> cc,
        List<String> bcc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, content, null, toArray, ccArray, bccArray);
    }


    /****************************** HTML *********************************/

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, String content,
        String... to) throws PantomimeException {

        return make(from, subject, null, getInputStreamSource(content),
            to, null, null);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, String content,
        String[] to, String[] cc) throws PantomimeException {

        return make(from, subject, null, getInputStreamSource(content),
            to, cc, null);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, String content,
        List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, null, getInputStreamSource(content),
            toArray, ccArray, null);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, String content,
        String[] to, String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, null, getInputStreamSource(content),
            to, cc, bcc);
    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, String content,
        List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, null, getInputStreamSource(content),
            toArray, ccArray, bccArray);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, File content,
        String... to) throws PantomimeException {
        return make(from, subject, null, getInputStreamSource(content),
            to, null, null);
    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, File content,
        String[] to, String[] cc) throws PantomimeException {
        return make(from, subject, null, getInputStreamSource(content),
            to, cc, null);
    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, File content,
        List<String> to, List<String> cc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, null, getInputStreamSource(content),
            toArray, ccArray, null);
    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, File content,
        String[] to, String[] cc, String[] bcc) throws PantomimeException {

        return make(from, subject, null, getInputStreamSource(content),
            to, cc, bcc);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject, File content,
        List<String> to, List<String> cc, List<String> bcc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, null, getInputStreamSource(content),
            toArray, ccArray, bccArray);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject,
        InputStreamSource content, String... to) throws PantomimeException {

        return make(from, subject, null, content, to, null, null);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject,
        InputStreamSource content, String[] to, String[] cc)
        throws PantomimeException {

        return make(from, subject, null, content, to, cc, null);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject,
        InputStreamSource content, List<String> to, List<String> cc)
        throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        return make(from, subject, null, content, toArray, ccArray, null);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject,
        InputStreamSource content, String[] to, String[] cc, String[] bcc)
        throws PantomimeException {

        return make(from, subject, null, content, to, cc, bcc);

    }

    /**
     * Makes a new HTML-only messages.
     */
    public static Message html(String from, String subject,
        InputStreamSource content, List<String> to, List<String> cc,
        List<String> bcc) throws PantomimeException {

        String[] toArray = null;
        String[] ccArray = null;
        String[] bccArray = null;

        if ( to != null ) {
            toArray = to.toArray(new String[to.size()]);
        }

        if ( cc != null ) {
            ccArray = cc.toArray(new String[cc.size()]);
        }

        if ( bcc != null ) {
            bccArray = bcc.toArray(new String[bcc.size()]);
        }

        return make(from, subject, null, content, toArray, ccArray, bccArray);
    }


    private static InputStreamSource getInputStreamSource(final String s)
        throws PantomimeException {
        if ( s == null ) {
            return null;
        }

        return new InputStreamSource() {
            public InputStream getInputStream() throws PantomimeException {
                InputStream stream = new ByteArrayInputStream(s.getBytes());

                /* Do not call StreamMonitor.opened() here.
                 * We expect the consumer of this method to
                 * make that call. If we do it here do, we
                 * end up with spurious errors in the logs.
                 */

                return stream;
            }
        };
    }

    private static InputStreamSource getInputStreamSource(final File f)
        throws PantomimeException {
        if ( ( f == null ) || ( ! f.exists() ) ) {
            return null;
        }

        return new InputStreamSource() {
            public InputStream getInputStream() throws PantomimeException {

                try {
                    FileInputStream fis = new FileInputStream(f);

                    /* Do not call StreamMonitor.opened() here.
                     * We expect the consumer of this method to
                     * make that call. If we do it here do, we
                     * end up with spurious errors in the logs.
                     */

                    return fis;
                } catch (IOException e) {
                    throw new PantomimeException(e);
                }
            }
        };
    }

    private static Message make(String from, String subject,
        InputStreamSource plain, InputStreamSource html, String[] to,
        String[] cc, String[] bcc) throws PantomimeException {

        Message message = new Message();

        if ( ( plain != null ) && ( html != null ) ) {
            AlternativeContent content = new AlternativeContent();
            SimpleContent plainContent = new SimpleContent();
            SimpleContent htmlContent = new SimpleContent();

            plainContent.set(plain);
            htmlContent.set(html);

            content.setPlain(plainContent);
            content.setHtml(htmlContent);

            message.specializeAsMultipart();

            message.asMultipart().set(content);

        } else if ( plain != null ) {

            message.asSinglePart().set(plain, "text/plain", "utf-8");

        } else if ( html != null ) {

            message.asSinglePart().set(html, "text/html", "utf-8");

        } else {

            message.asSinglePart().set("", "text/plain", "utf-8");

        }

        message.setFrom(from);
        message.setSubject(subject);

        if ( to != null ) {
            for ( String recipient : to ) {
                message.addToRecipient(recipient);
            }
        }
        if ( cc != null ) {
            for ( String recipient : cc ) {
                message.addCcRecipient(recipient);
            }
        }
        if ( bcc != null ) {
            for ( String recipient : bcc ) {
                message.addBccRecipient(recipient);
            }
        }

        return message;
    }
}

