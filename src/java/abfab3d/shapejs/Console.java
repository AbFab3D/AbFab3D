/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

import org.mozilla.javascript.Context;

/**
 * Logs print() calls for later return;
 *
 * @author Alan Hudson
 */
public class Console {
    public Console() {
    }

    public static void log(String msg) {
        Context cx = Context.enter();
        try {
            DebugLogger.log(cx,msg);
        } finally {
            Context.exit();
        }
    }
}
