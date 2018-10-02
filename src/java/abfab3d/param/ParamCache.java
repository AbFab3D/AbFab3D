package abfab3d.param;

import abfab3d.core.CacheConfig;
import com.google.common.cache.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Caches parameters by value.  This is a singleton class meant to avoid expensive calculations when datasources are
 * recreated.  The key should be a value based key or hash from the parameters involved in the calculation.  This class
 * will attempt to preserve the calculation but the caller should always be prepared to regenerate if the value is lost
 * due to memory pressures.
 *
 * @author Alan Hudson
 */
public class ParamCache {
    private static final boolean STOP_CACHING;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_MISSES = false;
    private static final boolean REPORT_MEMORY_REFERENCE = false;

    private static final int JOB_RETAIN_MS = 60 * 60 * 1000;

    private static ParamCache instance;
    private static LoadingCache<String, Object> cache;
    private static BoundedStack<String> misses;

    static {
        if (DEBUG_MISSES) {
            misses = new BoundedStack<String>(25);
        }

        STOP_CACHING = !CacheConfig.CPU_CACHE;

        if (STOP_CACHING) {
            printf("*** Param Cache caching is turned off ***\n");
            new Exception().printStackTrace();
        }

        if (REPORT_MEMORY_REFERENCE) {
            printf("*** Report memory reference is turned on.  Do not release this code  ***\n");
        }
    }

    private ParamCache() {
        cache = CacheBuilder.newBuilder()
                .softValues()
                .expireAfterAccess(JOB_RETAIN_MS, TimeUnit.MILLISECONDS)
                .recordStats()
                .build(
                        new CacheLoader<String, Object>() {
                            public Object load(String key) throws ExecutionException {
                                if (DEBUG_MISSES) {
                                    misses.push(key);
                                }
                                // TODO: Not sure this is the greatest thing performance wise as an exception is expensive to create
                                throw new ExecutionException("Can't load key: ",null);
                            }
                        }
                );
    }

    public static ParamCache getInstance() {
        if (instance == null) {
            instance = new ParamCache();
        }

        return instance;
    }

    public Object get(String key) {
        if (STOP_CACHING) return null;
        if (REPORT_MEMORY_REFERENCE) {
            if (key.contains("@")) new IllegalArgumentException(fmt("Key contains @: %s",key)).printStackTrace();
        }

        try {
            Object co = cache.get(key);
            if (DEBUG) printf("Cache.get: %s SUCCESS\n", key);
            return co;
        } catch (ExecutionException ee) {
            if (DEBUG) printf("Cache.get: %s FAILED\n", key);
            return null;
        }
    }

    /**
       discard all entries
     */
    public void invalidateAll(){
        cache.invalidateAll();
    }

    public void remove(String key) {
        cache.invalidate(key);
    }

    public void put(String key, Object o) {
        if (STOP_CACHING) return;
        if (REPORT_MEMORY_REFERENCE) {
            if (key.contains("@")) new IllegalArgumentException(fmt("Key contains @: %s",key)).printStackTrace();
        }

        if (DEBUG) {
            printf("Cache.put: %s\n", key);
        }
        cache.put(key, o);
    }

/*  // Alan: removed this method as its getParamString usage was weird, so far haven't found anything using it, recode if you do
    public Object get(String name, Object src, Parameter[] params) {
        String vhash = BaseParameterizable.getParamString(name,src, params);
        return ParamCache.getInstance().get(vhash);
    }
*/
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

class BoundedStack<T> {

    private int limit;
    private LinkedList<T> list;

    public BoundedStack(int limit) {
        this.limit = limit;
        list = new LinkedList<T>();
    }

    public void push(T item) {
        if (list. size() == limit) {
            list.removeLast();
        }
        list.addFirst(item);
    }

    public int size() {
        return list.size();
    }

    public List<T> getAll() {
        return list;
    }

    public T peek() {
        return list.get(0);
    }
}