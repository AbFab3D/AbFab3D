/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.io.input;

import abfab3d.core.AttributeGrid;
import abfab3d.core.Bounds;
import abfab3d.core.GridProducer;
import abfab3d.core.Initializable;
import abfab3d.core.LabeledBuffer;
import abfab3d.core.MaterialType;
import abfab3d.core.VecTransform;

import abfab3d.datasources.AttributeGridSourceWrapper;
import abfab3d.datasources.DistanceToMeshDataSource;

import abfab3d.param.Parameter;
import abfab3d.param.URIParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.LongParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.ParamCache;
import abfab3d.param.CPUCache;
import abfab3d.param.ValueHash;

import abfab3d.util.URIUtils;
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
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * Loads 3D models
 *
 * @author Alan Hudson
 */
public class ModelLoader extends BaseParameterizable implements GridProducer {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_TIMING = true;

    private static final String BOUNDS_NAME = "_bounds";
    private static final String MATERIAL_TYPE_NAME = "_mtype";
    private static final String CHANNEL_COUNT_NAME = "_channels";

    private static final long MAX_ATTRIBUTED_SIZE = 800l * 800 * 800;

    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of voxels", 0.1 * MM);
    DoubleParameter mp_shellHalfThickness = new DoubleParameter("shellHalfThickness", "half thickness of initial shell in voxels", 2.);
    URIParameter mp_source = new URIParameter("source", "model source", null);
    DoubleParameter mp_margins = new DoubleParameter("margins", "size of margins", 2 * MM);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data", false);
    BooleanParameter mp_useMultiPass = new BooleanParameter("useMultiPass", "Use precise but slower distance calculation", false);
    IntParameter mp_densityBitCount = new IntParameter("densityBitCount", "Density Bit Count", 8);
    IntParameter mp_distanceBitCount = new IntParameter("distanceBitCount", "Distance Bit Count", 8);
    IntParameter mp_threadCount = new IntParameter("threadCount", "Threads count to use during loading", 0);
    DoubleParameter mp_maxInDistance = new DoubleParameter("maxInDistance", "Maximum in distance", 2 * MM);
    DoubleParameter mp_maxOutDistance = new DoubleParameter("maxOutDistance", "Maximum out distance", 2 * MM);
    LongParameter mp_maxGridSize = new LongParameter("maxGridSize", "Max grid size", 1000L * 1000 * 1000);
    LongParameter mp_minGridSize = new LongParameter("minGridSize", "Min grid size", 0);
    EnumParameter mp_distanceAlgorithm = new EnumParameter("distanceAlgorithm", "alg to be used for distance loading", sm_distAlgNames, "distance2");
    EnumParameter mp_densityAlgorithm = new EnumParameter("densityAlgorithm", "alg to be used for density loading", sm_densAlgNames, "wavelet");
    BooleanParameter mp_useCaching = new BooleanParameter("useCaching", "turn on/off data caching", true);

    protected String m_path;
    protected MaterialType m_materialType = MaterialType.SINGLE_MATERIAL;
    static String[] sm_distAlgNames = GridLoader.getDistanceAlgorithmNames();
    static String[] sm_densAlgNames = GridLoader.getDensityAlgorithmNames();
    private VecTransform m_transform = null;

    private static URIMapper uriMapper = null;
    // TODO: this will grow unbounded
    private static ConcurrentHashMap<String, URIToFileCacheEntry> uriToFileCache = new ConcurrentHashMap<String, URIToFileCacheEntry>();

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
            mp_shellHalfThickness,
            mp_useMultiPass,
            mp_threadCount,
            mp_distanceAlgorithm,
            mp_densityAlgorithm,
            mp_useCaching,
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
    protected void initParams() {
        super.addParams(m_aparam);
    }

    public static void setURIMapper(URIMapper mapper) {
        uriMapper = mapper;
    }

    public void setSource(String uri) {
        if (uri == null) throw new IllegalArgumentException("Source cannot be null");

        String path = null;

        /*  // Removed mapping to avoid exposing Shapeways loaded content
        if (uriMapper != null) {
            URIMapper.MapResult mr = uriMapper.mapURI(uri);
            uri = mr.uri;

            // TODO: What todo with sensitiveData
        }
        */

        printf("Model Loader: %s\n", uri);
        // TODO: How to deal with not wanting to cache user uploaded files(put in temp dir) versus local usage
        if (uri.startsWith("http")) {

            URIToFileCacheEntry entry = uriToFileCache.get(uri);

            if (entry != null) {
                m_path = entry.filename;
                if (DEBUG) printf("Found cached file for: %s its: %s\n", uri, uri);
                File f = new File(m_path);
                if (f.exists()) {
                    mp_source.setValue(m_path);
                    return;
                }
                if (DEBUG) printf("File gone for url: %s\n", uri);
            }

            if (DEBUG) printf("Downloading url: %s\n", uri);
            try {
                path = URIUtils.downloadURI("ModelLoader", uri);
                uriToFileCache.put(uri, new URIToFileCacheEntry(uri, path));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (URISyntaxException use) {
                use.printStackTrace();
            }
        } else {
            path = uri;
        }

        if (path == null) throw new IllegalArgumentException("Cannot resolve source: " + uri);

        if (FilenameUtils.getExtension(path).equalsIgnoreCase("zip")) {
            URIToFileCacheEntry entry = uriToFileCache.get(path);

            if (entry != null) {
                m_path = entry.filename;
                if (DEBUG) printf("Found cached file for: %s its: %s\n", uri, uri);
                File f = new File(m_path);
                if (f.exists()) {
                    mp_source.setValue(m_path);
                    return;
                }
                if (DEBUG) printf("File gone for url: %s\n", uri);
            }

            File src = new File(path);
            File dest = Files.createTempDir();
            dest.deleteOnExit();

            try {
                unzip(src, dest);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new IllegalArgumentException("Cannot unzip: " + path + " to: " + dest);
            }
            path = dest.getAbsolutePath();
            uriToFileCache.put(uri, new URIToFileCacheEntry(uri, path));
            if (DEBUG) printf("Caching uri: %s to: %s\n", uri, path);
        }
        mp_source.setValue(path);
        m_path = path;
    }

    public void setUseCaching(boolean value) {
        mp_useCaching.setValue(value);
    }

    public void setVoxelSize(double vs) {
        mp_voxelSize.setValue(vs);
    }

    public void setShellHalfThickness(double value) {
        mp_shellHalfThickness.setValue(value);
    }

    public void setMargins(double margins) {
        mp_margins.setValue(margins);
    }

    public void setAttributeLoading(boolean val) {
        mp_attributeLoading.setValue(val);

        if (val) {
            if (mp_maxGridSize.getValue() > MAX_ATTRIBUTED_SIZE) {
                mp_maxGridSize.setValue(MAX_ATTRIBUTED_SIZE);
            }
        }
    }

    public void setDensityBitCount(int val) {
        mp_densityBitCount.setValue(val);
    }

    public void setDistanceBitCount(int val) {
        mp_distanceBitCount.setValue(val);
    }

    public void setMaxInDistance(double val) {
        mp_maxInDistance.setValue(val);
    }

    public void setUseMultiPass(boolean value) {
        mp_useMultiPass.setValue(value);
    }

    public void setMaxOutDistance(double val) {
        mp_maxOutDistance.setValue(val);
    }

    public void setMaxGridSize(long val) {
        mp_maxGridSize.setValue(val);

        if (mp_attributeLoading.getValue()) {
            if (mp_maxGridSize.getValue() > MAX_ATTRIBUTED_SIZE) {
                mp_maxGridSize.setValue(MAX_ATTRIBUTED_SIZE);
            }
        }
    }

    public void setMinGridSize(long val) {
        mp_minGridSize.setValue(val);
    }

    public void reportCachingStatus() {

        printf("*** ModelLoader caching :%s  ****\n", mp_useCaching.getValue());
        new Exception().printStackTrace();

    }


    /**
     @return loaded mesh for this model
     */
    public AttributedMesh getMesh() {
        String vhash = getValueHash();
        ModelCacheEntry co = (ModelCacheEntry) ParamCache.getInstance().get(vhash);
        AttributedMesh mesh = null;
        if (DEBUG) printf("ML.getMesh() Checking cache: %s\n", vhash);

        if (co != null) {
            if (co.mesh != null) {
                m_materialType = co.materialType;
                if (DEBUG) printf("Found mesh cached, returning\n");
                return co.mesh;
            }
        }

        AttributedMeshReader reader = new AttributedMeshReader(m_path);
        if (m_transform instanceof Initializable) ((Initializable) m_transform).initialize();
        reader.setTransform(m_transform);
        reader.initialize();
        mesh = new AttributedMesh();
        reader.getAttTriangles(mesh);

        mesh.setDataDimension(reader.getDataDimension());
        m_materialType = makeMaterialType(reader.getDataDimension());
        if (m_materialType != MaterialType.SINGLE_MATERIAL) {
            mesh.setAttributeCalculator(reader.getAttributeCalculator());
        }

        if (mp_useCaching.getValue()) {
            ParamCache.getInstance().put(getValueHash(), new ModelCacheEntry(null, mesh, reader, m_materialType));
        }
        return mesh;
    }

    /**
     return grid for loaded model
     */
    public AttributeGrid getGrid() {
        long t0 = System.currentTimeMillis();
        AttributedMesh mesh = null;

        AttributeGrid grid = null;
        try {
            AttributedMeshReader reader = null;
            if (mp_useCaching.getValue()) {
                String vhash = getValueHash();
                if (DEBUG) printf("ML.getGrid() Checking cache: %s\n", vhash);

                ModelCacheEntry co = (ModelCacheEntry) ParamCache.getInstance().get(vhash);
                if (co != null) {
                    if (DEBUG) printf("Found a cache entry: %s\n", vhash);
                    if (co.grid != null) {
                        if (DEBUG) printf("Found cached grid: %s\n", getValueHash());
                        m_materialType = co.materialType;
                        return co.grid;
                    } else if (co.mesh != null) {
                        mesh = co.mesh;
                        reader = co.reader;
                        m_materialType = co.materialType;
                        printf("Found a mesh cached2, returning\n");
                        if (reader == null) throw new IllegalArgumentException("reader cannot be null");
                    }
                } else {
                    if (DEBUG) printf("No cache for grid: %s\n", getValueHash());
                }
            }

            GridLoader loader = new GridLoader();
            loader.setMaxGridSize(mp_maxGridSize.getValue());
            loader.setMinGridSize(mp_minGridSize.getValue());
            loader.setDensityBitCount(mp_densityBitCount.getValue());
            loader.setDistanceBitCount(mp_distanceBitCount.getValue());
            loader.setPreferredVoxelSize(mp_voxelSize.getValue());
            loader.setDistanceAlgorithm(mp_distanceAlgorithm.getValue());
            loader.setDensityAlgorithm(mp_densityAlgorithm.getValue());
            loader.setMaxInDistance(mp_maxInDistance.getValue());
            loader.setMaxOutDistance(mp_maxOutDistance.getValue());
            loader.setMargins(mp_margins.getValue());
            loader.setShellHalfThickness(mp_shellHalfThickness.getValue());
            loader.setThreadCount(mp_threadCount.getValue());
            loader.setUseMultiPass(mp_useMultiPass.getValue());

            int dim;

            if (DEBUG) printf("Getting grid: %s  attributed: %b\n", m_path, mp_attributeLoading.getValue());
            if (mp_attributeLoading.getValue()) {
                // textured or colored mesh loading
                if (mesh != null) {
                    grid = loader.rasterizeAttributedTriangles(mesh, reader.getAttributeCalculator());
                    dim = reader.getDataDimension();
                } else {
                    reader = new AttributedMeshReader(m_path);
                    reader.setTransform(m_transform);
                    grid = loader.rasterizeAttributedTriangles(reader);
                    dim = reader.getDataDimension();
                }
            } else {
                // plain mesh loading
                dim = 3;
                if (mesh != null) {
                    grid = loader.loadDistanceGrid(mesh);
                } else {
                    if (m_transform != null) {
                        grid = loader.loadDistanceGrid(m_path, m_transform);
                    } else {
                        grid = loader.loadDistanceGrid(m_path);
                    }
                }
            }

            m_materialType = makeMaterialType(dim);
            String baseVhash = getValueHash();            
            grid = new AttributeGridSourceWrapper(baseVhash,grid);
            if (mp_useCaching.getValue()) {

                printf("Caching file: %s\n",baseVhash);
                ParamCache.getInstance().put(getValueHash(), new ModelCacheEntry(grid, mesh, reader, m_materialType));

                String bhash = baseVhash + BOUNDS_NAME;
                String mhash = baseVhash + MATERIAL_TYPE_NAME;
                String cchash = baseVhash + CHANNEL_COUNT_NAME;

                LabeledBuffer<double[]> boundsBuffer = new LabeledBuffer<double[]>(bhash, grid.getGridBounds().getArray());
                CPUCache.getInstance().put(boundsBuffer);
                LabeledBuffer<byte[]> materialTypeBuffer = new LabeledBuffer<byte[]>(mhash, m_materialType.toString().getBytes());
                CPUCache.getInstance().put(materialTypeBuffer);
                LabeledBuffer<byte[]> channelCountBuffer = new LabeledBuffer<byte[]>(cchash, new byte[] {(byte)grid.getDataDesc().size()});
                CPUCache.getInstance().put(channelCountBuffer);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // HACK: Delete cached files in this case
            // hmm, cant get at ShapeJSGlobal

            String prefix = "url";  // Well known name shared with ShapeJSGlobal
            if (m_path.contains(prefix)) {
                // Bad apple, remove it from disk
                printf("Invalid file, removing from disk, HACK: %s\n", m_path);
                if (!new File(m_path).delete()) {
                    printf("Could not delete bad file: %s\n", m_path);
                }

            } else {
                printf("Leaving bad file alone: %s\n", m_path);
            }
            throw new IllegalArgumentException("Model file invalid: " + m_path);

        }

        return grid;
    }

    /**
     returns loaded model represented as DistanceToMeshDataSource
     */
    public DistanceToMeshDataSource getDistanceToMeshDataSource() {

        if (mp_attributeLoading.getValue()) {
            AttributedMesh mesh = getMesh();
            return new DistanceToMeshDataSource(mesh, mesh.getAttributeCalculator());
        } else {
            return new DistanceToMeshDataSource(getMesh());
        }

    }

    public Bounds getGridBounds() {
        String vhash = getValueHash();
        vhash += BOUNDS_NAME;
        CPUCache cache = CPUCache.getInstance();

        LabeledBuffer<double[]> boundsBuffer = cache.get(vhash);
        if (boundsBuffer == null) {
            if (DEBUG) printf("***Failed to get getGridBounds via cache: %s\n",vhash);
            // oh well, load the damn thing
            AttributeGrid grid = getGrid();
            Bounds ret_val = grid.getGridBounds();
            return ret_val;
        } else {
            Bounds bnds = new Bounds(boundsBuffer.getBuffer(),mp_voxelSize.getValue());
            if (DEBUG) printf("***Got getGridBounds via cache: %s\n", bnds);
            return bnds;
        }
    }

    public int getChannelCount() {
        String vhash = getValueHash();
        vhash += CHANNEL_COUNT_NAME;
        CPUCache cache = CPUCache.getInstance();

        LabeledBuffer<byte[]> channelCountBuffer = cache.get(vhash);

        if (channelCountBuffer == null) {
            if (DEBUG) printf("***Failed to get channelCountType via cache: %s\n",vhash);
            // oh well, load the damn thing
            AttributeGrid grid = getGrid();
            return grid.getDataDesc().size();
        } else {
            return channelCountBuffer.getBuffer()[0];
        }
    }
    
    public MaterialType getMaterialType() {
        String vhash = getValueHash();
        vhash += MATERIAL_TYPE_NAME;
        CPUCache cache = CPUCache.getInstance();

        LabeledBuffer<byte[]> materialTypeBuffer = cache.get(vhash);

        if (materialTypeBuffer == null) {
            if (DEBUG) printf("***Failed to get materialType via cache: %s\n",vhash);
            // oh well, load the damn thing
            AttributeGrid grid = getGrid();
            return m_materialType;
        } else {
            String mt = new String(materialTypeBuffer.getBuffer());
            if (DEBUG) printf("***Got materialType via cache: %s\n", mt);
            return MaterialType.valueOf(mt);
        }
    }

    protected String getValueHash() {

        // TODO: transform should likely be a parameter but need to think through
        // TODO: we need to get a value hash of trans not its default memory address
        String trans = null;
        if (m_transform instanceof ValueHash) {
            trans = ((ValueHash) m_transform).getParamString();
        } else {
            if (m_transform == null) {
                trans = "null";
            } else {
                trans = m_transform.toString();
            }
        }

        return BaseParameterizable.getParamString(getClass().getSimpleName(), m_aparam, m_transform == null ? "trans=null" : "trans=" + trans);
    }


    public void setTransform(VecTransform trans) {
        if (DEBUG) printf("Got a setTransform: %s\n", trans);
        if (trans instanceof Initializable) ((Initializable) trans).initialize();
        m_transform = trans;
    }

    private static MaterialType makeMaterialType(int dataDimension) {

        switch (dataDimension) {
            case 3:
                return MaterialType.SINGLE_MATERIAL;
            case 5:
            case 6:
            default:
                return MaterialType.COLOR_MATERIAL;
        }
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
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
        }
    }

    private static void createDir(File dir) {
        if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
    }

    /**
       wrapper to save data to ParamCache 
     */
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

    static class URIToFileCacheEntry {
        public String uri;
        public String filename;

        public URIToFileCacheEntry(String uri, String filename) {
            this.uri = uri;
            this.filename = filename;
        }
    }

    /**
     * Implement this as a value
     * @return
     */
    public String getParamString() {
        return BaseParameterizable.getParamString("ModelLoader", getParams());
    }

    public void getParamString(StringBuilder sb) {
        sb.append(BaseParameterizable.getParamString("ModelLoader", getParams()));
    }

    public int initialize() {
        return 0;
    }
}
