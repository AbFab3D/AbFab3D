package transforms;

import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import datasources.InstNode;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;
import render.Instruction;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by giles on 1/27/2015.
 */
public class OpenCLRotation extends OpenCLTransform {
    private static final HashSet<String> exclude;

    static {
        exclude = new HashSet<String>();
        exclude.add("rotation");
    }

    @Override
    public String getCode(String version) throws IOException {

        InputStream is = ProgramLoader.getStreamFor("rotation_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    @Override
    public void traverse(Map<Parameterizable,InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        handleTransforms(node,nodeMap,insts);

        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL","");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        Instruction.addCallParams(node, inst, exclude);


        AxisAngle4d rotation = (AxisAngle4d) node.getParam("rotation").getValue();

        Matrix4d mat_inv = new Matrix4d();
        mat_inv.setIdentity();
        mat_inv.set(new AxisAngle4d(rotation.x, rotation.y, rotation.z, -rotation.angle));

        Instruction.addCallParam(ParameterType.MATRIX_4D, mat_inv, inst);
        insts.add(inst);
    }
}
