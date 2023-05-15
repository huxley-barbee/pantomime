package org.blackmist.pantomime;

import java.io.InputStream;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartInputStream extends InputStream {

    private static Logger log =
        LoggerFactory.getLogger(PartInputStream.class.getName());

    private Part part;
    private List<Part> subParts = null;
    private InputStream partStream;
    private InputStream content = null;
    private int partIndex = 0;
    private boolean initialized = false;
    private LinkedList<Byte> buffer;
    private boolean done = false;
    private boolean donePreamble = false;

    PartInputStream(Part part) {
        this.part = part;
    }

    private void serializeHeaders() throws PantomimeException {
        StringBuilder builder = new StringBuilder();
        for ( Header header : part.getHeaderList() ) {

            builder.append(header.getName())
                .append(": ")
                .append(header.getTransferEncodedValue())
                .append("\r\n");
        }

        builder.append("\r\n");

        buffer = new LinkedList<Byte>();

        for ( byte b : builder.toString().getBytes() ) {
            buffer.add(b);
        }

    }

    private void init() throws PantomimeException {


        if ( part.isMultipart() ) {

            if ( part.asMultipart().getSubPartCount() > 0 ) {
                subParts = part.asMultipart().getSubParts();
            }

        } else {

            content = part.asSinglePart().getTransferEncodedBody();

        }

        initialized = true;

    }

    private void writePreamble() throws PantomimeException {
        String preamble;
        
        if ( donePreamble ) {
            return;
        }

        donePreamble = true;

        preamble = part.asMultipart().getPreamble();

        if ( preamble == null ) {
            return;
        }

        if ( preamble.length() == 0 ) {
            return;
        }

        for ( byte b : preamble.getBytes() ) {
            buffer.add(b);
        }

        buffer.add((byte)13);
        buffer.add((byte)10);
        buffer.add((byte)13);
        buffer.add((byte)10);

    }

    private void writeEpilogue() throws PantomimeException {
        String epilogue = part.asMultipart().getEpilogue();

        if ( epilogue == null ) {
            return;
        }

        if ( epilogue.length() == 0 ) {
            return;
        }

        buffer.add((byte)13);
        buffer.add((byte)10);
        buffer.add((byte)13);
        buffer.add((byte)10);

        for ( byte b : epilogue.getBytes() ) {
            buffer.add(b);
        }

        buffer.add((byte)13);
        buffer.add((byte)10);

    }

    private void markSubPartStart() throws PantomimeException {

        buffer.add((byte)45);
        buffer.add((byte)45);

        for ( byte b : part.asMultipart().getBoundary().getBytes() ) {
            buffer.add(b);
        }

        buffer.add((byte)13);
        buffer.add((byte)10);

        partStream = subParts.get(partIndex).serialize();
    }

    private void markSubPartEnd() throws IOException, PantomimeException {
        StreamUtility.close(this, partStream);
        partStream = null;
        partIndex++;

        buffer.add((byte)13);
        buffer.add((byte)10);
        buffer.add((byte)13);
        buffer.add((byte)10);
    }
 
    public int read() throws IOException {

        try {

            if ( buffer == null ) {
                serializeHeaders();
            }

            if ( buffer.peek() != null ) {
                return buffer.poll();
            }

            if ( done ) {
                return -1;
            }

            if ( ! initialized ) {
                init();
            }

            if ( content != null ) {
                int b = content.read();
                return b;
            }


            if ( (subParts == null) || (subParts.size() == 0) ) {
                return -1;
            }

            if ( partStream == null ) {

                writePreamble();

                markSubPartStart();

                return buffer.poll();

            } else {

                int b = partStream.read();

                if ( b > -1 ) {
                    return b;
                }

                markSubPartEnd();

                if ( partIndex >= subParts.size() ) {
                    markMultipartEnd();
                    writeEpilogue();
                }


                return buffer.poll();

            }

        } catch (PantomimeException e) {
            throw new IOException(e);
        }

    }

    private void markMultipartEnd() {
        buffer.add((byte)45);
        buffer.add((byte)45);

        for ( byte b2 : part.asMultipart().getBoundary().getBytes() ) {
            buffer.add(b2);
        }

        buffer.add((byte)45);
        buffer.add((byte)45);

        done = true;
    }

    public void close() throws IOException {
        StreamUtility.close(this, content);
        StreamUtility.close(this, partStream);
    }

    public void reset() {
        subParts = null;
        partStream = null;
        content = null;
        partIndex = 0;
        initialized = false;
        buffer = null;
        done = false;
        donePreamble = false;
    }
}

