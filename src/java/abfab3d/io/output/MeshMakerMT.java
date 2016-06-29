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
package abfab3d.io.output;

import java.util.Comparator;
import java.util.Arrays;

import javax.vecmath.Vector3d;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import abfab3d.core.Grid;
import abfab3d.core.ResultCodes;
import abfab3d.grid.DensityMaker;
import abfab3d.grid.DensityMakerSubvoxel;

import abfab3d.mesh.EdgeTester;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.MeshDecimator;
import abfab3d.mesh.WingedEdgeTriangleMesh;

import abfab3d.util.AbFab3DGlobals;
import abfab3d.core.MathUtil;
import abfab3d.core.TriangleCollector;



import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static java.lang.System.nanoTime;

/**
 * MeshMakerMT takes grid, extract isosurface and decimates mesh in one operation.
 * <p/>
 * Uses multihreaded pipeline by converting grid into array of small blocks, which fit into processor cache
 *
 * @author Vladimir Bulatov
 */
public class MeshMakerMT {
    public enum StatusType {SUCCESS, FAIL};  
    
    /** Should we print debug information */
    private static final boolean DEBUG = false;

    /** Should we collect stats information */
    private static final boolean STATS = false;

    static final double MM = 0.001;

    public static final int RESULT_OK = 0;

    protected int m_threadCount = 1;

    // size of block of grid to make in one chunk 
    protected int m_blockSize = 20;
    protected int m_noDecimationSize = 100;

    //max error of decimation
    protected double m_maxDecimationError = 1.e-9;
    final static int VERSION1 = 1, VERSION2 =2;
    static int version=VERSION2;
    
    protected StatusType status;

    protected int m_interpolationAlgorithm = IsosurfaceMaker.INTERPOLATION_LINEAR;

    protected double m_smoothingWidth = 1.;
    
    // converter from grid attribute into density 
    protected DensityMaker m_densityMaker = new DensityMakerSubvoxel(255); 


    protected int m_maxDecimationCount = 7;

    // Maximum allowed triangles.  Will relax maxDecimationError to achieve
    protected int m_maxTriangles = Integer.MAX_VALUE;
    protected EdgeTester m_edgeTester;

    public MeshMakerMT() {
        m_threadCount = ((Number)AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
    }

    public void setThreadCount(int count) {
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number)AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }

    public void setMaxTriangles(int tris) {
        this.m_maxTriangles = tris;
    }

    /**
       @deprecated, replaced with setDensityMaker()
     */
    public void setMaxAttributeValue(int value) {
        
        m_densityMaker = new DensityMakerSubvoxel(value);
    }

    /**
       sets the instance of DensityMaker used for conversion grid attribute into density value
     */
    public void setDensityMaker(DensityMaker densityMaker) {

        m_densityMaker = densityMaker;

    }

    public void setMaxDecimationError(double value) {

        m_maxDecimationError = value;

    }

    public void setSmoothingWidth(double value) {

        m_smoothingWidth = value;

    }


    public void setBlockSize(int size) {
        m_blockSize = size;
    }

    public void setMaxDecimationCount(int count) {

        m_maxDecimationCount = count;

    }

    /**
     * set tester to test edge collapses
     * edge can be collapsed only if tester return true
     */
    public void setEdgeTester(EdgeTester tester) {

        m_edgeTester = tester;

    }


    /**
       set interpolation algorith to use 
       INTERPOLATION_LINEAR
       or 
       INTERPOLATION_INDICATOR_FUNCTION       
     */
    public void setInterpolationAlgorithm(int algorithm){

        m_interpolationAlgorithm = algorithm;
    }

    /**
     * creates mesh and feeds it into triangle collector
     */
    public int makeMesh(Grid grid, TriangleCollector tc) {
    	status = StatusType.SUCCESS;
    	
        switch(version){
        default: 
        case VERSION1:
            return makeMesh_v1(grid, tc);
        case VERSION2:
            return makeMesh_v2(grid, tc);
        }
    }

    /**
       uses octree for block structure 
     */
    public int makeMesh_v2(Grid grid, TriangleCollector tc) {

        printf("Mesh maker using threads: %d\n",m_threadCount);
        long t0 = time();
        GridBlockSet blocks = makeBlocksOctree(grid.getWidth(), grid.getHeight(), grid.getDepth(), m_blockSize);
                
        //blocks.dump();

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);

        BlockProcessor threads[] = new BlockProcessor[m_threadCount];
        double smoothKernel[] = null;
        if (m_smoothingWidth > 0.) {
            smoothKernel = MathUtil.getGaussianKernel(m_smoothingWidth);
        }

        for (int i = 0; i < m_threadCount; i++) {
            threads[i] = new BlockProcessor(grid, blocks, smoothKernel);
            if (m_edgeTester != null) {
                threads[i].setEdgeTester((EdgeTester) (m_edgeTester.clone()));
            }
            executor.submit(threads[i]);

        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        printf("MESH_EXTRACTION_TIME: %d ms\n", (time() - t0));

        // last block has the final mesh 
        GridBlock block = blocks.getLast(); 
        if(true){
            //printf("    lastBlock: %s\n", block);
            printf("    origFaceCount: %d\n", block.origFaceCount);
            printf("    finalFaceCount: %d\n", block.finalFaceCount);
        }
        block.writeTriangles(tc);        
        return ResultCodes.RESULT_OK;

    }

    /**
       uses array for block
     */
    public int makeMesh_v1(Grid grid, TriangleCollector tc) {

        long t0 = time();

        GridBlockSet blocks = makeBlocks(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, m_blockSize);


        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);

        BlockProcessor threads[] = new BlockProcessor[m_threadCount];
        double smoothKernel[] = null;
        if (m_smoothingWidth > 0.) {
            smoothKernel = MathUtil.getGaussianKernel(m_smoothingWidth);
        }

        for (int i = 0; i < m_threadCount; i++) {
            threads[i] = new BlockProcessor(grid, blocks, smoothKernel);
            if (m_edgeTester != null) {
                threads[i].setEdgeTester((EdgeTester) (m_edgeTester.clone()));
            }
            executor.submit(threads[i]);

        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long num_tris = 0;
        for(int i=0; i < threads.length;i++) {
            num_tris += threads[i].getNumTriangles();
        }

        if(DEBUG)
            printf("Raw number of triangles before decimation: %d\n",num_tris);


        printf("MESH_EXTRACTION_TIME: %d ms\n", (time() - t0));

        blocks.rewind();
        GridBlock block;
        int origFaceCount = 0, finalFaceCount = 0;

        while ((block = blocks.getNext()) != null) {

            //printf(" isosurface: %d ms, decimation: %d ms  faces: %d -> : %d\n",
            //       block.timeIsosurface, block.timeDecimation, block.origFaceCount,block.finalFaceCount);
            origFaceCount += block.origFaceCount;
            finalFaceCount += block.finalFaceCount;
        }

        if(DEBUG)
            printf("finalFaceCount: %d\n", finalFaceCount);

        int max_loop = 3;
        int attempts = 0;
        int last_tri_count =0;

        while(finalFaceCount > m_maxTriangles && attempts < max_loop) {
            blocks.rewind();
            // TODO: Not sure how aggressive to change decimation error;
            m_maxDecimationError *= 10;
            System.out.println("Count is above max triangle limit: " + finalFaceCount + " new decimationError: " + m_maxDecimationError);

            executor = Executors.newFixedThreadPool(m_threadCount);

            BlockDecimator[] workers = new BlockDecimator[m_threadCount];
            for (int i = 0; i < m_threadCount; i++) {
                workers[i] = new BlockDecimator(blocks);
                if (m_edgeTester != null) {
                    workers[i].setEdgeTester((EdgeTester) (m_edgeTester.clone()));
                }

                executor.submit(workers[i]);
            }

            executor.shutdown();

            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            blocks.rewind();

            finalFaceCount = 0;
            while ((block = blocks.getNext()) != null) {

                //printf(" isosurface: %d ms, decimation: %d ms  faces: %d -> : %d\n",
                //       block.timeIsosurface, block.timeDecimation, block.origFaceCount,block.finalFaceCount);
                finalFaceCount += block.finalFaceCount;
            }

            System.out.println("Current faceCount: " + finalFaceCount);
            attempts++;

            if (finalFaceCount == last_tri_count) {
                System.out.println("No new triangles found, bailing");
                break;
            }

            last_tri_count = finalFaceCount;
        }

        blocks.rewind();
        origFaceCount = 0;
        finalFaceCount = 0;

        while ((block = blocks.getNext()) != null) {

            //printf(" isosurface: %d ms, decimation: %d ms  faces: %d -> : %d\n",
            //       block.timeIsosurface, block.timeDecimation, block.origFaceCount,block.finalFaceCount);
            origFaceCount += block.origFaceCount;
            finalFaceCount += block.finalFaceCount;
            block.writeTriangles(tc);

        }

        if(DEBUG){
            printf("originalFaceCount: %d\n", origFaceCount);
            printf("finalFaceCount: %d\n", finalFaceCount);
        }
        return ResultCodes.RESULT_OK;

    }

    /**
     * generates set of blocks of approximately blockSize, which tile the (NX x NY x NZ) grid
     * if tiles
     */
    public static GridBlockSet makeBlocks(int nx, int ny, int nz, int blockSize) {

        int blocksX = (nx + blockSize - 1) / blockSize;
        int blocksY = (ny + blockSize - 1) / blockSize;
        int blocksZ = (nz + blockSize - 1) / blockSize;
        // minial sizes of blocks 
        int bx = nx / blocksX;
        int by = ny / blocksY;
        int bz = nz / blocksZ;

        // reminders. We add 1 to size of first rx blocks in each directions 
        int rx = nx % blocksX;
        int ry = ny % blocksY;
        int rz = nz % blocksZ;

        GridBlockSet blocks = new GridBlockSet();
        for (int y = 0, iy = 0; y < blocksY; y++) {

            int iy1 = iy + by;
            if (y < ry) iy1++;

            for (int x = 0, ix = 0; x < blocksX; x++) {

                int ix1 = ix + bx;
                if (x < rx) ix1++;

                for (int z = 0, iz = 0; z < blocksZ; z++) {

                    int iz1 = iz + bz;
                    if (z < rz) iz1++;
                    GridBlock block = new GridBlock();
                    block.setBlock(ix, ix1, iy, iy1, iz, iz1);
                    //if(DEBUG) printf("block: (%3d,%3d,%3d,%3d,%3d,%3d)\n", ix, ix1, iy, iy1, iz, iz1);

                    blocks.add(block);
                    iz = iz1;
                }
                ix = ix1;
            }
            iy = iy1;
        }

        System.out.println("***Total blocks made: " + blocks.gridBlocks.size() + " min tris is: " + (blocks.gridBlocks.size() * 100));
        return blocks;

    } // makeBlocks     

    /**
       
     */
    public static GridBlockSet makeBlocksOctree(int nx, int ny, int nz, int blockSize) {
        
        printf("makeBlocksOctree(%d %d %d %d)\n", nx, ny, nz, blockSize);
        GridBlockSet blocks = new GridBlockSet();
        GridBlock block = new GridBlock(0, nx, 0, ny, 0, nz);
        
        block.split(blockSize, blocks);

        blocks.sort();
        
        //for(int i = 0; i < blocks.size(); i++){
        //    printf("block: %s\n,", blocks.get(i));
        //}

        GridBlock gb  = blocks.getFirst();
        
        blocks.faceCounts = new AtomicInteger[gb.level + 1];
        for(int i = 0; i < blocks.faceCounts.length; i++){
            
            blocks.faceCounts[i] = new AtomicInteger(0);
            printf("blocks.faceCounts[%d]: %s\n", i, blocks.faceCounts[i]);
        }
        blocks.currentLevel = gb.level;
        
        return blocks;
        
    } // makeBlocksOctree
    
    public StatusType getStatus() {
    	return status;
    }

    /**
     *
     * 
     * block of grid
     */
    static class GridBlock {

        // mesh generated 
        //WingedEdgeTriangleMesh mesh; 
        // bounds of the grid fragment 
        //double bounds[];
        int faces[];
        double vertices[];
        // grid dimensions of the grid fragment 
        //int nx, ny, nz;

        // block of grid to process (inclusive) 
        int xmin, xmax, ymin, ymax, zmin, zmax;

        int origFaceCount;
        int finalFaceCount;
        long timeIsosurface;
        long timeDecimation;
        // children of octree 
        GridBlock children[];
        int childCount;
        int finishedChildCount;
        GridBlock parent; // parent in octree 
        int level; // subdivision level of that block 

        static final int 
            C000 = 0,
            C001 = 1,
            C010 = 2,
            C011 = 3,
            C100 = 4,
            C101 = 5,
            C110 = 6,
            C111 = 7;

        static final int SPLITX = 1, SPLITY = 2, SPLITZ = 4;

        
        IndexedTriangleSetBuilder its = null;

        GridBlock(){

        }

        GridBlock(int xmi, int xma, int ymi, int yma, int zmi, int zma ){
            setBlock(xmi, xma, ymi, yma, zmi, zma);
        }

        void setParent(GridBlock block){
            parent = block;
        }

        void setBlock(int x0, int x1, int y0, int y1, int z0, int z1) {

            xmin = x0;
            xmax = x1;

            ymin = y0;
            ymax = y1;

            zmin = z0;
            zmax = z1;

        }

        void split(int blockSize, GridBlockSet blocks){
            if(DEBUG) printf("split %s\n", this);
            int flags = 0;
            if(xmax - xmin > blockSize) flags |= SPLITX;
            if(ymax - ymin > blockSize) flags |= SPLITY;
            if(zmax - zmin > blockSize) flags |= SPLITZ;
            
            if(flags == 0){
                //if(DEBUG) printf(" NOSPLIT %s\n", this);
                // no split is needed 
                blocks.add(this);
                return;
            }
            
            children = new GridBlock[8];

            int x1 = 0, y1 = 0, z1 = 0;
            switch(flags){
            default: 
                // should not happens
                break;
            case C001:
                x1 = (xmin+xmax)/2;
                children[C000] = new GridBlock(xmin,   x1, ymin, ymax, zmin, zmax);
                children[C001] = new GridBlock(x1,   xmax, ymin, ymax, zmin, zmax);
                break;

            case C010:
                y1 = (ymin+ymax)/2;
                children[C000] = new GridBlock(xmin, xmax, ymin,   y1, zmin, zmax);
                children[C010] = new GridBlock(xmin, xmax, y1,   ymax, zmin, zmax);
                break;
            case C100:
                z1 = (zmin+zmax)/2;
                children[C000] = new GridBlock(xmin, xmax, ymin, ymax, zmin, z1);
                children[C100] = new GridBlock(xmin, xmax, ymin, ymax, z1, zmax);
                break;

            case C011:
                x1 = (xmin+xmax)/2;
                y1 = (ymin+ymax)/2;
                children[C000] = new GridBlock(xmin,   x1, ymin,   y1, zmin, zmax);
                children[C001] = new GridBlock(x1,   xmax, ymin,   y1, zmin, zmax);
                children[C010] = new GridBlock(xmin,   x1, y1,   ymax, zmin, zmax);
                children[C011] = new GridBlock(x1,   xmax, y1,   ymax, zmin, zmax);
                break;
            case C101:
                x1 = (xmin+xmax)/2;
                z1 = (zmin+zmax)/2;
                children[C000] = new GridBlock(xmin,   x1, ymin, ymax, zmin, z1);
                children[C001] = new GridBlock(x1,   xmax, ymin, ymax, zmin, z1);
                children[C100] = new GridBlock(xmin,   x1, ymin, ymax, z1, zmax);
                children[C101] = new GridBlock(x1,   xmax, ymin, ymax, z1, zmax);
                break;
            case C110:
                y1 = (ymin+ymax)/2;
                z1 = (zmin+zmax)/2;
                children[C000] = new GridBlock(xmin, xmax, ymin,   y1, zmin, z1);
                children[C010] = new GridBlock(xmin, xmax, y1,   ymax, zmin, z1);
                children[C100] = new GridBlock(xmin, xmax, ymin,   y1, z1, zmax);
                children[C110] = new GridBlock(xmin, xmax, y1,   ymax, z1, zmax);
                break;
            case C111:
                x1 = (xmin+xmax)/2;
                y1 = (ymin+ymax)/2;
                z1 = (zmin+zmax)/2;
                children[C000] = new GridBlock(xmin,   x1, ymin,   y1, zmin, z1);
                children[C001] = new GridBlock(x1,   xmax, ymin,   y1, zmin, z1);
                children[C010] = new GridBlock(xmin,   x1, y1,   ymax, zmin, z1);
                children[C011] = new GridBlock(x1,   xmax, y1,   ymax, zmin, z1);
                children[C100] = new GridBlock(xmin,   x1, ymin,   y1, z1, zmax);
                children[C101] = new GridBlock(x1,   xmax, ymin,   y1, z1, zmax);
                children[C110] = new GridBlock(xmin,   x1, y1,   ymax, z1, zmax);
                children[C111] = new GridBlock(x1,   xmax, y1,   ymax, z1, zmax);

            }

            int childLevel = this.level+1;
            for(int i = 0; i < children.length; i++){
                GridBlock child = children[i];
                if(child != null){
                    childCount++;
                    child.level = childLevel;
                    child.split(blockSize, blocks);
                    child.setParent(this);
                }                    
                   
            }
                
        }
        
        void writeTriangles(TriangleCollector tc) {

            if (its != null) {
                its.getTriangles(tc);
                return;
            }

            if (faces == null || faces.length < 3) {
                //printf("triCount: 0\n");
                return;
            }

            // triangles are 
            Vector3d v0 = new Vector3d();
            Vector3d v1 = new Vector3d();
            Vector3d v2 = new Vector3d();

            for (int i = 0; i < faces.length; i += 3) {

                int iv0 = 3 * faces[i];
                int iv1 = 3 * faces[i + 1];
                int iv2 = 3 * faces[i + 2];

                v0.set(vertices[iv0], vertices[iv0 + 1], vertices[iv0 + 2]);
                v1.set(vertices[iv1], vertices[iv1 + 1], vertices[iv1 + 2]);
                v2.set(vertices[iv2], vertices[iv2 + 1], vertices[iv2 + 2]);
                tc.addTri(v0, v1, v2);
            }
        }

        synchronized void childFinished(GridBlockSet blocks){
            finishedChildCount++;
            if(DEBUG)                        
                printf("     finished: %s child %3d of %3d\n", this,finishedChildCount,childCount);
            if(finishedChildCount >= childCount){
                
                if(DEBUG)
                    printf("     adding to processing %s\n", this);
                blocks.add(this);
            }
        }

        void informParent(GridBlockSet blocks){

            blocks.faceCounts[level].addAndGet(finalFaceCount);

            if(parent != null)
                parent.childFinished(blocks);
        }
        boolean hasChildren(){
            return (children != null);
        }
        
        public String toString(){
            return fmt("block(%3d %3d %3d %3d %3d %3d)children: %s level: %d", xmin,xmax,ymin,ymax,zmin,zmax, (children != null), level);
        }

    } // class GridBlock 

    /**
     *
     * class GridBlockSet
     *
     * collection of grid blocks
     *
     */
    public static class GridBlockSet {

        Vector<GridBlock> gridBlocks;
        AtomicInteger currentBlock = new AtomicInteger(0);
        int currentLevel;
        AtomicInteger faceCounts[];

        GridBlockSet() {
            gridBlocks = new Vector<GridBlock>();
        }
        
        public void rewind() {
            currentBlock.set(0);
        }

        public synchronized GridBlock getNext() {

            int next = currentBlock.getAndIncrement();
            if (next >= gridBlocks.size()){
                // thread finished, but may be there is another thread running which will add new jobs 
                // so we have to revert 
                currentBlock.getAndDecrement();
                return null;

            } else {
                
                GridBlock block = gridBlocks.get(next);
                if(block.level != currentLevel){
                    int currentCount = faceCounts[currentLevel].get();
                    //if(true) printf("fcount[%d]:%d (%d)\n", currentLevel, currentCount, m_maxTriangles);
                    /*
                      TODO this is wrong place to increase  m_maxDecimationError
                    if(currentCount > (int)(m_maxTriangles)){
                        if(true){
                            printf("   face count: %d exceeded max count: %d\n",currentCount,m_maxTriangles);
                            printf("   current decimation error: %9.2e\n", m_maxDecimationError);
                        }
                        m_maxDecimationError *= 2;
                        if(true){
                            printf("   new decimation error: %9.2e\n", m_maxDecimationError);
                        }
                    }
                    */
                    currentLevel = block.level;
                }
                return block;
            }
        }

        public synchronized void add(GridBlock block) {
            if(DEBUG)
                printf("add(%s)\n", block);
            gridBlocks.add(block);
        }

        public int size(){
            return gridBlocks.size();
        }

        public GridBlock get(int i){
            return gridBlocks.get(i);
        }

        public GridBlock getLast(){
            return gridBlocks.get(gridBlocks.size()-1);
        }

        public GridBlock getFirst(){
            return gridBlocks.get(0);
        }
        
        public void sort(){
            // sort blocks in order of descenting level 
            GridBlock blk[]= gridBlocks.toArray(new GridBlock[0]);
            Arrays.sort(blk, new GridBlockComparator());
            for(int i =0; i < blk.length; i++)
                gridBlocks.setElementAt(blk[i], i);
        }

        public void dump(){
            for(int i = 0; i < gridBlocks.size(); i++){
                printf("%3d: %s\n", i, gridBlocks.get(i));
            }
        }

    } // class GridBlockSet

    public static class GridBlockComparator implements Comparator<GridBlock>{
        public int compare(GridBlock b1, GridBlock b2) {
            return b2.level - b1.level;            
        }
        public boolean equals(Object o) {            
            return (o == this);
        }
        
    }


    static AtomicInteger threadCount = new AtomicInteger(0);

    /**
     * extract mesh from a block of the grid
     */
    class BlockProcessor implements Runnable {

        String threadName;
        Grid grid;
        // physical bounds of the grid 
        double gridBounds[] = new double[6];
        // physical bounds of the block 
        double blockBounds[] = new double[6];

        GridBlockSet blocks;

        WingedEdgeTriangleMesh mesh;
        IndexedTriangleSetBuilder its;
        double vertices[]; // intermediate memory for vertices 
        int faces[];  // intermediate memory for face indexes 

        int gnx, gny, gnz;
        double gxmin, gymin, gzmin;
        double gdx, gdy, gdz;
        //double maxDecimationError;
        IsosurfaceMaker imaker;
        MeshDecimator decimator;
        IsosurfaceMaker.BlockSmoothingSlices slicer;
        double smoothKernel[];
        long origNumTriangles;

        EdgeTester edgeTester;

        BlockProcessor(Grid grid,
                       GridBlockSet blocks,
                       double smoothKernel[]
        ) {

            this.grid = grid;
            this.blocks = blocks;
            this.gridBounds = new double[6];
            grid.getGridBounds(gridBounds);

            gnx = grid.getWidth();
            gny = grid.getHeight();
            gnz = grid.getDepth();
            gxmin = gridBounds[0];
            gymin = gridBounds[2];
            gzmin = gridBounds[4];
            gdx = (gridBounds[1] - gridBounds[0]) / gnx;
            gdy = (gridBounds[3] - gridBounds[2]) / gny;
            gdz = (gridBounds[5] - gridBounds[4]) / gnz;

            slicer = new IsosurfaceMaker.BlockSmoothingSlices(grid);
            slicer.setDensityMaker(m_densityMaker);

            this.smoothKernel = smoothKernel;
            
            this.threadName = "thread" + threadCount.getAndIncrement();
        }

        void setEdgeTester(EdgeTester edgeTester) {

            this.edgeTester = edgeTester;

        }

        public void run() {
            origNumTriangles = 0;
            // make isosurface extrator

            while (true) {

                GridBlock block = blocks.getNext();
                if(DEBUG)
                    printf(" %s block: %s\n", threadName, block);
                if (block == null)
                    break;
                
                try {
                    if(block.hasChildren())
                        joinAndDecimate(block);
                    else 
                        buildAndDecimate(block);

                } catch (Exception e) {

                    e.printStackTrace();
                    status = StatusType.FAIL;
                    break;
                }
            }
        }

        void buildAndDecimate(GridBlock block) {

            if(DEBUG)
                printf("buildAndDecimate(%s)\n", block);
            blockBounds[0] = gxmin + block.xmin * gdx + gdx / 2;
            blockBounds[1] = blockBounds[0] + (block.xmax - block.xmin) * gdx;
            blockBounds[2] = gymin + block.ymin * gdy + gdy / 2;
            blockBounds[3] = blockBounds[2] + (block.ymax - block.ymin) * gdy;
            blockBounds[4] = gzmin + block.zmin * gdz + gdz / 2;
            blockBounds[5] = blockBounds[4] + (block.zmax - block.zmin) * gdz;
            if (DEBUG) {
                printf("processBlock() [%6.2f,%6.2f,%6.2f,%6.2f,%6.2f,%6.2f]\n",
                        blockBounds[0] / MM, blockBounds[1] / MM, blockBounds[2] / MM, blockBounds[3] / MM, blockBounds[4] / MM, blockBounds[5] / MM);
            }

            if (its == null) {
                its = new IndexedTriangleSetBuilder();
            } else {
                its.clear();
            }

            long t0;

            if (STATS) {
                t0 = nanoTime();
            }

            slicer.initBlock(block.xmin, block.xmax, block.ymin, block.ymax, block.zmin, block.zmax, smoothKernel);
            if (!slicer.containsIsosurface()) {
                //if (DEBUG) System.out.println("Nothing here");
                block.informParent(blocks);
                return;
            }

            if (imaker == null)
                imaker = new IsosurfaceMaker();

            imaker.setIsovalue(0.);
            imaker.setBounds(blockBounds);
            imaker.setGridSize(block.xmax - block.xmin + 1, block.ymax - block.ymin + 1, block.zmax - block.zmin + 1);
            imaker.setInterpolationAlgorithm(m_interpolationAlgorithm);
            imaker.makeIsosurface(slicer, its);


            long t1;
            if (STATS) {
                t1 = nanoTime();

                block.timeIsosurface = (t1 - t0);
            }

            int vertexCount = its.getVertexCount();
            int faceCount = its.getFaceCount();

            origNumTriangles += faceCount;

            //printf("faceCount: %d vertexCount: %d\n", faceCount, vertexCount);

            if (faceCount < m_noDecimationSize) {
                // no decimation is needed 
                block.faces = new int[faceCount * 3];
                its.getFaces(block.faces);
                block.vertices = new double[3 * its.getVertexCount()];
                its.getVertices(block.vertices);

                // TODO: added this, I suspect total faceCount was wrong without this.
                block.finalFaceCount = faceCount;

                block.informParent(blocks);
                return;
            }

            // will do decimation 
            vertices = its.getVertices(vertices);
            faces = its.getFaces(faces);

            block.origFaceCount = faceCount;

            //printf("vertCount: %d faceCont: %d\n", vertexCount, faceCount);        

            if (mesh == null) {
                //printf("new mesh\n");
                mesh = new WingedEdgeTriangleMesh(vertices, vertexCount, faces, faceCount);
            } else {
                //printf("reusing old mesh\n");
                mesh.clear();
                mesh.setFaces(vertices, vertexCount, faces, faceCount);
            }

            //block.mesh = 
            //intf("mesh created: %d ms\n", (time() - t0));

            if (decimator == null) {
                decimator = new MeshDecimator();
                if (edgeTester != null) {
                    decimator.setEdgeTester(edgeTester);
                }
            }

            if (edgeTester != null) {
                edgeTester.initialize(mesh);
            }

            //printf("start decimation\n");

            int count = m_maxDecimationCount;

            int fcount = mesh.getTriangleCount();

            decimator.setMaxCollapseError(m_maxDecimationError);
            while (count-- > 0) {

                int target = fcount / 2;
                decimator.processMesh(mesh, target);
                fcount = mesh.getTriangleCount();

            }

            //printf("decimation done. fcount: %d\n",fcount);

            if (STATS) block.timeDecimation = (nanoTime() - t1);
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(fcount);
            mesh.getTriangles(its);
            block.its = its;
            block.finalFaceCount = fcount;
            
            if(DEBUG)
                printf("%s informParent()\n", threadName);
            block.informParent(blocks);
            if(DEBUG)
                printf("%s parent informed blocks: %d\n", threadName, blocks.size());
        }

        //
        // joining faces of children blocks 
        //
        void joinAndDecimate(GridBlock block) {

            if(DEBUG)
                printf("joinAndDecimate(%s)\n", block);

            its.clear();

            for(int i = 0; i < block.children.length; i++){
                GridBlock child = block.children[i];
                if(child != null){
                    if(DEBUG)
                        printf("  child %s origFaces: %d finalFaces: %d\n", child, child.origFaceCount, child.finalFaceCount);
                    child.writeTriangles(its);
                    block.origFaceCount += child.origFaceCount;
                }
            }

            int vertexCount = its.getVertexCount();
            int faceCount = its.getFaceCount();

            if(DEBUG)
                printf("    fcount before decimation: %d\n", faceCount);
            
            if (faceCount < m_noDecimationSize) {
                // no decimation is needed - store result 
                block.faces = new int[faceCount * 3];
                its.getFaces(block.faces);
                block.vertices = new double[3 * its.getVertexCount()];
                its.getVertices(block.vertices);
                block.finalFaceCount = faceCount;
                block.informParent(blocks);
                return;
            }

            // will do decimation 
            vertices = its.getVertices(vertices);
            faces = its.getFaces(faces);
            
            mesh.clear();
            mesh.setFaces(vertices, vertexCount, faces, faceCount);
            
            int iterations = m_maxDecimationCount;

            int fcount = mesh.getTriangleCount();
            if(DEBUG)
                printf("  decimating fcount: %d\n", fcount); 
            decimator.setMaxCollapseError(m_maxDecimationError);
            while(iterations-- > 0){
                //TODO better algorithm to deal with block boundaries
                decimator.processMesh(mesh, fcount/2);
                int fc = mesh.getTriangleCount();
                if(DEBUG)
                    printf("        fcount: %d\n", fc);                        
                if(fc > (int)(fcount*0.99))
                    break;
                fcount = fc;
            }
            IndexedTriangleSetBuilder ts = new IndexedTriangleSetBuilder(fcount);
            mesh.getTriangles(ts);
            block.its = ts;
            block.finalFaceCount = fcount; 
            //TODO - release children memory

            block.informParent(blocks);

        }

        public long getNumTriangles() {
            return origNumTriangles;
        }

    } // class BlockProcessor

    /**
     * Decimate a block further
     */
    class BlockDecimator implements Runnable {

        GridBlockSet blocks;

        WingedEdgeTriangleMesh mesh;
        double vertices[]; // intermediate memory for vertices
        int faces[];  // intermediate memory for face indexes

        //double maxDecimationError;
        MeshDecimator decimator;
        EdgeTester edgeTester;

        BlockDecimator(GridBlockSet blocks){

            this.blocks = blocks;
            //this.maxDecimationError = maxDecimationError;
        }

        void setEdgeTester(EdgeTester edgeTester) {
            this.edgeTester = edgeTester;
        }

        public void run() {
            while (true) {

                GridBlock block = blocks.getNext();

                if (block == null)
                    break;

                try {
                    processBlock(block);

                } catch (Exception e) {

                    e.printStackTrace();
                    status = StatusType.FAIL;
                    break;
                }
            }
        }

        void processBlock(GridBlock block) {
            long t1 = nanoTime();

            if (block.its == null) {
                return;
            }

            if (block.finalFaceCount < m_noDecimationSize) {
                // no decimation is needed
                return;
            }


            vertices = block.its.getVertices(vertices);
            faces = block.its.getFaces(faces);

            block.origFaceCount = block.finalFaceCount;

            int vertexCount = vertices.length / 3;
            int faceCount = block.finalFaceCount;

            //printf("decimate faceCount: %d vertexCount: %d\n", faceCount, vertexCount);

            //printf("vertCount: %d faceCont: %d\n", vertexCount, faceCount);

            if (mesh == null) {
                //printf("new mesh\n");
                mesh = new WingedEdgeTriangleMesh(vertices, vertexCount, faces, faceCount);
            } else {
                //printf("reusing old mesh\n");
                mesh.clear();
                mesh.setFaces(vertices, vertexCount, faces, faceCount);
            }

            //block.mesh =
            //intf("mesh created: %d ms\n", (time() - t0));

            if (decimator == null) {
                decimator = new MeshDecimator();
                decimator.setMaxCollapseError(m_maxDecimationError);
                if (edgeTester != null) {
                    decimator.setEdgeTester(edgeTester);
                }
            }

            if (edgeTester != null) {
                edgeTester.initialize(mesh);
            }


            int count = m_maxDecimationCount;

            int fcount = mesh.getTriangleCount();

            while (count-- > 0) {

                int target = fcount / 2;
                decimator.processMesh(mesh, target);
                fcount = mesh.getTriangleCount();

            }

            //printf("decimation done. orig: %d --> fcount: %d\n",block.origFaceCount,fcount);

            if (STATS) block.timeDecimation = (nanoTime() - t1);
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(fcount);
            mesh.getTriangles(its);
            block.its = its;
            block.finalFaceCount = fcount;
        }

    } // class BlockProcessor

}// class MeshMakerMT 

