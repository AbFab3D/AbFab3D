package viewer;

import abfab3d.datasources.SNode;
import abfab3d.datasources.Sphere;
import abfab3d.param.Parametrizable;
import abfab3d.util.DataSource;

import javax.vecmath.Vector3d;

import static abfab3d.util.Output.printf;
import static java.lang.Math.PI;

/**
 * Created by giles on 1/13/2015.
 */
public class OpenCLWriter {
    private int nodeId;

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
        StringBuilder bldr = new StringBuilder();

        bldr.append("float readShapeJS(float3 pos) {\n");
        bldr.append("\tpos = pos * (float3)(");
        bldr.append(scale.x);
        bldr.append(",");
        bldr.append(scale.y);
        bldr.append(",");
        bldr.append(scale.z);
        bldr.append(");");
        generate(source, bldr);
        bldr.append("\treturn clamp(data");
        bldr.append(nodeId-1);
        bldr.append(",0.0,1.0f); \n}\n");

        return bldr.toString();
        /*
        String prog = "float readShapeJS(float3 pos) {\n" +
                "    float vs = voxelSize;\n" +
                "    float radius = 1;\n" +
                "    float data1 = sphere(vs, radius, 0, 0, 0, true, pos);\n" +
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

        // terminal add node details

        // TODO: decide if we add a getName() field or int ID
        String class_name = source.getClass().getSimpleName();

        printf("Node: %s\n",class_name);
        // TODO: change to map
        if (class_name.equals("Sphere")) {
            bldr.append("\tfloat data");
            bldr.append(nodeId++);
            bldr.append(" = clamp(sphere(voxelSize,");
            double radius = ((Double) ((Parametrizable) source).getParam("radius").getValue()).doubleValue();
            bldr.append(radius);
            bldr.append(",0,0,0,true,pos),0.0,1.0);\n");
        } else if (class_name.equals("Torus")) {
                bldr.append("\tfloat data");
                bldr.append(nodeId++);
                bldr.append(" = clamp(torus(voxelSize,");
                double rout = ((Double) ((Parametrizable) source).getParam("rout").getValue()).doubleValue();
                bldr.append(rout);
                bldr.append(",");
                double rin = ((Double) ((Parametrizable) source).getParam("rin").getValue()).doubleValue();
                bldr.append(rin);
                bldr.append(",0,0,0,pos),0.0,1.0);\n");
        } else if (class_name.equals("Gyroid")) {
            bldr.append("\tfloat data");
            bldr.append(nodeId++);
            bldr.append(" = clamp(gyroid(voxelSize,");
            //float data1 = clamp(gyroid(vs,level,factor,thickness, (float3)(0,0,0),pos),0.0f,1.0f);

            double level = ((Double)((Parametrizable)source).getParam("level").getValue()).doubleValue();
            double period = ((Double)((Parametrizable)source).getParam("period").getValue()).doubleValue();
            // TODO: why divide?
            double factor = 2*PI/period;
            double thickness = ((Double)((Parametrizable)source).getParam("thickness").getValue()).doubleValue();
            bldr.append(level);
            bldr.append(",");
            bldr.append(factor);
            bldr.append(",");
            bldr.append(thickness);
            bldr.append(",(float3)(0,0,0),pos),0.0,1.0);\n");
        } else if (class_name.equals("Intersection")) {
            bldr.append("\tfloat data");
            bldr.append(nodeId++);
            bldr.append(" = clamp(intersection(data0,data1),0.0,1.0);\n");
        } else {
            printf("Unmapped datasource: %s\n",class_name);
        }
    }
}
