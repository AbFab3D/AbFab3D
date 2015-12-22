package abfab3d.param;

import com.google.common.cache.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static abfab3d.util.Output.printf;

/**
 * Caches parameters by value.  This is a singleton class meant to avoid expensive calculations when datasources are
 * recreated.  The key should be a value based key or hash from the parameters involved in the calculation.  This class
 * will attempt to preserve the calculation but the caller should always be prepared to regenerate if the value is lost
 * due to memory pressures.
 *
 * @author Alan Hudson
 */
public class ParamCache {
    private static final boolean STOP = false;
    private static final boolean DEBUG = false;
    private static final int JOB_RETAIN_MS = 5 * 60 * 1000;

    private static ParamCache instance;
    private static LoadingCache<String, Object> cache;

    private ParamCache() {
        cache = CacheBuilder.newBuilder()
                .softValues()
                .expireAfterAccess(JOB_RETAIN_MS, TimeUnit.MILLISECONDS)
                .build(
                        new CacheLoader<String, Object>() {
                            public Object load(String key) throws ExecutionException {
                                throw new ExecutionException(new IllegalArgumentException("Can't load key: " + key));
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
        if (STOP) return null;

        try {
            Object co = cache.get(key);
            if (DEBUG) printf("Cache.get: %s SUCCESS\n", key);
            return co;
        } catch (ExecutionException ee) {
            if (DEBUG) printf("Cache.get: %s FAILED\n", key);
            return null;
        }
    }

    public void remove(String key) {
        cache.invalidate(key);
    }

    public void put(String key, Object o) {
        if (STOP) return;

        if (DEBUG) {
            printf("Cache.put: %s\n", key);
        }
        cache.put(key, o);
    }

    public Object get(String name, Object src, Parameter[] params) {
        String vhash = BaseParameterizable.getParamString(name,src, params);
        return ParamCache.getInstance().get(vhash);
    }
}
