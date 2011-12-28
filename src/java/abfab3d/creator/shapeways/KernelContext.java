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
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Defines a set of methods that a hosted creator uses to communicate with its hosting container, for example, to get resources or write to a log file.
 *
 * There is one context per kernel per Java Virtual Machine.
 *
 * @author Alan Hudson
 */
public interface KernelContext {
    /**
     * Returns the resource located at the named path as an InputStream object.
     *
     * The data in the InputStream can be of any type or length. The path must be specified according to the rules given in getResource. This method returns null if no resource exists at the specified path.
     * Meta-information such as content length and content type that is available via getResource method is lost when using this method.
     * The servlet container must implement the URL handlers and URLConnection objects necessary to access the resource.
     *
     * This method is different from java.lang.Class.getResourceAsStream, which uses a class loader. This method allows servlet containers to make a resource available to a servlet from any location, without using a class loader.
     *
     * @param name - a String specifying the path to the resource
     * @returns the InputStream returned to the servlet, or null if no resource exists at the specified path
     */
    public InputStream getResourceAsStream(String name);

    /**
     * Returns a URL to the resource that is mapped to a specified path. The path must begin with a "/" and is interpreted as relative to the current context root.
     *
     * This method allows the servlet container to make a resource available to servlets from any source. Resources can be located on a local or remote file system, in a database, or in a .war file.
     * The servlet container must implement the URL handlers and URLConnection objects that are necessary to access the resource.
     *
     * This method returns null if no resource is mapped to the pathname.
     *
     * Some containers may allow writing to the URL returned by this method using the methods of the URL class.
     *
     * The resource content is returned directly, so be aware that requesting a .jsp page returns the JSP source code. Use a RequestDispatcher instead to include results of an execution.
     *
     * This method has a different purpose than java.lang.Class.getResource, which looks up resources based on a class loader. This method does not use class loaders.
     *
     * @param path a String specifying the path to the resource
     * @returns the resource located at the named path, or null if there is no resource at that path
     * @throws: java.net.MalformedURLException - if the pathname is not given in the correct form
     */
    public java.net.URL getResource(java.lang.String path)
                         throws java.net.MalformedURLException;
}
