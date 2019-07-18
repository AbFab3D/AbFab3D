/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package shapejs;

import abfab3d.core.*;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridIntIntervals;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.op.GridMaker;
import abfab3d.grid.op.ImageMaker;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.param.Parameterizable;
import abfab3d.param.Shape;
import abfab3d.shapejs.*;
import abfab3d.util.AbFab3DGlobals;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import static abfab3d.core.MaterialType.COLOR_MATERIAL;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * CPU based ShapeJS command backend.  Uses the abfab3d level ShapeJS implementation
 *
 * @author Alan Hudson
 */
public abstract class BaseCommandBackend implements CommandBackend {
    private static final boolean DEBUG = true;

    private List<String> libDirs = new ArrayList<>();

    public BaseCommandBackend() {
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, Runtime.getRuntime().availableProcessors());
    }

    public void setLibDirs(List<String> libs) {
        libDirs.clear();
        libDirs.addAll(libs);
    }

    /**
     * Load the content from the params.  Content can either be Project/Variant based or just a script.
     * If both are provided it uses the Project/Variant
     * @param params
     */
    public Scene loadContent(ParamContainer params) throws IOException {
        String project = params.getProject();
        String variant = params.getVariant();
        String script = params.getScript();

        if (project == null && variant == null) {
            if (script == null) throw new IllegalArgumentException("No project or script content provided");

            return loadScript(script);
        }

        // TODO: We don't actually need the project path to execute a variant...
        if (project == null || variant == null) {
            throw new IllegalArgumentException("No content specified, project and variant or script required");
        }

        /*
        Project proj = Project.load(project,libDirs);
        List<VariantItem> variants = proj.getVariants();
        VariantItem currVariantItem =  null;
        for(VariantItem vi : variants) {
            if (vi.getPath().equals(variant)) {
                currVariantItem = vi;
                break;
            }
        }

        if (currVariantItem == null) throw new IllegalArgumentException(fmt("Cannot find variant: %s",variant));
        */

        Variant currVariant = new Variant();
        try {
            currVariant.readDesign(libDirs, variant, true);

            Scene scene = currVariant.getScene();

            return scene;
        } catch(NotCachedException nce) {
            nce.printStackTrace();
        } catch(InvalidScriptException ise) {
            ise.printStackTrace();
        }

        throw new IllegalArgumentException("Invalid content");
    }

    public void saveModel(Scene scene, File file) {
        String filePath = file.getAbsolutePath();

        try {
            AttributeGrid grid = createGrid(scene);
            float texPixelSize = 0.75f;
            float texTriGap = 1.8f; // lower then this we see pronounced triangle lines
            float texTriExt = 1.8f;

            if (DEBUG) printf(" rendering grid: %s\n", grid);

            Material mat = scene.getShapes().get(0).getMaterial();


            boolean writeTextured = (mat.getMaterialType() == COLOR_MATERIAL) &&
                    (!FilenameUtils.getExtension(filePath).equalsIgnoreCase("stl"));

/*
            if (writeTextured) {
                CLGridMakerDensBGR maker = new CLGridMakerDensBGR(m_env);
                maker.setSource(scene.getSource());
                maker.renderGrid(grid);
            } else {
                CLGridMaker maker = new CLGridMaker(m_env);
                maker.setSource(scene.getSource());
                maker.renderDensityGrid(grid);
            }
*/
            List<Parameterizable> sources = scene.getSource();

            // TODO: Not sure if handling multiple sources is correct to just call grid maker multiple times?
            for(Parameterizable src : sources) {
                Shape shape = (Shape) src;
                DataSource ds = shape.getSource();

                if (ds instanceof Initializable) {
                    ((Initializable)ds).initialize();
                }

                if (writeTextured) {
                    // TODO: Suspect we need to specify custom packers here
                    GridMaker maker = new GridMaker();
                    maker.setSource(shape.getSource());
                    maker.execute(grid);
                } else {
                    GridMaker maker = new GridMaker();
                    maker.setSource(shape.getSource());
                    maker.execute(grid);
                }
            }

            GridSaver saver = new GridSaver();
            saver.setWriteTexturedMesh(writeTextured);
            saver.setTexPixelSize(texPixelSize);
            saver.setTexTriGap(texTriGap);
            saver.setTexTriExt(texTriExt);
            printf("Setting min shell volume: %f CM3\n",scene.getMinShellVolume() / Units.CM3);
            saver.setMinShellVolume(scene.getMinShellVolume());
            saver.setMeshSmoothingWidth(scene.getMeshSmoothingWidth());
            saver.setMeshErrorFactor(scene.getMeshErrorFactor());
            saver.setMaxShellsCount(scene.getMaxPartsCount());

            long t0 = time();
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            OutputStream os = null;
            boolean useZip = (writeTextured);

            // Make sure the file has the proper extension
            String ext = ".stl";
            if (useZip) {
                ext = ".zip";
            }

            File targetFile = file;

            // Create new file with proper extension if user provided one is incorrect
            if (!file.getName().endsWith(ext)) {
                targetFile = new File(file.getAbsolutePath() + ext);
                filePath = targetFile.getAbsolutePath();
            }

            try {
                fos = new FileOutputStream(filePath);
                bos = new BufferedOutputStream(fos);
                if (useZip) {
                    os = new ZipOutputStream(bos);
                } else {
                    os = bos;
                }

                //WingedEdgeTriangleMesh mesh = saver.writeAsMesh(grid, filePath);
                saver.write(grid, os, GridSaver.getOutputType("." + FilenameUtils.getExtension(filePath)));
                printf(" %s file saved %d ms\n", file.getCanonicalPath(), (time() - t0));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(fos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Scene loadScript(String scriptPath) throws IOException {
        String baseDir = null;

        ScriptManager sm = ScriptManager.getInstance();

        String jobID = UUID.randomUUID().toString();
        HashMap<String, Object> sparams = new HashMap<String, Object>();

        String script = IOUtils.toString(new FileInputStream(scriptPath));

        ScriptResources sr = sm.prepareScript(jobID, baseDir, script, sparams, false);

        sm.executeScript(sr);

        Scene scene = (Scene) sr.evaluatedScript.getResult();

        return scene;
    }

    public AttributeGrid createGrid(Scene scene) {

        Bounds bounds = scene.getBounds();

        Material mat = scene.getShapes().get(0).getMaterial();

        switch (mat.getMaterialType()) {

            default:
            case SINGLE_MATERIAL:
                //return createDensityGrid(bounds);
                return createDistanceGrid(bounds);

            case COLOR_MATERIAL:
                //return createDBGRGrid(bounds);
                return createDistBGRGrid(bounds);

        }
    }

    public AttributeGrid createDistanceGrid(Bounds bounds) {

        if(DEBUG)printf("BaseCommandBackend.createDistanceGrid(Bounds bounds)\n");
        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);

        AttributeGrid grid = null;
        if (nx * ny * nz < Integer.MAX_VALUE)
            grid = new ArrayAttributeGridByte(bounds, vs, vs);
        else
            grid = new GridShortIntervals(bounds, vs, vs);
        double maxDist = 1*MM;

        grid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DISTANCE, "dist", 8, 0, -maxDist, maxDist)));

        return grid;
    }

    public AttributeGrid createDensityGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);

        AttributeGrid grid = null;
        if (nx * ny * nz < Integer.MAX_VALUE)
            grid = new ArrayAttributeGridByte(bounds, vs, vs);
        else
            grid = new GridShortIntervals(bounds, vs, vs);

        grid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DENSITY, "dens", 8, 0, 0.0, 1.0)));

        return grid;
    }

    public AttributeGrid createDistBGRGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        if (nx * ny * nz * 4 < Integer.MAX_VALUE)
            grid = new ArrayAttributeGridInt(bounds, vs, vs);
        else
            grid = new GridIntIntervals(bounds, vs, vs);

        // make data description for the grid 
        GridDataDesc at = new GridDataDesc();

        int bitCount = 8;
        double maxDist = 1*MM;

        at.addChannel(new GridDataChannel(GridDataChannel.DISTANCE,    "0_distance", bitCount,  0,  -maxDist, maxDist));
        at.addChannel(new GridDataChannel(GridDataChannel.COLOR_RED,   "1_red",     bitCount,  24, 0., 1.));
        at.addChannel(new GridDataChannel(GridDataChannel.COLOR_GREEN, "2_green",   bitCount,  16, 0., 1.));
        at.addChannel(new GridDataChannel(GridDataChannel.COLOR_BLUE,  "3_blue",    bitCount,   8, 0., 1.));
        
        return grid;
    }


    public AttributeGrid createDBGRGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        if (nx * ny * nz * 4 < Integer.MAX_VALUE)
            grid = new ArrayAttributeGridInt(bounds, vs, vs);
        else
            grid = new GridIntIntervals(bounds, vs, vs);

        grid.setDataDesc(GridDataDesc.getDensBGR());

        return grid;
    }

    /**

     */
    private void writeIsosurface(AttributeGrid grid, double bounds[], double voxelSize, int smoothSteps, String fpath){

        printf("writeIsosurface(%s)\n",fpath);

        IsosurfaceMaker im = new IsosurfaceMaker();

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        im.setIsovalue(0.);
        im.setBounds(MathUtil.extendBounds(bounds, -voxelSize/2));
        im.setGridSize(nx, ny, nz);

        IsosurfaceMaker.SliceGrid fdata = new IsosurfaceMaker.SliceGrid(grid, bounds, smoothSteps);

        try {
            STLWriter stlwriter = new STLWriter(fpath);
            im.makeIsosurface(fdata, stlwriter);
            stlwriter.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
