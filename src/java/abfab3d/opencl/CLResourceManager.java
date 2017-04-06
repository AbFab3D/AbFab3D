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

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLObject;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static abfab3d.core.Output.printf;

/**
 * OpenCL Resource manager.  Tracks and manages resource usage on a GPU.  This class does not
 * handle the allocation or transfer.
 *
 * Fixed sized memory pool
 * Eviction is based on timeout and then LRU if it needs more
 *    TODO: should we fix this?  Should this be time + LRU
 *
 * @author Alan Hudson
 */
public class CLResourceManager implements Runnable {
    private static final boolean DEBUG = false;
    /** How long before we clean out a resource */
    private static final int DEFAULT_TIMEOUT_MS = 360 * 1000;
    private int timeout;

    private long maxBytes;
    private long currBytes;

    private Map<Resource, CacheEntry> cache;
    private volatile boolean freeing;
    private ScheduledExecutorService scheduler;
    private long contextID;  // The context string of this instance
    private boolean shutdown;  // Has this manager been shutdown

    /** Single managers per context */
    private static ConcurrentHashMap<Long,CLResourceManager> managers = new ConcurrentHashMap<Long, CLResourceManager>();

    private CLResourceManager(long capacity) {
        this(capacity, DEFAULT_TIMEOUT_MS);
    }

    private CLResourceManager(long capacity, int timeout) {
        this.maxBytes = capacity;
        this.timeout = timeout;
        freeing = false;

        // My expectation is the number of threads will be low(ie # GPUs) so a ConcurrentLinkedHashMap is not needed.
        //cache = Collections.synchronizedMap(new LinkedHashMap<Resource, CacheEntry>());
        cache = new ConcurrentHashMap<Resource, CacheEntry>();

        /*
        //  Commented out, use explicit cleanupOldEntries method when its safe
        scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("CLResourceManager"));

        scheduler.scheduleAtFixedRate(this, timeout, timeout, TimeUnit.MILLISECONDS);
        */
    }

    public static CLResourceManager getInstance(long contextID) {
        return getInstance(contextID,-1,DEFAULT_TIMEOUT_MS);
    }

    public static CLResourceManager getInstance(long contextID, long capacity) {
        return getInstance(contextID,capacity,DEFAULT_TIMEOUT_MS);
    }

    public static CLResourceManager getInstance(long contextID, long capacity, int timeout) {
        if (DEBUG) printf("CLRM rm: %s  thread: %s cl: %s\n",managers,Thread.currentThread(),Thread.currentThread().getContextClassLoader());
        CLResourceManager rm = managers.get(contextID);

        if (rm != null && !rm.isShutdown()) {
            return rm;
        }

        if (capacity < 0) {
            throw new IllegalArgumentException("CLResourceManager not properly initialized");
        }

        if (DEBUG) printf("CLRM creating resource manager for: %s\n",contextID);
        rm = new CLResourceManager(capacity,timeout);
        rm.setContextID(contextID);
        managers.put(contextID,rm);

        return rm;
    }

    protected void setContextID(long id) {
        contextID = id;
    }

    /**
     * Add a resource for management.  This will remove other buffers if necessary.  Not locked by default
     */
    public void add(Resource resource, long size) {
        add(resource,size,false,null);
    }

    /**
     * Add a resource for management.  This will remove other buffers if necessary.
     */
    public void add(Resource resource, long size, boolean lock) {
        add(resource,size,lock,null);
    }

    /**
     * Add a resource for management.  This will remove other buffers if necessary.
     */
    public void add(Resource resource, long size, boolean lock, ResourceRemovedListener listener) {
        if (shutdown) throw new IllegalArgumentException("Manager already shutdown: contextID: " + contextID + " this:" + this);
        if (resource == null) throw new IllegalArgumentException("Cannot add a null resource\n");
        if (resource.isReleased()) throw new IllegalArgumentException("Cannot add a released resource");

        if (DEBUG) printf("CLRM add: %s underlying: %s size: %d  entries: %d\n",resource,((OpenCLResource)resource).getResource(),size,cache.size());
        insureCapacity(size);

        cache.put(resource, new CacheEntry(resource,size,lock,listener));
        currBytes += size;
    }

    /**
     * Lock the resource.  This resource will not be removed until unlocked or released.
     *
     * @param resource
     */
    public void lockResource(Resource resource) {
        CacheEntry ce = cache.get(resource);

        if (ce == null) throw new IllegalArgumentException("Unknown resource");

        ce.setLocked(true);
    }

    public void unlockResource(Resource resource) {
        CacheEntry ce = cache.get(resource);

        if (ce == null) throw new IllegalArgumentException("Unknown resource");

        ce.setLocked(false);
    }

    /**
     * Checks whether a buffer is resident on the GPU.
     * @param resource
     * @return
     */
    public boolean isResident(Resource resource) {

        if (resource == null) return false;
        if (DEBUG) printf("CLRM isResident: %s underlying: %s\n",resource,((OpenCLResource)resource).getResource());

        CacheEntry ce = cache.get(resource);
        if (ce == null) return false;

        // sanity check make sure opencl doesnt think its released
        if (resource instanceof OpenCLResource) {
            OpenCLResource oclr = (OpenCLResource) resource;
            if (oclr.getResource().isReleased()) return false;

            CLObject clobj = (CLObject) oclr.getResource();
            CLContext context = clobj.getContext();

            // Check to make sure this resource belongs to this GPU
            if (context.getID() != contextID) {
                if (DEBUG) printf("CLRM resource not on this context");
                return false;
            }


        }
        ce.lastAccess = System.currentTimeMillis();

        return true;
    }

    /**
     * Remove a resource from management and release its GPU resources.
     */
    public void release(Resource resource) {
        waitForNotFreeing();

        freeing = true;

        if (DEBUG) printf("CLRM release: %s  underlying: %s\n",resource,((OpenCLResource)resource).getResource());
        try {
            CacheEntry ce = cache.remove(resource);
            if (ce == null) {
                // already removed, ignore
                return;
            }
            currBytes -= ce.size;
            resource.release();
            ce.resource = null;
        } finally {
            freeing = false;
        }
    }

    /**
     * Insures there is capacity available.  Will evict resources.
     * @param bytes
     */
    public void insureCapacity(long bytes) {
        if (DEBUG) printf("CLRM Check capacity:  req: %d reqTot: %d  max: %d\n",bytes,(bytes+currBytes),maxBytes);

        if (bytes > maxBytes) {
            throw new IllegalArgumentException("Requested size: " + bytes + " exceeds capacity: " + maxBytes);
        }
        if (bytes + currBytes > maxBytes) {
            freeMemory(bytes);
        }
    }

    /**
     * Insure free memory is at least bytes large.  Will remove resources on a LRU basis.
     * @param bytes
     */
    private void freeMemory(long bytes) {
        waitForNotFreeing();

        freeing = true;

        try {
            long freeMemory = maxBytes - currBytes;
            Iterator<Map.Entry<Resource,CacheEntry>> itr = cache.entrySet().iterator();

            ArrayList<Resource> toremove = null;
            long time = System.currentTimeMillis();

            // TODO: decide how much we want this to really be LRU, would have to sort each time

            while(freeMemory < bytes && itr.hasNext()) {
                Map.Entry<Resource, CacheEntry> me = itr.next();
                if (me == null) {
                    if (DEBUG) printf("CLRM Nothing left to free\n");
                    return;
                }

                CacheEntry ce = me.getValue();
                if (!ce.locked && time > ce.lastAccess + timeout) {
                    if (DEBUG) printf("CLRM Freeing1: %s resc: %s\n", me.getKey(),ce.resource);
                    if (!ce.resource.isReleased()) {
                        ce.resource.release();
                    }
                    if (ce.listener != null) ce.listener.resourceRemoved(ce.resource,contextID);
                    ce.resource = null;
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
                if (DEBUG) printf("CLRM Trying harder to remove memory\n");
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
                        if (DEBUG) printf("CLRM Nothing left to free\n");
                        return;
                    }

                    CacheEntry ce = me.getValue();

                    if (!ce.locked) {
                        if (DEBUG) printf("CLRM Freeing2: %s\n", me.getKey());
                        if (!ce.resource.isReleased()) {
                            ce.resource.release();
                        }
                        if (ce.listener != null) ce.listener.resourceRemoved(ce.resource,contextID);
                        ce.resource = null;
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
            }
        } finally {
            freeing = false;
        }
    }

    /**
     * Wait for non freeing state
     */
    private void waitForNotFreeing() {
        while (freeing) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * Clear out old entries
     */
    @Override
    public void run() {
        cleanOldEntries();
    }


    /**
       clear all entries
     */
    public void clearAll() {

        if (DEBUG) printf("%s.cleaAll()\n",this);
        waitForNotFreeing();

        freeing = true;
        Iterator<Map.Entry<Resource,CacheEntry>> itr = cache.entrySet().iterator();
        long time = System.currentTimeMillis();

        try {
            while (itr.hasNext()) {
                Map.Entry<Resource, CacheEntry> me = itr.next();
                CacheEntry ce = me.getValue();
                if (!ce.locked) {
                    itr.remove();
                    ce.resource.release();
                    ce.resource = null;
                } 
            }
        } finally {
            freeing = false;
        }        
    }
    
    public void cleanOldEntries() {
        waitForNotFreeing();

        freeing = true;
        Iterator<Map.Entry<Resource,CacheEntry>> itr = cache.entrySet().iterator();

        long time = System.currentTimeMillis();

        if (DEBUG) printf("CLRM Clearing old entries.  size: %d\n",cache.size());
        try {
            while (itr.hasNext()) {
                Map.Entry<Resource, CacheEntry> me = itr.next();
                CacheEntry ce = me.getValue();
                if (DEBUG) printf("CLRM checking: %s lastAccess: %d old: %b\n",ce.resource,ce.lastAccess,(time > ce.lastAccess + timeout));
                if (!ce.locked && time > ce.lastAccess + timeout) {
                    if (DEBUG) printf("CLRM Removing old: %s\n", ce.resource);

                    itr.remove();
                    ce.resource.release();
                    currBytes -= ce.size;
                    ce.resource = null;
                } else {
                    if (DEBUG) printf("\n");
                    break;
                }
            }
        } finally {
            freeing = false;
        }

        if (DEBUG) printf("CLRM Clearing Exited Run.\n");
    }
    public void shutdown() {
        if (shutdown) throw new IllegalArgumentException("Manager already shutdown: contextID: " + contextID + " this:" + this);
        shutdown = true;
        if (DEBUG) printf("CLResourceManager Shutting down.  this: %s context: %s\n",this,contextID);

        waitForNotFreeing();

        freeMemory(maxBytes);

        if (scheduler != null) scheduler.shutdownNow();
        if (cache != null) cache.clear();
        cache = null;
        scheduler = null;
        if (DEBUG) printf("CLResourceManager Shut down.\n");
    }

    protected boolean isShutdown() {
        return shutdown;
    }

    static class CacheEntry implements Comparator {
        public long lastAccess;
        public Resource resource;
        public boolean locked;
        public long size;
        public ResourceRemovedListener listener;

        public CacheEntry() {

        }

        public CacheEntry(Resource resource, long size, boolean locked, ResourceRemovedListener listener) {
            this.resource = resource;
            this.size = size;
            this.locked = locked;
            this.listener = listener;
            lastAccess = System.currentTimeMillis();
        }

        public void setLocked(boolean val) {
            locked = val;
        }

        @Override
        public int compare(Object o1, Object o2) {
            CacheEntry ce1 = (CacheEntry) o1;
            CacheEntry ce2 = (CacheEntry) o2;

            return Long.compare(ce1.lastAccess,ce2.lastAccess);
        }
    }

    public class NamedThreadFactory implements ThreadFactory {

        private static final String THREAD_NAME_PATTERN = "%s-%d";
        private final AtomicInteger counter = new AtomicInteger();
        private String namePrefix;

        public NamedThreadFactory(final String namePrefix) {
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            final String threadName = String.format(THREAD_NAME_PATTERN, namePrefix, counter.incrementAndGet());
            return new Thread(r, threadName);
        }
    }



}


