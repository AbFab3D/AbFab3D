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

import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.core.Transformer;
import abfab3d.core.Vec;
import abfab3d.core.VecTransform;
import abfab3d.datasources.ImageColorMap;
import abfab3d.util.*;
import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.TextureTransformMatrix;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
 * Class to read collection of triangles and textures from X3D file
 *
 * @author Alan Hudson
 */
public class AttributedX3DReader implements AttributedTriangleProducer, Transformer {

    static final boolean DEBUG = true;

    /** Transformation to apply to positional vertices, null for none */
    private VecTransform m_transform;
    private X3DFileLoader m_fileLoader;

    /** Path to file to read from */
    private String m_path;
    private InputStream m_is;
    private String m_baseURL;
    private boolean m_initialized = false;


    private ImageColorMap[] m_textures;
    AttributeCalculator m_attributeCalculator = null;

    private int m_dataDimension = 0;
    private int m_lastTex = 0;
    private HashMap<String,Integer> texMap = new HashMap<String, Integer>();
    public AttributedX3DReader(String path) {
        File f = new File(path);
        m_path = f.getAbsolutePath();
        m_baseURL = f.getParent();
        printf("base url: %s\n",m_baseURL);
    }

    public AttributedX3DReader(InputStream is, String baseURL) {
        m_is = is;
        m_baseURL = baseURL;
    }

    /**
     * Set the transform to be applied to each triangle before passing it to TriangleCollector.
     *
     * @param transform The transform or null for identity.
     */
    public void setTransform(VecTransform transform) {
        this.m_transform = transform;
    }

    public void initialize(){
        if(m_initialized)
            return;

        try {
            m_fileLoader = new X3DFileLoader(new SysErrorReporter(SysErrorReporter.PRINT_ERRORS));
        
            if (m_is != null) {
                m_fileLoader.load(m_baseURL,m_is);
            } else {
                m_fileLoader.loadFile(new File(m_path));
            }
            List<CommonEncodable> shapes = m_fileLoader.getShapes();
            
            m_dataDimension = calcDataDimension(shapes);
            m_attributeCalculator = new AttributeCalculator(m_textures, m_dataDimension-3);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
        m_initialized = true;
    }

    /**
       reads file and passes triangles to TriangleCollector
     */
    private void read(AttributedTriangleCollector out)  throws IOException {

        initialize();

        List<CommonEncodable> shapes = m_fileLoader.getShapes();

        Iterator<CommonEncodable> itr = shapes.iterator();
        int texID = 0;
        m_textures = new ImageColorMap[shapes.size()];

        while(itr.hasNext()) {

            CommonEncodable shape = itr.next();
            CommonEncodable appNode = (CommonEncodable) shape.getValue("appearance");
            CommonEncodable texNode = null;
            CommonEncodable transNode = null;
            if (appNode != null) {
                texNode = (CommonEncodable) appNode.getValue("texture");
                transNode = (CommonEncodable) appNode.getValue("textureTransform");  // not supported yet but will
            }
            CommonEncodable its = (CommonEncodable) shape.getValue("geometry");
            CommonEncodable coordNode = (CommonEncodable) its.getValue("coord");
            CommonEncodable tcoordNode = (CommonEncodable) its.getValue("texCoord");
            float[] coord = (float[]) ((ArrayData)coordNode.getValue("point")).data;
            int[] coordIndex = (int[]) ((ArrayData)its.getValue("index")).data;
            ArrayData tcoordData = null;
            float[] tcoord = null;
            if (tcoordNode != null) {
                tcoordData = ((ArrayData) tcoordNode.getValue("point"));
                tcoord = (float[]) ((ArrayData) tcoordNode.getValue("point")).data;
            }
            int[] tcoordIndex = coordIndex;
            ArrayData tci = ((ArrayData)its.getValue("texCoordIndex"));
            if (tci != null) tcoordIndex = (int[]) tci.data;

            if (transNode != null) {
                Matrix4f tmat = getMatrix(transNode);
                Point3f pnt = new Point3f();

                int num_coord = tcoordData.num;
                int num_point = num_coord / 2;
                float[] point_xfrm = new float[num_coord];
                int idx = 0;
                for (int j = 0; j < num_point; j++) {
                    pnt.x = tcoord[idx];
                    pnt.y = tcoord[idx + 1];
                    tmat.transform(pnt);
                    point_xfrm[idx] = pnt.x;
                    point_xfrm[idx + 1] = pnt.y;
                    idx += 2;
                }
                tcoord = point_xfrm;
            }

            if (texNode != null) texID = getTextureID(texNode);

            switch(m_dataDimension) {
                case 3:
                    addTriangles3(coord, coordIndex, out);
                    break;
                case 5:
                    addTriangles5(coord, tcoord, coordIndex, tcoordIndex, out);
                    break;
                case 6:
                    addTriangles6(coord, tcoord, texID, coordIndex, tcoordIndex, out);
                    break;
            }
        }

    }

    /**
     * Calculate the dimension of vectors.
     * 3,5,6 vec size based on source material
     *    3 - no color info(x,y,z)
     *    5 - single texture(x,y,z,u,v)
     *    6 - multi texture(x,y,z,u,v,texIndex)
     *    n - multi attributes(x,y,z,a1,a2,a3...an)
     * @param shapes
     * @return
     */
    private int calcDataDimension(List<CommonEncodable> shapes) {
        Iterator<CommonEncodable> itr = shapes.iterator();
        int tex = 0;

        while(itr.hasNext()) {

            CommonEncodable shape = itr.next();
            CommonEncodable appNode = (CommonEncodable) shape.getValue("appearance");
            CommonEncodable texNode = null;

            if (appNode != null) {
                texNode = (CommonEncodable) appNode.getValue("texture");
                if (texNode != null) tex++;
            }
        }

        switch(tex) {
            case 0:
                return 3;
            case 1:
                return 5;
            default:
                return 6;
        }
    }

    private int getTextureID(CommonEncodable tex) {
        String[] url = (String[]) ((ArrayData)tex.getValue("url")).data;
        Boolean repeatX = (Boolean) tex.getValue("repeatX");
        Boolean repeatY = (Boolean) tex.getValue("repeatY");

        String path = m_baseURL + File.separator + url[0];
        Integer idx = texMap.get(path);
        if (idx != null) {
            return idx;
        }

        idx = m_lastTex++;

        if (DEBUG) printf("X3DReader.getTextureID: %s\n",path);
        ImageColorMap icm = new ImageColorMap(path,1,1,1);
        icm.setCenter(new Vector3d(0.5,0.5,0.5));
        if (repeatX != null) icm.setRepeatX(repeatX);
        if (repeatY != null) icm.setRepeatY(repeatY);

        icm.initialize();
        m_textures[idx] = icm;
        texMap.put(path,idx);

        return idx;
    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTriangles3(float coord[],int coordIndex[], AttributedTriangleCollector out){
        if(DEBUG)printf("%s.addTriangles(coord:%d, coordIndex:%d)\n", this,coord.length, coordIndex.length );
        // count of triangles
        int len = coordIndex.length / 3;

        Vec
                v0 = new Vec(3),
                v1 = new Vec(3),
                v2 = new Vec(3);

        for(int i=0, idx = 0; i < len; i++ ) {

            int off = coordIndex[idx++] * 3;
            v0.v[0] = coord[off++];
            v0.v[1] = coord[off++];
            v0.v[2] = coord[off++];

            off = coordIndex[idx++] * 3;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            off = coordIndex[idx++] * 3;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            makeTransform(v0, v1, v2);
            out.addAttTri(v0, v1, v2);
        }

    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTriangles6(float coord[],float tcoord[],int tex, int coordIndex[], int[] tcoordIndex, AttributedTriangleCollector out){
        if(DEBUG)printf("%s.addTriangles(coord:%d, coordIndex:%d)\n", this,coord.length, coordIndex.length );
        // count of triangles
        int len = coordIndex.length / 3;

        Vec
            v0 = new Vec(6),
            v1 = new Vec(6),
            v2 = new Vec(6);

        for(int i=0, idx = 0; i < len; i++ ) {

            int off = coordIndex[idx] * 3;
            int toff = tcoordIndex[idx++] * 2;
            v0.v[0] = coord[off++];
            v0.v[1] = coord[off++];
            v0.v[2] = coord[off++];

            v0.v[3] = tcoord[toff++];
            v0.v[4] = 1-tcoord[toff++];
            v0.v[5] = tex;

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            v1.v[3] = tcoord[toff++];
            v1.v[4] = 1-tcoord[toff++];
            v1.v[5] = tex;

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            v2.v[3] = tcoord[toff++];
            v2.v[4] = 1-tcoord[toff++];
            v2.v[5] = tex;
            makeTransform(v0, v1, v2);
            out.addAttTri(v0, v1, v2);
        }

    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTriangles5(float coord[],float tcoord[], int coordIndex[], int[] tcoordIndex, AttributedTriangleCollector out){
        if(DEBUG)printf("%s.addTriangles(coord:%d, coordIndex:%d)\n", this,coord.length, coordIndex.length );
        // count of triangles
        int len = coordIndex.length / 3;

        Vec
                v0 = new Vec(5),
                v1 = new Vec(5),
                v2 = new Vec(5);

        for(int i=0, idx = 0; i < len; i++ ) {

            int off = coordIndex[idx] * 3;
            int toff = tcoordIndex[idx++] * 2;
            v0.v[0] = coord[off++];
            v0.v[1] = coord[off++];
            v0.v[2] = coord[off++];

            v0.v[3] = tcoord[toff++];
            v0.v[4] = 1-tcoord[toff++];

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            v1.v[3] = tcoord[toff++];
            v1.v[4] = 1-tcoord[toff++];

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            v2.v[3] = tcoord[toff++];
            v2.v[4] = 1-tcoord[toff++];

            makeTransform(v0, v1, v2);
            out.addAttTri(v0, v1, v2);
        }

    }

    final void makeTransform(Vec v0, Vec v1, Vec v2){

        if(m_transform == null)
            return;

        m_transform.transform(v0,v0);
        m_transform.transform(v1,v1);
        m_transform.transform(v2,v2);
    }


    /**
     * interface TriangleProducer2
     */
    public boolean getAttTriangles(AttributedTriangleCollector out) {
        try {

            read(out);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(fmt("Exception while reading file:%s\n", m_path), e);
        }
    }

    /**
     *  Calculate attribute properties at (u,v, tz) point.
     * @return
     */
    public DataSource getAttributeCalculator() {

        return m_attributeCalculator;
    }

    /**
     * Return the transform matrix
     *
     * @return the transform matrix
     */
    private Matrix4f getMatrix(CommonEncodable transform) {
        TextureTransformMatrix matrixSource = new TextureTransformMatrix();
        ArrayData translation_data = (ArrayData) transform.getValue("translation");
        if (translation_data != null) {
            matrixSource.setTranslation((float[]) translation_data.data);
        }
        Float rotation_data = (Float) transform.getValue("rotation");
        if (rotation_data != null) {
            matrixSource.setRotation(rotation_data);
        }
        ArrayData scale_data = (ArrayData) transform.getValue("scale");
        if (scale_data != null) {
            matrixSource.setScale((float[]) scale_data.data);
        }

        ArrayData center_data = (ArrayData) transform.getValue("center");
        if (center_data != null) {
            matrixSource.setCenter((float[]) center_data.data);
        }
        return (matrixSource.getMatrix());
    }

    /**
     * Returns how many data channels this file contains.  Must be called after getAttTriangles call.
     */
    public int getDataDimension() {
        initialize();
        return m_dataDimension;
    }


    /**
       calculates color value for point in texture coordinates
       multiple textures are indexed by 3rd coordinate
       supports multiple textures
     */
    static class AttributeCalculator implements DataSource {
        static int debugCount = 5000;

        int channelsCount;
        ImageColorMap textures[];
        AttributeCalculator(ImageColorMap[] textures, int channelsCount){
            this.channelsCount = channelsCount;
            this.textures = textures;
        }

        /**
         * Returns u,v or u,v,texIndex
         *
         * @param pnt Point where the data is calculated
         * @param dataValue - storage for returned calculated data
         * @return
         */
        public int getDataValue(Vec pnt, Vec dataValue) {
            int tz = 0;
            double EPS = 1e-5;

            // TODO: All of these compares are bad, switch to different impls based on dimensions
/*
            if (pnt.v[0] == 0.000) {
                printf("pnt: %s --> dv: %s\n", Vec.toString(pnt), Vec.toString(dataValue));
            }
*/
            if (channelsCount > 2) {
                tz = (int) pnt.v[2];
            }

            if (tz > textures.length - 1 || textures[tz] == null) {
                dataValue.set(pnt);
                return ResultCodes.RESULT_OK;
            }

            ImageColorMap icm = textures[tz];
            icm.getDataValue(pnt,dataValue);

            if (DEBUG) {
                if (debugCount-- > 0) {
                    if (pnt.v[0] == 0.0) {
                        printf("pnt: %s --> dv: %s\n", Vec.toString(pnt), Vec.toString(dataValue));
                    }
                }
            }
            return ResultCodes.RESULT_OK;
        }

        /**

           @Override
        */
        public int getChannelsCount() {
            return channelsCount;
        }

        /**
           @Override
        */
        public Bounds getBounds() {
            return null;
        }
        /**
           @Override
         */
        public void setBounds(Bounds bounds) {
            //ignore
        }
    } // class AttributeCalculator

}
