/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.opencl;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.printf;

/**
 * OpenCL Resource manager.  Tracks and manages resource usage on a GPU.  This class does not
 * handle the allocation or transfer.
 *
 * @author Alan Hudson
 */
public class CLResourceManager implements Runnable {
    /** How long before we clean out a resource */
    private static final int DEFAULT_TIMEOUT_MS = 60 * 1000;
    private int timeout;

    private long maxBytes;
    private long currBytes;

    private Map<Resource, CacheEntry> cache;
    private volatile boolean freeing;
    private ScheduledExecutorService scheduler;

    public CLResourceManager(long capacity) {
        this(capacity,DEFAULT_TIMEOUT_MS);
    }

    public CLResourceManager(long capacity, int timeout) {
        this.maxBytes = capacity;
        this.timeout = timeout;
        freeing = false;

        // My expectation is the number of threads will be low(ie # GPUs) so a ConcurrentLinkedHashMap is not needed.
        cache = Collections.synchronizedMap(new LinkedHashMap<Resource, CacheEntry>());
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this, timeout, timeout, TimeUnit.MILLISECONDS);

    }

    /**
     * Add a resource for management.  This will remove other buffers if necessary.
     * @param resource
     */
    public void add(Resource resource, long size) {
        insureCapacity(size);

        cache.put(resource, new CacheEntry(resource,size));
        currBytes += size;
    }

    /**
     * Checks whether a buffer is resident on the GPU.
     * @param resource
     * @return
     */
    public boolean isResident(Resource resource) {
        while(freeing) {
            try { Thread.sleep(10); } catch(InterruptedException ie) {}
        }

        CacheEntry ce = cache.get(resource);
        if (ce == null) return false;
        ce.lastAccess = System.currentTimeMillis();

        return true;
    }

    /**
     * Remove a resource from management and release its GPU resources.
     */
    public void release(Resource resource) {
        freeing = true;

        try {
            CacheEntry ce = cache.remove(resource);
            if (ce == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }
            currBytes -= ce.size;
            resource.release();
        } finally {
            freeing = false;
        }
    }

    /**
     * Insures there is capacity available.  Will evict resources.
     * @param bytes
     */
    public void insureCapacity(long bytes) {
        if (bytes > maxBytes) {
            throw new IllegalArgumentException("Requested size: " + bytes + " exceeds capacity: " + maxBytes);
        }
        if (bytes + currBytes > maxBytes) {
            freeMemory(bytes);
        }
    }

    /**
     * Insure free memory is at least bytes large.  Will remove resources on a FIFO basis.
     * @param bytes
     */
    private void freeMemory(long bytes) {
        freeing = true;

        // TODO: This should take one pass and get rid of timeout resources and if not nuke oldest
        try {
            long freeMemory = maxBytes - currBytes;
            Iterator<Map.Entry<Resource,CacheEntry>> itr = cache.entrySet().iterator();

            ArrayList<Resource> toremove = null;
            long time = System.currentTimeMillis();

            while(freeMemory < bytes && itr.hasNext()) {
                Map.Entry<Resource, CacheEntry> me = itr.next();
                if (me == null) {
                    printf("Nothing left to free\n");
                    return;
                }

                CacheEntry ce = me.getValue();
                if (time > ce.lastAccess + timeout) {
                    printf("Freeing: %s\n", me.getKey());
                    ce.resource.release();
                    if (toremove == null) toremove = new ArrayList();
                    toremove.add(me.getKey());
                    currBytes -= ce.size;
                    freeMemory = maxBytes - currBytes;
                }
            }

            if (toremove != null) {
                for (Resource r : toremove) {
                    cache.remove(r);
                }
            }

            if (freeMemory < bytes) {
                printf("Trying harder to remove memory\n");
                // TODO: Stop generating garbage
                List<Map.Entry<Resource, CacheEntry>> sorted = new ArrayList(cache.entrySet());
                Collections.sort(sorted, new Comparator<Map.Entry<Resource, CacheEntry>>() {
                    @Override
                    public int compare(Map.Entry<Resource, CacheEntry> o1, Map.Entry<Resource, CacheEntry> o2) {
                        return Long.compare(o1.getValue().lastAccess, o2.getValue().lastAccess);
                    }
                });

                itr = sorted.iterator();

                toremove = null;
                time = System.currentTimeMillis();

                while(freeMemory < bytes && itr.hasNext()) {
                    Map.Entry<Resource, CacheEntry> me = itr.next();
                    if (me == null) {
                        printf("Nothing left to free\n");
                        return;
                    }

                    CacheEntry ce = me.getValue();
                    printf("Freeing: %s\n", me.getKey());
                    ce.resource.release();
                    if (toremove == null) toremove = new ArrayList();
                    toremove.add(me.getKey());
                    currBytes -= ce.size;
                    freeMemory = maxBytes - currBytes;
                }

                if (toremove != null) {
                    for (Resource r : toremove) {
                        cache.remove(r);
                    }
                }
            }
        } finally {
            freeing = false;
        }
    }

    /**
     * Clear out old entries
     */
    @Override
    public void run() {
        Iterator<Map.Entry<Resource,CacheEntry>> itr = cache.entrySet().iterator();

        long time = System.currentTimeMillis();

        printf("Clearing old entries\n");
        freeing = true;
        try {
            while (itr.hasNext()) {
                Map.Entry<Resource, CacheEntry> me = itr.next();
                CacheEntry ce = me.getValue();
                if (time > ce.lastAccess + timeout) {
                    printf("Removing old: %s\n", ce.resource);
                    release(ce.resource);
                } else {
                    break;
                }
            }
        } finally {
            freeing = false;
        }
    }

    static class CacheEntry implements Comparator {
        public long lastAccess;
        public Resource resource;
        public long size;

        public CacheEntry() {

        }

        public CacheEntry(Resource resource, long size) {
            this.resource = resource;
            this.size = size;
            lastAccess = System.currentTimeMillis();
        }

        @Override
        public int compare(Object o1, Object o2) {
            CacheEntry ce1 = (CacheEntry) o1;
            CacheEntry ce2 = (CacheEntry) o2;

            return Long.compare(ce1.lastAccess,ce2.lastAccess);
        }
    }
}

