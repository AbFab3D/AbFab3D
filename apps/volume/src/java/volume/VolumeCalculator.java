/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package volume;

// External Imports
import java.text.NumberFormat;
import java.util.ArrayList;

import java.io.File;

import javax.vecmath.*;

import org.web3d.vrml.sav.ErrorHandler;

//Internal Imports
import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.util.SysErrorReporter;
import abfab3d.util.ExitCodes;
import abfab3d.validate.VolumeChecker;

/**
 * Calculate the volume of a mesh. Compute the volume as a sum of signed
 * volumes of tetrahedra.
 *
 * Technique borrowed from this paper: Implicit Fairing of Irregular
 *    Meshes Using Diffusion and Curvature Flow
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class VolumeCalculator {

    /** The usage message printed for this code */
    private final static String USAGE_MSG =
        "usage: VolumeChecker [-debug 2] [-scale 1.0][-maxRunTime n] filename\n" +
        " -debug int        0-4 to indicate log level, 0 = print all, 4 = print fatal\n" +
        " -scale float      Default output is in metres, and cubic metres. This will \n" +
        "                   scale change the units used (eg scale of 100 generates\n" +
        "                   output numbers in centimetres.\n";


    /** The maximum number of digits for a fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 2;

    /** The scale multiplier to go from meters  */
    private final static float DEFAULT_SCALE_MULTIPLIER = 1.0f;

    /** the total volume */
    private float volume;

    /** Set the formating of numbers for output */
    private NumberFormat numberFormatter;

    /** current scale factor to use */
    private static float scale;

    /** The volume calculator */
    private VolumeChecker calc;

    /**
     * Simple constructor.
     *
     * @param errorHandler a SysErrorReporter.
     */
    public VolumeCalculator(ErrorHandler errorReporter) {
        numberFormatter = NumberFormat.getNumberInstance();
        numberFormatter.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        numberFormatter.setGroupingUsed(false);

        calc = new VolumeChecker(errorReporter);
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------


    /**
     * Execute the command
     */
    public static void main(String args[]) {
        int debug = SysErrorReporter.PRINT_ERRORS;
        ErrorHandler console = new SysErrorReporter(debug);

        VolumeCalculator checker = new VolumeCalculator(console);

        console.messageReport("Begining volume check...");

        //
        // check to make sure we have some appropriate input args
        //
        if( args.length <= 0){
            printUsage();
            System.exit(ExitCodes.INVALID_ARGUMENTS);
        }
        long millisToWait = 0;

        scale = DEFAULT_SCALE_MULTIPLIER;

        //
        // parse the arguments, sort out a help request
        //
        for (int argIndex = 0 ; argIndex < args.length; argIndex++ ) {

            String argument = args[argIndex];

            if (argument.startsWith("-")) {

                if (argument.equals( "-debug" )) {
                    debug = Integer.parseInt(args[++argIndex]);
                    ((SysErrorReporter)console).setLogLevel(debug);
                } else if (argument.equals( "-scale" )) {
                    scale = Float.parseFloat(args[++argIndex]);
                } else {
                    // ignore this argument; it is not recognized
                }
            }
        } // end for loop

        // the final option must be the file
        File input = new File(args[args.length - 1]);

        if (input.exists() && input.isFile() && input.canRead()) {

            //
            // Begin the main parsing work.

            console.messageReport("using scale: " + scale);
            checker.processFile(input);
        } else {
            printUsage();
            System.exit(ExitCodes.FILE_NOT_FOUND);
        }

        System.exit(0);
    }

    private void processFile(File input) {
        calc.processFile(input);

        float vol = calc.calculateVolume(scale);

        System.out.println("Volume is: " + vol);
    }

    /**
     * Print the usage statement
     */
    private static void printUsage() {
        System.out.println(USAGE_MSG);
    }
}