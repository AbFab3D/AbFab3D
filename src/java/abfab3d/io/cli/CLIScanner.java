/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.HashSet;

import static abfab3d.core.Output.printf;

/**
 * Custom scanner for CLI format.  Splits the file into command and data chunks.  
 *
 * @author Alan Hudson
 */
class CLIScanner implements Closeable {
    private static final boolean DEBUG = false;

    protected static final String COMMENT = "//";
    protected static final String HEADER_START = "$$HEADERSTART";
    protected static final String ASCII = "$$ASCII";
    protected static final String BINARY = "$$BINARY";
    protected static final String UNITS = "$$UNITS/";
    protected static final String DIMENSION = "$$DIMENSION/";
    protected static final String VERSION = "$$VERSION/";
    protected static final String ALIGN = "$$ALIGN";
    protected static final String LABEL = "$$LABEL/";
    protected static final String LAYERS = "$$LAYERS/";
    protected static final String USERDATA = "$$USERDATA/";
    protected static final String HEADER_END = "$$HEADEREND";
    protected static final String GEOMETRY_START = "$$GEOMETRYSTART";
    protected static final String LAYER = "$$LAYER/";
    protected static final String POLYLINE = "$$POLYLINE/";
    protected static final String HATCHES = "$$HATCHES/";
    protected static final String GEOMETRY_END = "$$GEOMETRYEND";

    private static final HashMap<Integer,String> commands;
    private static final int BINARY_HASH = BINARY.hashCode();
    private static final int HEADER_END_HASH = HEADER_END.hashCode();

    static {
        commands = new HashMap<>();
        commands.put(HEADER_START.hashCode(),HEADER_START);
        commands.put(HEADER_END.hashCode(),HEADER_END);
        commands.put(ASCII.hashCode(),ASCII);
        commands.put(BINARY.hashCode(),BINARY);
        commands.put(UNITS.hashCode(),UNITS);
        commands.put(DIMENSION.hashCode(),DIMENSION);
        commands.put(VERSION.hashCode(),VERSION);
        commands.put(ALIGN.hashCode(),ALIGN);
        commands.put(LABEL.hashCode(),LABEL);
        commands.put(LAYERS.hashCode(),LAYERS);
        commands.put(LAYER.hashCode(),LAYER);
        commands.put(USERDATA.hashCode(),USERDATA);
        commands.put(GEOMETRY_START.hashCode(),GEOMETRY_START);
        commands.put(POLYLINE.hashCode(),POLYLINE);
        commands.put(HATCHES.hashCode(),HATCHES);
        commands.put(GEOMETRY_END.hashCode(),GEOMETRY_END);
    }

    // Boolean is true if source is done
    private boolean sourceClosed = false;

    // Boolean indicating if this scanner has been closed
    private boolean closed = false;

    // A holder of the last IOException encountered
    private IOException lastException;

    private InputStream source;
    private int pos = 0;
    private boolean binary = false;

    private ByteBuffer buff = ByteBuffer.allocate(10*1024);

    public CLIScanner(InputStream is) {
        source = is;
    }

    /**
     * Scan for the next command in the stream
     *
     * @return The command or null if eof
     * @return
     */
    public String nextCommand() throws IOException {
        int runningHash = 0;

        String cmd;

        buff.clear();

        while(true) {
            int b = source.read();

            //printf("%d\n",b);
            if (b == -1) return null;
            //printf("%s",(char)b);
            if (buff.position() == 0 && b == 10) continue;  // Skip bare newlines
            if (buff.position() == 0 && b == '/') {
                // Skip comments
                while((b = source.read()) != 10) {
                    // Skip till linefeed
                }

                continue;
            }
            if (buff.position() > 2 && (b == '/')) {
                buff.put(((byte)b));
                runningHash =+ 31*runningHash + b;

                cmd = commands.get(runningHash);
                if (cmd != null) {
                    return cmd;
                }
            } else if (buff.position() > 2 && (b == 10)) {
                cmd = commands.get(runningHash);
                if (cmd != null) {
                    if (runningHash == BINARY_HASH) {
                        if (DEBUG) printf("Binary mode\n");
                        binary = true;
                    }
                    return cmd;
                } else {
                    printf("Unknown command: %s", new String(buff.array()));
                    // We got an unknown command just skip
                    buff.reset();
                }
            } else {
                buff.put(((byte)b));
            }

            runningHash =+ 31*runningHash + b;

            if (binary && runningHash == HEADER_END_HASH) {
                // In binary mode we will not have a line feed
                return HEADER_END;
            }
        }
    }

    /**
     * Scan for the next data block.  Data is always terminated with a linefeed or eof
     * @return The data block or null if none.  Do not call this on commands that do not have data
     */
    public byte[] nextData() throws IOException {
        buff.clear();

        while(true) {
            int b = source.read();

            //printf("%d\n",b);
            if (b == -1) return null;

            //printf("%s",(char)b);
            if (b == 10 || b == '/') {
                byte[] ret = new byte[buff.position()];
                buff.rewind();
                buff.get(ret);
                return ret;
            }

            if (buff.remaining() == 0) {
                int newSize = buff.capacity() * 2;

                ByteBuffer newBuff = ByteBuffer.allocate(newSize);
                newBuff.order(buff.order());
                newBuff.put(buff);
                /*
                buff.rewind();
                newBuff.put(buff);
                newBuff.flip();
                newBuff.position(buff.limit()+1);
                 */
                buff = newBuff;
            }

            buff.put((byte) b);
        }
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;
        if (source instanceof Closeable) {
            try {
                ((Closeable)source).close();
            } catch (IOException ioe) {
                lastException = ioe;
            }
        }
        sourceClosed = true;
        source = null;
        closed = true;
    }
}
