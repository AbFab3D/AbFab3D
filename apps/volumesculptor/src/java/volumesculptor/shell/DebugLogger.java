package volumesculptor.shell;

import java.util.ArrayList;
import java.util.HashMap;
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

    public static void log(Object key, String msg) {
        System.out.println("Log entry: " + key + " msg: " + msg);
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
