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
package abfab3d.opencl;

import com.jogamp.opencl.CLResource;

/**
 * A resource wrapper for a JOGL CLResource
 *
 * @author Alan Hudson
 */
public class OpenCLResource implements Resource {
    private CLResource resource;
    private String name;

    public OpenCLResource(CLResource resource) {
        this.resource = resource;
    }

    public OpenCLResource(CLResource resource, String name) {
        this.resource = resource;
        this.name = name;
    }

    @Override
    public void release() {
        if (resource != null) {
            resource.release();
            // Release reference to hopefull cause cleanup of DirectBuffer
            resource = null;
        }
    }

    public CLResource getResource() {
        return resource;
    }

    public boolean isReleased() {
        if (resource == null) return true;
        return resource.isReleased();
    }
    public String toString() {
        if (name != null) {
            return super.toString() + " name: " + name;
        } else return super.toString();
    }
}
