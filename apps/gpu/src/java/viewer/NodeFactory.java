package viewer;

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
