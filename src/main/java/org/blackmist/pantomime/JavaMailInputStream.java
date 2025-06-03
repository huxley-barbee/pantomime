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
 * $Id: JavaMailMessageSource.java,v 1.18 2015/05/27 10:47:18 barbee Exp $
**/

package org.blackmist.pantomime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.mail.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JavaMailInputStream extends PipedInputStream {

    private static final Logger log = LoggerFactory.getLogger(JavaMailInputStream.class.getName());

    private PipedOutputStream pipedOutputStream = new PipedOutputStream();
    private ExecutorService service = null;
    private boolean started = false;
    private Future<String> result;
    private jakarta.mail.Part part;

    private synchronized void checkInitialized() {

        if ( started ) {
            return;
        }

        Callable<String> executingCallable = new DataProducer();
        result = service.submit(executingCallable);
        log.trace("Thread invoked by[{}] queued for start.");

        started = true;

    }

    private void checkException() throws IOException {

        try {

            result.get(1, TimeUnit.SECONDS);

        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            final IOException e1 = new IOException("Exception producing data "
                + this);
            e1.initCause(t);
            throw e1;
        } catch (final InterruptedException e) {
            final IOException e1 = new IOException("Thread interrupted");
            e1.initCause(e);
            throw e1;
        } catch (final TimeoutException e) {
            log.trace("This timeout should never happen, "
                    + "the thread should terminate correctly. "
                    + "Please contact io-tools support." + e.getMessage());
        }

    }

    @Override
    public int read() throws IOException {

        checkInitialized();

        int result = super.read();

        if (result < 0) {
            checkException();
        }

        return result;

    }

    @Override
    public int read(final byte[] b, final int off, final int len)
            throws IOException {

        checkInitialized();

        int result = super.read(b, off, len);

        if (result < 0) {
            checkException();
        }

        return result;

    }
   
    @Override
    public int read(byte[] b) throws IOException {

        checkInitialized();

        int result = super.read(b);

        if (result < 0) {
            checkException();
        }

        return result;

    }

    private class Num {
        private int blah = 0;
    }

    private class DataProducer implements Callable<String> {

        public String call () throws Exception {

            final Num count = new Num();

            log.trace("Writing out mime part " + part + ".");

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

            part.writeTo(baos);

            part.writeTo(pipedOutputStream);

            pipedOutputStream.close();

            return null;

        }

    }

    public JavaMailInputStream ( jakarta.mail.Part part ) throws IOException {

        this.part = part;

        service = Executors.newCachedThreadPool();

        connect ( pipedOutputStream );
        
    }

    @Override
    public void close () throws IOException {

        service.shutdownNow();

        super.close();

    }

}
