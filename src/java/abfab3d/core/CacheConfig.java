/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
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

package abfab3d.core;

import static abfab3d.core.Output.printf;

/**
 * Configure caching properties
 *
 * @author Alan Hudson
 */
public class CacheConfig {
    public final static String CPU_CACHE_PROPERTY = "abfab3d.core.cpuCacheEnabled";
    public final static boolean CPU_CACHE;
    public final static String GPU_CACHE_PROPERTY = "abfab3d.core.gpuCacheEnabled";
    public final static boolean GPU_CACHE;
    public final static String DISK_CACHE_PROPERTY = "abfab3d.core.diskCacheEnabled";
    public final static boolean DISK_CACHE;

    private static CacheConfig instance;

    static {

        CPU_CACHE = parseProperty(CPU_CACHE_PROPERTY,true);
        GPU_CACHE = parseProperty(GPU_CACHE_PROPERTY,true);
        DISK_CACHE = parseProperty(DISK_CACHE_PROPERTY,true);

        printf("Cache Control.  CPU: %b  GPU: %b  Disk: %b\n",CPU_CACHE,GPU_CACHE,DISK_CACHE);
    }

    private static boolean parseProperty(String prop, boolean defVal) {
        boolean ret = defVal;

        String propVal = System.getProperty(prop);

        printf("Prop: %s  val: %s\n",prop,propVal);
        if (propVal == null) return ret;

        try {
            ret = Boolean.parseBoolean(propVal);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static CacheConfig getInstance() {
        if (instance == null) {
            instance = new CacheConfig();
        }

        return instance;
    }
}
