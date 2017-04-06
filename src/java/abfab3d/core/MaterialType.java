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
package abfab3d.core;

/**
 * Type of output created in a Scene.
 *
 * @author Alan Hudson
 */
public enum MaterialType {
    SINGLE_MATERIAL(0),
    MULTI_MATERIAL(1),
    COLOR_MATERIAL(2),
    INDEXED_MATERIAL(3);

    private int id;

    MaterialType(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public static String[] getStringValues() {
        MaterialType[] states = values();
        String[] names = new String[states.length];

        for (int i = 0; i < states.length; i++) {
            names[i] = states[i].name();
        }

        return names;
    }
}
