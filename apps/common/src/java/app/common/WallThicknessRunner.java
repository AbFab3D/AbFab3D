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

package app.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import app.common.WallThicknessResult.ResultType;

/**
 * Runs wallthickness.
 *
 * This should eventually go through the Shapeways API to add to a services queue.  But for now it will
 * directly call wallthickness assuming its installed locally.
 *
 * @author Alan Hudson
 */
public class WallThicknessRunner {
    public static final String RESULT = "result";
    public static final String VISUALIZATION = "visualization";

    private static final String SCRIPT_FILE = "wallthickness_ribt.sh";
    private static final String RELEASE_LOC_UNIX = "/var/www/html/release/server";
    private static final String RELEASE_LOC_WIN  = "\\var\\www\\html\\release\\server";

    private static final String RESULT_MARKER = "WT_PROCESSING_RESULT:";
    private static final String PARTS_COUNT_ORIGINAL_MARKER = "PARTS_COUNT_ORIGINAL:";
    private static final String PARTS_COUNT_ERODED_MARKER = "PARTS_COUNT_ERODED:";
    private static final String ORIGINAL_GENUS_MARKER = "GENUS_ORIGINAL:";
    private static final String ERODED_GENUS_MARKER = "GENUS_ERODED:";
    private static final String RUMP_MARKER = "TOTAL_RUMP:";
    private static final String THIN_MARKER = "TOTAL_THIN:";
    private static final String INTERFACE_MARKER = "TOTAL_INTERFACE:";
    private static final String BOUNDARY_MARKER = "TOTAL_BOUNDARY:";
    private static final String THIN_RUMP_RATIO_MARKER = "TOTAL_THIN_RUMP_RATIO:";
    private static final String THIN_REGION_MARKER = "REGION:";
    private static final String WT_WARNING_MARKER = "WT_WARNING:";
    private static final String VIZ_MARKER = "VIZ_FILE:";

    /** The output stream */
    protected ByteArrayOutputStream outStream;

    /** The output stream */
    protected ByteArrayOutputStream errStream;

    private String[] ENV_VARIABLES;

    private static final HashMap<String, MaterialProperties> wtProps;

    static {
        String[] availableMaterials = new String[] {"White Strong & Flexible", "White Strong & Flexible Polished",
                "Silver", "Silver Glossy", "Stainless Steel","Gold Plated Matte", "Gold Plated Glossy","Antique Bronze Matte",
                "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

        wtProps = new HashMap<String, MaterialProperties>();
        MaterialProperties mp = new MaterialProperties(availableMaterials[0], 0.0007);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[1], 0.0008);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[2], 0.0006);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[3], 0.0008);
        wtProps.put(mp.getName(), mp);

        // Designs says 3mm, but down to 1.5mm well supported.
        mp = new MaterialProperties(availableMaterials[4], 0.002);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[5], 0.002);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[6], 0.002);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[7], 0.002);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[8], 0.002);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[9], 0.0008);
        wtProps.put(mp.getName(), mp);
        mp = new MaterialProperties(availableMaterials[9], 0.001);
        wtProps.put(mp.getName(), mp);

    }
    /**
     * Run wallthickness
     *
     * @param filename  X3D file, must meet analytical file constraints.
     */
    public WallThicknessResult runWallThickness(String filename, String material) {


        String releaseLoc = null;
        String command = null;
        String osName = System.getProperty("os.name");

        if(osName.indexOf("Windows") > -1) {
            // Assume we have cygwin installed
            releaseLoc = RELEASE_LOC_WIN;
            command = releaseLoc + "\\" + SCRIPT_FILE;
            ENV_VARIABLES = new String[] {"YMT_DIR=" + releaseLoc + "\\"};
        } else {
            // Assume a UNIX box
            releaseLoc = RELEASE_LOC_UNIX;
            command = releaseLoc + "/" + SCRIPT_FILE;
            ENV_VARIABLES = new String[] {"YMT_DIR=" + releaseLoc + "/"};
        }

/*
528  time ./wallthickness_ribt.sh -input 262856.v1.s04.analytical_mesh_convert
er.sh.x3db -wt 0.0007 -vpwt 7 -visType 2 -visDir wtOutput -maxReg 10 -debug 4 -b
*/
        MaterialProperties props = wtProps.get(material);

        double wt = props.getMinWallthickness();
//        double bir = 2.7;
        double bir = 2.7;
        double minSuspectVol = 0.05;
        double minUnsafeVol = 0.5;

        // TODO: Stop hardcoding params
        String[] params = new String[] {"-input", filename, "-wt", Double.toString(wt), "-visType","1",
                "-visDir","/tmp", "-maxReg", "500", "-debug","4", "-bir", Double.toString(bir),
                "-minSuspectVol",Double.toString(minSuspectVol),"-minUnsafeVol",Double.toString(minUnsafeVol)};
        String workingDirPath = "/tmp";

        WallThicknessResult ret_val = null;

        try {
            System.out.println("Executing command: " + command);
            System.out.println("Params:  " + java.util.Arrays.toString(params));
            int exit_code = executeScript(command, params, new File(workingDirPath));

            if (exit_code != 0) {
                System.out.println("ExitCode: " + exit_code);
                System.out.println("out: " + outStream.toString());
                System.out.println("err: " + errStream.toString());
                return new WallThicknessResult(exit_code, ResultType.SUSPECT, null);
            } else {
                System.out.println("out: " + outStream.toString());
            }
            HashMap<String,Object> outMap = new HashMap<String,Object>();

            parseOutputStream(outMap);

            ResultType res = ResultType.valueOf((String)outMap.get(RESULT));
            String viz = (String) outMap.get(VISUALIZATION);
            ret_val = new WallThicknessResult(exit_code, res, viz);

            System.out.println("Result: " + res);
            System.out.println("viz:" + viz);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            ret_val = new WallThicknessResult(1, ResultType.SUSPECT, null);
        }

        return ret_val;
    }

    /**
     * Parse the output stream for the triangle count info and add
     * to the parameter map.
     *
     * @param outMap The parameter map
     */
    protected void parseOutputStream(Map<String, Object> outMap) {
        String output = outStream.toString();
        StringTokenizer lineTokenizer = new StringTokenizer(output, "\n");

        StringTokenizer st;
        String val = null;
        ArrayList<String> outFiles = new ArrayList<String>();
        String thinRegionText = "";
        String warningText = "";

        try {
            while (lineTokenizer.hasMoreTokens()) {
                String line = lineTokenizer.nextToken().trim();

                // get the eroded region count
                if (line.startsWith(PARTS_COUNT_ORIGINAL_MARKER)) {
                    val = line.replace(PARTS_COUNT_ORIGINAL_MARKER, "");
                    String[] text = (val.trim()).split(" ");
                    outMap.put("originalParts", Integer.valueOf(text[0]));

                    // get the number of rump voxels
                } else if (line.startsWith(PARTS_COUNT_ERODED_MARKER)) {
                    val = line.replace(PARTS_COUNT_ERODED_MARKER, "");
                    String[] text = (val.trim()).split(" ");
                    outMap.put("erodedParts", Integer.valueOf(text[0]));

                    // get the genus of the original model
                } else if (line.startsWith(ORIGINAL_GENUS_MARKER)) {
                    val = line.replace(ORIGINAL_GENUS_MARKER, "");
                    String[] text = (val.trim()).split(" ");
                    outMap.put("originalGenus", Integer.valueOf(text[0]));

                    // get the genus of the eroded model
                } else if (line.startsWith(ERODED_GENUS_MARKER)) {
                    val = line.replace(ERODED_GENUS_MARKER, "");
                    String[] text = (val.trim()).split(" ");
                    outMap.put("erodedGenus", Integer.valueOf(text[0]));

                    // get the number of rump voxels
                } else if (line.startsWith(RUMP_MARKER)) {
                    val = line.replace(RUMP_MARKER, "");
                    outMap.put("rumpVoxels", Integer.valueOf(val.trim()));

                    // get the number of thin voxels
                } else if (line.startsWith(THIN_MARKER)) {
                    val = line.replace(THIN_MARKER, "");
                    outMap.put("thinVoxels", Integer.valueOf(val.trim()));

                    // get the number of interface voxels
                } else if (line.startsWith(INTERFACE_MARKER)) {
                    val = line.replace(INTERFACE_MARKER, "");
                    outMap.put("interfaceVoxels", Integer.valueOf(val.trim()));

                    // get the number of boundary voxels
                } else if (line.startsWith(BOUNDARY_MARKER)) {
                    val = line.replace(BOUNDARY_MARKER, "");
                    outMap.put("boundaryVoxels", Integer.valueOf(val.trim()));

                    // get the ratio of thin to rump voxels
                } else if (line.startsWith(THIN_RUMP_RATIO_MARKER)) {
                    val = line.replace(THIN_RUMP_RATIO_MARKER, "");
                    outMap.put("thinRumpRatio", Double.valueOf(val.trim()));

                    // get the text for the thin regions
                } else if (line.startsWith(THIN_REGION_MARKER)) {
                    val = line.replace(THIN_REGION_MARKER, "");
                    thinRegionText = thinRegionText + val.trim() + "\n";

                    // get the wt warning message
                } else if (line.startsWith(WT_WARNING_MARKER)) {
                    val = line.replace(WT_WARNING_MARKER, "");
                    warningText = warningText + val.trim() + "\n";


                    // get the wallthickness result
                } else if (line.startsWith(RESULT_MARKER)) {
                    val = line.replace(RESULT_MARKER, "");
                    outMap.put(RESULT, val.trim());
                } else if (line.startsWith(VIZ_MARKER)) {
                    val = line.replace(VIZ_MARKER, "");
                    outMap.put(VISUALIZATION, val.trim());
                }
            }

            if (warningText != null && warningText.length() > 0) {
                outMap.put("warning", warningText);
            }

            if (thinRegionText != null && thinRegionText.length() > 0) {
                outMap.put("thinRegionOutput", thinRegionText);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Launch a shell script requiring bash.
     *
     * @param name The command to launch
     * @param params The params
     * @param dir The dir to launch from
     * @return The return code, 0 if successful
     */
    public int launchBashProcess(String name, String[] params, File dir,
                                 PrintStream out, PrintStream outDup, PrintStream err, PrintStream errDup, String[] addEnvVars) {


        try {
            String osName = System.getProperty("os.name" );
            String[] cmd = new String[2 + params.length];

            if(osName.indexOf("Windows") > -1) {
                // Assume we have cygwin installed
                cmd[0] = "\\cygwin\\bin\\bash.exe" ;
                cmd[1] = name;
            } else {
                // Assume a UNIX box
                cmd[0] = "/bin/bash" ;
                cmd[1] = name;
            }

            for(int i=0; i < params.length; i++) {
                cmd[i+2] = params[i];
            }


//System.out.println("Dir: " + dir);
//System.out.println("Commands:");
/*
            for(int i=0; i < cmd.length; i++) {
                System.out.println(cmd[i]);
            }
*/
            // Add environment variables required for execution
            // If there are additional variables, must also get system environment variables
            String[] envVarList = null;

            if (addEnvVars != null && addEnvVars.length > 0) {
                envVarList = new String[addEnvVars.length + 1];

                for (int i=0; i<envVarList.length-1; i++) {
                    envVarList[i] = addEnvVars[i];
                }

                // Get system environment variables
                String systemEnvVars = System.getenv("PATH");
                envVarList[envVarList.length-1] = "PATH=" + systemEnvVars;
            }
//            String[] env = new String[] {"PATH=" + envPath, "YMT_DIR=/var/www/html/release/server/"};
//System.out.println("==> envVarList: " + java.util.Arrays.toString(envVarList));

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd, envVarList, dir);

            StreamLogger errorLogger = new
                    StreamLogger(proc.getErrorStream(), err, errDup);

            StreamLogger outputLogger = new
                    StreamLogger(proc.getInputStream(), out, outDup);

            errorLogger.start();
            outputLogger.start();

            int exitVal = proc.waitFor();

            // Wait for all logging to stop so its complete.
            while(errorLogger.isAlive() || outputLogger.isAlive()) {
                try {Thread.sleep(25); } catch(Exception e) {}
            }

            return exitVal;
        } catch (Throwable t) {
            t.printStackTrace(err);
        }

        return -1;
    }

    /**
     * Launch a shell script requiring bash.
     *
     * @param name The command to launch
     * @param params The params
     * @param dir The dir to launch from
     * @return The return code, 0 if successful
     */
    public int launchBashProcess(String name, String[] params, File dir, PrintStream out, PrintStream err, String[] addEnvVars) {
        return launchBashProcess(name,params,dir,out,null,err,null,addEnvVars);
    }

    /**
     * Execute a bash script.
     *
     * @param name The script location and name
     * @param params The command line params
     * @param dir The directory
     * @return The exit code from the script
     */
    protected int executeScript(String name, String[] params, File dir) throws IOException {
        int idx = 0;

        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();

        PrintStream out = new PrintStream(outStream);
        PrintStream err = new PrintStream(errStream);

//        int exit_code = pl.launchBashProcess(name, params, dir, out, System.out, err, System.err, ENV_VARIABLES);
        int exit_code = launchBashProcess(name, params, dir, out, err, ENV_VARIABLES);

        out.flush();
        err.flush();
        outStream.flush();
        errStream.flush();

        return exit_code;
    }
}

class MaterialProperties {
    private String name;
    private double minWallthickness;

    MaterialProperties(String name, double minWallthickness) {
        this.name = name;
        this.minWallthickness = minWallthickness;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMinWallthickness() {
        return minWallthickness;
    }

    public void setMinWallthickness(double minWallthickness) {
        this.minWallthickness = minWallthickness;
    }
}