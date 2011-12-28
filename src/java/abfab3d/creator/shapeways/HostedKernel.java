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

package abfab3d.creator.shapeways;

// External Imports

// Internal Imports
import abfab3d.creator.GeometryKernel;

/**
 * Provides an abstract class to be subclassed to create a creator suitable
 * for creators deployed to Shapeways Hosted Creators.
 *
 * All hosted creators must extend this class.
 *
 * Any number of instances may be started to service requests on the backend.
 * An instance may be reused between calls.
 *
 * @author Alan Hudson
 */
public abstract class HostedKernel implements GeometryKernel {
    /** Result Ref URN */
    public static final String RESULT_URN = "hcn:shapeways:resultref:";

    /** User uploaded parameter ref URN */
    private static final String PARAM_URN = "hcn:shapeways:paramref:";


    /**
     * Get the services available to hosted creators.
     *
     * @return The services manager
     */
    public ServicesAPI getServicesManager() {
        return LocalServicesAPI.getInstance();
    }

    /**
     * Called by the creator container to indicate to a creator that the creator
     * instance is being placed into service.
     */
    public void init() {
    }

    /**
     * Called by the creator container to indicate to a creator that the creator
     * instance is being taken out of service
     */
    public void destroy() {
    }

    /**
     * Get the kernel's context.
     */
    public KernelContext getKernelContext() {
        return new LocalKernelContext();
    }

    /**
     * Get the host name we are housed in.
     */
    public String getServerInfo() {
        return "localhost";
    }
}
