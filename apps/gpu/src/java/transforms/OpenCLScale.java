package transforms;

import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import abfab3d.param.Vector3dParameter;
import datasources.InstNode;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;
import render.Instruction;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by giles on 1/27/2015.
 */
public class OpenCLScale extends OpenCLTransform {
    @Override
    public String getCode(String version) throws IOException {

        InputStream is = ProgramLoader.getStreamFor("scale_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    public void traverse(Map<Parameterizable, InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL", "");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        //Instruction.addCallParams(node, inst);

        // Convert scale to inverse so we can use multiple in shader script
        Vector3dParameter scale = ((Vector3dParameter) ((Parameterizable) node).getParam("scale"));

        Vector3d ovec = scale.getValue();
        Vector3d vec = new Vector3d();
        vec.x = 1.0 / ovec.x;
        vec.y = 1.0 / ovec.y;
        vec.z = 1.0 / ovec.z;

        Instruction.addCallParam(ParameterType.VECTOR_3D, vec, inst);

        insts.add(inst);
    }

}
