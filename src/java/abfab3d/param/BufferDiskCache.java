package abfab3d.param;

import abfab3d.core.LabeledBuffer;
import org.apache.commons.io.IOUtils;
//import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static abfab3d.core.Output.printf;

/**
 * Disk based cache for Buffers
 * <p/>
 * Filenames will be based on a safe,unique name from the label, shortened to 64 chars.
 * Each entry will contain a metadata file with:  the original label, type and size
 * <p/>
 * This class is designed to be thread safe though its not very efficient at it.
 * <p/>
 * TODO:
 * Currently using compression seems to make the loading speed slower
 *
 * use unsafe for faster read/write
 *  Look at unsafe to speed this up.   http://mechanical-sympathy.blogspot.de/2012/07/native-cc-like-performance-for-java.html
 * compress data
 * delete data
 *
 * @author Alan Hudson
 */
public class BufferDiskCache implements Runnable {
    private static final boolean CACHE_ENABLED = true;
    
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;
    private static long DEFAULT_SIZE = (long) (4 * 1e9);
    private static String DEFAULT_LOC = "/var/www/html/cache/buffer";
    private static boolean DEFAULT_COMPRESS = false;
    private static boolean DEFAULT_LAZY_WRITES = true;

    private boolean compress = false;
    private boolean lazyWrites = true;
    private FileDiskCache diskCache;
    private String basedir;

    private static BufferDiskCache cache;

    // Lazy write variables
    private LinkedBlockingQueue<LabeledBuffer> writeQueue;
    private volatile boolean terminate;
    private Thread writeThread;


    private static ThreadLocal<ThreadVars> threadVars = new ThreadLocal<ThreadVars>() {
        public ThreadVars initialValue() {
            ThreadVars ret_val = new ThreadVars();

            return ret_val;
        }
    };

    private BufferDiskCache(long maxSize, String basedir, boolean compress, boolean lazyWrites) {
        this.basedir = basedir;
        this.compress = compress;
        this.lazyWrites = lazyWrites;

        diskCache = new FileDiskCache(basedir, maxSize);
        if (lazyWrites) {
            writeQueue = new LinkedBlockingQueue<>();
            terminate = false;

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    printf("Shutting down BufferDiskCache");
                    shutdown();
                }
            });
            writeThread = new Thread(this);
            writeThread.start();
        }

        if (!CACHE_ENABLED) {
            printf("**** Buffer disk cache turned off, should not happen in production ****");
        }
    }

    /**
     * Reconfigure this instance.  Bit strange for a singleton but couldnt think of a better design.  Should
     * only be called early in the process.
     * @param dir
     * @param maxSize
     */
    public void reconfigure(String dir, long maxSize) {
        diskCache = new FileDiskCache(dir, maxSize);
    }

    public static BufferDiskCache getInstance() {
        if (cache == null) {
            cache = new BufferDiskCache(DEFAULT_SIZE, DEFAULT_LOC, DEFAULT_COMPRESS,DEFAULT_LAZY_WRITES);
        }
        return cache;
    }

    public synchronized static BufferDiskCache getInstance(long maxSize, String basedir, boolean compress, boolean lazyWrites) {
        if (cache != null) return cache;

        cache = new BufferDiskCache(maxSize, basedir, compress,lazyWrites);

        return cache;
    }

    public void put(LabeledBuffer buff) {
        if (!CACHE_ENABLED) return;

        if (lazyWrites) {
            putLazy(buff);
        } else {
            putDirect(buff);
        }
    }

    public void putDirect(LabeledBuffer buff) {
        if (!CACHE_ENABLED) return;

        try {
            ThreadVars tvars = threadVars.get();
            HashMap<String, Object> extra = tvars.extra;
            extra.clear();

            String path = writeFile(buff, tvars.extra);

            extra.put("type", buff.getType().toString());
            extra.put("numElements", buff.getNumElements());

            //printf("Put: %s to --> %s\n", buff.getLabel(), path);
            diskCache.put(buff.getLabel(), extra, path);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Lazy put code, just add to queue
     * @param buff
     */
    private void putLazy(LabeledBuffer buff) {
        if (!CACHE_ENABLED) return;

        try {
            writeQueue.put(buff);
        } catch(InterruptedException ie) {
            // ignore
        }
    }

    public void run() {
        while(!terminate) {
            try {
                LabeledBuffer buff = writeQueue.take();
                putDirect(buff);
            } catch(InterruptedException ie) {
                // ignore
            }
        }
    }

    public void shutdown() {
        terminate = true;
    }

    public LabeledBuffer get(String label) {
        if (!CACHE_ENABLED) return null;
        
        ThreadVars tvars = threadVars.get();
        HashMap<String, Object> extra = tvars.extra;
        extra.clear();

        String path = diskCache.get(label, extra);

        if (path == null) return null;

        try {
            return loadFile(label, path, extra);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (DEBUG) printf("BufferDiskCache failed to find label: %s at: %s\n", label, path);
        return null;
    }

    public void clear() {
        diskCache.clear();
    }

    /**
     * Write a buffer to disk
     *
     * @param buff
     * @param extra
     * @return The absolute path to the file
     * @throws IOException
     */
    private String writeFile(LabeledBuffer buff, Map<String, Object> extra) throws IOException {
        long t0 = System.currentTimeMillis();
        String path = diskCache.convKeyToFilename(buff.getLabel(), "");

        File df = new File(basedir, path);
        FileChannel fc = null;
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(df);
            fc = fos.getChannel();

            switch (buff.getType()) {
                case BYTE:
                    if (compress) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        GZIPOutputStream gos = new GZIPOutputStream(baos);
                        try {
                            gos.write((byte[]) buff.getBuffer());
                        } finally {
                            IOUtils.closeQuietly(gos);
                            IOUtils.closeQuietly(baos);
                        }
                        ByteBuffer byteBuffer2 = ByteBuffer.wrap(baos.toByteArray());
                        fc.write(byteBuffer2);
                        extra.put("compressed", true);
                    } else {
                        ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) buff.getBuffer());
                        fc.write(byteBuffer);
                    }
                    break;
                case INT:
                    if (compress) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        GZIPOutputStream gos = new GZIPOutputStream(baos);
                        try {
                            ByteBuffer bdata = null;

                            bdata = ByteBuffer.allocate(buff.getSizeBytes());
                            IntBuffer intBuffer = bdata.asIntBuffer();
                            intBuffer.put((int[]) buff.getBuffer());

                            gos.write(bdata.array());
                        } finally {
                            IOUtils.closeQuietly(gos);
                            IOUtils.closeQuietly(baos);
                        }
                        ByteBuffer byteBuffer2 = ByteBuffer.wrap(baos.toByteArray());
                        fc.write(byteBuffer2);
                        extra.put("compressed", true);
                    } else {
                        ByteBuffer byteBuffer2 = ByteBuffer.allocate(buff.getSizeBytes());
                        IntBuffer intBuffer = byteBuffer2.asIntBuffer();
                        intBuffer.put((int[]) buff.getBuffer());
                        fc.write(byteBuffer2);
                    }
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
            IOUtils.closeQuietly(fc);
            IOUtils.closeQuietly(fos);
        }

        if (DEBUG_TIMING)
            printf("BufferDiskCache.writeFile: %d  ms  type: %s elems: %d size: %d\n", (System.currentTimeMillis() - t0), buff.getType(), buff.getNumElements(), buff.getSizeBytes());

        return df.getAbsolutePath();
    }


    private LabeledBuffer loadFile(String label, String path, Map<String, Object> extra) throws IOException {
        long t0 = System.currentTimeMillis();

        if (DEBUG) printf("DiskCache reading label: %s from file: %s  type: %s\n", label, path, extra.get("type"));
        LabeledBuffer.Type type = LabeledBuffer.Type.valueOf((String) extra.get("type"));
        int numElements = ((Number) extra.get("numElements")).intValue();

        File f = new File(path);
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        boolean compressed = false;

        if (extra.containsKey("compressed")) {
            compressed = (boolean) extra.get("compressed");
        }

        try {
            switch (type) {
                case BYTE:
                    if (compressed) {
                        GZIPInputStream gis = new GZIPInputStream(fis);
                        byte[] ucBytes = IOUtils.toByteArray(gis);

                        return new LabeledBuffer(label, ucBytes);
                    } else {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(numElements);
                        fc.read(byteBuffer);

                        return new LabeledBuffer(label, byteBuffer.array());
                    }
                case INT:
                    if (compressed) {
                        GZIPInputStream gis = new GZIPInputStream(fis);
                        byte[] ucBytes = IOUtils.toByteArray(gis);

                        IntBuffer ib = ByteBuffer.wrap(ucBytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                        int[] iarr = new int[numElements];
                        ib.get(iarr);
                        return new LabeledBuffer(label, iarr);
                    } else {
                        MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
                        LabeledBuffer buff = convByteArrayToBuffer(label,type,byteBuffer,numElements);

                        ((DirectBuffer)byteBuffer).cleaner().clean();
                        return buff;
                    }
                case FLOAT:
                    if (compressed) {
                        GZIPInputStream gis = new GZIPInputStream(fis);
                        byte[] ucBytes = IOUtils.toByteArray(gis);

                        FloatBuffer ib = ByteBuffer.wrap(ucBytes).order(ByteOrder.BIG_ENDIAN).asFloatBuffer();
                        float[] farr = new float[numElements];
                        ib.get(farr);
                        return new LabeledBuffer(label, farr);
                    } else {
                        MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
                        LabeledBuffer buff = convByteArrayToBuffer(label,type,byteBuffer,numElements);

                        ((DirectBuffer)byteBuffer).cleaner().clean();
                        return buff;
                    }
                case DOUBLE:
                    if (compressed) {
                        GZIPInputStream gis = new GZIPInputStream(fis);
                        byte[] ucBytes = IOUtils.toByteArray(gis);

                        DoubleBuffer ib = ByteBuffer.wrap(ucBytes).order(ByteOrder.BIG_ENDIAN).asDoubleBuffer();
                        double[] darr = new double[numElements];
                        ib.get(darr);
                        return new LabeledBuffer(label, darr);
                    } else {
                        MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
                        LabeledBuffer buff = convByteArrayToBuffer(label,type,byteBuffer,numElements);

                        ((DirectBuffer)byteBuffer).cleaner().clean();
                        return buff;
                    }
                case SHORT:
                    if (compressed) {
                        GZIPInputStream gis = new GZIPInputStream(fis);
                        byte[] ucBytes = IOUtils.toByteArray(gis);

                        ShortBuffer ib = ByteBuffer.wrap(ucBytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
                        short[] sarr = new short[numElements];
                        ib.get(sarr);
                        return new LabeledBuffer(label, sarr);
                    } else {
                        MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
                        LabeledBuffer buff = convByteArrayToBuffer(label, type, byteBuffer, numElements);

                        ((DirectBuffer)byteBuffer).cleaner().clean();
                        return buff;
                    }
                default:
                    throw new IllegalArgumentException("Unhandled type: " + type);
            }
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fc);
            if (DEBUG_TIMING)
                printf("BufferDiskCache.readFile: %d  ms  type: %s elems: %d\n", (System.currentTimeMillis() - t0), type, numElements);
        }
    }

    /**
     * Convert byte buffers into labeled buffers.  In theory later we might optimize this by using unsafe methods.
     *
     * @param label
     * @param type
     * @param byteBuffer
     * @param numElements
     * @return
     */
    private LabeledBuffer convByteArrayToBuffer(String label, LabeledBuffer.Type type, ByteBuffer byteBuffer, int numElements) {
        switch(type) {
            case INT:
                IntBuffer intBuffer = byteBuffer.asIntBuffer();
                int[] iarr = new int[numElements];
                intBuffer.get(iarr);
                return new LabeledBuffer(label, iarr);
            case FLOAT:
                FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
                float[] farr = new float[numElements];
                floatBuffer.get(farr);
                return new LabeledBuffer(label, farr);
            case DOUBLE:
                DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
                double[] darr = new double[numElements];
                doubleBuffer.get(darr);
                return new LabeledBuffer(label, darr);
            case SHORT:
                ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                short[] sarr = new short[numElements];
                shortBuffer.get(sarr);
                return new LabeledBuffer(label, sarr);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }


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

    static class ThreadVars {
        public HashMap<String, Object> extra;


        public ThreadVars() {
            extra = new HashMap<>();
        }
    }
}
