package datasources;

import abfab3d.datasources.TransformableDataSource;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNode;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;
import render.Instruction;
import viewer.NodeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Intersection datasource
 *
 * @author Alan Hudson
 */
public class OpenCLUnion extends OpenCLDataSource {
    private static final HashSet<String> exclude;

    static {
        exclude = new HashSet<String>();
        exclude.add("center");
        exclude.add("period");
    }

    public String getCode(String version) throws IOException {
        InputStream is = ProgramLoader.getStreamFor("union_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    public void traverse(Map<Parameterizable,InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        //handleTransforms(node,nodeMap,insts);

        SNode[] children = ((TransformableDataSource) node).getChildren();

        OpenCLNode n = NodeFactory.getNode(children[0].getClass().getSimpleName());
        n.traverse(nodeMap, (Parameterizable) children[0], insts);

        Instruction inst = new Instruction("unionStart");
        insts.add(inst);

        int len = children.length;
        for(int i=1; i < len; i++) {
            n = NodeFactory.getNode(children[i].getClass().getSimpleName());
            n.traverse(nodeMap, (Parameterizable) children[i], insts);

            if (i != len -1) {
                inst = new Instruction("unionMid");
                insts.add(inst);
            }
        }
        inst = new Instruction("unionEnd");
        insts.add(inst);
    }
}
