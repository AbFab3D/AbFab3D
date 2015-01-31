package datasources;

import abfab3d.param.DoubleParameter;
import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;
import render.Instruction;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Sphere datasource
 *
 * @author Alan Hudson
 */
public class OpenCLSphere extends OpenCLDataSource {
    public String getCode(String version) throws IOException {
        InputStream is = ProgramLoader.getStreamFor("sphere_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    public void traverse(Map<Parameterizable, InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        handleTransforms(node, nodeMap, insts);

        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL", "");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        Instruction.addCallParams(node, inst);

        // Add initializable params

        DoubleParameter dp = ((DoubleParameter) ((Parameterizable) node).getParam("radius"));
        double radius = dp.getValue();
        boolean sign;

        if (radius < 0) {
            sign = false;
        } else {
            sign = true;
        }

        Instruction.addCallParam(ParameterType.BOOLEAN, new Boolean(sign), inst);

        insts.add(inst);
    }
}
