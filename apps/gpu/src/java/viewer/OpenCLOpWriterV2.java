package viewer;

import abfab3d.datasources.TransformableDataSource;
import abfab3d.param.*;
import abfab3d.util.VecTransform;
import datasources.InstNode;
import datasources.NodeFactory;
import datasources.OpenCLNode;
import datasources.Instruction;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static abfab3d.util.Output.printf;

/**
 * Write a datasource tree to OpenCL using OpCodes
 *
 * @author Alan Hudson
 */
public class OpenCLOpWriterV2 {
    /** Each node has a unique id for future reference */
    private int nodeId;
    private int transId;

    /** Map of each datasource to its nodeId */
    private HashMap<Parameterizable, Integer> idMap = new HashMap<Parameterizable, Integer>();
    private HashMap<Parameterizable, Integer> transIdMap = new HashMap<Parameterizable, Integer>();

    private List<Instruction> instructions;
    private Vector3d worldScale;
    // Maximum number of datasource results
    private NodeFactory factory = new NodeFactory();

    private NumberFormat format = new DecimalFormat("####.######");

    private LinkedList<InstNode> queue;
    private HashMap<Parameterizable,InstNode> nodeMap;

    public OpenCLOpWriterV2() {

        nodeId = 0;
        transId = 0;

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
        queue = new LinkedList<InstNode>();
        nodeMap = new HashMap<Parameterizable, InstNode>();

        worldScale = new Vector3d(1.0/scale.x,1.0/scale.y,1.0/scale.z);

        Stack<Parameterizable> transChain = new Stack<Parameterizable>();
        generate(source, null, transChain);

        Collections.reverse(queue);
        Iterator<InstNode> itr = queue.iterator();

        Parameterizable parent = null;

        if (queue.size() == 1) {
            // I don't like this but seems necessary, revisit logic
            InstNode n = queue.get(0);
            OpenCLNode clnode = NodeFactory.getNode(n.node.getClass().getSimpleName());
            clnode.traverse(nodeMap, n.node, instructions);
        } else {
            while (itr.hasNext()) {
                InstNode n = itr.next();
                printf("Node: %40s  parent: %40s trans: %d\n", n.node, n.parent, n.chain.size());
//            if (n.parent != null) {
/*
                if (n.parent != null && n.parent == parent) {
                    continue;
                }

                if (n.parent != null) {
                    OpenCLNode clnode = NodeFactory.getNode(n.parent.getClass().getSimpleName());
                    clnode.traverse(n.chain, n.parent, instructions);
                    parent = n.parent;
                } else {
                    OpenCLNode clnode = NodeFactory.getNode(n.node.getClass().getSimpleName());
                    clnode.traverse(n.chain, n.node, instructions);
                }
*/
//            }
                if (n.parent != null) {

                    if (n.parent == parent) {
                        continue;
                    }

                    OpenCLNode clnode = NodeFactory.getNode(n.parent.getClass().getSimpleName());
                    clnode.traverse(nodeMap, n.parent, instructions);
                    parent = n.parent;
                }
            }
        }
        return instructions;
    }

    /**
     * Create a textual openCL representation of the instruction list
     * @param list
     * @return
     */
    public String createText(List<Instruction> list, Vector3d scale) {
        StringBuilder bldr = new StringBuilder();

        printf("Instructions: %d\n",list.size());
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
            String func = Instruction.convertOpToFunction(inst.getOpCode());

            bldr.append("\t");
            bldr.append(func);
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

            bldr.append(");\n");
        }

        bldr.append(";\n}\n");

        return bldr.toString();
    }

    private void generateOld(Parameterizable source, Stack<Parameterizable> transChain) {

        Parameterizable trans = null;

        if (source instanceof VecTransform) {
            trans = (Parameterizable) source;
        } else if (source instanceof TransformableDataSource) {
            trans = (Parameterizable)(((TransformableDataSource) source).getTransform());
        }

        if (trans != null) {
            // We have a real transform so we need to reset to the original pos
            Instruction inst = new Instruction();
            inst.setOpCode(Instruction.getOpCode("reset"));
            inst.addFloatVector3(worldScale);
            instructions.add(inst);

            transChain.push(trans);
        }


        // TODO: decide if we add a getName() field or int ID
        String class_name = source.getClass().getSimpleName();

        //printf("Node: %s\n",class_name);

        OpenCLNode node = factory.getNode(class_name);
        node.traverse(nodeMap,source,instructions);

        if (trans != null) transChain.pop();
    }

    private void generate(Parameterizable source, Parameterizable parent, Stack<Parameterizable> transChain) {

        Parameterizable trans = null;

        if (source instanceof VecTransform) {
            trans = (Parameterizable) source;
        } else if (source instanceof TransformableDataSource) {
            trans = (Parameterizable)(((TransformableDataSource) source).getTransform());
        }

        if (trans != null) {
            // We have a real transform so we need to reset to the original pos
            Instruction inst = new Instruction();
            inst.setOpCode(Instruction.getOpCode("reset"));
            //instructions.add(inst);

            transChain.push(trans);
        }


        // TODO: decide if we add a getName() field or int ID
        String class_name = source.getClass().getSimpleName();

        //printf("Node: %s\n",class_name);

        //OpenCLNode node = factory.getNode(class_name);
        //node.traverse(transChain,source,instructions);

        InstNode inode = new InstNode(source,parent,transChain);
        queue.add(inode);
        nodeMap.put(source,inode);

        SNode[] children = ((TransformableDataSource) source).getChildren();

        if (children != null) {
            inode.setStart(true);
            int len = children.length;
            for (int i = 0; i < len; i++) {
                generate((Parameterizable) children[i], source, transChain);
            }
            if (len > 1) {
                inode = queue.peek();
                inode.setEnd(true);
            }
        }
        if (trans != null) transChain.pop();
    }

}

