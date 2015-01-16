package viewer;

import abfab3d.param.*;
import abfab3d.datasources.TransformableDataSource;
import abfab3d.util.DataSource;

import javax.vecmath.Vector3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static abfab3d.util.Output.printf;
import static java.lang.Math.PI;

/**
 * Write a datasource tree to OpenCL
 *
 * @author Alan Hudson
 */
public class OpenCLWriter {
    /** Each node has a unique id for future reference */
    private int nodeId;

    /** Map of each datasource to its nodeId */
    private HashMap<DataSource, Integer> idMap = new HashMap<DataSource, Integer>();

    /** Parameter excludes for special cases */
    private static final HashSet<String> boxExclude;
    private static final HashSet<String> gyroidExclude;

    static {
        boxExclude = new HashSet<String>();
        boxExclude.add("size");
        boxExclude.add("center");

        gyroidExclude = new HashSet<String>();
        gyroidExclude.add("center");
        gyroidExclude.add("period");
    }

    public OpenCLWriter() {
        nodeId = 0;
    }

    /**
     * Generate OpenCL code from the data source
     *
     * @param source
     * @return
     */
    public String generate(DataSource source, Vector3d scale) {
        nodeId = 0;
        idMap.clear();

        StringBuilder bldr = new StringBuilder();

        bldr.append("float readShapeJS(float3 pos) {\n");
        bldr.append("\tpos = pos * (float3)(");
        bldr.append(scale.x);
        bldr.append(",");
        bldr.append(scale.y);
        bldr.append(",");
        bldr.append(scale.z);
        bldr.append(");\n");
        generate(source, bldr);
        //bldr.append("\treturn clamp(ds");
        bldr.append("\treturn ds");
        bldr.append(nodeId-1);
        //bldr.append("; \n}\n");
        bldr.append("; \n}\n");

        return bldr.toString();
    }

    private void generate(DataSource source, StringBuilder bldr) {
        SNode[] children = ((SNode)source).getChildren();

        if (children != null) {
            for(SNode child : children) {
                generate((DataSource)child,bldr);
            }
        }

        idMap.put(source,new Integer(nodeId));

        // terminal add node details

        // TODO: decide if we add a getName() field or int ID
        String class_name = source.getClass().getSimpleName();

        printf("Node: %s\n",class_name);

        String pos = "pos";
        Parametrizable trans = (Parametrizable)(((TransformableDataSource) source).getTransform());
        if (trans != null) {
            String trans_name = trans.getClass().getSimpleName();
            if (trans_name.equals("Translation")) {
                Vector3d t = (Vector3d) trans.getParam("translation").getValue();
                pos = "translation(pos,(float3)(" + t.x + "," + t.y + "," + t.z + "))";
            }
        }
        // TODO: change to map
        if (class_name.equals("Sphere")) {
            addCallParams(source, bldr);

            // Add initializable params

            DoubleParameter dp = ((DoubleParameter)((Parametrizable)source).getParam("radius"));
            double radius = dp.getValue();
            boolean sign;

            if( radius < 0) {
                sign = false;
            } else {
                sign = true;
            }

            bldr.append(",");
            bldr.append(sign);

            bldr.append(",");
            bldr.append(pos);
            bldr.append(")");
            //bldr.append(",0.0,1.0)");
            bldr.append(";\n");
        } else if(class_name.equals("Box")) {
            addCallParams(source, bldr, boxExclude);

            // Add initializable params

            Vector3d center = ((Vector3d) ((Parametrizable) source).getParam("center").getValue());
            Vector3d size = ((Vector3d) ((Parametrizable) source).getParam("size").getValue());

            float xmin = (float) (center.x - size.x / 2);
            float xmax = (float) (center.x + size.x / 2);
            float ymin = (float) (center.y - size.y / 2);
            float ymax = (float) (center.y + size.y / 2);
            float zmin = (float) (center.z - size.z / 2);
            float zmax = (float) (center.z + size.z / 2);


            //float box(float vs, float xmin, float xmax, float ymin, float ymax, float zmin, float zmax) {
            bldr.append(xmin);
            bldr.append(",");
            bldr.append(xmax);
            bldr.append(",");
            bldr.append(ymin);
            bldr.append(",");
            bldr.append(ymax);
            bldr.append(",");
            bldr.append(zmin);
            bldr.append(",");
            bldr.append(zmax);
            bldr.append(",");
            bldr.append(pos);
            bldr.append(")");
            //bldr.append(",0.0,1.0)");
            bldr.append(";\n");
        } else if (class_name.equals("Gyroid")) {
            addCallParams(source, bldr,gyroidExclude);

            // Add initializable params

            double period = ((Double)((Parametrizable)source).getParam("period").getValue()).doubleValue();
            double factor = 2*PI/period;
            bldr.append(factor);
            bldr.append(",");
            bldr.append(pos);
            bldr.append(")");
            //bldr.append(",0.0,1.0)");
            bldr.append(";\n");
        } else if (class_name.equals("Intersection")) {
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);
                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                //bldr.append(" = clamp(");
                bldr.append(" = intersectionOp(");
                bldr.append("ds");
                bldr.append(data0);
                bldr.append(",");
                bldr.append("ds");
                bldr.append(data1);
                bldr.append(")");
                //bldr.append(",0.0,1.0)");
                bldr.append(";\n");
            } else {
                bldr.append("\tfloat arr");
                bldr.append(nodeId);
                bldr.append("[] = {");
                int len = nchilds.length;
                for(int i=0; i < len; i++) {
                    bldr.append("ds");
                    bldr.append(idMap.get((DataSource) nchilds[i]));
                    if (i != len - 1) bldr.append(",");
                }
                bldr.append("};\n");

                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                //bldr.append(" = clamp(");
                bldr.append(" = intersectionArr(");
                bldr.append("arr");
                bldr.append((nodeId-1));
                bldr.append(",");
                bldr.append(len);
                bldr.append(")");
                //bldr.append(",0.0,1.0)");
                bldr.append(";\n");
            }
        } else if (class_name.equals("Union")) {
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);
                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                bldr.append(" = unionOp(");
                bldr.append("ds");
                bldr.append(data0);
                bldr.append(",");
                bldr.append("ds");
                bldr.append(data1);
                bldr.append(");\n");
            } else {
                bldr.append("\tfloat arr");
                bldr.append(nodeId);
                bldr.append("[] = {");
                int len = nchilds.length;
                for(int i=0; i < len; i++) {
                    bldr.append("ds");
                    bldr.append(idMap.get((DataSource) nchilds[i]));
                    if (i != len - 1) bldr.append(",");
                }
                bldr.append("};\n");

                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                bldr.append(" = unionArr(");
                bldr.append("arr");
                bldr.append((nodeId-1));
                bldr.append(",");
                bldr.append(len);
                bldr.append(");\n");
            }
        } else if (class_name.equals("Subtraction")) {
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();
            int data0 = idMap.get((DataSource)nchilds[0]);
            int data1 = idMap.get((DataSource)nchilds[1]);
            bldr.append("\tfloat ds");
            bldr.append(nodeId++);
            bldr.append(" = clamp(subtraction(");
            bldr.append("ds");
            bldr.append(data0);
            bldr.append(",");
            bldr.append("ds");
            bldr.append(data1);
            bldr.append("),0.0,1.0);\n");
        } else {
            // generic mapper, will not work if the function has an initializer

            addCallParams(source,bldr,null);
            bldr.append(",");
            bldr.append(pos);
            bldr.append("),0.0,1.0);\n");
        }
    }

    /**
     * Add in the call params.
     * @param source
     * @param bldr
     * @param exclude Params to exclude
     */
    private void addCallParams(DataSource source, StringBuilder bldr, Set<String> exclude) {
        Parameter[] params = ((Parametrizable) source).getParams();
        bldr.append("\tfloat ds");
        bldr.append(nodeId++);
        bldr.append(" = ");
        //bldr.append(" clamp(");
        bldr.append(source.getClass().getSimpleName().toLowerCase());
        bldr.append("(voxelSize,");

        // generic mapper, will not work if the function has an initializer

        int len = params.length;

        for(int i=0; i < len; i++) {
            if (exclude != null && exclude.contains(params[i].getName())) continue;

            ParameterType type = params[i].getType();
            switch(type) {
                case DOUBLE:
                    DoubleParameter dp = (DoubleParameter)params[i];
                    double d = dp.getValue();
                    bldr.append(d);
                    break;
                case VECTOR3D:
                    Vector3dParameter v3dp = ((Vector3dParameter)params[i]);
                    Vector3d v3d = v3dp.getValue();
                    bldr.append("(float3)(");
                    bldr.append(v3d.x);
                    bldr.append(",");
                    bldr.append(v3d.y);
                    bldr.append(",");
                    bldr.append(v3d.z);
                    bldr.append(")");
                    break;
            }
            if (i != len - 1) bldr.append(",");
        }

    }

    private void addCallParams(DataSource source, StringBuilder bldr) {
        addCallParams(source,bldr,null);
    }
}
