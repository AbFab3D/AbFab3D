/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
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

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static abfab3d.util.Output.printf;

/**
 * SVX Reader.
 *
 * Reads the Shapeways voxel transmittal format.
 *
 * @author Alan Hudson
 */
public class SVXReader {
    /**
     * Load a SVX file into a grid.
     *
     * @param file The zip file
     * @return
     * @throws IOException
     */
    public AttributeGrid load(String file) throws IOException {

        ZipFile zip = null;

        try {
            zip = new ZipFile(file);

            ZipEntry entry = zip.getEntry("manifest.xml");
            InputStream is = zip.getInputStream(entry);
            SVXManifest mf = parseManifest(is);

            if (mf == null) {
                throw new IOException("Could not parse manifest file");
            }

            AttributeGrid ret_val = new ArrayAttributeGridByte(mf.getGridSizeX(),mf.getGridSizeY(),mf.getGridSizeZ(),mf.getVoxelSize(),mf.getVoxelSize());

            double[] bounds = new double[6];

            bounds[0] = mf.getOriginX();
            bounds[1] = mf.getOriginX() + mf.getGridSizeX() * mf.getVoxelSize();
            bounds[2] = mf.getOriginY();
            bounds[3] = mf.getOriginY() + mf.getGridSizeY() * mf.getVoxelSize();
            bounds[4] = mf.getOriginZ();
            bounds[5] = mf.getOriginZ() + mf.getGridSizeZ() * mf.getVoxelSize();
            ret_val.setGridBounds(bounds);

            return ret_val;
        } finally {
            if (zip != null) zip.close();
        }
    }

    /**
     * Parse the manifest file
     * @param src The manifest file
     * @return
     */
    private SVXManifest parseManifest(InputStream src) throws IOException {

        String field = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(src);

            NodeList grid_list = doc.getElementsByTagName("grid");

            if (grid_list == null) {
                throw new IllegalArgumentException("File contains no grid element");
            }

            Element grid = (Element) grid_list.item(0);

            if (grid == null) {
                throw new IllegalArgumentException("File contains no grid element");
            }

            SVXManifest ret_val = new SVXManifest();

            field = "gridSizeX";
            ret_val.setGridSizeX(Integer.parseInt(grid.getAttribute(field)));
            field = "gridSizeY";
            ret_val.setGridSizeY(Integer.parseInt(grid.getAttribute(field)));
            field = "gridSizeZ";
            ret_val.setGridSizeZ(Integer.parseInt(grid.getAttribute(field)));
            field = "voxelSize";
            ret_val.setVoxelSize(Double.parseDouble(grid.getAttribute(field)));
            field = "originX";
            ret_val.setOriginX(Double.parseDouble(grid.getAttribute(field)));
            field = "originY";
            ret_val.setOriginY(Double.parseDouble(grid.getAttribute(field)));
            field = "originZ";
            ret_val.setOriginZ(Double.parseDouble(grid.getAttribute(field)));


            return ret_val;
        } catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch(SAXException saxe) {
            saxe.printStackTrace();
        } catch(Exception e) {
            printf("Cannot parse field: %s\n",field);
            e.printStackTrace();
        }

        return null;
    }
}
