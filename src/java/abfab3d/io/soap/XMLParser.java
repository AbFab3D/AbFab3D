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

package abfab3d.io.soap;

// External imports
import java.io.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;

// Local imports

/**
 * Common reader implementation that supports the parsing of an
 * XML imput stream.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class XMLParser {

    // Variables for our general use during parsing

    /** The factory to generate SAX parser instances */
    private SAXParserFactory parserFactory;

    /** Reader of the XML stream */
    private XMLReader reader;

    /**
     * Create a new instance of the reader.
     */
    public XMLParser()
        throws FactoryConfigurationError, ParserConfigurationException, SAXException {

        try {

            // use the factory to create a parser
            parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(true);

            SAXParser parser = parserFactory.newSAXParser();
            reader = parser.getXMLReader();

        } catch (FactoryConfigurationError fce) {

            throw new FactoryConfigurationError("No SAX parser defined");

        } catch (ParserConfigurationException pce) {

            throw new ParserConfigurationException("Unable to configure factory as required");

        } catch (SAXException se) {

            throw new SAXException("General SAX Error");

        }

     }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Parse a XML document represented by the given input source. This
     * method should not be called while currently processing a stream. If
     * multiple concurrent streams need to be parsed then multiple instances
     * of this interface should be used.
     *
     * @param input The input source to be used
     * @throws IOException An I/O error while reading the stream
     */
    public void parse(InputStream input) throws IOException {

        // Convert our InputStream, to a SAX InputSource
        org.xml.sax.InputSource xis = new org.xml.sax.InputSource();
        xis.setByteStream(input);

        try {

            reader.parse(xis);

        } catch(SAXException se) {

            Exception e = se.getException();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            if(e != null) {
                e.printStackTrace(pw);
            } else {
                se.printStackTrace(pw);
            }

            StringBuffer buf = new StringBuffer("SAX Error: ");
            buf.append(se.toString());
            buf.append(sw.toString());
            throw new IOException(buf.toString());

        }

    }

    public void setContentHandler(ContentHandler soapAdapter) {
        // assign the adaptor
        reader.setContentHandler(soapAdapter);
    }

}
