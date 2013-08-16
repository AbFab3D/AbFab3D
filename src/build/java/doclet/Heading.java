/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package doclet;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML Heading structure.
 *
 * @author Alan Hudson
 */
class Heading {
    private String id;
    private String text;
    private ArrayList<Heading> children;

    Heading(String id, String text) {
        this.id = id;
        this.text = text;
        children = new ArrayList<Heading>();
    }

    String getId() {
        return id;
    }

    String getText() {
        return text;
    }

    public void add(Heading child) {
        children.add(child);
    }

    public List<Heading> getChildren() {
        return children;
    }
}
