package doclet;

import java.io.*;

/**
 * Created by giles on 2/8/2016.
 */
public class LineCountWriter extends Writer {
    private final static int UNKNOWN = 0;
    private final static int CR = 1;
    private final static int CRLF = 2;
    private final static int LF = 3;
    private final static int LFCR = 4;

    private int lineSeparator = UNKNOWN;
    private Writer out;
    private int count;
    private int previous = -1;

    public LineCountWriter(OutputStream os) {
        this(new BufferedWriter(new OutputStreamWriter(os)));
    }

    public LineCountWriter(Writer out) {
        this.out = out;
    }

    public int getCount() {
        return count;
    }

    public void write(char cbuf[]) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(cbuf[off + i]);
        }
    }

    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    public void write(String str, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(str.charAt(off + i));
        }
    }

    public void write(int c) throws IOException {
        boolean newLine = false;
        synchronized (lock) {
            out.write(c);

            switch (lineSeparator) {
                default:
                case UNKNOWN:
                    switch (previous) {
                        case '\r':
                            count++;
                            lineSeparator = (c == '\n') ? CRLF : CR;
                            if (c == '\r')
                                count++;
                            break;
                        case '\n':
                            count++;
                            lineSeparator = (c == '\r') ? LFCR : LF;
                            if (c == '\n')
                                count++;
                            break;
                        default:
                            break;
                    }
                    break;
                case CR:
                    if (c == '\r') {
                        count++;
                        newLine = true;
                    }
                    break;
                case CRLF:
                    if ((previous == '\r') && (c == '\n')) {
                        count++;
                        newLine = true;
                    }
                    break;
                case LF:
                    if (c == '\n') {
                        count++;
                        newLine = true;
                    }
                    break;
                case LFCR:
                    if ((previous == '\n') && (c == '\r')) {
                        count++;
                        newLine = true;
                    }
                    break;
            }
            previous = c;
        }
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }
}
