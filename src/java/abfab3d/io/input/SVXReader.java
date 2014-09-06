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
import org.web3d.util.spatial.SliceRegion;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
    /** The manifest for the last load call */
    private SVXManifest mf;

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
            mf = parseManifest(is);

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

            List<Channel> channels = mf.getChannels();

            for(Channel chan : channels) {
                if (chan.getType().getId() == Channel.Type.DENSITY.getId()) {
                    SlicesReader sr = new SlicesReader();
                    sr.readSlices(ret_val,zip,chan.getSlices(),0,0,mf.getGridSizeY());
                }
            }
            return ret_val;
        } finally {
            if (zip != null) zip.close();
        }
    }

    public SVXManifest getManifest() {
        return mf;
    }

    /**
     * Parse the manifest file
     * @param src The manifest file
     * @return
     */
    private SVXManifest parseManifest(InputStream src) throws IOException {

        String field = null;
        String val = null;

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
            val = grid.getAttribute(field);
            ret_val.setGridSizeX(Integer.parseInt(val));
            field = "gridSizeY";
            val = grid.getAttribute(field);
            ret_val.setGridSizeY(Integer.parseInt(val));
            field = "gridSizeZ";
            val = grid.getAttribute(field);
            ret_val.setGridSizeZ(Integer.parseInt(val));
            field = "voxelSize";
            val = grid.getAttribute(field);
            ret_val.setVoxelSize(Double.parseDouble(val));
            field = "originX";
            val = grid.getAttribute(field);
            ret_val.setOriginX(Double.parseDouble(val));
            field = "originY";
            val = grid.getAttribute(field);
            ret_val.setOriginY(Double.parseDouble(val));
            field = "originZ";
            val = grid.getAttribute(field);
            ret_val.setOriginZ(Double.parseDouble(val));
            field = "subvoxelBits";
            val = grid.getAttribute(field);
            ret_val.setSubvoxelBits(Integer.parseInt(val));

            field = "channels";

            NodeList channel_list = doc.getElementsByTagName("channel");

            if (channel_list == null) {
                throw new IllegalArgumentException("File contains no channels element");
            }

            int len = channel_list.getLength();

            ArrayList<Channel> clist = new ArrayList<Channel>();
            for(int i=0; i < len; i++) {
                Element channel = (Element) channel_list.item(i);

                field = "type";
                val = channel.getAttribute(field);
                String type = val;

                field = "slices";
                val = channel.getAttribute(field);
                String slices = val;

                clist.add(new Channel(Channel.Type.valueOf(type),slices));
            }

            ret_val.setChannels(clist);

            return ret_val;
        } catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch(SAXException saxe) {
            saxe.printStackTrace();
        } catch(Exception e) {
            printf("Cannot parse field: %s  val: %s\n",field,val);
            e.printStackTrace();
        }

        return null;
    }
}
