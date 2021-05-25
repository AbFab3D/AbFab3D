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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
 * Class to read collection of triangles and textures from X3D file
 *
 *
 * Texture values will trump all other color channels
 * Per Vertex color will trump material
 *
 * @author Alan Hudson
 */
public class AttributedX3DReader implements AttributedTriangleProducer, Transformer {

    static final boolean DEBUG = false;

    /** Transformation to apply to positional vertices, null for none */
    private VecTransform m_transform;
    private X3DFileLoader m_fileLoader;

    /** Path to file to read from */
    private String m_path;
    private InputStream m_is;
    private String m_baseURL;
    private boolean m_initialized = false;


    private ImageColorMap[] m_textures;
    private DataSource[] m_calcs = null;
    private DataSource m_attributeCalculator = null;

    private int m_dataDimension = 0;
    private int m_lastTex = 0;
    private HashMap<String,Integer> texMap = new HashMap<String, Integer>();

    public AttributedX3DReader(String path) {
        File f = new File(path);
        m_path = f.getAbsolutePath();
        m_baseURL = f.getParent();
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
        initialize(null);
    }

    private void initialize(AttributedTriangleCollector out){
        if(m_initialized)
            return;

        try {
            m_fileLoader = new X3DFileLoader(new SysErrorReporter(SysErrorReporter.PRINT_ERRORS));

            if (m_is != null) {
                m_fileLoader.load(m_baseURL,m_is);
            } else {
                m_fileLoader.loadFile(new File(m_path));
            }

            read(out);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
        m_initialized = true;
    }

    /**
       reads file and passes triangles to TriangleCollector
     */
    private void read(AttributedTriangleCollector out)  throws IOException {

        List<CommonEncodable> shapes = m_fileLoader.getShapes();

        Iterator<CommonEncodable> itr = shapes.iterator();

        if (m_textures == null || m_textures.length != shapes.size()) m_textures = new ImageColorMap[shapes.size()];
        m_dataDimension = calcDataDimension(shapes);
        int cnt = 0;
        if (m_calcs == null || m_calcs.length != shapes.size()) m_calcs = new DataSource[shapes.size()];

        if (DEBUG) printf("Reading x3d.  shapes: %d dims: %d\n  output: %b",shapes.size(),m_dataDimension,out != null);
        while(itr.hasNext()) {
            boolean hasVertexColor = false;
            boolean hasMaterialColor = false;
            boolean hasTexture = false;

            CommonEncodable shape = itr.next();
            CommonEncodable appNode = (CommonEncodable) shape.getValue("appearance");
            CommonEncodable texNode = null;
            CommonEncodable transNode = null;
            CommonEncodable matNode = null;
            if (appNode != null) {
                texNode = (CommonEncodable) appNode.getValue("texture");
                transNode = (CommonEncodable) appNode.getValue("textureTransform");
                matNode = (CommonEncodable) appNode.getValue("material");
            }
            CommonEncodable its = (CommonEncodable) shape.getValue("geometry");
            CommonEncodable coordNode = (CommonEncodable) its.getValue("coord");
            CommonEncodable tcoordNode = (CommonEncodable) its.getValue("texCoord");
            CommonEncodable colorNode = (CommonEncodable) its.getValue("color");
            float[] coord = (float[]) ((ArrayData)coordNode.getValue("point")).data;
            int[] coordIndex = (int[]) ((ArrayData)its.getValue("index")).data;
            float[] color = null;

            if (colorNode != null) {
                color = (float[]) ((ArrayData)colorNode.getValue("color")).data;
                if (color != null) {
                    hasVertexColor = true;
                }
            }

            if (matNode != null) {
                ArrayData ad = (ArrayData) matNode.getValue("diffuseColor");
                if (ad != null && ad.data != null) {
                    hasMaterialColor = true;
                }
            }

            if (texNode != null) {
                hasTexture = true;
            }

            ArrayData tcoordData = null;
            float[] tcoord = null;
            if (tcoordNode != null) {
                tcoordData = ((ArrayData) tcoordNode.getValue("point"));
                tcoord = (float[]) ((ArrayData) tcoordNode.getValue("point")).data;
            }
            int[] tcoordIndex = coordIndex;
            ArrayData tci = ((ArrayData)its.getValue("texCoordIndex"));
            if (tci != null) tcoordIndex = (int[]) tci.data;

            int[] colorIndex = coordIndex;
            ArrayData cci = ((ArrayData)its.getValue("colorIndex"));
            if (cci != null) colorIndex = (int[]) cci.data;

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

            int tex = -1;
            if (texNode != null) tex = addTexture(texNode);

            if (DEBUG) printf("Shape: %d  dim: %d hasTexture: %b  hasVertex: %b  hasMaterial: %b\n",cnt,m_dataDimension,hasTexture,hasVertexColor,hasMaterialColor);
            switch(m_dataDimension) {
                case 3:
                    if (out != null) addTriangles3(coord, coordIndex, out);
                    m_calcs[cnt] =  new InterpolatedAttributeCalculator();
                    break;
                case 5:
                    if (out != null) addTriangles5(coord, tcoord, coordIndex, tcoordIndex, out);
                    m_calcs[cnt] = new SingleTextureAttributeCalculator(m_textures[tex]);
                    break;
                case 6:
                    if (hasTexture) {
                        if (out != null) addTriangles6(coord, tcoord, tex, coordIndex, tcoordIndex, out);
                        m_calcs[cnt] = new SingleTextureAttributeCalculator(m_textures[tex]);
                    } else if (hasVertexColor) {
                        if (out != null) addTrianglesColor(coord, color, coordIndex, colorIndex, out);
                        m_calcs[cnt] = new InterpolatedAttributeCalculator();
                    } else if (hasMaterialColor) {
                        ArrayData ad = (ArrayData) matNode.getValue("diffuseColor");
                        Vec c = new Vec(3);
                        c.set(((float[])ad.data)[0],((float[])ad.data)[1],((float[])ad.data)[2]);
                        if (out != null) addTrianglesColor(coord, coordIndex, c, out);
                        m_calcs[cnt] = new InterpolatedAttributeCalculator();
                    } else {
                        // assume default diffuseColor if it doesn't exist
                        Vec c = new Vec(3);
                        c.set(0.8f,0.8f,0.8f);
                        if (matNode != null) {
                            ArrayData ad = (ArrayData) matNode.getValue("diffuseColor");
                            if (ad != null && ad.data != null) {
                                c.set(((float[])ad.data)[0],((float[])ad.data)[1],((float[])ad.data)[2]);
                            }
                        }

                        if (out != null) addTrianglesColor(coord, coordIndex, c, out);
                        m_calcs[cnt] = new InterpolatedAttributeCalculator();
                    }
                    break;
            }

            if (DEBUG) printf("Shape: %d  AttributeCalc: %s\n",cnt,m_calcs[cnt]);
            cnt++;
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

        if (shapes.size() > 1) return 6;

        boolean hasVertexColor = false;
        boolean hasMaterialColor = false;

        while(itr.hasNext()) {

            CommonEncodable shape = itr.next();
            CommonEncodable appNode = (CommonEncodable) shape.getValue("appearance");
            CommonEncodable texNode = null;
            CommonEncodable matNode = null;

            CommonEncodable its = (CommonEncodable) shape.getValue("geometry");
            CommonEncodable colorNode = null;
            if (its != null) {
                colorNode = (CommonEncodable) its.getValue("color");
                float[] color = null;

                if (colorNode != null) {
                    color = (float[]) ((ArrayData) colorNode.getValue("color")).data;
                    if (color != null) {
                        hasVertexColor = true;
                    }
                }
            }
            if (appNode != null) {
                texNode = (CommonEncodable) appNode.getValue("texture");
                if (texNode != null) {
                    tex++;
                }

                matNode = (CommonEncodable) appNode.getValue("material");
                if (matNode != null) {
                    ArrayData ad = (ArrayData) matNode.getValue("diffuseColor");
                    if (ad != null && ad.data != null) {
                        hasMaterialColor = true;
                    }
                }
            }
        }

        switch(tex) {
            case 0:
                if (hasVertexColor || hasMaterialColor) return 6;
                return 3;
            case 1:
                return 5;
            default:
                return 6;
        }
    }

    private int addTexture(CommonEncodable tex) {
        String[] url = (String[]) ((ArrayData)tex.getValue("url")).data;
        Boolean repeatX = (Boolean) tex.getValue("repeatX");
        Boolean repeatY = (Boolean) tex.getValue("repeatY");

        String path = m_baseURL + File.separator + url[0];
        Integer idx = texMap.get(path);
        if (idx != null) {
            return idx;
        }

        idx = m_lastTex++;

        if (DEBUG) printf("X3DReader.addTexture: %s\n",path);
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
            v0.v[4] = tcoord[toff++];
            v0.v[5] = tex;

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            v1.v[3] = tcoord[toff++];
            v1.v[4] = tcoord[toff++];
            v1.v[5] = tex;

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            v2.v[3] = tcoord[toff++];
            v2.v[4] = tcoord[toff++];
            v2.v[5] = tex;
            makeTransform(v0, v1, v2);
            out.addAttTri(v0, v1, v2);
        }

    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTrianglesColor(float coord[],float color[],int coordIndex[], int[] colorIndex, AttributedTriangleCollector out){
        if(DEBUG)printf("%s.addTriangles(coord:%d, coordIndex:%d)\n", this,coord.length, coordIndex.length );
        // count of triangles
        int len = coordIndex.length / 3;

        Vec
                v0 = new Vec(6),
                v1 = new Vec(6),
                v2 = new Vec(6);

        for(int i=0, idx = 0; i < len; i++ ) {

            int off = coordIndex[idx] * 3;
            int coff = colorIndex[idx++] * 3;
            v0.v[0] = coord[off++];
            v0.v[1] = coord[off++];
            v0.v[2] = coord[off++];

            v0.v[3] = color[coff++];
            v0.v[4] = color[coff++];
            v0.v[5] = color[coff++];

            off = coordIndex[idx] * 3;
            coff = colorIndex[idx++] * 3;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            v1.v[3] = color[coff++];
            v1.v[4] = color[coff++];
            v1.v[5] = color[coff++];

            off = coordIndex[idx] * 3;
            coff = colorIndex[idx++] * 3;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            v2.v[3] = color[coff++];
            v2.v[4] = color[coff++];
            v2.v[5] = color[coff++];
            makeTransform(v0, v1, v2);
            out.addAttTri(v0, v1, v2);
        }

    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTrianglesColor(float coord[],int coordIndex[], Vec color, AttributedTriangleCollector out){
        if(DEBUG)printf("%s.addTriangles(coord:%d, coordIndex:%d)\n", this,coord.length, coordIndex.length );
        // count of triangles
        int len = coordIndex.length / 3;

        Vec
                v0 = new Vec(6),
                v1 = new Vec(6),
                v2 = new Vec(6);

        for(int i=0, idx = 0; i < len; i++ ) {

            int off = coordIndex[idx++] * 3;
            v0.v[0] = coord[off++];
            v0.v[1] = coord[off++];
            v0.v[2] = coord[off++];

            v0.v[3] = color.v[0];
            v0.v[4] = color.v[1];
            v0.v[5] = color.v[2];

            off = coordIndex[idx++] * 3;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            v1.v[3] = color.v[0];
            v1.v[4] = color.v[1];
            v1.v[5] = color.v[2];

            off = coordIndex[idx++] * 3;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            v2.v[3] = color.v[0];
            v2.v[4] = color.v[1];
            v2.v[5] = color.v[2];

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
            v0.v[4] = tcoord[toff++];

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v1.v[0] = coord[off++];
            v1.v[1] = coord[off++];
            v1.v[2] = coord[off++];

            v1.v[3] = tcoord[toff++];
            v1.v[4] = tcoord[toff++];

            off = coordIndex[idx] * 3;
            toff = tcoordIndex[idx++] * 2;

            v2.v[0] = coord[off++];
            v2.v[1] = coord[off++];
            v2.v[2] = coord[off++];

            v2.v[3] = tcoord[toff++];
            v2.v[4] = tcoord[toff++];

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
            if (!m_initialized) initialize(out);
            else read(out);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(fmt("Exception while reading file:%s\n", m_path), e);
        }
    }

    /**
     *  Calculate attribute properties at (u,v, tz) point.
     * @return
     */
    public DataSource getAttributeCalculator() {
        if(m_attributeCalculator == null) {

            initialize();
            if (m_calcs.length == 1) {
                m_attributeCalculator = m_calcs[0];
            } else {
                m_attributeCalculator = new MultiAttributeCalculator(m_calcs);
            }
            return m_attributeCalculator;
        }

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
    static class MultiAttributeCalculator implements DataSource {
        DataSource[] calcs;
        MultiAttributeCalculator(DataSource[] calcs){
            this.calcs = calcs;

            for(int i=0; i < calcs.length; i++) {
                if (calcs[i] == null) {
                    throw new IllegalArgumentException("Calculator cannot be null: " + i);
                }
            }
        }

        /**
         * Returns u,v or u,v,texIndex
         *
         * @param pnt Point where the data is calculated
         * @param dataValue - storage for returned calculated data
         * @return
         */
        public int getDataValue(Vec pnt, Vec dataValue) {
            int tz = (int) (pnt.v[2] + 0.5);

            return calcs[tz].getDataValue(pnt,dataValue);
        }

        /**

           @Override
        */
        public int getChannelsCount() {
            return 3;
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

    /**
     calculates color value for point in texture coordinates
     multiple textures are indexed by 3rd coordinate
     supports multiple textures
     */
    static class SingleTextureAttributeCalculator implements DataSource {
        ImageColorMap texture;
        SingleTextureAttributeCalculator(ImageColorMap texture){
            if (texture == null) throw new IllegalArgumentException("Texture cannot be null");
            this.texture = texture;
        }

        /**
         * Returns u,v or u,v,texIndex
         *
         * @param pnt Point where the data is calculated
         * @param dataValue - storage for returned calculated data
         * @return
         */
        public int getDataValue(Vec pnt, Vec dataValue) {
            texture.getDataValue(pnt,dataValue);

            return ResultCodes.RESULT_OK;
        }

        /**

         @Override
         */
        public int getChannelsCount() {
            return 2;
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

    /**
     calculates color value for point in texture coordinates
     multiple textures are indexed by 3rd coordinate
     supports multiple textures
     */
    static class InterpolatedAttributeCalculator implements DataSource {
        InterpolatedAttributeCalculator(){
        }

        /**
         * Returns u,v or u,v,texIndex
         *
         * @param pnt Point where the data is calculated
         * @param dataValue - storage for returned calculated data
         * @return
         */
        public int getDataValue(Vec pnt, Vec dataValue) {
            dataValue.set(pnt.v[0], pnt.v[1], pnt.v[2]);
            return ResultCodes.RESULT_OK;
        }

        /**

         @Override
         */
        public int getChannelsCount() {
            return 3;
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
