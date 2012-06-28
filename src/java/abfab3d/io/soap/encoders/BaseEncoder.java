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

package abfab3d.io.soap.encoders;

import abfab3d.io.soap.*;

/**
 * Reads XML and assigns the properties to the Java Object representation.
 * Writes XML from the Java Object.  Uses a custom encoder if specified in
 * the TypeMappingRegistry.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public abstract class BaseEncoder {

    /** The XML Writer to use */
    protected XMLWriter xmlWriter;

    /** The XML Parser to use */
    protected Encodeable returnObject;

    /** The current schema being used */
    protected String schema;

    /** The current schema instance being used */
    protected String schemaInstance;

    /** The current encoding style being used */
    protected String encodingStyle;

    /**
     * Protected Constructor
     */
    protected BaseEncoder() {}

    /**
     * Set XML writer.
     *
     * @param xmlWriter The writer to use
     */
    public void setXmlWriter(XMLWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    /**
     * Set Encodeable returnObject
     *
     * @param xmlReader The reader to use
     */
    public void setReturnObject(Encodeable returnObject) {
        this.returnObject = returnObject;
    }

    /**
     * Set the schema information.  Generally done from the Envelope.
     *
     * @param schema String representing the schema.
     * @param schemaInstance String representing the schema instance.
     * @param encodingStyle String representing the encoding style.
     */
    public void setSchemas(
            String schema,
            String schemaInstance,
            String encodingStyle) {

        this.schema = schema;
        this.schemaInstance = schemaInstance;
        this.encodingStyle = encodingStyle;
    }

    /**
     * Returns the current schema instance
     * @return the schemaInstance
     */
    public String getSchemaInstance() {
        return schemaInstance;
    }


}
