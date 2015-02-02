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

import abfab3d.datasources.TransformableDataSource;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNode;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Subtraction datasource
 *
 * @author Alan Hudson
 */
public class OpenCLSubtraction extends OpenCLDataSource {
    public String getCode(String version) throws IOException {
        InputStream is = ProgramLoader.getStreamFor("subtraction_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    public void traverse(Map<Parameterizable, InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        handleTransforms(node, nodeMap, insts);

        SNode[] children = ((TransformableDataSource) node).getChildren();

        Instruction inst = new Instruction("subtractionStart");
        insts.add(inst);

        OpenCLNode n = NodeFactory.getNode(children[0].getClass().getSimpleName());
        n.traverse(nodeMap, (Parameterizable) children[0], insts);

/*
        n = NodeFactory.getNode(children[1].getClass().getSimpleName());
        n.traverse(chain, (Parameterizable) children[1], insts);
*/
        inst = new Instruction("subtractionEnd");
        insts.add(inst);
    }
}
