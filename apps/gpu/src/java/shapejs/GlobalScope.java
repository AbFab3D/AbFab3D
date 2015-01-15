package shapejs;

/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import org.mozilla.javascript.*;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;
import org.mozilla.javascript.tools.ToolErrorReporter;

import java.io.*;
import java.util.Map;

import shapejs.ShapeJSGlobal;

import static abfab3d.util.Output.printf;

/**
 * Global context for ecmascript
 *
 * @author Alan Hudson
 */
public class GlobalScope extends ImporterTopLevel
{
    static final long serialVersionUID = 4029130780977538005L;

    private boolean sealedStdLib = false;
    boolean initialized;
    private String[] prompts = { "js> ", "  > " };

    public GlobalScope()
    {
    }

    public GlobalScope(Context cx)
    {
        initShapeJS(cx);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initShapeJS(ContextFactory factory)
    {
        factory.call(new ContextAction() {
            public Object run(Context cx)
            {
                initShapeJS(cx);
                return null;
            }
        });
    }

    public void initShapeJS(Context cx) {
        // Define some global functions particular to the shell. Note
        // that these functions are not part of ECMA.
        initStandardObjects(cx, sealedStdLib);

        ShapeJSGlobal globals = new ShapeJSGlobal();

        // Initialize AbFab3D specific globals
        defineFunctionProperties(globals.getFunctions(), ShapeJSGlobal.class,
                ScriptableObject.DONTENUM);

        Map<String,Object> props = globals.getProperties();
        for(Map.Entry<String,Object> e : props.entrySet()) {
            defineProperty(e.getKey(), e.getValue(),
                    ScriptableObject.DONTENUM);
        }

        initialized = true;
    }

    /**
     * Print the string values of its arguments.
     *
     * This method is defined as a JavaScript function.
     * Note that its arguments are of the "varargs" form, which
     * allows it to handle an arbitrary number of arguments
     * supplied to the JavaScript function.
     *
     */
    public static Object print(Context cx, Scriptable thisObj,
                               Object[] args, Function funObj)
    {

        //PrintStream out = getInstance(funObj).getOut();
        StringBuilder bldr = new StringBuilder();
        for (int i=0; i < args.length; i++) {
            if (i > 0) {
                //out.print(" ");
                bldr.append(" ");
            }
            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);

            //out.print(s);
            bldr.append(s);
        }
        //out.println();
        bldr.append("\n");

        printf("%s\n", bldr.toString());
        //DebugLogger.log(cx, bldr.toString());
        return Context.getUndefinedValue();
    }
}


