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
package shapejs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logs print() calls for later return;
 *
 * @author Alan Hudson
 */
public class DebugLogger {
    private static ConcurrentHashMap<Object, List<String>> logs;

    static {
        logs = new ConcurrentHashMap<Object, List<String>>();
    }

    public static List<String> getLog(Object key) {
        return logs.get(key);
    }

    public static void clearLog(Object key) {
        logs.get(key).clear();
    }

    public static void log(Object key, String msg) {
        List<String> entries = logs.get(key);
        if (entries == null) {
            entries = new ArrayList<String>();
            logs.put(key, entries);
        }

        entries.add(msg);
    }

    public static void clear(Object key) {
        logs.remove(key);
    }
}
