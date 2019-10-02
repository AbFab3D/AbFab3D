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

package abfab3d.shapejs;

import abfab3d.core.*;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridIntIntervals;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.output.*;
import abfab3d.param.ParamMap;
import abfab3d.param.Parameterizable;
import abfab3d.param.Shape;
import abfab3d.shapejs.Scene;
import abfab3d.util.AbFab3DGlobals;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import static abfab3d.core.MaterialType.COLOR_MATERIAL;
import static abfab3d.core.Output.*;
import static abfab3d.core.Units.MM;

/**
 * CPU based ShapeJS executor.  Uses the abfab3d level ShapeJS CPU implementation
 *
 * @author Alan Hudson
 */
public abstract class BaseShapeJSExecutor implements ShapeJSExecutor {
    private static final boolean DEBUG = true;
    private HashMap params = new HashMap();

    public BaseShapeJSExecutor() {
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Configure the backend.  Can be called at anytime to change values.
     *
     * @param params
     */
    public void configure(Map params) {
        this.params.clear();
        this.params.putAll(params);
    }

    /**
     * Render a scene to a SVX suitable image stack
     *
     * @param scene The scene to render
     */
    public void renderSVX(Scene scene, OutputStream os) {

        AttributeGrid grid = createGrid(scene);
        MaterialType mtype;

        mtype = MaterialType.SINGLE_MATERIAL;
        Material mat = scene.getShapes().get(0).getMaterial();

        boolean writeTextured = (mat.getMaterialType() == COLOR_MATERIAL);

        if (writeTextured) {
            mtype = COLOR_MATERIAL;
        }

        // TODO: This will need to be updated for indexed material modes
        fillGrid(scene, mtype, grid);

        SVXWriter svxWriter = new SVXWriter();
        svxWriter.write(grid, os);
    }

    @Override
    public void renderPolyJet(Scene scene, ParamMap params, String filePath) {
        try {
            if (!new File(filePath).mkdirs())
                return;

            PolyJetWriter writer = new PolyJetWriter();

            Bounds bounds = scene.getBounds();
            Shape shape = (Shape) scene.getShapes().get(0);
            DataSource source = shape.getSource();
            Material mat = shape.getMaterial();

            writer.setBounds(bounds);
            writer.set("model", source);

            writer.set("ditheringType", 0);
            writer.set("firstSlice", params.get("firstSlice", -1));
            writer.set("slicesCount", params.get("slicesCount", -1));
            writer.set("outFolder", filePath);
            //writer.set("mapping", "color_rgba");
            writer.set("mapping", params.get("materialMapping", "material"));
            writer.set("sliceThickness", params.get("sliceThickness", PolyJetWriter.DEFAULT_SLICE_THICKNESS));

            writer.set("materials", new String[]{
                    (String) params.get("material0", PolyJetWriter.DEFAULT_MATERIAL0),
                    (String) params.get("material1", PolyJetWriter.DEFAULT_MATERIAL1),
                    (String) params.get("material2", PolyJetWriter.DEFAULT_MATERIAL2),
                    (String) params.get("material3", PolyJetWriter.DEFAULT_MATERIAL3),
                    (String) params.get("material4", PolyJetWriter.DEFAULT_MATERIAL4),
                    (String) params.get("material5", PolyJetWriter.DEFAULT_MATERIAL5),
            });
            writer.write();

        } catch (Exception e) {
            printf(fmt("exception while writing slices:%s\n", filePath));
            e.printStackTrace();
        }
    }

    public void shutdown() {
        ScriptManager.getInstance().shutdown();
    }

    public void saveModel(Scene scene, OutputStream os, String format) {
        try {
            AttributeGrid grid = createGrid(scene);
            MaterialType mtype;

            mtype = MaterialType.SINGLE_MATERIAL;
            Material mat = scene.getShapes().get(0).getMaterial();

            boolean writeTextured = (mat.getMaterialType() == COLOR_MATERIAL) &&
                    (!format.equalsIgnoreCase("stl"));

            if (writeTextured) {
                mtype = COLOR_MATERIAL;
            }

            fillGrid(scene, mtype, grid);

            float texPixelSize = 0.75f;
            float texTriGap = 1.8f; // lower then this we see pronounced triangle lines
            float texTriExt = 1.8f;

            if (DEBUG) printf(" rendering grid: %s\n", grid);


            GridSaver saver = new GridSaver();
            saver.setWriteTexturedMesh(writeTextured);
            saver.setTexPixelSize(texPixelSize);
            saver.setTexTriGap(texTriGap);
            saver.setTexTriExt(texTriExt);
            //printf("Setting min shell volume: %f CM3\n",scene.getMinShellVolume() / Units.CM3);
            saver.setMinShellVolume(scene.getMinShellVolume());
            saver.setMeshSmoothingWidth(scene.getMeshSmoothingWidth());
            saver.setMeshErrorFactor(scene.getMeshErrorFactor());
            saver.setMaxShellsCount(scene.getMaxPartsCount());

            long t0 = time();
            boolean useZip = (writeTextured);

            // Make sure the file has the proper extension
            String ext = ".stl";
            if (useZip) {
                ext = ".zip";
            }

            // Create new file with proper extension if user provided one is incorrect
            if (!("." + format).equalsIgnoreCase(ext)) {
                throw new IllegalArgumentException(fmt("Cannot save textured file into: %s", ext));
            }

            if (useZip) {
                os = new ZipOutputStream(os);
            }

            saver.write(grid, os, GridSaver.getOutputType("." + format));
            printf("file saved %d ms\n", (time() - t0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void fillGrid(Scene scene, MaterialType mtype, AttributeGrid grid) {
        if (DEBUG) printf(" filling grid: %s\n", grid);

        boolean writeTextured = (mtype == COLOR_MATERIAL);

        List<Parameterizable> sources = scene.getSource();

        // TODO: Not sure if handling multiple sources is correct to just call grid maker multiple times?
        for (Parameterizable src : sources) {
            Shape shape = (Shape) src;
            DataSource ds = shape.getSource();

            if (ds instanceof Initializable) {
                ((Initializable) ds).initialize();
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
    }

    protected AttributeGrid createGrid(Scene scene) {

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

    protected AttributeGrid createDistanceGrid(Bounds bounds) {

        if (DEBUG) printf("BaseShapeJSExecutor.createDistanceGrid(Bounds bounds)\n");
        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);

        AttributeGrid grid = null;
        if (nx * ny * nz < Integer.MAX_VALUE)
            grid = new ArrayAttributeGridByte(bounds, vs, vs);
        else
            grid = new GridShortIntervals(bounds, vs, vs);
        double maxDist = 1 * MM;

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
        double maxDist = 1 * MM;

        at.addChannel(new GridDataChannel(GridDataChannel.DISTANCE, "0_distance", bitCount, 0, -maxDist, maxDist));
        at.addChannel(new GridDataChannel(GridDataChannel.COLOR_RED, "1_red", bitCount, 24, 0., 1.));
        at.addChannel(new GridDataChannel(GridDataChannel.COLOR_GREEN, "2_green", bitCount, 16, 0., 1.));
        at.addChannel(new GridDataChannel(GridDataChannel.COLOR_BLUE, "3_blue", bitCount, 8, 0., 1.));

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
     *
     */
    private void writeIsosurface(AttributeGrid grid, double bounds[], double voxelSize, int smoothSteps, String fpath) {

        printf("writeIsosurface(%s)\n", fpath);

        IsosurfaceMaker im = new IsosurfaceMaker();

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        im.setIsovalue(0.);
        im.setBounds(MathUtil.extendBounds(bounds, -voxelSize / 2));
        im.setGridSize(nx, ny, nz);

        IsosurfaceMaker.SliceGrid fdata = new IsosurfaceMaker.SliceGrid(grid, bounds, smoothSteps);

        try {
            STLWriter stlwriter = new STLWriter(fpath);
            im.makeIsosurface(fdata, stlwriter);
            stlwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
