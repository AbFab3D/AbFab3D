/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
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

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridProducer;
import abfab3d.datasources.AttributeGridSourceWrapper;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.LongParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.shapejs.MaterialType;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import static abfab3d.core.Units.*;
import static abfab3d.core.Output.printf;

/**
 * Loads 3D models
 *
 * @author Alan Hudson
 */
public class ModelLoader extends BaseParameterizable implements GridProducer {
    private static final boolean DEBUG = false;
    private static final boolean STOP_CACHING = false;

    private static final long MAX_ATRRIBUTED_SIZE = 800l * 800 * 800;

    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize","size of voxels", 0.1*MM);
    DoubleParameter mp_shellHalfThickness = new DoubleParameter("shellHalfThickness","half thickness of initial shell in voxels", 2.);
    ObjectParameter mp_source = new ObjectParameter("source","model source",null);
    DoubleParameter mp_margins = new DoubleParameter("margins","size of margins", 2*MM);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data",false);
    IntParameter mp_densityBitCount = new IntParameter("densityBitCount","Density Bit Count", 8);
    IntParameter mp_distanceBitCount = new IntParameter("distanceBitCount","Distance Bit Count", 8);
    DoubleParameter mp_maxInDistance = new DoubleParameter("maxInDistance","Maximum in distance", 2*MM);
    DoubleParameter mp_maxOutDistance = new DoubleParameter("maxOutDistance","Maximum out distance", 2*MM);
    LongParameter mp_maxGridSize = new LongParameter("maxGridSize","Max grid size", 1000l * 1000 * 1000);
    LongParameter mp_minGridSize = new LongParameter("minGridSize","Min grid size", 0);

    protected String m_path;
    protected abfab3d.shapejs.MaterialType m_materialType = MaterialType.SINGLE_MATERIAL;
    protected String m_vhash;

    static {
        if (STOP_CACHING) {
            printf("*** ModelLoader caching turned off ****\n");
            new Exception().printStackTrace();
        }
    }
    Parameter m_aparam[] = new Parameter[]{
            mp_source,
            mp_voxelSize,
            mp_margins,
            mp_attributeLoading,
            mp_densityBitCount,
            mp_distanceBitCount,
            mp_maxInDistance,
            mp_maxOutDistance,
            mp_maxGridSize,
            mp_minGridSize,
            mp_shellHalfThickness
    };


    public ModelLoader(String path) {
        initParams();

        setSource(path);
    }

    /**
       load model from given path using given voxel size and margins
       
     */
    public ModelLoader(String path, double vs, double margins) {
        initParams();

        setSource(path);
        setVoxelSize(vs);
        setMargins(margins);
    }

    /**
     * @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    public void setSource(String path) {
        if (FilenameUtils.getExtension(path).equalsIgnoreCase("zip")) {
            File src = new File(path);
            File dest = Files.createTempDir();
            dest.deleteOnExit();

            try {
                unzip(src, dest);
            } catch(IOException ioe) {
                ioe.printStackTrace();
                throw new IllegalArgumentException("Cannot unzip: " + path + " to: " + dest);
            }
            path = dest.getAbsolutePath();
        }
        mp_source.setValue(path);
        m_path = path;
        m_vhash = null;

    }

    public void setVoxelSize(double vs) {
        mp_voxelSize.setValue(vs);
        m_vhash = null;
    }

    public void setShellHalfThickness(double value) {
        mp_shellHalfThickness.setValue(value);
        m_vhash = null;
    }

    public void setMargins(double margins) {
        mp_margins.setValue(margins);
        m_vhash = null;
    }

    public void setAttributeLoading(boolean val) {
        mp_attributeLoading.setValue(val);
        m_vhash = null;

        if (val) {
            if (mp_maxGridSize.getValue() > MAX_ATRRIBUTED_SIZE) {
                mp_maxGridSize.setValue(MAX_ATRRIBUTED_SIZE);
            }
        }
    }

    public AttributedMesh getMesh() {
        ModelCacheEntry co = (ModelCacheEntry) ParamCache.getInstance().get(getValueHash());
        AttributedMesh mesh = null;

        if (co != null) {
            if (co.mesh != null) {
                m_materialType = co.materialType;
                return co.mesh;
            }
        }

        AttributedMeshReader reader = new AttributedMeshReader(m_path);
        mesh = new AttributedMesh();
        reader.getAttTriangles(mesh);

        mesh.setDataDimension(reader.getDataDimension());

        int dim = reader.getDataDimension();

        switch(dim) {
            case 3:
                m_materialType = abfab3d.shapejs.MaterialType.SINGLE_MATERIAL;
                break;
            case 5:
            case 6:
            default:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
        }

        if (!STOP_CACHING) {
            ParamCache.getInstance().put(getValueHash(), new ModelCacheEntry(null, mesh, reader,m_materialType));
        }
        return mesh;
    }

    public void setDensityBitCount(int val) {
        mp_densityBitCount.setValue(val);
        m_vhash = null;
    }

    public void setDistanceBitCount(int val) {
        mp_distanceBitCount.setValue(val);
        m_vhash = null;
    }

    public void setMaxInDistance(double val) {
        mp_maxInDistance.setValue(val);
        m_vhash = null;
    }

    public void setMaxOutDistance(double val) {
        mp_maxOutDistance.setValue(val);
        m_vhash = null;
    }

    public void setMaxGridSize(long val) {
        mp_maxGridSize.setValue(val);
        m_vhash = null;

        if (mp_attributeLoading.getValue()) {
            if (mp_maxGridSize.getValue() > MAX_ATRRIBUTED_SIZE) {
                mp_maxGridSize.setValue(MAX_ATRRIBUTED_SIZE);
            }
        }
    }

    public void setMinGridSize(long val) {
        mp_minGridSize.setValue(val);
        m_vhash = null;
    }

    public AttributeGrid getGrid() {
        ModelCacheEntry co = (ModelCacheEntry) ParamCache.getInstance().get(getValueHash());
        AttributedMesh mesh = null;
        AttributeGrid grid = null;
        AttributedMeshReader reader = null;

        if (co != null) {
            if (co.grid != null) {
                if (DEBUG) printf("Found cached grid: %s\n",getValueHash());
                m_materialType = co.materialType;
                return co.grid;
            } else if (co.mesh != null) {
                mesh = co.mesh;
                reader = co.reader;
                if (reader == null) throw new IllegalArgumentException("reader cannot be null");
            }
        } else {
            if (DEBUG) printf("No cache for grid: %s\n",getValueHash());
        }

        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(mp_maxGridSize.getValue());
        loader.setMinGridSize(mp_minGridSize.getValue());
        loader.setDensityBitCount(mp_densityBitCount.getValue());
        loader.setDistanceBitCount(mp_distanceBitCount.getValue());
        loader.setPreferredVoxelSize(mp_voxelSize.getValue());
        loader.setDensityAlgorithm(GridLoader.RASTERIZER_DISTANCE2);
        loader.setMaxInDistance(mp_maxInDistance.getValue());
        loader.setMaxOutDistance(mp_maxOutDistance.getValue());
        loader.setMargins(mp_margins.getValue());
        loader.setShellHalfThickness(mp_shellHalfThickness.getValue());
        loader.setThreadCount(0);

        int dim;

        if (DEBUG) printf("Getting grid: %s  attributed: %b\n",m_path,mp_attributeLoading.getValue());
        if (mp_attributeLoading.getValue()) {
            if (mesh != null) {
                grid = loader.rasterizeAttributedTriangles(mesh, reader.getAttributeCalculator());
                dim = reader.getDataDimension();
            } else {
                reader = new AttributedMeshReader(m_path);
                grid = loader.rasterizeAttributedTriangles(reader);
                dim = reader.getDataDimension();
            }
        } else {
            dim = 3;
            if (mesh != null) {
                grid = loader.loadDistanceGrid(mesh);
            } else {
                grid = loader.loadDistanceGrid(m_path);
            }
        }

        switch(dim) {
            case 3:
                m_materialType = abfab3d.shapejs.MaterialType.SINGLE_MATERIAL;
                break;
            case 5:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
            case 6:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
            default:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
        }

        if (DEBUG) printf("Dim: %d\n",dim);

        grid = new AttributeGridSourceWrapper(m_vhash,grid);
        if (!STOP_CACHING) {
            ParamCache.getInstance().put(getValueHash(), new ModelCacheEntry(grid, mesh, reader,m_materialType));
        }

        return grid;
    }

    public abfab3d.shapejs.MaterialType getMaterialType() {
        if (DEBUG) printf("MaterialType: %s\n",m_materialType);
        return m_materialType;
    }

    protected String getValueHash() {
        if (m_vhash != null) return m_vhash;

        m_vhash = BaseParameterizable.getParamString(getClass().getSimpleName(), m_aparam);
        return m_vhash;
    }

    /**
     * Unzip a file into a destination directory
     *
     * @param src
     * @param dest
     */
    private static void unzip(File src, File dest) throws IOException {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(src);

            for (Enumeration e = zipFile.getEntries(); e.hasMoreElements(); ) {
                ZipArchiveEntry entry = (ZipArchiveEntry) e.nextElement();
                unzipEntry(zipFile, entry, dest);
            }
        } finally {
            if (zipFile != null) zipFile.close();
        }
    }

    private static void unzipEntry(ZipFile zipFile, ZipArchiveEntry entry, File dest) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(dest, entry.getName()));
            return;
        }

        File outputFile = new File(dest, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            if (outputStream != null ) outputStream.close();
            if (inputStream != null) inputStream.close();
        }
    }

    private static void createDir(File dir) {
        if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
    }

    static class ModelCacheEntry {
        AttributeGrid grid;
        AttributedMesh mesh;
        MaterialType materialType;
        AttributedMeshReader reader;

        public ModelCacheEntry(AttributeGrid grid, AttributedMesh mesh, AttributedMeshReader reader, MaterialType materialType) {
            this.grid = grid;
            this.mesh = mesh;
            this.materialType = materialType;
            this.reader = reader;
        }
    }
}
