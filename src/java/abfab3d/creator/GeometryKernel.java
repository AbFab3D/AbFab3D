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

package abfab3d.creator;

// External Imports
import java.io.*;
import java.util.*;
import org.web3d.vrml.sav.BinaryContentHandler;

// Internal Imports

/**
 * Generates 3D geometry from parameters.  A kernel should require
 * no user-input.
 *
 * All Kernels should have a no parameter constructor.  All
 * instance params should come through the generate method.
 *
 * Hosted editors will insure that only a single thread will call
 * generate at a time on a particular instance.
 * This means class variable usage is safe.
 *
 * @author Alan Hudson
 */
public interface GeometryKernel {
    /** The accuracy to generate the model. */
    public enum Accuracy {VISUAL, PRINT};

    /**
     * Generate X3D stream from the specified parameters.  Do not include any
     * X3D Bindables such as Viewpoints, NavigationInfo, Fog, or Background nodes.
     * No lights either.  Just geometry and appearance information.
     *
     * @param params The parameters
     * @param acc The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String,Object> params, GeometryKernel.Accuracy acc, BinaryContentHandler handler) throws IOException;

    // TODO: add revenue model scheme
    //public CostScheme getCostScheme();

   /**
     * Validates the specified parameters.  Will throw a IllegalArgumentException if its
     * invalid.  A user interface may use this method to insure the parameters are
     * valid.  This should be more then just range checking, it should make sure the
     * object will generate.  If you have geometric relationships that must hold true then
     * you should generate the object and say whether it passed.  If you can inspect the
     * values and know its ok then just do that.
     *
     * The goal is to allow the UI to show if the input is valid without having to generate
     * the model completely.  Hopefully the latency on this is less then the actual generation.
     *
     * Perhaps consider caching results temporarily in case the generate method is called?
     *
     * @param params The parameters
     */
    //public boolean validateParams(Map<String,Object> params) throws IOException;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams();
}
