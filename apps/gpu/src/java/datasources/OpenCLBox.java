package datasources;

import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;
import render.Instruction;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Box datasource
 *
 * @author Alan Hudson
 */
public class OpenCLBox extends OpenCLDataSource {
    private static final HashSet<String> exclude;

    static {
        exclude = new HashSet<String>();
        exclude.add("size");
        exclude.add("center");
    }

    public String getCode(String version) throws IOException {
        InputStream is = ProgramLoader.getStreamFor("box_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    @Override
    public void traverse(Map<Parameterizable, InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        handleTransforms(node, nodeMap, insts);

        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL", "");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        Instruction.addCallParams(node, inst, exclude);

        // Add initializable params

        Vector3d center = ((Vector3d) node.getParam("center").getValue());
        Vector3d size = ((Vector3d) node.getParam("size").getValue());

        float xmin = (float) (center.x - size.x / 2);
        float xmax = (float) (center.x + size.x / 2);
        float ymin = (float) (center.y - size.y / 2);
        float ymax = (float) (center.y + size.y / 2);
        float zmin = (float) (center.z - size.z / 2);
        float zmax = (float) (center.z + size.z / 2);
        Instruction.addCallParam(ParameterType.VECTOR_3D, new Vector3d(xmin, ymin, zmin), inst);
        Instruction.addCallParam(ParameterType.VECTOR_3D, new Vector3d(xmax, ymax, zmax), inst);

        insts.add(inst);
    }
}
