package transforms;

import abfab3d.param.Parameterizable;
import abfab3d.param.SNode;
import datasources.InstNode;
import datasources.OpenCLNode;
import render.Instruction;
import viewer.NodeFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by giles on 1/27/2015.
 */
public class OpenCLCompositeTransform extends OpenCLTransform {
    @Override
    public String getCode(String version) throws IOException {

        return "";
    }

    @Override
    public void traverse(Map<Parameterizable,InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        SNode[] children = ((SNode) node).getChildren();

        int len = children.length;
        for (int i = len - 1; i >= 0; i--) {
//        for (int i = 0; i < len; i++) {
            OpenCLNode n = NodeFactory.getNode(children[i].getClass().getSimpleName());
            n.traverse(nodeMap, (Parameterizable) children[i], insts);
        }
    }
}
