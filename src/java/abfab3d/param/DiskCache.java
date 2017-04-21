package abfab3d.param;

import abfab3d.core.LabeledBuffer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Comparator;

import static abfab3d.core.Output.printf;

/**
 * Disk based cache.
 * <p/>
 * Filenames will be based on a safe,unique name from the label, shortened to 64 chars.
 * Each entry will contain a metadata file with:  the original label, type and size
 *
 * TODO:
 *    load entries on startup?  Benchmark to see if desired
 *    use unsafe for faster read/write
 *    compress data
 *    delete data
 *
 * @author Alan Hudson
 */
public class DiskCache {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;

    /** Maximum size on bytes for the cache */
    private long maxSize;

    /** Base directory to manage */
    private String basedir;
    private static DiskCache cache;

    private DiskCache(long maxSize, String basedir) {
        this.basedir = basedir;
        this.maxSize = maxSize;

        File f = new File(basedir);
        f.mkdirs();

        loadEntries();
    }

    public static DiskCache getInstance() {
        if (cache == null) throw new IllegalArgumentException("Cache must be initialized before use");
        return cache;
    }

    public synchronized static DiskCache getInstance(long maxSize, String basedir) {
        if (cache != null) return cache;

        cache = new DiskCache(maxSize, basedir);

        return cache;
    }


    public void put(LabeledBuffer buff) {
        if (maxSize <= 0) return;  // Allow config code to turn this off

        try {
            writeFile(buff);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public LabeledBuffer get(String label) {
        String name = getName(label);
        String mdName = name + ".meta";
        File f = new File(basedir, mdName);

        try {
            if (f.exists()) {
                return loadFile(label);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (DEBUG) printf("DiskCache failed to find label: %s at -> %s\n",label,f.getAbsolutePath());
        return null;
    }

    private void writeFile(LabeledBuffer buff) throws IOException {
        long t0 = System.currentTimeMillis();
        String path = getName(buff.getLabel());

        String dataFile = path + ".data";
        String mdFile = path + ".meta";

        File md = new File(basedir, mdFile);
        if (DEBUG) printf("DiskCache Storing: %s to: %s   size: %d\n", buff.getLabel(),md.getAbsolutePath(),buff.getSizeBytes());

        FileOutputStream fos = new FileOutputStream(md);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            dos.writeUTF(buff.getLabel());
            dos.writeUTF(buff.getType().toString());
            dos.writeInt(buff.getNumElements());
        } finally {
            IOUtils.closeQuietly(dos);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }

        File df = new File(basedir, dataFile);
        FileChannel fc = null;
        try {
            fos = new FileOutputStream(df);
            fc = fos.getChannel();

            switch (buff.getType()) {
                case BYTE:
                    ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) buff.getBuffer());
                    fc.write(byteBuffer);
                    break;
                case INT:
                    ByteBuffer byteBuffer2 = ByteBuffer.allocate(buff.getSizeBytes());
                    IntBuffer intBuffer = byteBuffer2.asIntBuffer();
                    intBuffer.put((int[]) buff.getBuffer());
                    fc.write(byteBuffer2);
                    break;
                case FLOAT:
                    ByteBuffer byteBuffer3 = ByteBuffer.allocate(buff.getSizeBytes());
                    FloatBuffer floatBuffer = byteBuffer3.asFloatBuffer();
                    floatBuffer.put((float[]) buff.getBuffer());
                    fc.write(byteBuffer3);
                    break;
                case DOUBLE:
                    ByteBuffer byteBuffer4 = ByteBuffer.allocate(buff.getSizeBytes());
                    DoubleBuffer doubleBuffer = byteBuffer4.asDoubleBuffer();
                    doubleBuffer.put((double[]) buff.getBuffer());
                    fc.write(byteBuffer4);
                    break;
                case SHORT:
                    ByteBuffer byteBuffer5 = ByteBuffer.allocate(buff.getSizeBytes());
                    ShortBuffer shortBuffer = byteBuffer5.asShortBuffer();
                    shortBuffer.put((short[]) buff.getBuffer());
                    fc.write(byteBuffer5);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled type: " + buff.getType());
            }
        } finally {
            if (fc != null) {
                fc.close();
            }
            IOUtils.closeQuietly(fos);
        }

        if (DEBUG_TIMING) printf("writeFile: %d  ms  type: %s size: %d\n",(System.currentTimeMillis() - t0),buff.getType(),buff.getSizeBytes());
    }


    private LabeledBuffer loadFile(String label) throws IOException {
        long t0 = System.currentTimeMillis();
        String path = getName(label);

        String dataFile = path + ".data";
        String mdFile = path + ".meta";

        if (DEBUG) printf("DiskCache reading label: %s from file: %s\n",label,dataFile);
        if (DEBUG) {
            new Exception().printStackTrace(System.out);
        }
        File md = new File(basedir, mdFile);
        FileInputStream fis = new FileInputStream(md);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        String flabel;
        LabeledBuffer.Type type;
        int size;

        try {
            flabel = dis.readUTF();
            String ftype = dis.readUTF();
            type = LabeledBuffer.Type.valueOf(ftype);
            size = dis.readInt();
        } finally {
            IOUtils.closeQuietly(dis);
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fis);
        }

        File f = new File(basedir, dataFile);
        FileChannel fc = new FileInputStream(f).getChannel();

        // Look at unsafe to speed this up.   http://mechanical-sympathy.blogspot.de/2012/07/native-cc-like-performance-for-java.html
        try {
            switch (type) {
                case BYTE:
                    ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                    fc.read(byteBuffer);
                    return new LabeledBuffer(flabel, byteBuffer.array());
                case INT:
                    ByteBuffer byteBuffer2 = ByteBuffer.allocate(4 * size);
                    fc.read(byteBuffer2);
                    byteBuffer2.rewind();
                    IntBuffer intBuffer = byteBuffer2.asIntBuffer();
                    int[] iarr = new int[size];
                    intBuffer.get(iarr);
                    return new LabeledBuffer(flabel, iarr);
                case FLOAT:
                    ByteBuffer byteBuffer3 = ByteBuffer.allocate(4 * size);
                    fc.read(byteBuffer3);
                    byteBuffer3.rewind();
                    FloatBuffer floatBuffer = byteBuffer3.asFloatBuffer();
                    float[] farr = new float[size];
                    floatBuffer.get(farr);
                    return new LabeledBuffer(flabel, farr);
                case DOUBLE:
                    ByteBuffer byteBuffer4 = ByteBuffer.allocate(8 * size);
                    fc.read(byteBuffer4);
                    byteBuffer4.rewind();
                    DoubleBuffer doubleBuffer = byteBuffer4.asDoubleBuffer();
                    double[] darr = new double[size];
                    doubleBuffer.get(darr);
                    return new LabeledBuffer(flabel, darr);
                case SHORT:
                    ByteBuffer byteBuffer5 = ByteBuffer.allocate(2 * size);
                    fc.read(byteBuffer5);
                    byteBuffer5.rewind();
                    ShortBuffer shortBuffer = byteBuffer5.asShortBuffer();
                    short[] sarr = new short[size];
                    shortBuffer.get(sarr);
                    return new LabeledBuffer(flabel, sarr);
                default:
                    throw new IllegalArgumentException("Unhandled type: " + type);
            }
        } finally {
            IOUtils.closeQuietly(fis);
            if (DEBUG_TIMING) printf("readFile: %d  ms  type: %s elems: %d\n",(System.currentTimeMillis() - t0),type,size);

        }
    }

    /**
     * Get a directory name from a label.  These labels are likely very long and contain shit filenames shouldnt have
     *
     * @param label
     * @return
     */
    private String getName(String label) {
        StringBuilder sb = new StringBuilder();

        String sha = DigestUtils.sha1Hex(label);


        // Strip any non safe characters and limit to 64 characters
        label = label.replaceAll("[:\\\\/*\"?|<>'.;]", "_");
        //label = label.substring(0,2) + File.separator + label.substring(2);

        int len = Math.min(label.length(), 64);
        sb.append(label.substring(0, len));

        sb.append("_");
        sb.append(sha);

        return sb.toString();
    }

    /**
     * Load entries from disk
     */
    private void loadEntries() {

    }

    static class CacheEntry implements Comparator {
        public long lastAccess;
        public String path;

        public CacheEntry(String path) {
            this.path = path;
            lastAccess = System.currentTimeMillis();
        }

        @Override
        public int compare(Object o1, Object o2) {
            CacheEntry ce1 = (CacheEntry) o1;
            CacheEntry ce2 = (CacheEntry) o2;

            return Long.compare(ce1.lastAccess, ce2.lastAccess);
        }
    }
}
