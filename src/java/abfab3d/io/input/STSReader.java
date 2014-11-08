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

import abfab3d.grid.AttributeGrid;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.TriangleMesh;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static abfab3d.util.Output.printf;

/**
 * SVX Reader.
 *
 * Reads the Simple Triangle Shells format.
 *
 * @author Alan Hudson
 */
public class STSReader {
    /** The manifest for the last load call */
    private STSManifest mf;

    /**
     * Load a STS file into a grid.
     *
     * @param file The zip file
     * @return
     * @throws java.io.IOException
     */
    public TriangleMesh[] loadMeshes(String file) throws IOException {
        ZipFile zip = null;
        TriangleMesh[] ret_val = null;

        try {
            zip = new ZipFile(file);

            ZipEntry entry = zip.getEntry("manifest.xml");
            if (entry == null) {
                throw new IOException("Cannot find manifest.xml in top level");
            }

            InputStream is = zip.getInputStream(entry);
            mf = parseManifest(is);

            if (mf == null) {
                throw new IOException("Could not parse manifest file");
            }

            List<STSPart> plist = mf.getParts();
            int len = plist.size();

            ret_val = new TriangleMesh[len];
            for(int i=0; i < len; i++) {
                STSPart part = plist.get(i);
                ZipEntry ze = zip.getEntry(part.getFile());
                MeshReader reader = new MeshReader(zip.getInputStream(ze),"", FilenameUtils.getExtension(part.getFile()));
                IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
                reader.getTriangles(its);

                // TODO: in this case we could return a less heavy triangle mesh struct?
                WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
                ret_val[i] = mesh;
            }

            return ret_val;
        } finally {
            if (zip != null) zip.close();
        }
    }

    /**
     * Load a STS file into a grid.
     *
     * @param is The stream
     * @return
     * @throws java.io.IOException
     */
    public TriangleMesh[] loadMeshes(InputStream is) throws IOException {
        ZipArchiveInputStream zis = null;
        TriangleMesh[] ret_val = null;

        try {
            zis = new ZipArchiveInputStream(is);

            ArchiveEntry entry = null;
            int cnt = 0;

            while ((entry = zis.getNextEntry()) != null) {
                // TODO: not sure we can depend on this being first
                if (entry.getName().equals("manifest.xml")) {
                    mf = parseManifest(zis);

                    ret_val = new TriangleMesh[mf.getParts().size()];
                } else {
                    MeshReader reader = new MeshReader(zis, "", FilenameUtils.getExtension(entry.getName()));
                    IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
                    reader.getTriangles(its);

                    // TODO: in this case we could return a less heavy triangle mesh struct?
                    WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
                    ret_val[cnt++] = mesh;
                }
            }
        } finally {
            zis.close();
        }

        return ret_val;
    }

    /**
     * Load a STS file into a grid.
     *
     * @param file The zip file
     * @return
     * @throws java.io.IOException
     */
    public AttributeGrid loadGrid(String file) throws IOException {

        ZipFile zip = null;

        try {
            zip = new ZipFile(file);

            ZipEntry entry = zip.getEntry("manifest.xml");
            if (entry == null) {
                throw new IOException("Cannot find manifest.xml in top level");
            }

            InputStream is = zip.getInputStream(entry);
            mf = parseManifest(is);

            if (mf == null) {
                throw new IOException("Could not parse manifest file");
            }
/*
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
*/
            return null;
        } finally {
            if (zip != null) zip.close();
        }
    }

    public STSManifest getManifest() {
        return mf;
    }

    /**
     * Parse the manifest file
     * @param src The manifest file
     * @return
     */
    private STSManifest parseManifest(InputStream src) throws IOException {

        String field = null;
        String val = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(src);

            NodeList model_list = doc.getElementsByTagName("model");

            if (model_list == null) {
                throw new IllegalArgumentException("File contains no model element");
            }

            Element model = (Element) model_list.item(0);

            if (model == null) {
                throw new IllegalArgumentException("File contains no model element");
            }

            STSManifest ret_val = new STSManifest();
            int len;
            field = "parts";

            NodeList part_list = doc.getElementsByTagName("part");

            if (part_list == null) {
                throw new IllegalArgumentException("File contains no part elements");
            }

            len = part_list.getLength();

            ArrayList<STSPart> plist = new ArrayList<STSPart>();
            for(int i=0; i < len; i++) {
                Element part = (Element) part_list.item(i);

                field = "file";
                val = part.getAttribute(field);
                String file = val;

                field = "material";
                val = part.getAttribute(field);
                String material = val;

                plist.add(new STSPart(file, material));
            }

            ret_val.setParts(plist);

            NodeList metadata_list = doc.getElementsByTagName("entry");

            NodeList material_list = doc.getElementsByTagName("material");

            if (material_list != null) {
                HashMap<String,String> map = new HashMap<String,String>();
                len = material_list.getLength();

                for(int i=0; i < len; i++) {
                    Element entry = (Element) material_list.item(i);
                    String key = entry.getAttribute("id");
                    String value = entry.getAttribute("urn");

                    if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                        map.put(key, value);
                    }
                }
                ret_val.setMaterials(map);
            }

            if (metadata_list != null) {
                HashMap<String,String> map = new HashMap<String,String>();
                len = metadata_list.getLength();

                for(int i=0; i < len; i++) {
                    Element entry = (Element) metadata_list.item(i);
                    String key = entry.getAttribute("key");
                    String value = entry.getAttribute("value");

                    if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                        map.put(key, value);
                    }
                }
                ret_val.setMetadata(map);
            }

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
