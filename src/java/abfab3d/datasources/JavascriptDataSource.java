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

package abfab3d.datasources;

import abfab3d.core.Bounds;
import abfab3d.core.Vec;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import static abfab3d.core.Output.printf;

/**
 * A datasource implemented using Javascript code
 *
 * @author Alan Hudson
 */
public class JavascriptDataSource extends TransformableDataSource {
    private NativeObject no;
    private Callable gdv;  // Required getDataValue
    private Callable gcc;  // Optional getChannelCount
    private Callable gb;   // Optional getBounds

    public JavascriptDataSource(NativeObject no) {
        this.no = no;

        gdv = getFunction("getDataValue", false);
        gcc = getFunction("getChannelCount", true);
        gb = getFunction("getBounds", true);
    }

    private Callable getFunction(String name, boolean optional) {
        Object gdv = ScriptableObject.getProperty(no, name);

        if (gdv == ScriptableObject.NOT_FOUND) {
            if (optional) return null;

            throw new IllegalArgumentException("DataSource must implement getDataValue");
        }
        if (!(gdv instanceof Callable)) {
            throw new IllegalArgumentException("getDataValue must be a function");
        }

        return (Callable) gdv;
    }


    private int debugCount = 1;

    public int getBaseValue(Vec pnt, Vec data) {
        // Note:  We require the calling class to call Context.enter on each thread.  For now that is just
        // SurfacePointsFinderDS.  If we use this class in other multithreaded areas we'll need to add the context handling there.

        Context context = Context.getCurrentContext();

        if (context == null && debugCount > 0) {
            System.out.printf("Missing context handling in this path\n");
            new Exception().printStackTrace();
            debugCount--;
        }
        Object[] args = new Object[]{pnt, data};
        Object ret = ((Callable) gdv).call(context, no, no, args);

        return (int) ret;
    }

    /**
     * @returns count of data channels,
     * it is the count of data values returned in  getDataValue()
     */
    public int getChannelsCount() {
        if (gcc == null) return super.getChannelsCount();

        Context context = Context.getCurrentContext();

        if (context == null && debugCount > 0) {
            System.out.printf("Missing context handling in this path\n");
            new Exception().printStackTrace();
            debugCount--;
        }

        Object[] args = new Object[]{};
        Object ret = ((Callable) gcc).call(context, no, no, args);

        return (int) ret;
    }

    /**
     * @return bounds of this data source. It may be null for data sources without bounds
     */
    public Bounds getBounds() {
        if (gb == null) return super.getBounds();

        Context context = Context.getCurrentContext();

        if (context == null && debugCount > 0) {
            System.out.printf("Missing context handling in this path\n");
            new Exception().printStackTrace();
            debugCount--;
        }

        Object[] args = new Object[]{};
        Object ret = ((Callable) gb).call(context, no, no, args);

        return (Bounds) ret;
    }
}