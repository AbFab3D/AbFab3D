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

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A utility class to write XML to the provided ut stream
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class XMLWriter extends DefaultHandler {

    /** The stream to write to */
    private Writer out;

    /** A temporary buffer for character data */
    private StringBuffer textBuffer;

    public XMLWriter(Writer out) {
        textBuffer = new StringBuffer();
        this.out = out;
    }

    //----------------------------------------------------------
    // SAX ContentHandler methods
    //----------------------------------------------------------

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument() throws SAXException {

        print("<?xml version='1.0' encoding='UTF-8'?>");
        printNewLine();

    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() throws SAXException {

        try {
            printNewLine();
            out.flush();
        } catch (IOException e) {
            throw new SAXException("SAX error", e);
        }

    }

    /**
     * Handles characters event.
     *
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters .
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        String s = new String(ch, start, length);

        bodyElement(s);

    }

    /**
     * Handles startElement event.
     *
     * @param namespaceURI The namespace URI.
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     * @param attributes The specified or defaulted attributes.
     */
    public void startElement(
            String namespaceURI,
            String localName,
            String qName,
            Attributes attributes) throws SAXException {

        flushText();

        String name = null;

        if (localName.equals("")) {
            name = qName;
        } else {
            name = localName;
        }

        print("<" + name);

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String aName = attributes.getLocalName(i);

                if ("".equals(aName)) {
                    aName = attributes.getQName(i);
                }

                String value = attributes.getValue(i);
                print(" " + aName + "='" + value + "'");

            }

        }
        print(">");

    }

    /**
     * Handles endElement event.
     *
     * @param namespaceURI namespace URI
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     */
    public void endElement(String namespaceURI, String localName, String qName )
        throws SAXException {

        flushText();

        String name = localName;

        if ("".equals(name)) {
            name = qName;
        }

        print("</" + name + ">");

    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Helper function to append the text to the node
     *
     * @param content The ocntent to add
     */
    public void bodyElement(String content) {

        // check for possible character issues
        if (content.contains("&")) {
            content = content.replaceAll("&", "&#38;");
        }
        if (content.contains("<")) {
            content = content.replaceAll("<", "&#60;");
        }
        if (content.contains(">")) {
            content = content.replaceAll(">", "&#62;");
        }
        if (content.contains("\"")) {
            content = content.replaceAll("\"", "&#34;");
        }
        if (content.contains("\'")) {
            content = content.replaceAll("\'", "&#39;");
        }
        if (content.contains("!")) {
            content = content.replaceAll("!", "&#33;");
        }

        if (textBuffer == null) {
            textBuffer = new StringBuffer(content);
        } else {
            textBuffer.append(content);
        }

    }

    /**
     * Display a string.
     */
    private void print(String s) throws SAXException {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    /**
     * Display a newLine.
     */
    private void printNewLine() throws SAXException {
        try {
            out.write(System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    /**
     * Flush the text accumulated in the character buffer.
     */
    private void flushText() throws SAXException {
        if (textBuffer == null) {
            return;
        }

        print(textBuffer.toString());
        textBuffer = null;
    }

}
