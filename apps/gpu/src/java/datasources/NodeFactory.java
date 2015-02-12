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
package datasources;

import datasources.OpenCLNode;

/**
 * Created by giles on 1/27/2015.
 */
public class NodeFactory {
    public static OpenCLNode getNode(String name) {

        // TODO: is slow
        try {
            String cname = "datasources." + "OpenCL" + name;
            return (OpenCLNode) Class.forName(cname).newInstance();
        } catch(Exception e) {
            String cname = "transforms." + "OpenCL" + name;
            try {
                return (OpenCLNode) Class.forName(cname).newInstance();
            } catch(Exception e2) {
                e2.printStackTrace();
            }
        }

        return null;
    }
}
