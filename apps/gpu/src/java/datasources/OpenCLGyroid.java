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

import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import org.apache.commons.io.IOUtils;
import program.ProgramLoader;
import render.Instruction;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.lang.Math.PI;

/**
 * Gyroid datasource
 *
 * @author Alan Hudson
 */
public class OpenCLGyroid extends OpenCLDataSource {
    private static final HashSet<String> exclude;

    static {
        exclude = new HashSet<String>();
        exclude.add("center");
        exclude.add("period");
    }

    public String getCode(String version) throws IOException {
        InputStream is = ProgramLoader.getStreamFor("gyroid_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }

    public void traverse(Map<Parameterizable, InstNode> nodeMap, Parameterizable node, List<Instruction> insts) {
        handleTransforms(node, nodeMap, insts);

        Instruction inst = new Instruction();
        String op_name = getClass().getSimpleName().replace("OpenCL", "");
        inst.setOpCode(Instruction.getOpCode(op_name.toLowerCase()));

        Instruction.addCallParams(node, inst, exclude);

        // Add initializable params

        double period = ((Double) ((Parameterizable) node).getParam("period").getValue()).doubleValue();
        double factor = 2 * PI / period;

        Instruction.addCallParam(ParameterType.DOUBLE, new Double(factor), inst);

        insts.add(inst);
    }
}
