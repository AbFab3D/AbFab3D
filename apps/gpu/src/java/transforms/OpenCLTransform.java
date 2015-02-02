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

package transforms;

import abfab3d.param.Parameterizable;
import datasources.InstNode;
import datasources.OpenCLNode;
import datasources.Instruction;

import java.util.List;
import java.util.Map;

/**
 * OpenCL representation of a transform.
 *
 * @author Alan Hudson
 */
public abstract class OpenCLTransform extends OpenCLNode {

    public void traverse(Map<Parameterizable, InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL", "");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        Instruction.addCallParams(node, inst);

        insts.add(inst);
    }
}
