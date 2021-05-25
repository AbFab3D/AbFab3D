/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.input;

// External Imports
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.web3d.parser.vrml97.VRML97Reader;
import org.web3d.vrml.parser.BaseReader;
import xj3d.filter.AbstractFilter;

import xj3d.filter.node.CommonEncodedBaseFilter;
import xj3d.filter.node.CommonScene;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.ArrayData;


import org.web3d.vrml.export.Exporter;
import org.web3d.vrml.export.NullExporter;
import org.web3d.vrml.sav.ErrorHandler;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.InputSource;


//import org.web3d.vrml.nodes.*;
//import org.xj3d.core.loading.*;
import org.web3d.parser.x3d.X3DReader;
import org.web3d.vrml.lang.VRMLException;

// Internal Imports

/**
 * Loads an X3D file into an in memory representation.
 *
 * All supported incoming geometry is converted to an indexed triangle array.
 *
 * @author Alan Hudson
 */
public class X3DFileLoader extends CommonEncodedBaseFilter {
    /** Default major spec version to export as in X3D */
    private static final int DEFAULT_OUPUT_MAJOR_VERSION = 3;

    /** Default major spec version to export as in X3D */
    private static final int DEFAULT_OUPUT_MINOR_VERSION = 1;

    /** Reference to the registered error handler if we have one */
    private static ErrorHandler console;

    /** The loaded scene */
    private CommonScene parsedScene;

    /** The loaded Shapes */
    private ArrayList<CommonEncodable> shapes;

    public X3DFileLoader(ErrorHandler eh) {
        console = eh;
        shapes = new ArrayList<CommonEncodable>();
    }

    /**
     * Load the specified file X3D file.
     */
    public void loadFile(File input) throws IOException, IllegalArgumentException {
        int export_major_version = DEFAULT_OUPUT_MAJOR_VERSION;
        int export_minor_version = DEFAULT_OUPUT_MINOR_VERSION;

        InputSource is = new InputSource(input);

        Exporter writer = new NullExporter(export_major_version,
                                      export_minor_version,
                                      console);

        BaseReader reader;

        if (FilenameUtils.getExtension(input.getAbsolutePath()).equalsIgnoreCase("wrl")) {
            reader = new VRML97Reader();
        } else {
            reader = new X3DReader();
        }

        AbstractFilter filter = this;

        reader.setContentHandler(filter);
        reader.setRouteHandler(filter);
        reader.setScriptHandler(filter);
        reader.setProtoHandler(filter);
        reader.setErrorReporter(console);

        filter.setContentHandler(writer);
        filter.setRouteHandler(writer);
        filter.setScriptHandler(writer);
        filter.setProtoHandler(writer);



        reader.parse(is);

        try {
            // clean up...
            is.close();
        } catch (IOException ioe) {
            // ignore
        }

        // Convert all supported geometry to ITS
        List<CommonEncodable> nodes = parsedScene.getRootNodes();

        // Assume the scenegraph is flat via visual_mesh_conversion.sh

        int len = nodes.size();
        for(int i=0; i < len; i++) {
            CommonEncodable enc = nodes.get(i);
            if (enc.getNodeName().equals("Shape")) {
                if(enc.getValue("geometry") != null){ // skip empty shapes 
                    processShape(enc);
                    shapes.add(enc);
                }
            }
        }

        Iterator<CommonEncodable> itr2 = shapes.iterator();
        while(itr2.hasNext()) {
            CommonEncodable n = itr2.next();
            parsedScene.removeRootNode(n);
        }
    }

    /**
     * Load the specified file X3D file.
     */
    public void load(String baseURL, InputStream input) throws IOException, IllegalArgumentException {
        int export_major_version = DEFAULT_OUPUT_MAJOR_VERSION;
        int export_minor_version = DEFAULT_OUPUT_MINOR_VERSION;

        InputSource is = new InputSource(baseURL,input);

        Exporter writer = new NullExporter(export_major_version,
                export_minor_version,
                console);

        X3DReader reader = new X3DReader();

        AbstractFilter filter = this;

        reader.setContentHandler(filter);
        reader.setRouteHandler(filter);
        reader.setScriptHandler(filter);
        reader.setProtoHandler(filter);
        reader.setErrorReporter(console);

        filter.setContentHandler(writer);
        filter.setRouteHandler(writer);
        filter.setScriptHandler(writer);
        filter.setProtoHandler(writer);


        reader.parse(is);

        try {
            // clean up...
            is.close();
        } catch (IOException ioe) {
            // ignore
        }

        // Convert all supported geometry to ITS
        List<CommonEncodable> nodes = parsedScene.getRootNodes();

        // Assume the scenegraph is flat via visual_mesh_conversion.sh

        int len = nodes.size();
        for(int i=0; i < len; i++) {
            CommonEncodable enc = nodes.get(i);
            if (enc.getNodeName().equals("Shape")) {
                if(enc.getValue("geometry") != null){ // skip empty shapes 
                    processShape(enc);
                    shapes.add(enc);
                }
            }
        }

        Iterator<CommonEncodable> itr2 = shapes.iterator();
        while(itr2.hasNext()) {
            CommonEncodable n = itr2.next();
            parsedScene.removeRootNode(n);
        }
    }

    /**
     * Get all the global nodes such as Viewpoints.  No Shape data will be present.
     */
    public List<CommonEncodable> getGlobals() {
        return parsedScene.getRootNodes();
    }

    /**
     * Get the Shape bundles of Appearance and Geometry.
     */
    public List<CommonEncodable> getShapes() {
        return shapes;
    }

    /**
     * Clear all resources associated with this loader.
     */
    public void clear() {
        parsedScene = null;
        shapes = null;
    }

    /**
     * Convert geometry into ITS
     */
    private void processShape(CommonEncodable node) throws IllegalArgumentException {
        CommonEncodable geom = (CommonEncodable) node.getValue("geometry");

        ArrayData adata;
        int[] coord_index = null;

        String nodeName = geom.getNodeName();

        if (nodeName.equals("IndexedFaceSet")) {
            adata = (ArrayData) geom.getValue("coordIndex");
        } else {
            adata = (ArrayData) geom.getValue("index");
        }

        coord_index = (int[]) adata.data;

        if (nodeName.equals("IndexedTriangleSet")) {
        } else if (nodeName.equals("IndexedFaceSet")) {
        	coord_index = convertIFSToITS(coord_index);
        } else if (nodeName.equals("IndexedTriangleStripSet")) {
            coord_index = MeshConverter.convertITSSToITS(coord_index);
        } else if (nodeName.equals("IndexedTriangleFanSet")) {
            coord_index = MeshConverter.convertITFSToITS(coord_index);
        } else {
            System.out.println("Unsupported geometry: " + geom.getNodeName());
            return;
        }

        CommonEncodable its = factory.getEncodable("IndexedTriangleSet", null);
        its.setValue("index",new ArrayData(coord_index, coord_index.length));
        its.setValue("coord", (CommonEncodable)geom.getValue("coord"));
        its.setValue("color", (CommonEncodable)geom.getValue("color"));
        its.setValue("normal", (CommonEncodable)geom.getValue("normal"));
        its.setValue("texCoord", (CommonEncodable)geom.getValue("texCoord"));
        its.setValue("ccw", (Boolean) geom.getValue("ccw"));
        its.setValue("colorPerVertex", (Boolean) geom.getValue("colorPerVertex"));
        its.setValue("normalPerVertex", (Boolean) geom.getValue("normalPerVertex"));
        its.setValue("solid", (Boolean) geom.getValue("solid"));

        node.setValue("geometry", its);
    }

    /**
     * Converts the IndexedFaceSet index list to IndexedTriangleSet index list.
     * Assumes that the IFS index list is constructed as triangles (every 4th
     * index is the -1 separator); otherwise an IllegalArgumentException is thrown.
     * 
     * @param coord_index The IndexedFaceSet index
     * @return newly ordered IndexedTriangleSet index
     * @throws IllegalArgumentException
     */
    private int[] convertIFSToITS(int[] coord_index) throws IllegalArgumentException {
    	// Calculate the length of the new index array
    	// Assumes that every 4th index is the IFS -1 separator
    	int len = coord_index.length - (coord_index.length / 4);
    	
        int[] triangleIndex = new int[len];
        int newIndex = 0;
        
        try {
            for (int i=0; i<coord_index.length; i++) {
            	// Skip every 4th index
            	if ((i + 1) % 4 == 0) {
            		continue;
            	}
            	
            	// Make sure that index is not -1, which can happen if triangles 
            	// and quads (for example) are in the index list
            	if (coord_index[i] < 0) {
            		throw new IllegalArgumentException();
            	}
            	
            	triangleIndex[newIndex] = coord_index[i];
            	newIndex++;
            }
        } catch (Exception e) {
        	throw new IllegalArgumentException("Error converting IFS to ITS. IFS index list must be in triangles.");
        }

        return triangleIndex;
    }
    
    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endDocument() throws SAVException, VRMLException {

        parsedScene = scene;

        super.endDocument();
    }

}