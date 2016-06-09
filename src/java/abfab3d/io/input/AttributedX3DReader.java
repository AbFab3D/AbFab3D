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

import abfab3d.datasources.ImageColorMap;
import abfab3d.util.*;
import org.apache.commons.io.FilenameUtils;
import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.TextureTransformMatrix;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


/**
 * Class to read collection of triangles and textures from X3D file
 *
 * @author Alan Hudson
 */
public class AttributedX3DReader implements TriangleProducer2, Transformer, DataSource {

    static final boolean DEBUG = false;

    /** Transformation to apply to positional vertices, null for none */
    private VecTransform m_transform;
    private X3DFileLoader m_fileLoader;

    /** Path to file to read from */
    private String m_path;
    private InputStream m_is;
    private String m_baseURL;

    private ImageColorMap[] textures;
    protected Bounds m_bounds = Bounds.INFINITE;
    private int m_dataDimension;

    public AttributedX3DReader(String path) {

        m_path = path;
        m_baseURL = FilenameUtils.getPath(path);
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

    /**
       reads file and passes triangles to TriangleCollector
     */
    private void read(TriangleCollector2 out) throws IOException {

        if(m_fileLoader == null){
            m_fileLoader = new X3DFileLoader(new SysErrorReporter(SysErrorReporter.PRINT_ERRORS));

            if (m_is != null) {
                m_fileLoader.load(m_baseURL,m_is);
            } else {
                m_fileLoader.loadFile(new File(m_path));
            }
        }
                
        List<CommonEncodable> shapes = m_fileLoader.getShapes();

        Iterator<CommonEncodable> itr = shapes.iterator();
        int tex = 0;

        textures = new ImageColorMap[shapes.size()];
        m_dataDimension = calcDataDimension(shapes);

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

            switch(m_dataDimension) {
                case 3:
                    addTriangles3(coord, coordIndex, out);
                    break;
                case 5:
                    addTriangles5(coord, tcoord, coordIndex, tcoordIndex, out);
                    break;
                case 6:
                    addTriangles6(coord, tcoord, tex, coordIndex, tcoordIndex, out);
                    break;
            }
            if (texNode != null) addTexture(tex,texNode);

            tex++;
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

    private void addTexture(int idx, CommonEncodable tex) {
        String[] url = (String[]) ((ArrayData)tex.getValue("url")).data;
        Boolean repeatX = (Boolean) tex.getValue("repeatX");
        Boolean repeatY = (Boolean) tex.getValue("repeatY");

        ImageColorMap icm = new ImageColorMap(m_baseURL + File.separator + url[0],1,1,1);
        if (repeatX != null) icm.setRepeatX(repeatX);
        if (repeatY != null) icm.setRepeatY(repeatY);

        icm.initialize();
        textures[idx] = icm;
    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTriangles3(float coord[],int coordIndex[], TriangleCollector2 out){
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
            out.addTri2(v0, v1, v2);
        }

    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTriangles6(float coord[],float tcoord[],int tex, int coordIndex[], int[] tcoordIndex, TriangleCollector2 out){
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
            out.addTri2(v0, v1, v2);
        }
        
    }

    /**
     * Send triangles stored as indices to TriangleCollector
     */
    private void addTriangles5(float coord[],float tcoord[], int coordIndex[], int[] tcoordIndex, TriangleCollector2 out){
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
            out.addTri2(v0, v1, v2);
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
    public boolean getTriangles2(TriangleCollector2 out) {
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
        return this;
    }


    /**
     * Returns u,v or u,v,texIndex
     *
     * @param pnt Point where the data is calculated
     * @param dataValue - storage for returned calculated data
     * @return
     */
    public int getDataValue(Vec pnt, Vec dataValue) {
        int tz = (int)pnt.v[2];
        ImageColorMap icm = textures[tz];

        icm.getDataValue(pnt,dataValue);

        return RESULT_OK;
    }

    @Override
    public int getChannelsCount() {
        return m_dataDimension - 3;
    }

    @Override
    public Bounds getBounds() {
        return m_bounds;
    }

    /**
     * Set the bounds of this data source.  For infinite bounds use Bounds.INFINITE
     * @param bounds
     */
    public void setBounds(Bounds bounds) {
        this.m_bounds = bounds.clone();
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
     * Returns how many data channels this file contains.  Must be called after getTriangles2 call.
     */
    public int getDataDimension() {
        return m_dataDimension;
    }
}

