/**
 * Classes that model various components of an email, especially the MIME
 * part.
 * <p>
 * You can create a message from scratch by instantiating a new
 * {@link Message} object.o
 * <p>
 * You can also load messages some sort of storage like file
 * ({@link FileMessageSource}, database BLOB {@link BlobFileMessageSource},
 * or a JavaMail {@link javax.mail.internet.MimeMessage}
 * ({@link JavaMailMessageSource}).
 *
 * <p>
 * Pantomime only offer MIME manipulation and storage.
 * <p>
 * To send a message over SMTP, you must convert a Pantomime message into
 * a JavaMail {@link javax.mail.internet.MimeMessage} and use the 
 * {@link  javax.mail.Transport} API.
 *
 * Please refere to manual at <a href="http://blah">blah</a> for more
 * information and examples.
 */
package org.blackmist.pantomime;
