package viewer;

import abfab3d.param.SNode;
import abfab3d.datasources.TransformableDataSource;
import abfab3d.param.Parametrizable;
import abfab3d.util.DataSource;

import javax.vecmath.Vector3d;

import java.util.HashMap;

import static abfab3d.util.Output.printf;
import static java.lang.Math.PI;

/**
 * Created by giles on 1/13/2015.
 */
public class OpenCLWriter {
    private int nodeId;
    private HashMap<DataSource, Integer> idMap = new HashMap<DataSource, Integer>();


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
        bldr.append("\treturn clamp(ds");
        bldr.append(nodeId-1);
        bldr.append(",0.0,1.0f); \n}\n");

        return bldr.toString();
        /*
        String prog = "float readShapeJS(float3 pos) {\n" +
                "    float vs = voxelSize;\n" +
                "    float radius = 1;\n" +
                "    float ds1 = sphere(vs, radius, 0, 0, 0, true, pos);\n" +
                "    data1 = clamp(data1,0.0f,1.0f);\n" +
                "\n" +
                "   return data1;\n" +
                "}";

        return prog;
        */
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
            bldr.append("\tfloat ds");
            bldr.append(nodeId++);
            bldr.append(" = clamp(sphere(voxelSize,");
            double radius = ((Double) ((Parametrizable) source).getParam("radius").getValue()).doubleValue();
            bldr.append(radius);
            bldr.append(",0,0,0,true,");
            bldr.append(pos);
            bldr.append("),0.0,1.0);\n");
        } else if(class_name.equals("Box")) {
            Vector3d center = ((Vector3d) ((Parametrizable) source).getParam("center").getValue());
            Vector3d size = ((Vector3d) ((Parametrizable) source).getParam("size").getValue());

            float xmin = (float) (center.x - size.x / 2);
            float xmax = (float) (center.x + size.x / 2);
            float ymin = (float) (center.y - size.y / 2);
            float ymax = (float) (center.y + size.y / 2);
            float zmin = (float) (center.z - size.z / 2);
            float zmax = (float) (center.z + size.z / 2);


            //float box(float vs, float xmin, float xmax, float ymin, float ymax, float zmin, float zmax) {
            bldr.append("\tfloat ds");
            bldr.append(nodeId++);
            bldr.append(" = clamp(box(voxelSize,");
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
            bldr.append("),0.0,1.0);\n");
        } else if (class_name.equals("Torus")) {
                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                bldr.append(" = clamp(torus(voxelSize,");
                double rout = ((Double) ((Parametrizable) source).getParam("rout").getValue()).doubleValue();
                bldr.append(rout);
                bldr.append(",");
                double rin = ((Double) ((Parametrizable) source).getParam("rin").getValue()).doubleValue();
                bldr.append(rin);
                bldr.append(",0,0,0,");
                bldr.append(pos);
                bldr.append("),0.0,1.0);\n");
        } else if (class_name.equals("Gyroid")) {
            bldr.append("\tfloat ds");
            bldr.append(nodeId++);
            bldr.append(" = clamp(gyroid(voxelSize,");
            //float ds1 = clamp(gyroid(vs,level,factor,thickness, (float3)(0,0,0),pos),0.0f,1.0f);

            double level = ((Double)((Parametrizable)source).getParam("level").getValue()).doubleValue();
            double period = ((Double)((Parametrizable)source).getParam("period").getValue()).doubleValue();
            double factor = 2*PI/period;
            double thickness = ((Double)((Parametrizable)source).getParam("thickness").getValue()).doubleValue();
            bldr.append(level);
            bldr.append(",");
            bldr.append(factor);
            bldr.append(",");
            bldr.append(thickness);
            bldr.append(",(float3)(0,0,0),");
            bldr.append(pos);
            bldr.append("),0.0,1.0);\n");
        } else if (class_name.equals("Intersection")) {
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);
                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                bldr.append(" = clamp(intersectionOp(");
                bldr.append("ds");
                bldr.append(data0);
                bldr.append(",");
                bldr.append("ds");
                bldr.append(data1);
                bldr.append("),0.0,1.0);\n");
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
                bldr.append(" = clamp(intersectionArr(");
                bldr.append("arr");
                bldr.append((nodeId-1));
                bldr.append(",");
                bldr.append(len);
                bldr.append("),0.0,1.0);\n");
            }
        } else if (class_name.equals("Union")) {
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);
                bldr.append("\tfloat ds");
                bldr.append(nodeId++);
                bldr.append(" = clamp(unionOp(");
                bldr.append("ds");
                bldr.append(data0);
                bldr.append(",");
                bldr.append("ds");
                bldr.append(data1);
                bldr.append("),0.0,1.0);\n");
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
                bldr.append(" = clamp(unionArr(");
                bldr.append("arr");
                bldr.append((nodeId-1));
                bldr.append(",");
                bldr.append(len);
                bldr.append("),0.0,1.0);\n");
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
            printf("Unmapped datasource: %s\n",class_name);
        }
    }
}
