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

import app.common.WallThicknessResult.ResultType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Runs aopt to optimize an X3D file into a binary form.
 *
 * @author Alan Hudson
 */
public class AoptRunner {
    private static final String SCRIPT_FILE = "aopt.sh";
    private static final String RELEASE_LOC_UNIX = "/var/www/html/release/server_v1.5.10";
    private static final String RELEASE_LOC_WIN  = "\\var\\www\\html\\release\\server_v1.5.10";

    /** The output stream */
    protected ByteArrayOutputStream outStream;

    /** The output stream */
    protected ByteArrayOutputStream errStream;

    private String[] ENV_VARIABLES;

    /**
     * Run aopt
     *
     * @param filename  X3D file, must meet analytical file constraints.
     * @param perface True for per-face normals otherwise pervertex
     */
    public AoptResult run(String filename, boolean perface) {


        String releaseLoc = null;
        String command = null;
        String osName = System.getProperty("os.name");
        String outfile = "aopt_out.x3d";

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
/opt/instantReality/bin/aopt -i 291616.v0.s05.visual_mesh_conversion.sh.x3db -d Normal -f PrimitiveSet:normalPerVertex:false -F Scene:'cacheopt(true)' -f PrimitiveSet:optimizationMode:none -V -G ./:sacp -f SpatialGeometry:solid:false -x aopt_out.x3d
*/
        String[] params = null;

        if (perface) {
            params = new String[] {"-i", filename, "-d", "Normal", "-f","PrimitiveSet:normalPerVertex:false",
                "-F","Scene:'cacheopt(true)'", "-f", "PrimitiveSet:optimizationMode:none", "-V","-G", "./:sacp", "-f",
                "SpatialGeometry:solid:false", "-x", outfile};
        } else {
            params = new String[] {"-i", filename, "-d", "Normal", "-f","PrimitiveSet:normalPerVertex:true", "-f", "PrimitiveSet:creaseAngle:4",
                    "-F","Scene:'cacheopt(true)'", "-f", "PrimitiveSet:optimizationMode:none", "-V","-G", "./:sacp", "-f",
                    "SpatialGeometry:solid:false", "-x", outfile};
        }
        String workingDirPath = "/tmp";

        AoptResult ret_val = null;

        File f = new File(command);
        if (!f.canExecute()) {
            System.out.println("Cannot execute aopt, ignoring request.");
            return new AoptResult(0, filename);
        }

        try {
            System.out.println("Executing command: " + command);
            System.out.println("Params:  " + java.util.Arrays.toString(params));
            int exit_code = executeScript(command, params, new File(workingDirPath));

            if (exit_code != 0) {
                System.out.println("ExitCode: " + exit_code);
                System.out.println("out: " + outStream.toString());
                System.out.println("err: " + errStream.toString());
                return new AoptResult(exit_code, null);
            } else {
                System.out.println("out: \n" + outStream.toString());
            }

            ret_val = new AoptResult(exit_code, workingDirPath + File.separator + outfile);

        } catch(IOException ioe) {
            ioe.printStackTrace();
            ret_val = new AoptResult(1, null);
        }

        return ret_val;
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
                cmd[0] = "c:\\cygwin\\bin\\bash.exe" ;
                cmd[1] = name;

                File f = new File(cmd[0]);
                if (!f.exists()) {
                    cmd[0] = "c:\\cygwin\\bin\\bash.exe" ;
                    f = new File(cmd[0]);

                    if (!f.exists()) {
                        System.out.println("Cannot find cygwin bash.exe");
                        return -1;
                    }
                }
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

