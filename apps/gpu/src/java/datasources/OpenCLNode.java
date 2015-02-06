/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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

import abfab3d.param.Parameterizable;

import java.io.IOException;
import java.util.*;

/**
 * Base representation for an OpenCL Operation
 *
 * @author Alan Hudson
 */
public abstract class OpenCLNode {
    /**
     * Get the OpenCL code for this operation.
     *
     * @param version The renderer version
     * @return
     * @throws IOException
     */
    public abstract String getCode(String version) throws IOException;

    /**
     * Issue instructions to execute in OpenCL for this operation.
     * @param nodeMap Maps the source to its tree data such as transform and material
     * @param node
     * @param insts
     */
    public void traverse(Map<Parameterizable,InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {

        // issue transformation op codes
        handleTransforms(node,nodeMap,insts);

        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL", "");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        Instruction.addCallParams(node, inst);

        insts.add(inst);
    }

    protected void handleTransforms(Parameterizable node, Map<Parameterizable,InstNode> nodeMap, List<Instruction> insts) {
        InstNode inode = nodeMap.get(node);
        if (inode == null) return;

        Stack<Parameterizable> transChain = inode.chain;

        // issue transformation op codes
        if (transChain.size() > 0) {
            Instruction inst = new Instruction();
            inst.setOpCode(Instruction.oRESET);
            insts.add(inst);

            // TODO: is this right?
            Collections.reverse(transChain);

            Iterator<Parameterizable> itr = transChain.iterator();
            while(itr.hasNext()) {
                Parameterizable p = itr.next();
                OpenCLNode n = NodeFactory.getNode(p.getClass().getSimpleName());
                n.traverse(nodeMap,p, insts);                
            }
        }

    }
}
