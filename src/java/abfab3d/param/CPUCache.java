package abfab3d.param;

import abfab3d.core.LabeledBuffer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.Output.printf;

/**
 * Caches parameters by value.  This is a singleton class meant to avoid expensive calculations when datasources are
 * recreated.  The key should be a value based key or hash from the parameters involved in the calculation.  This class
 * will attempt to preserve the calculation but the caller should always be prepared to regenerate if the value is lost
 * due to memory pressures.
 *
 * @author Alan Hudson
 */
public class CPUCache {
    private static final boolean STOP_CACHING = false;
    private static final boolean USE_DISK_CACHE = true;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_MISSES = true;
    private static final int JOB_RETAIN_MS = 60 * 60 * 1000;

    private static CPUCache instance;
    private static LoadingCache<String, LabeledBuffer> cache;
    private static BoundedStack<String> misses;

    static {
        if (DEBUG_MISSES) {
            misses = new BoundedStack<String>(25);
        }

        if (STOP_CACHING) {
            printf("*** CPUCache caching is turned off ***\n");
            new Exception().printStackTrace();
        }
    }

    private CPUCache() {
        cache = CacheBuilder.newBuilder()
                .softValues()
                .expireAfterAccess(JOB_RETAIN_MS, TimeUnit.MILLISECONDS)
                .recordStats()
                .build(
                        new CacheLoader<String, LabeledBuffer>() {
                            public LabeledBuffer load(String key) throws ExecutionException {
                                if (DEBUG_MISSES) {
                                    misses.push(key);
                                }
                                // TODO: Not sure this is the greatest thing performance wise as an exception is expensive to create
                                throw new ExecutionException("Can't load key: ",null);
                            }
                        }
                );
    }

    public static CPUCache getInstance() {
        if (instance == null) {
            instance = new CPUCache();
        }

        return instance;
    }

    public LabeledBuffer get(String label) {
        if (STOP_CACHING) return null;

        try {
            LabeledBuffer co = cache.get(label);
            if (DEBUG) printf("CPUCache.get: %s SUCCESS\n", label);
            return co;
        } catch (ExecutionException ee) {
            if (DEBUG) printf("CPUCache.get: %s FAILED\n", label);

            if (DEBUG_MISSES) printf("CPUCache missed: %s\n",label);
            LabeledBuffer di = DiskCache.getInstance().get(label);
            if (DEBUG) printf("CPUCache checking DiskCache: %s\n",di);

            if (di != null) {
                put(di,true);
                return di;
            }
            return null;
        }
    }

    public void remove(String label) {
        cache.invalidate(label);
    }

    public void put(LabeledBuffer buffer) {
        put(buffer,false);
    }

    private void put(LabeledBuffer buffer, boolean justLoaded) {
        if (STOP_CACHING) return;

        if (DEBUG) {
            printf("Cache.put: %s\n", buffer.getLabel());
        }
        cache.put(buffer.getLabel(), buffer);

        if (USE_DISK_CACHE && !justLoaded) {
            DiskCache.getInstance().put(buffer);
        }
    }

    public CacheStats getStats() {
        return cache.stats();
    }

    /**
     * Get the last cache misses.  Class must be compiled with DEBUG_MISSES for anything to be returned
     * @return The list or an empty list if not enabled
     */
    public List<String> getMisses() {
        if (misses != null) {
            return misses.getAll();
        }

        return new ArrayList<String>(0);
    }
}

