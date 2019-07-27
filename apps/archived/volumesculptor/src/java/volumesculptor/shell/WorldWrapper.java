/*****************************************************************************
 *                        Copyright Shapeways, Inc (c) 2011
 *                               Java Source
 *
 *
 *
 ****************************************************************************/

package volumesculptor.shell;

import org.mozilla.javascript.*;

public class WorldWrapper extends ScriptableObject {
    private static final long serialVersionUID = 438270592527335643L;

    private World world;

    // The zero-argument constructor used by Rhino runtime to create instances
    public WorldWrapper() {
        world = new World();
    }

    public World getWorld() {
        return world;
    }

    // Method jsConstructor defines the JavaScript constructor
    public void jsConstructor() {
    }

    // The class name is defined by the getClassName method
    @Override
    public String getClassName() { return "World"; }

/*
    // The method jsGet_count defines the count property.
    public int jsGet_count() { return count++; }

    // Methods can be defined using the jsFunction_ prefix. Here we define
    //  resetCount for JavaScript.
    public void jsFunction_resetCount() { count = 0; }
*/
}
