package abfab3d.param;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import static abfab3d.core.Output.printf;

/**
 * Disk based cache to manage file resources.  The paths can be specific files or directories.
 *
 * This class is designed to be thread safe though its not very efficient at it.
 *
 * Fixed size
 * LRU Eviction
 *
 * @author Alan Hudson
 */
public class FileDiskCache {
    private static final boolean DEBUG = false;
    private static final long DEFAULT_SIZE = (long) (4 * 1e9);
    private static final int MAX_FILENAME_LENGTH = 108;

    /** The directory to place files */
    private String dir;

    /** The maximum size in bytes */
    private long maxSize;

    private long currentSize;
    private HashMap<String,CacheEntry> entries = new HashMap<String,CacheEntry>();

    private static ThreadLocal<ThreadVars> threadVars = new ThreadLocal<ThreadVars>() {
        public ThreadVars initialValue() {
            ThreadVars ret_val = new ThreadVars();

            return ret_val;
        }
    };

    public FileDiskCache(String dir) {
        this(dir, DEFAULT_SIZE);
    }

    public FileDiskCache(String dir, long maxSize) {
        this.dir = dir;
        this.maxSize = maxSize;

        File d = new File(dir);
        d.mkdirs();

        loadEntries();
    }

    /**
     * Add a file to the cache.  The file will be moved into the caches directory.
     * If the key exists its contents will be returned.  If you want overwrite an entry call remove before put
     *
     * @param key The unique key
     * @param path The path to the file
     * @return The permanent path to use
     */
    public synchronized String put(String key, String path) throws IOException {
        String ret_val = get(key);

        if (ret_val != null) return ret_val;

        File f = new File(path);

        long size;

        if (f.isDirectory()) {
            size = FileUtils.sizeOfDirectory(f);
        } else {
            size = f.length();
        }

        if (!insureCapacity(size)) {
            // Return the original path without storing
            return path;
        }

        return addEntry(key, null, f);
    }

    /**
     * Add a file to the cache.  The file will be moved into the caches directory.
     * If the key exists its contents will be returned.  If you want overwrite an entry call remove before put
     *
     * @param key The unique key
     * @param meta Extra meta information
     * @param path The path to the file
     * @return The permanent path to use
     */
    public synchronized String put(String key, Map<String,Object> meta, String path) throws IOException {
        String ret_val = get(key);

        if (ret_val != null) return ret_val;

        File f = new File(path);

        long size;

        if (f.isDirectory()) {
            size = FileUtils.sizeOfDirectory(f);
        } else {
            size = f.length();
        }

        if (!insureCapacity(size)) {
            // Return the orignal path without storing
            return path;
        }

        return addEntry(key, meta, f);
    }

    /**
     * Remove an entry
     * @param key
     */
    public synchronized boolean remove(String key) {
        if (DEBUG) printf("Removing: %s\n",key);

        CacheEntry ce = entries.get(key);

        if (ce == null) return true;

        File md = new File(ce.path + ".meta");
        if (!md.delete()) printf("Delete failed: %s\n",md);

        File df = new File(ce.path);
        FileUtils.deleteQuietly(df);

        if (df.exists()) return false;

        entries.remove(key);
        currentSize -= ce.size;

        return true;
    }

    /**
     * Get an entry from a key.  Updates last access time for LRU logic
     * @param key
     * @return
     */
    public synchronized String get(String key) {

        CacheEntry me = entries.get(key);
        if (me != null) {
            File f = new File(me.path);
            if (!f.exists()) return null;

            // Update the access time in the entry
            updateAccessTime(me);
            
            // Update the access time in the meta file
            File meta = new File(me.path + ".meta");
            try {
            	CacheEntry.update(meta,me);
            } catch (IOException ioe) {
            	printf("*** Failed to update last access time in meta file.  File: %s\n", meta.getAbsolutePath());
            	printf("    Ignoring update of meta file\n");
            }
            
            return me.path;
        }

        return null;
    }

    /**
     * Get an entry from a key.  Updates last access time for LRU logic
     * @param key
     * @return
     */
    public synchronized String get(String key, Map<String,Object> extra) {
        if (DEBUG) printf("FileDiskCache.get: %s found: %b\n",key,entries.containsKey(key));
        CacheEntry me = entries.get(key);
        if (me != null) {
            if (DEBUG) printf("Cache entry found: %s  path: %s\n",key,me.path);
            File f = new File(me.path);
            if (!f.exists()) {
                if (DEBUG) printf("FileDiskCache file not on disk?");
                return null;
            }

            // Update the access time in the entry
            updateAccessTime(me);

            // Update the access time in the meta file
            File meta = new File(me.path + ".meta");
            try {
                CacheEntry.update(meta,me);
            } catch (IOException ioe) {
                printf("*** Failed to update last access time in meta file.  File: %s\n", meta.getAbsolutePath());
                printf("    Ignoring update of meta file\n");
            }

            if (me.extra != null) {
                extra.putAll(me.extra);
            }

            if (DEBUG) printf("FileDiskCache returning success: %s\n",me.path);
            return me.path;
        }

        return null;
    }

    /**
     * Clear all entries, this will delete items on disk
     */
    public synchronized void clear() {
        if (DEBUG) printf("Clearing entries:\n");
        File fdir = new File(dir);

        // Delete each entry separately in case someone is in the directory
        for(File file : fdir.listFiles()) {
            try {
                if (file.isDirectory()) {
                    FileUtils.deleteDirectory(file);
                } else {
                    file.delete();
                }
            } catch(IOException ioe2) {
                // ignore
            }
        }

        fdir.mkdirs();

        entries.clear();
        currentSize = 0;
    }


    /**
     * Update the access time to reflect recent activity
     */
    private void updateAccessTime(CacheEntry entry) {
        entry.lastAccess = System.currentTimeMillis();
    }

    /**
     * Insure there is capacity for an object
     * @param size
     */
    private boolean insureCapacity(long size) {
        //printf("Insuring capacity, current size: %d  max: %d req: %d\n",currentSize,maxSize,(currentSize + size));

        if (currentSize + size <= maxSize) return true;


        List<Map.Entry<String, CacheEntry>> sorted = new ArrayList(entries.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<String, CacheEntry>>() {
            public int compare(Map.Entry<String, CacheEntry> o1, Map.Entry<String, CacheEntry> o2) {
                return Long.compare(o1.getValue().lastAccess, o2.getValue().lastAccess);
            }
        });

        for(Map.Entry<String,CacheEntry> entry : sorted) {
            remove(entry.getKey());

            if (currentSize + size <= maxSize) break;
        }

        if (currentSize <= maxSize) return true;
        return false; // Couldnt do it
    }

    /**
     * Adds an entry.  Copies the file over to the managed area, returns the new path.
     * @param key
     * @param path
     * @return
     */
    private String addEntry(String key,Map<String,Object> extra,File path) throws IOException {

        if (path.isDirectory()) {
            String filename = convKeyToFilename(key,null);
            File dest = new File(dir,filename);
            FileUtils.moveDirectory(path,dest);

            String ret_val = dest.getAbsolutePath();
            CacheEntry ce = new CacheEntry();
            ce.lastAccess = System.currentTimeMillis();
            ce.key = key;
            ce.path = ret_val;
            ce.size = FileUtils.sizeOfDirectory(dest);
            HashMap<String,Object> md = new HashMap<>();
            if (extra != null) md.putAll(extra);
            ce.extra = md;

            entries.put(key, ce);
            currentSize += ce.size;

            File meta = new File(dir,filename + ".meta");
            CacheEntry.update(meta,ce);
            return ret_val;
        } else {
            String filename = convKeyToFilename(key,FilenameUtils.getExtension(path.toString()));
            File dest = new File(dir,filename);

            if (DEBUG) printf("add Entry.  key: %s\n",key);
            if (!dest.exists()) {
                if (DEBUG) printf("Moving: %s to: %s\n", path, dest);
                FileUtils.moveFile(path, dest);
            }

            String ret_val = dest.getAbsolutePath();
            CacheEntry ce = new CacheEntry();
            ce.lastAccess = System.currentTimeMillis();
            ce.key = key;
            ce.path = ret_val;
            ce.size = dest.length();
            HashMap<String,Object> md = new HashMap<>();
            if (extra != null) md.putAll(extra);
            ce.extra = md;

            entries.put(key, ce);
            currentSize += ce.size;

            File meta = new File(dir,filename + ".meta");
            CacheEntry.update(meta,ce);
            return ret_val;
        }
    }

    /**
     * Convert a key to a valid filename.  It must not clash with similar unique keys.  Ideally it would
     * stay semireadable
     * @param key
     * @return
     */
    public String convKeyToFilename(String key, String ext) {
        // replace bad directory characters
        String ret_val = key.replaceAll("[ :\\\\/*\"?|<>'.;#$=]", "");
        ThreadVars tvars = threadVars.get();

        // TODO: I've seen some model reuses that make the think 32 bits is not enough here
        CRC32 crc = tvars.crc;
        StringBuilder sb = tvars.sb;

        // Add a crc to insure uniqueness
        crc.reset();
        crc.update(key.getBytes());

        String crcSt = Long.toHexString(crc.getValue());

        sb.setLength(0);
        int elen = 0;
        if (ext != null) elen = ext.length() + 1;

        int len = ret_val.length() + crcSt.length() + 1 + elen;

        if (len > MAX_FILENAME_LENGTH){
            int start = len - MAX_FILENAME_LENGTH;

            ret_val = ret_val.substring(0,ret_val.length() - start);
        }
        sb.append(ret_val);
        sb.append("_");
        sb.append(crcSt);

        if (ext != null && ext.length() > 0) {
            sb.append(".");
            sb.append(ext);
        }
        return sb.toString();
    }

    private void loadEntry(String name) {
        File entry = new File(dir,name);
        if (!entry.exists()) {
            return;
        }

        File meta = new File(dir,name + ".meta");
        CacheEntry me = CacheEntry.parse(meta);
        if (me == null) {
            printf("Cannot load entry: %s\n",name);
            return;
        }

        me.path = entry.getAbsolutePath();
        if (DEBUG) printf("Loaded entry: %s -> %s size: %d\n",me.key,me.path,me.size);
        entries.put(me.key, me);
        currentSize += me.size;
    }

    /**
     * Load entries into memory for faster operations
     */
    private void loadEntries() {
        currentSize = 0;

        File fdir = new File(dir);

        File[] files = fdir.listFiles();

        for(File f : files) {
            String name = FilenameUtils.getBaseName(f.getName());
            String ext = FilenameUtils.getExtension(f.getName());

            if (ext.equals("meta")) {
                loadEntry(name);
            }
        }
    }

    public long getCurrentSize() {
        return currentSize;
    }

    static class CacheEntry implements Comparator {
        public long lastAccess;
        public String key;
        public String path;
        public long size;
        public Map<String,Object> extra;

        public static CacheEntry parse(File f) {
            try {
                String json = FileUtils.readFileToString(f);
                if (json == null) return null;

                Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
                Map val = gson.fromJson(json, Map.class);
                if (val == null) return null;

                CacheEntry me = new CacheEntry();
                me.lastAccess = ((Number) val.get("lastAccess")).longValue();
                me.key = (String) val.get("key");
                if (val.containsKey("extra")) {
                    me.extra = (Map<String,Object>) val.get("extra");
                }
                String metaPath = f.getAbsolutePath();
                String filePath = null;
                int idx = metaPath.indexOf(".meta");
                if (idx > 0) {
                    filePath = metaPath.substring(0,idx);
                } else {
                    filePath = metaPath;
                }

                File df = new File(filePath);
                if (!df.isDirectory()) {
                    me.size = df.length();
                } else {
                    me.size = FileUtils.sizeOfDirectory(df);
                }

                return me;
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return null;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Update or create an entry
         * @param f
         * @param me
         */
        public static void update(File f, CacheEntry me) throws IOException {
            HashMap val = new HashMap();
            val.put("lastAccess", me.lastAccess);
            val.put("key",me.key);
            if (me.extra != null) {
                val.put("extra",me.extra);
            }

            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();

            String json = gson.toJson(val);

            FileUtils.write(f,json);
        }

        public int compare(Object o1, Object o2) {
            CacheEntry ce1 = (CacheEntry) o1;
            CacheEntry ce2 = (CacheEntry) o2;

            return Long.compare(ce1.lastAccess,ce2.lastAccess);
        }

    }

    static class ThreadVars {
        public StringBuilder sb;
        public CRC32 crc;

        public ThreadVars() {
            sb = new StringBuilder();
            crc = new CRC32();
        }
    }

}
