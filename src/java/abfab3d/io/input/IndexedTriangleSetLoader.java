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

package abfab3d.io.input;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFileChooser;

import org.web3d.parser.DefaultFieldParserFactory;
import org.web3d.parser.DefaultVRMLParserFactory;
import org.web3d.util.SimpleStack;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.sav.*;

import abfab3d.util.ExitCodes;

/**
 * This class is used to load a file and parse the
 * IndexedTriangleSet information into an array of
 * coordinates and an array of vertices.
 *
 * @author Eric Fickenscher
 * @version $Revision: 1.1 $
 */
public class IndexedTriangleSetLoader
        implements StringContentHandler,
                   BinaryContentHandler {

    /** String message to use if a file does not contain IndexedTriangleSets */
    private static final String NO_ITS_ERROR_MESSAGE =
        "File parsing failed - no IndexedTriangleSets found";

    /**
     * The coordinates array contains a float coordinate
     * for every _unique_ vertex.
     * <p>
     * Example: To extract the three floats of the second
     * vertex of triangle TI:<br> {@link #vertices}[TI+1]
     * gives us an index to the second vertex of the triangle.<p>
     * VI = {@link #vertices}[TI+1]<ul>
     * <li>x = coordinates[ &nbsp;VI&nbsp;&nbsp;&nbsp;]</li>
     * <li>y = coordinates[VI+1]</li>
     * <li>z = coordinates[VI+2]</li></ul>
     */
    protected float[] coordinates;

    /**
     * This array provides an index into the {@link #coordinates} array
     * for every vertex of every triangle. Thus,
     * vertices.length is three times the number of triangles/faces.<p>
     * For a given triangle index TI, <ul><li>
     * vertices[ &nbsp;TI&nbsp;&nbsp;&nbsp;] points to the first
     * vertex of the triangle; </li><li>
     * vertices[TI+1] points to the second vertex of the triangle;
     * </li><li>
     * vertices[TI+2] points to the third vertex of the triangle. </li></ul>
     *
     * Note that this is a simplification intended to reduce overall number
     * of multiplications needed. <br> We do so by modifying the original
     * IndexedTriangleArray by pushing a 'times 3' operation through
     * when building the array. <p>
     * Thus, a two-triangle X3D &lt;IndexedTriangleSet index='0 1 2 0 2 3'&gt;
     * would produce a vertices array of [0, 3, 6, 0, 6, 9].
     */
    protected int[] vertices;

    /** The bounds [minX, maxX, minY, maxY, minZ, maxZ] of
     * the {@link #coordinates}. */
    protected float[] bounds;

    /** After doing a bounds check on the object, what is the largest
     * {@link #bounds} value, in terms of absolute value? */
    protected float maxAbsoluteBounds;

    /** Reference to the registered error handler if we have one */
    protected static ErrorHandler console;

    /** Are we processing a document */
    private boolean processingDocument;

    /** Are we inside an IndexedTriangleSet? */
    private boolean insideITS;

    /** Have we found any IndexedTriangleSets in the entire file? */
    private boolean foundITS;

    /** A stack of node names */
    private SimpleStack nodeStack;

    /** A stack of field names */
    private SimpleStack fieldStack;

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    /** Used to read a VRML formated field */
    private VRMLFieldReader fieldReader;

    /** List of supported Nodes */
    private HashSet<String> validNodes;

    /**
     * Constructor
     * @param errorHandler Reference to the registered error handler.
     */
    public IndexedTriangleSetLoader(ErrorHandler errorHandler){
        console = errorHandler;

        //
        // initialize variables needed for document parsing
        //
        insideITS = false;
        processingDocument = false;
        foundITS = false;

        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        fieldValuesStack = new SimpleStack();

        // define the list of allowed nodes
        validNodes = new HashSet<String>();
        validNodes.add("IndexedTriangleFanSet");
        validNodes.add("IndexedTriangleSet");
        validNodes.add("IndexedTriangleStripSet");
    }

    //-----------------------------------------------------------------------
    // Methods for interface ContentHandler
    //-----------------------------------------------------------------------

    /**
     * Ignored here.<br>
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <CODE>startDocument()</CODE> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    public void setDocumentLocator(Locator loc) {
        //locator = loc;
    }

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The full VRML version string of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        if (processingDocument)
            return;

        processingDocument = true;

        int majorVersion = 3;
        int minorVersion = 0;

        if(type.charAt(1) == 'V') {
            // we're in VRML model either 97 or 1.0.
            // Look at the 6th character to see the version number
            // ie "VRML V1.0" or "VRML V2.0"
            boolean is_20 = (version.charAt(1) == '2');

            if(is_20) {
                majorVersion = 2;
            }

        } else {
            // Parse the number string looking for the version minor number.
            int dot_index = version.indexOf('.');
            String minor_num = version.substring(dot_index + 1);

            // Should this look for a badly formatted number here or just
            // assume the parsing beforehad has correctly identified something
            // already dodgy?
            minorVersion = Integer.parseInt(minor_num);

        }

        //console.messageReport("Using version: " + majorVersion + "." + minorVersion);

        DefaultFieldParserFactory fieldParserFactory =
            new DefaultFieldParserFactory();
        fieldReader = fieldParserFactory.newFieldParser(majorVersion,
                                                        minorVersion);

    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentName)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * A META declaration has been found in the code. There may be zero
     * or more meta declarations in the file, appearing just after the
     * component declaration. Each meta declaration has a key and value
     * strings. No information is to be implied from this. It is for extra
     * data only.
     *
     * @param key The value of the key string
     * @param value The value of the value string
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void metaDecl(String key, String value)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * An IMPORT declaration has been found in the document. All three
     * parameters will always be provided, regardless of whether the AS keyword
     * has been used or not. The parser implementation will automatically set
     * the local import name as needed.
     *
     * @param inline The name of the inline DEF nodes
     * @param exported The exported name from the inlined file
     * @param imported The local name to use for the exported name
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void importDecl(String inline, String exported, String imported)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * An EXPORT declaration has been found in the document. Both paramters
     * will always be provided regardless of whether the AS keyword has been
     * used. The parser implementation will automatically set the exported
     * name as needed.
     *
     * @param defName The DEF name of the nodes to be exported
     * @param exported The name to be exported as
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void exportDecl(String defName, String exported)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endDocument() throws SAVException, VRMLException {
        processingDocument = false;
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (validNodes.contains(name)) {
            foundITS = true;
            insideITS = true;
            fieldValuesStack.push(new HashMap());

        }

        nodeStack.push(name);

    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endNode() throws SAVException, VRMLException {

        String nodeName = (String) nodeStack.pop();

        HashMap<String, Object> fieldValues;

        if (validNodes.contains(nodeName)) {

            insideITS = false;
            fieldValues = (HashMap<String, Object>) fieldValuesStack.pop();

            /////////////////////////////////////////////////////////////////
            // Get the geometry information

            // get the indices, convert index as needed
            vertices = null;

            if (fieldValues.get("IndexedTriangleSet.index") != null) {

                Object fieldVal = fieldValues.get("IndexedTriangleSet.index");

                if(fieldVal instanceof int[]) {
                    vertices = (int [])fieldVal;
                } else if(fieldVal instanceof String) {
                    vertices = fieldReader.MFInt32((String)fieldVal);
                }
            } else if (fieldValues.get("IndexedTriangleStripSet.index") != null) {

                Object fieldVal = fieldValues.get("IndexedTriangleStripSet.index");

                int[] tmpIndex = null;
                if(fieldVal instanceof int[]) {
                    tmpIndex = (int [])fieldVal;
                } else if(fieldVal instanceof String) {
                    tmpIndex = fieldReader.MFInt32((String)fieldVal);
                }
                vertices = MeshConverter.convertITSSToITS(tmpIndex);

            } else if (fieldValues.get("IndexedTriangleFanSet.index") != null) {

                Object fieldVal = fieldValues.get("IndexedTriangleFanSet.index");

                int[] tmpIndex = null;
                if(fieldVal instanceof int[]) {
                    tmpIndex = (int [])fieldVal;
                } else if(fieldVal instanceof String) {
                    tmpIndex = fieldReader.MFInt32((String)fieldVal);
                }
                vertices = MeshConverter.convertITFSToITS(tmpIndex);
            }

            // get the coordinates
            coordinates = null;
            if (fieldValues.get("Coordinate.point") != null) {

                Object fieldVal = fieldValues.get("Coordinate.point");
                if(fieldVal instanceof float[]) {
                    coordinates = (float [])fieldVal;
                } else if(fieldVal instanceof String) {
                    coordinates = fieldReader.MFVec3f((String)fieldVal);
                }
            }

            if (coordinates == null || vertices == null) {
                console.messageReport("IndexedTriangle* geomtery not well defined.");
                System.exit(ExitCodes.INVALID_INPUT_FILE);
            }

            //
            // vertex and coordinate information has been set.
            // time for the rest of our preprocessing
            //
            updateVertices();

        } // else (! validNodes.contains(nodeName))
    }


    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startField(String name) throws SAVException, VRMLException {

        fieldStack.push(name);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void useDecl(String defName) throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)},
     * {@link StringContentHandler#fieldValue(String)}. or any of the
     * fieldValue methods in {@link BinaryContentHandler}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endField() throws SAVException, VRMLException {
        //System.out.println((String)
        fieldStack.pop();
    }

    //-----------------------------------------------------------------------
    // Methods for interface StringContentHandler
    //-----------------------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String value) throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            HashMap<String, String> fieldValues =
                (HashMap)fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }
    }

    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] values) throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        // flatten the array
        StringBuilder value  = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            value.append(values[i]);
            value.append(" ");
        }

        if (insideITS) {
            HashMap<String, String> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName,
                            value.toString().trim());
        }
    }

    //-----------------------------------------------------------------------
    // Methods for interface BinaryContentHandler
    //-----------------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            HashMap<String, Integer> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(int[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            HashMap<String, int[]> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, values);
        }

    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            HashMap<String, Boolean> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }

    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(boolean[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            HashMap<String, boolean[]> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, values);
        }

    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            HashMap<String, Float> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(float[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                value.append(" ");
            }

            HashMap<String, float[]> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, values);
        }

    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            HashMap<String, Long> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(long[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            HashMap<String, long[]> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, values);
        }
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            HashMap<String, Double> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }

    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(double[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            HashMap<String, double[]> fieldValues =
                (HashMap) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, values);
        }

    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                value.append(" ");
            }

            HashMap<String, String> fieldValues =
                (HashMap)fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName,
                            value.toString().trim());
        }

    }

    //---------------------------------------------------------------
    // Local Methods - public
    //---------------------------------------------------------------

    /**
     * Process the input file (which, when complete, will call
     * the method {@link #endNode()}.<p>
     * No error checking - Assumes that the file is non-null (this method
     * should only be called by {@link #loadFile(String)}.
     *
     * @param inputFile The file to look at for the calculations.
     * @see #endNode()
     */
    public void processFile(File inputFile) {

        //
        // process the file
        //
        console.messageReport("Processing file " + inputFile.toString() + "...");
        foundITS = false;

        InputSource source = new InputSource(inputFile);

        DefaultVRMLParserFactory parserFactory =
            new DefaultVRMLParserFactory();
        VRMLReader vrmlReader = parserFactory.newVRMLReader();
        vrmlReader.setContentHandler(this);
        vrmlReader.setErrorHandler(console);

        try {
            vrmlReader.parse(source);
        } catch (IOException e) {
            console.errorReport("File IO error", e);
            System.exit(ExitCodes.FILE_NOT_FOUND);
        } catch (VRMLException v) {
            console.errorReport("File parsing failed.", v);
            System.exit(ExitCodes.INVALID_INPUT_FILE);
        }

        //
        // If no indexed triangle sets were found, then there
        // is no usable geometry
        //
        if( !foundITS){
            console.errorReport(NO_ITS_ERROR_MESSAGE, new Exception());
            System.exit(ExitCodes.NO_GEOMETRY);
        }
    }


    /**
     * This method is used to compute {@link #bounds} and
     * {@link #maxAbsoluteBounds}.
     * <p>
     * This code iterates through the list of coordinates and does
     * a 'max or min?' check on each.<br>
     * It assumes that each coordinate will be used by the vertices
     * array.  This might not be the case; if we have coordinates
     * that are not referenced they will (incorrectly) be computed
     * in this bounds check.<p>
     * However, this is faster than the alternative of
     * looping through the vertices array.  <br> Though doing so
     * guarantees that only the 'used' coordinates contribute to
     * the bounds, it means processing each vertex multiple times
     * - once for each triangle that uses the vertex.
     */
    public void computeModelBounds(){

        // set the bounds using the first coordinate found
        float cx = coordinates[0];
        float cy = coordinates[1];
        float cz = coordinates[2];

        float min_x = cx;
        float max_x = cx;
        float min_y = cy;
        float max_y = cy;
        float min_z = cz;
        float max_z = cz;

        //
        // Iterate through the list of coordinates and check
        // if each x, y, and z is a new minimum or new maximum.
        //
        for( int vi = 0; vi < coordinates.length; ){

            //
            // get coords of the current vertex
            //
            cx = coordinates[ vi++ ];
            cy = coordinates[ vi++ ];
            cz = coordinates[ vi++ ];

            // gets max and min bounds
            if (cx > max_x)
                max_x = cx;

            if (cy > max_y)
                max_y = cy;

            if (cz > max_z)
                max_z = cz;

            if (cx < min_x)
                min_x = cx;

            if (cy < min_y)
                min_y = cy;

            if (cz < min_z)
                min_z = cz;
        }
        bounds = new float[]{min_x, max_x, min_y, max_y, min_z, max_z};

        //
        // Record the maximum absolute value of the bounds
        //
        for(int i = 0; i<bounds.length; i++){
            if( Math.abs(bounds[i]) > maxAbsoluteBounds)
                maxAbsoluteBounds = Math.abs(bounds[i]);
        }
    }


    /**
     * Get the coordinates.  Only call if {@link #processFile(File)}
     * has been called.
     * @return pointer to the {@link #coordinates} array.
     */
    public float[] getCoords(){
        return coordinates;
    }


    /**
     * Get the vertex indices.  Only call if {@link #processFile(File)}
     * has been called.
     * @return pointer to the {@link #vertices} array.
     */
    public int[] getVerts(){
        return vertices;
    }


    //---------------------------------------------------------------
    // Local Methods - private
    //---------------------------------------------------------------


    /**
     * This method finishes of preprocessing by computing the
     * {@link #bounds} and updating the {@link #vertices} array.<p>
     * At this point, for a given triangle index TI,<ul><li>
     * vertices[(TI*3)  &nbsp;&nbsp;&nbsp;&nbsp;] points to the
     * first vertex of the triangle;</li><li>
     * vertices[(TI*3)+1] points to the second vertex of the triangle;</li><li>
     * vertices[(TI*3)+2] points to the third vertex of the triangle.</li></ul>
     *
     * In order to reduce the overall number of multiplications needed,
     * here we modify the vertices index by pushing the "times 3"
     * multiplication through.  Thus, a two-triangle IndexedTriangleSet
     * with an vertex index='0 1 2 0 2 3' will end up with vertices=
     * [0, 3, 6, 0, 6, 9].  <p>That lets us use this instead: <ul><li>
     * vertices[ &nbsp;TI&nbsp;&nbsp;&nbsp;] points to the first vertex
     * of the triangle;</li><li>
     * vertices[TI+1] points to the second vertex of the triangle;</li><li>
     * vertices[TI+2] points to the third vertex of the triangle.</li></ul>
     *
     * This simplification lets us find the coordinates of a particular
     * vertex of a particular triangle like so:<p>
     * Example coords for the the second vertex of triangle TI:<ul><li>
     * x = coordinates[ vertices[ TI +1]  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;]
     * </li><li>y = coordinates[ vertices[ TI +1] +1]</li><li>
     * z = coordinates[ vertices[ TI +1] +2]</li></ul>
     */
    private void updateVertices(){
        //
        // update the vertices array
        //
        for(int i = 0; i < vertices.length; i++){
            vertices[i] *= 3;
        }
    }
}
