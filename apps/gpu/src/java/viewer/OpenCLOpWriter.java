package viewer;

import abfab3d.datasources.TransformableDataSource;
import abfab3d.param.*;
import abfab3d.util.DataSource;
import abfab3d.util.VecTransform;
import render.Instruction;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static abfab3d.util.Output.printf;
import static java.lang.Math.PI;

/**
 * Write a datasource tree to OpenCL using OpCodes
 *
 * @author Alan Hudson
 */
public class OpenCLOpWriter {
    /** Each node has a unique id for future reference */
    private int nodeId;
    private int transId;

    /** Map of each datasource to its nodeId */
    private HashMap<Parameterizable, Integer> idMap = new HashMap<Parameterizable, Integer>();
    private HashMap<Parameterizable, Integer> transIdMap = new HashMap<Parameterizable, Integer>();

    /** Parameter excludes for special cases */
    private static final HashSet<String> boxExclude;
    private static final HashSet<String> cylinderExclude;
    private static final HashSet<String> gyroidExclude;
    private static final HashSet<String> rotationExclude;
    private static final HashMap<String,Integer> opcodes;
    private static final HashMap<Integer,String> opMethods;


    private List<Instruction> instructions;
    private Vector3d worldScale;
    // Maximum number of datasource results
    private int maxResults;

    private NumberFormat format = new DecimalFormat("####.######");
    static {
        boxExclude = new HashSet<String>();
        boxExclude.add("size");
        boxExclude.add("center");

        gyroidExclude = new HashSet<String>();
        gyroidExclude.add("center");
        gyroidExclude.add("period");

        cylinderExclude = new HashSet<String>();

        rotationExclude = new HashSet<String>();
        rotationExclude.add("rotation");

        opcodes = new HashMap<String, Integer>();
        opcodes.put("sphere",0);
        opcodes.put("box",1);
        opcodes.put("gyroid",2);
        opcodes.put("intersection",3);
        opcodes.put("union",4);
        opcodes.put("subtraction",5);
        opcodes.put("intersectionArr",6);
        opcodes.put("torus",8);

        opcodes.put("reset",1000);
        opcodes.put("scale",1001);
        opcodes.put("translation",1002);
        opcodes.put("rotation",1003);

        opMethods = new HashMap<Integer, String>();
        opMethods.put(3,"intersectionOp");
        opMethods.put(4,"unionOp");
        opMethods.put(6,"intersectionArr");
        opMethods.put(7,"unionArr");

        // System op codes
        opcodes.put("pop_stack",10000);
    }

    public OpenCLOpWriter() {

        nodeId = 0;
        transId = 0;

        maxResults = 1;
        instructions = new ArrayList<Instruction>();
    }

    /**
     * Generate OpenCL code from the data source
     *
     * @param source
     * @return
     */
    public List<Instruction> generate(Parameterizable source, Vector3d scale) {
        nodeId = 0;
        idMap.clear();
        transIdMap.clear();

        Instruction inst = new Instruction();
        inst.setOpCode(opcodes.get("scale"));
        worldScale = new Vector3d(1.0/scale.x,1.0/scale.y,1.0/scale.z);
        inst.addFloatVector3(worldScale);
        instructions.add(inst);

        generate(source, "pos");

        return instructions;
    }

    /**
     * Create a textual openCL representation of the instruction list
     * @param list
     * @return
     */
    public String createText(List<Instruction> list, Vector3d scale) {
        StringBuilder bldr = new StringBuilder();

        bldr.append("float readShapeJS(float3 pos) {\n");
        /*
        bldr.append("\tpos = pos * (float3)(");
        bldr.append(scale.x);
        bldr.append(",");
        bldr.append(scale.y);
        bldr.append(",");
        bldr.append(scale.z);
        bldr.append(");\n");
        */
        Vector3d s3d = new Vector3d();
        Matrix4d m4d = new Matrix4d();

        int ridx = 0;
        for(Instruction inst : list) {
            int opCode = inst.getOpCode();
            if (opCode == 1000) {

                // reset code
                bldr.append("\tpos = pos0 / ((float3)(");
                inst.getFloatVector(0,s3d);
                bldr.append("(float3)(");
                bldr.append(format.format(s3d.x));
                bldr.append(",");
                bldr.append(format.format(s3d.y));
                bldr.append(",");
                bldr.append(format.format(s3d.z));
                bldr.append("));\n");
                continue;
            } else if (opCode >= 1000) {
                // special code
                bldr.append("\tpos = ");
                String func = opMethods.get(opCode);
                if (func == null) {
                    func = convertOpToFunction(inst.getOpCode());
                }
                bldr.append(func);
            } else {
                bldr.append("\tfloat ds");
                bldr.append(ridx++);
                bldr.append(" = ");
                String func = opMethods.get(opCode);
                if (func == null) {
                    func = convertOpToFunction(inst.getOpCode());
                }
                bldr.append(func);
            }
            bldr.append("(");

            ParameterType[] types = inst.getTypes();
            int len = inst.getTypeCount();
            int f_idx = 0;
            int fv_idx = 0;
            int m_idx = 0;
            int i_idx = 0;
            int b_idx = 0;

            for(int i=0; i < len; i++) {
                switch (types[i]) {
                    case FLOAT:
                        bldr.append(format.format(inst.getFloatParam(f_idx++)));
                        break;
                    case INTEGER:
                        bldr.append(inst.getIntParam(i_idx++));
                        break;
                    case BOOLEAN:
                        boolean b = inst.getBooleanParam(b_idx++);
                        int val;

                        if (b) val = 1; else val = 0;
                        bldr.append(val);
                        break;
                    case VECTOR_3D:
                        inst.getFloatVector(fv_idx++,s3d);
                        bldr.append("(float3)(");
                        bldr.append(format.format(s3d.x));
                        bldr.append(",");
                        bldr.append(format.format(s3d.y));
                        bldr.append(",");
                        bldr.append(format.format(s3d.z));
                        bldr.append(")");
                        break;
                    case MATRIX_4D:
                        inst.getMatrix(m_idx++, m4d);
                        bldr.append("(float16)(");
                        bldr.append(format.format(m4d.m00));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m01));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m02));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m03));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m10));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m11));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m12));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m13));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m20));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m21));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m22));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m23));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m30));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m31));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m32));
                        bldr.append(",");
                        bldr.append(format.format(m4d.m33));
                        bldr.append(")");
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled: " + types[i]);
                }
                if (i != len -1) {
                    bldr.append(",");
                }
            }

            bldr.append(",pos);\n");
        }

        bldr.append("return ds");
        bldr.append(ridx-1);
        bldr.append(";\n}\n");

        return bldr.toString();
    }

    private String convertOpToFunction(int op) {
        for(Map.Entry<String,Integer> entry : opcodes.entrySet()) {
            if (entry.getValue().equals(op)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private void generate(Parameterizable source, String pos) {
        Parameterizable trans = null;

        if (source instanceof VecTransform) {
            trans = (Parameterizable) source;
        } else if (source instanceof TransformableDataSource) {
            trans = (Parameterizable)(((TransformableDataSource) source).getTransform());
        }

        if (trans != null) {
            // We have a real transform so we need to reset to the original pos
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode("reset"));
            inst.addFloatVector3(worldScale);
            instructions.add(inst);

            generateTransform(trans);
        }


        SNode[] children = ((SNode)source).getChildren();

        if (children != null) {
            for(SNode child : children) {
                generate((Parameterizable)child,pos);
            }
        }

        idMap.put(source, new Integer(nodeId++));

        // terminal add node details

        // TODO: decide if we add a getName() field or int ID
        String class_name = source.getClass().getSimpleName();

        //printf("Node: %s\n",class_name);

        maxResults++;
        // TODO: change to map
        if (class_name.equals("Sphere")) {
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));

            addCallParams(source, inst);

            // Add initializable params

            DoubleParameter dp = ((DoubleParameter)((Parameterizable)source).getParam("radius"));
            double radius = dp.getValue();
            boolean sign;

            if( radius < 0) {
                sign = false;
            } else {
                sign = true;
            }

            addCallParam(ParameterType.BOOLEAN, new Boolean(sign), inst);
            instructions.add(inst);
        } else if(class_name.equals("Box")) {
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));
            addCallParams((Parameterizable) source, inst,boxExclude);

            // Add initializable params

            Vector3d center = ((Vector3d) ((Parameterizable) source).getParam("center").getValue());
            Vector3d size = ((Vector3d) ((Parameterizable) source).getParam("size").getValue());

            float xmin = (float) (center.x - size.x / 2);
            float xmax = (float) (center.x + size.x / 2);
            float ymin = (float) (center.y - size.y / 2);
            float ymax = (float) (center.y + size.y / 2);
            float zmin = (float) (center.z - size.z / 2);
            float zmax = (float) (center.z + size.z / 2);
            addCallParam(ParameterType.VECTOR_3D,new Vector3d(xmin,ymin,zmin),inst);
            addCallParam(ParameterType.VECTOR_3D,new Vector3d(xmax, ymax, zmax), inst);
            instructions.add(inst);
        } else if (class_name.equals("Gyroid")) {
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));
            addCallParams((Parameterizable) source, inst,gyroidExclude);

            // Add initializable params

            double period = ((Double)((Parameterizable)source).getParam("period").getValue()).doubleValue();
            double factor = 2*PI/period;

            addCallParam(ParameterType.DOUBLE, new Double(factor), inst);
            instructions.add(inst);
        } else if (class_name.equals("Intersection")) {
            Instruction inst = new Instruction();
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);

                //addCallParams(source, inst,null);
                addCallParam(ParameterType.INTEGER,data0,inst);
                addCallParam(ParameterType.INTEGER,data1,inst);
                instructions.add(inst);

            } else {
                inst.setOpCode(getOpCode("intersectionArr"));
                addCallParam(ParameterType.INTEGER,nchilds.length,inst);
                for(int i=0; i < nchilds.length; i++) {
                    addCallParam(ParameterType.INTEGER,idMap.get((DataSource) nchilds[i]),inst);
                }
                instructions.add(inst);
            }
        } else if (class_name.equals("IntersectionArr")) {
            Instruction inst = new Instruction();
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);

                //addCallParams(source, inst,null);
                addCallParam(ParameterType.INTEGER,data0,inst);
                addCallParam(ParameterType.INTEGER,data1,inst);
                instructions.add(inst);

            } else {
                inst.setOpCode(getOpCode("intersectionArr"));
                addCallParam(ParameterType.INTEGER, nchilds.length, inst);
                for(int i=0; i < nchilds.length; i++) {
                    addCallParam(ParameterType.INTEGER,idMap.get((DataSource) nchilds[i]),inst);
                }
                instructions.add(inst);
            }
        } else if (class_name.equals("Union")) {
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);

                //addCallParams(source, inst,null);
                addCallParam(ParameterType.INTEGER,data0,inst);
                addCallParam(ParameterType.INTEGER,data1,inst);
                instructions.add(inst);

            } else {
                inst.setOpCode(getOpCode("unionArr"));
                addCallParam(ParameterType.INTEGER, nchilds.length, inst);
                for(int i=0; i < nchilds.length; i++) {
                    addCallParam(ParameterType.INTEGER,idMap.get((DataSource) nchilds[i]),inst);
                }
                instructions.add(inst);
            }
        } else if (class_name.equals("Subtraction")) {
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));
            SNode[] nchilds = ((TransformableDataSource) source).getChildren();

            if (nchilds.length == 2) {
                int data0 = idMap.get((DataSource) nchilds[0]);
                int data1 = idMap.get((DataSource) nchilds[1]);

                //addCallParams(source, inst,null);
                addCallParam(ParameterType.INTEGER,data0,inst);
                addCallParam(ParameterType.INTEGER,data1,inst);
                instructions.add(inst);

            } else {
                throw new IllegalArgumentException("Unhandled");
            }
        } else {
            Instruction inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));

            addCallParams(source, inst);

            instructions.add(inst);
        }
    }

    private void generateTransform(Parameterizable source) {
        String class_name = source.getClass().getSimpleName();

        //printf("Transform: %s\n",class_name);

        Parameterizable trans = null;

        if (source instanceof VecTransform) {
            trans = (Parameterizable) source;
        } else if (source instanceof TransformableDataSource) {
            trans = (Parameterizable)(((TransformableDataSource) source).getTransform());
        }

        if (trans == null) return;

        Instruction inst = null;

        SNode[] tchildren = ((SNode)trans).getChildren();

        if (tchildren != null) {
            int len = tchildren.length;
            for (int i=len-1; i >= 0; i--) {
                SNode child = tchildren[i];
                String pos;

                transIdMap.put((Parameterizable)child,(transId));
                generateTransform((Parameterizable) child);
            }

            return;
        }

        String ret = null;
        String trans_name = trans.getClass().getSimpleName();
        if (trans_name.equals("Translation")) {
            inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));

            addCallParams(source, inst);

            instructions.add(inst);
        } else if (trans_name.equals("Scale")) {
            inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));

            addCallParams(source, inst);

            instructions.add(inst);
            // TODO: not sure if we need scaleFactor
        } else if (trans_name.equals("Rotation")) {
            inst = new Instruction();
            inst.setOpCode(getOpCode(source.getClass().getSimpleName().toLowerCase()));

            addCallParams(source, inst, rotationExclude);

            AxisAngle4d rotation = (AxisAngle4d) trans.getParam("rotation").getValue();

            Matrix4d mat_inv = new Matrix4d();
            mat_inv.setIdentity();
            mat_inv.set(new AxisAngle4d(rotation.x, rotation.y, rotation.z, -rotation.angle));

            addCallParam(ParameterType.MATRIX_4D, mat_inv, inst);
            instructions.add(inst);
        } else if (trans_name.equals("CompositeTransform")) {

        }

        return;
    }

    private String addTransform(Parameterizable source, StringBuilder bldr, Set<String> exclude) {
        /*
        String ret = "trans" + (transId);

        bldr.append("\tfloat3 trans");
        bldr.append(transId++);
        bldr.append(" = ");

        bldr.append(source.getClass().getSimpleName().toLowerCase());
        bldr.append("(");

        Parameter[] params = ((Parameterizable) source).getParams();

        int len = params.length;
        boolean first = true;

        //if (len > 0) bldr.append(",");

        for(int i=0; i < len; i++) {
            if (exclude != null && exclude.contains(params[i].getName())) continue;

            ParameterType type = params[i].getType();

            switch(type) {
                case DOUBLE:
                    if (!first) bldr.append(",");
                    DoubleParameter dp = (DoubleParameter)params[i];
                    double d = dp.getValue();
                    bldr.append(format.format(d));
                    break;
                case VECTOR_3D:
                    if (!first) bldr.append(",");
                    Vector3dParameter v3dp = ((Vector3dParameter)params[i]);
                    Vector3d v3d = v3dp.getValue();
                    addVector3d(v3d, bldr);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled parameter type");
            }
            first = false;
        }

        //bldr.append(");");

        return ret;
        */
        throw new IllegalArgumentException("Unhandled");
    }

    /**
     * Add in the call params.
     * @param source
     * @param inst
     * @param exclude Params to exclude
     */
    private void addCallParams(Parameterizable source, Instruction inst, Set<String> exclude) {
        Parameter[] params = ((Parameterizable) source).getParams();

        // generic mapper, will not work if the function has an initializer

        int len = params.length;

        for(int i=0; i < len; i++) {
            if (exclude != null && exclude.contains(params[i].getName())) continue;

            addCallParam(params[i],inst);
        }

    }

    private void addCallParam(Parameter param, Instruction inst) {
        ParameterType type = param.getType();
        Object value = param.getValue();

        addCallParam(type, value, inst);
    }

    /**
     * Add in the call params.
     * @param inst
     */
    private void addCallParam(ParameterType type, Object value, Instruction inst) {
        switch(type) {
            case INTEGER:
                inst.addInt((Integer) value);
                break;
            case DOUBLE:
                inst.addFloat(((Double)value).floatValue());
                break;
            case VECTOR_3D:
                Vector3d v3d = (Vector3d) value;
                float[] vec = new float[3];
                vec[0] = (float) v3d.x;
                vec[1] = (float) v3d.y;
                vec[2] = (float) v3d.z;
                inst.addFloatVector3(vec);
                break;
            case MATRIX_4D:
                Matrix4d m4d = (Matrix4d) value;
                float[] mvec = new float[16];
                mvec[0] = (float) m4d.m00;
                mvec[1] = (float) m4d.m01;
                mvec[2] = (float) m4d.m02;
                mvec[3] = (float) m4d.m03;
                mvec[4] = (float) m4d.m10;
                mvec[5] = (float) m4d.m11;
                mvec[6] = (float) m4d.m12;
                mvec[7] = (float) m4d.m13;
                mvec[8] = (float) m4d.m20;
                mvec[9] = (float) m4d.m21;
                mvec[10] = (float) m4d.m22;
                mvec[11] = (float) m4d.m23;
                mvec[12] = (float) m4d.m30;
                mvec[13] = (float) m4d.m31;
                mvec[14] = (float) m4d.m32;
                mvec[15] = (float) m4d.m33;

                inst.addMatrix(mvec);
                break;
            case BOOLEAN:
                inst.addBoolean((Boolean)value);
                break;
            default:
                throw new IllegalArgumentException("Parameter type not mapped: " + type);
        }
    }

    private void addCallParams(Parameterizable source, Instruction inst) {
        addCallParams(source,inst,null);
    }

    private int getOpCode(String name) {
        Integer op = opcodes.get(name);

        if (op ==  null) {
            throw new IllegalArgumentException("Undefined op: " + name);
        }

        return op.intValue();
    }
}

