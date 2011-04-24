/*****************************************************************************
 *                        Alan Hudson Copyright (c) 2011
 *                               Java Source
 *
 * This source is private and not licensed for any use.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.geom;

// External Imports
import java.util.*;
import java.io.*;

/**
 * Caches created geometry.  Reduces final filesize.  All geometry
 * creators must encode their params into a key string that can be
 * used to check for cached copies.  A string of "NOTCACHED" is
 * reserved for indicating no cached copy.
 *
 * @author Alan Hudson
 */
public class GeometryCache {
    public GeometryCache() {
    }

    /**
     * Cache an entry.
     *
     * @return whether to DEF(true) or USE(false)
     */
    public boolean add(String key, String defName) {
        // TODO: Need to implement
        return true;
    }
}
