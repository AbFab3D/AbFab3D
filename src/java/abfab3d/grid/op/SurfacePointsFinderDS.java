/*
 *                        Shapeways, Inc Copyright (c) 2012-2018
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

import java.util.Stack;


import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Vector;

import abfab3d.core.VecTransform;
import abfab3d.core.DataSource;
import abfab3d.core.Bounds;
import abfab3d.core.Vec;
import abfab3d.core.Units;
import abfab3d.core.Initializable;
import abfab3d.core.Output;

import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;

import abfab3d.util.AbFab3DGlobals;
import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;

import abfab3d.transforms.Identity;

import static abfab3d.core.Output.time;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.initialize;
import static java.lang.Math.*;

/**
   class calculates points on the isosurface of a DataSource 

   it returns PointSet of points the isosurface 

   @author Vladimir Bulatov
   
 */
public class SurfacePointsFinderDS extends BaseParameterizable {

    static final boolean DEBUG = true;

    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize",0.1*MM);
    IntParameter mp_blockSize = new IntParameter("blockSize",20);
    IntParameter mp_blockOverlap = new IntParameter("blockOverlap",1);
    IntParameter mp_threads = new IntParameter("threads",0);

    Parameter m_aparam[] = new Parameter[]{
        mp_voxelSize,
        mp_blockSize, 
        mp_blockOverlap, 
        mp_threads, 
    };

    public SurfacePointsFinderDS(){
        super.addParams(m_aparam);
    }


    private int getThreadCount() {
        int threads = mp_threads.getValue();
        if (threads == 0) {
            threads = Runtime.getRuntime().availableProcessors();
        }
        int ret_val = (int) Math.min(threads, ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue());
        return ret_val;
        //return threads;
    }

    public PointSet findSurfacePoints(DataSource source, Bounds bounds){
       
        int threads = getThreadCount();
        if(threads > 1) {
            return findSurfacePointsMultiBlocksMT(source, bounds, threads);
        }  else {
            return findSurfacePointsMultiBlocks(source, bounds);
            //return findSurfacePointsSingleBlock(source, bounds);
       }

    }

    public PointSet findSurfacePointsMultiBlocks(DataSource source, Bounds bounds){

        initialize(source);

        long t0 = time();

        if(DEBUG)printf("findSurfacePointsMultiBlocks()\n");
        Vector<GridBlock> blocks = GridBlock.makeBlocks(bounds, mp_voxelSize.getValue(), mp_blockSize.getValue(), mp_blockOverlap.getValue());
        if(DEBUG)printf("block size: %d\n", mp_blockSize.getValue());
        if(DEBUG)printf("block count: %d\n", blocks.size());

        if(DEBUG) printf("initialization time: %d ms\n", (time() - t0));

        
        BlockProcessor bp = new BlockProcessor(source, mp_voxelSize.getValue());
        
        PointSetArray points = new PointSetArray(100);
        Bounds blockBounds = new Bounds();
        t0 = time();
        for (int i = 0; i < blocks.size(); i++) {
            GridBlock block = blocks.get(i);
            block.getBounds(blockBounds);
            PointSetArray pnts = bp.findSurfacePoints(blockBounds);
            //PointSetArray pnts = null;
            if (false) printf("block[%3d %3d %3d %3d %3d %3d] count: %d\n", 
                             block.xmin, block.xmax,block.ymin, block.ymax, block.zmin, block.zmax,pnts.size());
            if(pnts != null) points.addPoints(pnts);
        }

        if(DEBUG) printf("processedBlocks: %d (%5.1f%%)\n", processedBlockCount, (100.*processedBlockCount/blocks.size()));
        if(DEBUG) printf("surface extraction time: %d ms\n", (time() - t0));
        
        return points;

        // return findSurfacePointsSingleBlock(source, bounds);
        
    }

    public PointSet findSurfacePointsMultiBlocksMT(DataSource source, Bounds bounds, int threadCount){
        
        initialize(source);
        PointSetArray points = new PointSetArray(1);
        PointsCollector pointsCollector = new PointsCollector(points);
        Vector<GridBlock> blocks = GridBlock.makeBlocks(bounds, mp_voxelSize.getValue(), mp_blockSize.getValue(), mp_blockOverlap.getValue());

        if (DEBUG) printf("SkeletonMaker2.findSurfacePointsMutiBlocksMT(threadCount:%d)\n", threadCount);
        if (DEBUG) printf("                       blocks count: %d\n", blocks.size());
        
        
        BlockManager blockManager = new BlockManager(blocks);
        BlockProcessor threads[] = new BlockProcessor[threadCount];

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {

            BlockProcessor bp = bp = new BlockProcessor(source, mp_voxelSize.getValue());
            bp.setBlockManager(blockManager);
            bp.setPointsCollector(pointsCollector);

            threads[i] = bp;

            executor.submit(threads[i]);
        }

        executor.shutdown();
        long maxTime = 5; 
        try {
            executor.awaitTermination(maxTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long t0 = time();
        for (int i = 0; i < threadCount; i++) {
            points.addPoints(threads[i].allPoints);
        }
        if (DEBUG) printf("points copy time: %d ms\n", (time() - t0));        
        if (DEBUG) printf("SurfacePointsFinderDS.findSurfacePointsMutiBlocksMT() points count: %d\n", points.size());

        return points;
}

    public PointSet findSurfacePointsSingleBlock(DataSource source, Bounds bounds){
        
        initialize(source);

        double vs = mp_voxelSize.getValue();

        int nn[] = bounds.getGridSize(vs);
        int nx = nn[0];
        int ny = nn[1];
        int nz = nn[2];
        int nx1 = nx-1;
        int ny1 = ny-1;
        int nz1 = nz-1;
        if(DEBUG)printf("findSurfacePointsSingleBlock, grid:%d x %d x %d\n",nx,ny,nz);
        int nxz = nx*nz;
        double values[] = new double[nx*ny*nz];
        
        Vec pnt = new Vec(3);
        Vec pntValue = new Vec(4);
        int dataIndex = 0;
        double ymin = bounds.ymin + vs/2;
        double xmin = bounds.xmin + vs/2;
        double zmin = bounds.zmin + vs/2;

        long t0 = time();

        for(int iy = 0; iy < ny; iy++){
            double y = ymin + iy*vs;
            
            for(int ix = 0; ix < nx; ix++){
                
                double x = xmin + ix*vs;
                
                int iz0 = ix*nz + iy*nxz;
                
                for(int iz = 0; iz < nz; iz++){
                    
                    double z = zmin + iz*vs;

                    pnt.set(x,y,z);
                    source.getDataValue(pnt, pntValue);
                    values[iz0 + iz] = pntValue.v[dataIndex];
                    //printf("%5.1f ", values[iz0 + iz]/MM);
                }
                //printf("\n");
            }            
            //printf("---------------\n");
        }
        printf("data calculation: %d ms\n", (time()-t0));

        PointSetArray points = new PointSetArray();
        t0 = time();
        for(int iy = 0; iy < ny1; iy++){
            double y = ymin + iy*vs;
            
            for(int ix = 0; ix < nx1; ix++){
                
                double x = xmin + ix*vs;
                
                int iz0 = ix*nz + iy*nxz;
                
                for(int iz = 0; iz < nz1; iz++){
                    
                    double z = zmin + iz*vs;
                    int ind = iz0 + iz;
                    double v0 = values[ind];
                    double vx = values[ind + nz];
                    double vy = values[ind + nxz];
                    double vz = values[ind+1];
                    if(v0 * vx <= 0.) {
                        points.addPoint(x + vs*coeff(v0,vx), y, z);
                    }
                    if(v0 * vy <= 0.) {
                        points.addPoint(x, y + vs*coeff(v0,vy), z);
                    }
                    if(v0 * vz <= 0.) {
                        points.addPoint(x, y, z + vs*coeff(v0,vz));
                    }
                }
            }
        }
        printf("surface search: %d ms\n", (time()-t0));
        return points;
    }

    /**
       return zero of linear function on interval (0,1) which has values v0 and v1 at the end of interval 

       f(x) = a*x + b;
       f(0) = v0
       f(1) = v1       
     */
    static final double coeff(double v0, double v1){
        
        if(v0 != v1) 
            return v0 /(v0-v1);
        else 
            return 0.5;
        
    }

    int processedBlockCount  = 0;



    /**

       processor of single block 
       
     */
    class BlockProcessor implements Runnable{

        DataSource source;
        double vs;
        double maxGradient = 1;
        double values[] = new double[10*10*10];
        Vec pnt = new Vec(3);
        Vec pntValue = new Vec(4);
        PointSetArray points = new PointSetArray();
        int dataIndex = 0;

        BlockManager blockManager;
        PointsCollector pointsCollector;
        PointSetArray allPoints = new PointSetArray(1);

        BlockProcessor(DataSource source, double voxelSize){
            this.source = source;
            this.vs = voxelSize;
        }

        void setBlockManager(BlockManager blockManager){
            this.blockManager = blockManager;
        }

        void setPointsCollector(PointsCollector pointsCollector){
            this.pointsCollector = pointsCollector;
        }

        /**
           find surface ponts in a block 
         */
        PointSetArray findSurfacePoints(Bounds bounds){

            
            int nx = bounds.getGridWidth(vs);
            int ny = bounds.getGridHeight(vs);
            int nz = bounds.getGridDepth(vs);
            int nx1 = nx-1;
            int ny1 = ny-1;
            int nz1 = nz-1;
            int nxz = nx*nz;
            int dataSize = nx*ny*nz;
            if(dataSize > values.length)
                values = new double[dataSize];
            
            double vs2 = vs/2;

            double xmin = bounds.xmin + vs2;
            double ymin = bounds.ymin + vs2;
            double zmin = bounds.zmin + vs2;

            double xmax = bounds.xmax -vs2;
            double ymax = bounds.ymax -vs2;
            double zmax = bounds.zmax -vs2;
            if(!canContainSurface(xmin,xmax, ymin, ymax, zmin, zmax)){
                return null;
            }

            points.clear();
            processedBlockCount++;
            if(false)printf("BlockProcessor.findSurfacePoints(),[%s]\n",bounds.toString("%4.1f",MM));
            
            for(int iy = 0; iy < ny; iy++){
                double y = ymin + iy*vs;
                
                for(int ix = 0; ix < nx; ix++){
                    
                    double x = xmin + ix*vs;
                    
                    int iz0 = ix*nz + iy*nxz;
                    
                    for(int iz = 0; iz < nz; iz++){
                        
                        double z = zmin + iz*vs;

                        values[iz0 + iz] = getValue(x,y,z);
                        //printf("%5.1f ", values[iz0 + iz]/MM);
                    }
                    //printf("\n");
                }            
                //printf("---------------\n");
            }
            
            for(int iy = 0; iy < ny1; iy++){
                double y = ymin + iy*vs;
                
                for(int ix = 0; ix < nx1; ix++){
                    
                    double x = xmin + ix*vs;
                    
                    int iz0 = ix*nz + iy*nxz;
                    
                    for(int iz = 0; iz < nz1; iz++){
                        
                        double z = zmin + iz*vs;
                        int ind = iz0 + iz;
                        double v0 = values[ind];
                        double vx = values[ind + nz];
                        double vy = values[ind + nxz];
                        double vz = values[ind + 1] ;
                        if(v0 * vx <= 0.) {
                            points.addPoint(x + vs*coeff(v0,vx), y, z);
                        }
                        if(v0 * vy <= 0.) {
                            points.addPoint(x, y + vs*coeff(v0,vy), z);
                        }
                        if(v0 * vz <= 0.) {
                            points.addPoint(x, y, z + vs*coeff(v0,vz));
                        }
                    }
                }
            }
            return points;            
        } // findSurfacePoints
        
        /**
           return true if source CAN have surface inside of block with coordinates 
           we assume the data source has limited maximal gradient
           
         */
        boolean canContainSurface(double xmin, double xmax, double ymin, double ymax,double zmin, double zmax){
            
            double v000,v010,v011,v001,v100,v110,v111,v101;
            v000 = getValue(xmin, ymin, zmin);
            v100 = getValue(xmax, ymin, zmin);
            v110 = getValue(xmax, ymax, zmin);
            v010 = getValue(xmin, ymax, zmin);
            v001 = getValue(xmin, ymin, zmax);
            v101 = getValue(xmax, ymin, zmax);
            v111 = getValue(xmax, ymax, zmax);
            v011 = getValue(xmin, ymax, zmax);
            int s = 0;
            if(v000 < 0.) s++;
            if(v010 < 0.) s++;
            if(v011 < 0.) s++;
            if(v001 < 0.) s++;
            if(v100 < 0.) s++;
            if(v110 < 0.) s++;
            if(v111 < 0.) s++;
            if(v101 < 0.) s++;
            if(s != 0 && s != 8) return true;
            double size = maxGradient*max(max(xmax-xmin, ymax-ymin),zmax-zmin);
            if(
               (abs(v000) < size) || (abs(v010) < size) || (abs(v001) < size) || (abs(v011) < size) || 
               (abs(v100) < size) || (abs(v110) < size) || (abs(v101) < size) || (abs(v111) < size) 
               ){ 
                return true;
            }

            return false;
            
        }

        final double getValue(double x,double y, double z){

            pnt.set(x,y, z); 
            source.getDataValue(pnt, pntValue); 
            return pntValue.v[dataIndex];
        }
                
        /**
           MT runner 
         */
        public void run() {

            if(blockManager == null) {
                throw new RuntimeException("blockManager can't be null");
            }
            if(pointsCollector == null){
                throw new RuntimeException("pointsCollector can't be null");
            }
            try {
                GridBlock block;
                while ((block = blockManager.getNext()) != null) {
                    // process block
                    PointSetArray pnts = findSurfacePoints(block.getBounds());
                    // save points
                    if (pnts != null && pnts.size() > 0){
                        //pointsCollector.addPoints(pnts);
                        allPoints.addPoints(pnts);
                    }
                }
            } catch (Throwable t) {
                printf("Error in Block Processor\n");
                t.printStackTrace();
            }
        }
    } // class BlockProcessor 

    /**
     * class supply next unprocessed block to the block processor
     */
    static class BlockManager {

        AtomicInteger count = new AtomicInteger(0);
        Vector<GridBlock> blocks;

        BlockManager(Vector<GridBlock> blocks) {
            this.blocks = blocks;
        }

        GridBlock getNext() {
            int nextIndex = count.getAndIncrement();
            if (nextIndex >= blocks.size())
                return null;
            else
                return blocks.get(nextIndex);
        }
    }// 

    /**
     * class to collect points from individiual blocks into compete set
     */
    static class PointsCollector {

        PointSetArray points;

        PointsCollector(PointSetArray points) {
            this.points = points;
        }

        //
        // this is called by block processors to add poins located in that block to the total set of points
        //
        synchronized void addPoints(PointSetArray newPoints) {
            //printf("addPoints( %d)\n", newPoints.size());
            points.addPoints(newPoints);

        }
    } // class PointsCollector 

}
