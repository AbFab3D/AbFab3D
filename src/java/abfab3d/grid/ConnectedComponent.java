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

package abfab3d.grid;


import java.util.LinkedList;

import static abfab3d.util.Output.printf;



/**


   makes 6-connected flood fill of given material

   @author Vladimir Bulatov

*/
public class ConnectedComponent implements Comparable, Region {

    public static final int ALG_SCANLINE_STACK = 1, ALG_SCANLINE_QUEUE = 0, ALG_FLOODFILL_QUEUE = 2, ALG_FLOODFILL_RECURSIVE = -1 ;

    public static final int DEFAULT_ALGORITHM = ALG_SCANLINE_STACK;


    protected ArrayInt m_component = null; // array of coordinates of found voxels
    AttributeTester materialTester; // tests if voxel belong to the material 
    AttributeGrid grid;

    GridBit mask; // mask of visited voxels

    int nx1, ny1, nz1; // dimensions of grid reduced by 1

    // region's bounds
    int xmin=Integer.MAX_VALUE,
        ymin=Integer.MAX_VALUE,
        zmin=Integer.MAX_VALUE,
        xmax=Integer.MIN_VALUE,
        ymax=Integer.MIN_VALUE,
        zmax=Integer.MIN_VALUE;

    long m_volume = 0;

    int seedX, seedY, seedZ;

    static final int MAXLEVEL = 2000;

    /**
        mask - bitmask of visited voxels
        x,y,z - start point
        material - material to use
        collectData - colect regions data or not
        algorithm -

    */
    public ConnectedComponent(AttributeGrid grid, GridBit mask, int x, int y, int z, AttributeTester materialTester, boolean collectData){
        this(grid, mask, x, y, z, materialTester, collectData, DEFAULT_ALGORITHM);
    }

    public ConnectedComponent(AttributeGrid grid, GridBit mask, int x, int y, int z, long material, boolean collectData){
        this(grid, mask, x, y, z, material, collectData, DEFAULT_ALGORITHM);
    }

    public ConnectedComponent(AttributeGrid grid, GridBit mask, int x, int y, int z, long material, boolean collectData, int algorithm){
        this(grid, mask, x, y, z, new AttributeTesterValue(material), collectData, algorithm);
    }
    public ConnectedComponent(AttributeGrid grid, GridBit mask, int x, int y, int z, AttributeTester materialTester, boolean collectData, int algorithm){

        this.grid = grid;
        this.mask = mask;
        this.materialTester = materialTester;
        this.seedX = x;
        this.seedY = y;
        this.seedZ = z;

        nx1 = grid.getWidth()-1;
        ny1 = grid.getHeight()-1;
        nz1 = grid.getDepth()-1;

        //printf("ConnectedComponent(%d, %d, %d)\n", x,y,z);
        if(collectData)
            m_component = new ArrayInt(30);
        switch(algorithm){
        default:
        case ALG_SCANLINE_STACK:
            fillScanLineStack(new int[]{x,y,z});
            break;
        case ALG_SCANLINE_QUEUE:
            fillScanLineQueue(new int[]{x,y,z});
            break;
        case ALG_FLOODFILL_QUEUE:
            floodFillQue(new int[]{x,y,z});
            break;
        case ALG_FLOODFILL_RECURSIVE:
            floodFill(x, y, z,0);
            break;
        }

        // release references
        mask = null;
        grid = null;

    }

    void dumpScanLine(int x, int yy, int z){

        for(int y = 0; y <= ny1; y++){

            long mat = grid.getAttribute(x,y,z);
            printf("y: %d, mat: %d\n", y, mat);

        }
    }

    public void getExtents(int min[], int max[]){

        min[0] = xmin;
        min[1] = ymin;
        min[2] = zmin;
        max[0] = xmax;
        max[1] = ymax;
        max[2] = zmax;
    }

    void updateBounds(int x, int y, int z){
        if(x < xmin)
            xmin = x;
        if(y < ymin)
            ymin = y;
        if(z < zmin)
            zmin = z;
        if(x > xmax)
            xmax = x;
        if(y > ymax)
            ymax = y;
        if(z > zmax)
            zmax = z;


    }

    /**
       fills region using analog of scan line algorithm
    */
    boolean fillScanLineQueue(int start[]) {

        //LinkedList<int[]> que = new LinkedList<int[]>();
        //ArrayDeque<int[]> que = new ArrayDeque<int[]>();
        QueueInt que = new QueueInt(100000);

        boolean spanxLeft, spanxRight,spanzLeft, spanzRight;
        int x = start[0], y = start[1], z = start[2];
        int pnt[] = new int[3];

        que.offer(x,y,z);
        while(!que.isEmpty()) {

            //int pnt[] = que.remove();
            que.remove(pnt);
            x = pnt[0];
            y = pnt[1];
            z = pnt[2];

            if(mask.get(x,y,z) != 0){
                // this line was already visited
                continue;
            }
            //printf("new scanline: [%d,%d,%d]\n", x, y, z);
            //dumpScanLine(x,y,z);

            int y1 = y;
            // go down in y as far as possible
            while(y1 >= 0 && compareAttribute(grid,x,y1,z)) {
                y1--;
            }
            y1++; // increment back
            //printf("scanline [%2d,%2d,%2d]->", x,y1,z);

            spanxLeft = spanxRight = spanzLeft = spanzRight = false;

            while((y1 <= ny1) && compareAttribute(grid,x,y1,z) ){
                // fill one scan line
                // mark voxel visited
                mask.set(x,y1,z,1);
                if(m_component != null)
                    m_component.add(x,y1,z);
                updateBounds(x,y1,z);
                m_volume++;

                // check x-direction
                if(!spanxLeft && (x > 0) &&  (mask.get(x-1, y1, z) == 0) && compareAttribute(grid,x-1,y1,z)){
                    // start of potential new span
                    //que.offer(new int[]{x - 1, y1, z});
                    que.offer(x - 1, y1, z);

                    spanxLeft = true;

                } else if(spanxLeft && (x > 0) && ((mask.get(x-1, y1, z)!= 0) || !compareAttribute(grid,x-1,y1,z))){
                    // end of potential new span

                    spanxLeft = false;

                }

                if(!spanxRight && x < nx1 &&  (mask.get(x+1, y1, z)==0)  && compareAttribute(grid,x+1,y1,z)) {
                    // start of potential new span

                    //que.offer(new int[]{x + 1, y1, z});
                    que.offer(x + 1, y1, z);
                    spanxRight = true;

                } else if(spanxRight && x < nx1 && ((mask.get(x+1, y1, z)!= 0) || !compareAttribute(grid,x+1,y1,z))) {
                    // end of potential new span

                    spanxRight = false;

                }

                // check z direction
                if(!spanzLeft && (z > 0) &&  (mask.get(x, y1, z-1)==0) && compareAttribute(grid, x,y1,z-1)){
                    // start of potential new span
                    //que.offer(new int[]{x, y1, z-1});
                    que.offer(x, y1, z-1);
                    spanzLeft = true;

                } else if(spanzLeft && (z > 0) && ((mask.get(x, y1, z-1)!= 0) || !compareAttribute(grid,x,y1,z-1))){
                    // end of potential new span

                    spanzLeft = false;

                }

                if(!spanzRight && z < nz1 &&  (mask.get(x, y1, z+1)==0)  && compareAttribute(grid, x,y1,z+1)) {
                    // start of potential new span
                    //que.offer(new int[]{x, y1, z+1});
                    que.offer(x, y1, z+1);
                    spanzRight = true;

                } else if(spanzRight && z < nz1 && ((mask.get(x, y1, z+1)!= 0) || !compareAttribute(grid,x,y1,z+1))) {
                    // end of potential new span

                    spanzRight = false;

                }
                y1++;
            }
            //printf("[%2d,%2d,%2d]\n ", x,y1,z);

        }

        //que.printStat();
        return true;
    } // fillScanLineQue

    /**
       fills region using analog of scan line algorithm
    */
    boolean fillScanLineStack(int start[]) {


        StackInt3 stack = new StackInt3(100);

        int pnt[] = new int[3]; // point to pop from stack

        boolean spanxLeft, spanxRight,spanzLeft, spanzRight;
        int x = start[0], y = start[1], z = start[2];

        stack.push(x, y, z);

        while(stack.pop(pnt)) {

            x = pnt[0];
            y = pnt[1];
            z = pnt[2];

            if(mask.get(x,y,z) != 0){
                // this line was already visited
                continue;
            }
            int y1 = y;

            // go down in y as far as possible
            while(y1 >= 0 && compareAttribute(grid,x,y1,z)) {
                y1--;
            }
            y1++; // increment back
            //printf("scanline [%2d,%2d,%2d]->", x,y1,z);

            spanxLeft = spanxRight = spanzLeft = spanzRight = false;

            while((y1 <= ny1) && compareAttribute(grid,x,y1,z) ){
                // fill one scan line
                // mark voxel visited
                mask.set(x,y1,z,1);
                if(m_component != null)
                    m_component.add(x,y1,z);
                updateBounds(x,y1,z);
                m_volume++;

                // check x-direction
                if(!spanxLeft && (x > 0) &&  (mask.get(x-1, y1, z)==0) && compareAttribute(grid,x-1,y1,z)){
                    // start of potential new span
                    if(!stack.push(x - 1, y1, z)) return false;
                    spanxLeft = true;

                } else if(spanxLeft && (x > 0) && ((mask.get(x-1, y1, z) != 0) || !compareAttribute(grid,x-1,y1,z))){
                    // end of potential new span
                    spanxLeft = false;

                }

                if(!spanxRight && x < nx1 &&  (mask.get(x+1, y1, z)==0)  && compareAttribute(grid,x+1,y1,z)) {

                    // start of potential new span
                    if(!stack.push(x + 1, y1, z)) return false; // stack overflow
                    spanxRight = true;

                } else if(spanxRight && x < nx1 && ((mask.get(x+1, y1, z)!= 0) || !compareAttribute(grid,x+1,y1,z))) {
                    // end of potential new span

                    spanxRight = false;

                }

                // check z direction
                if(!spanzLeft && (z > 0) &&  (mask.get(x, y1, z-1)==0) && compareAttribute(grid,x,y1,z-1)){
                    // start of potential new span
                    if(!stack.push(x, y1, z-1)) return false;
                    spanzLeft = true;

                } else if(spanzLeft && (z > 0) && ((mask.get(x, y1, z-1)!= 0) || !compareAttribute(grid,x,y1,z-1))){
                    // end of potential new span

                    spanzLeft = false;

                }

                if(!spanzRight && z < nz1 &&  (mask.get(x, y1, z+1)==0)  && compareAttribute(grid,x,y1,z+1)) {
                    // start of potential new span

                    if(!stack.push(x, y1, z+1)) return false; // stack overflow
                    spanzRight = true;

                } else if(spanzRight && z < nz1 && ((mask.get(x, y1, z+1)!= 0) || !compareAttribute(grid,x,y1,z+1))) {

                    // end of potential new span
                    spanzRight = false;

                }
                y1++;
            }
            //printf("[%2d,%2d,%2d]\n ", x,y1,z);

        }

        //stack.printStat();

        return true;
    } // fillScanLine

    /**
       similar to recursive fill, but uses no system stack
    */
    void floodFillQue(int start[]) {

        //int start_state = grid.getState(start[0], start[1], start[2] );

        LinkedList<int[]> que = new LinkedList<int[]>();

        que.add(start);
        mask.set(start[0],start[1],start[2],1);

        while(!que.isEmpty()) {
            int vc[] = que.remove();

            int i = vc[0];
            int j = vc[1];
            int k = vc[2];

            if (compareAttribute(grid, i,j,k)) {

                if(m_component != null)
                    m_component.add(i,j,k);
                updateBounds(i,j,k);
                m_volume++;
                // test adjacent voxels

                for(int n1=-1; n1 < 2; n1++) {
                    for(int n2=-1; n2 < 2; n2++) {
                        for(int n3=-1; n3 < 2; n3++) {
                            if (n1 == 0 && n2 == 0 && n3 == 0)
                                continue;

                            int ni = i+n1;
                            int nj = j+n2;
                            int nk = k+n3;

                            if (mask.get(ni,nj,nk) == 0) {

                                if (!compareAttribute(grid, ni, nj, nk))
                                    continue;

                                que.offer(new int[]{ni,nj,nk});
                                mask.set(ni,nj,nk,1);
                                //printf("que: %d (%d,%d,%d)\n",que.size(),ni,nj,nk);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
      this is very unefficient and stack intensive recursive algorithm
    */
    void floodFill(int x, int y, int z, int level){

        if(level > MAXLEVEL)
            return;
        level++;
        if(x <= 0 || y <= 0 || z <= 0 || x >= nx1 || y >= ny1 || z >= nz1){
            // we don't test boundary poins
            return;
        }

        if(mask.get(x,y,z) != 0){
            // voxel already was visited
            return;
        }

        if(!compareAttribute(grid, x,y,z)) // different material
            return;

        // voxel of our material - add it and check 6 heighbors
        if(m_component != null)
            m_component.add(x,y,z);
        updateBounds(x,y,z);
        m_volume++;

        mask.set(x,y,z,1);

        floodFill(x+1,y,z,level);
        floodFill(x-1,y,z,level);
        floodFill(x,y+1,z,level);
        floodFill(x,y-1,z,level);
        floodFill(x,y,z+1,level);
        floodFill(x,y,z-1,level);

    }

    /**
       returns voxel coordinates
     */
    public int[] getVoxelCoord(int index, int[] coord){

        if(m_component == null)
            return coord;
        //
        // coordinates are triplets
        //

        index *= 3;

        if(index + 2 > m_component.size())
            return coord;

        coord[0] = (int) m_component.get(index++);
        coord[1] = (int) m_component.get(index++);
        coord[2] = (int) m_component.get(index++);

        return coord;
    }

    /**
       return volume of this region
     */
    public long getVolume(){

        return m_volume;

    }

    /**
       return seed pont of this region
     */
    public int[] getSeed(){

        return new int[]{seedX, seedY, seedZ};

    }

    void dump(){
        printf("component volume: %d\n",m_volume);
    }

    /**
       return true if voxel is non zero and has given material

    */
    static boolean compareMaterial(AttributeGrid grid, int x, int y, int z, long material){
        byte s = grid.getState(x,y,z);
        if(s == AttributeGrid.OUTSIDE)
            return false;
        long m = grid.getAttribute(x,y,z);
        if(m == material)
            return true;
        else
            return false;
    }

    final boolean compareAttribute(AttributeGrid grid, int x, int y, int z){ 
        return materialTester.test(x,y,z,grid.getAttribute(x,y,z));
    }

    public int compareTo(Object o){
        long v1 = this.getVolume();
        long v2 = ((ConnectedComponent)o).getVolume();

        if (v1 < v2) {
            return -1;
        } else if (v1 == v2) {
            return 0;
        } else {
            return 1;
        }

    }

    public void traverse(RegionTraverser t){

        int len3 = m_component.size()/3;

        for(int i = 0, k = 0; k  < len3; k++){
            t.found((int)m_component.get(i++), (int) m_component.get(i++), (int)m_component.get(i++));
        }
    }

    public void traverseInterruptible(RegionTraverser t){
        int len3 = m_component.size()/3;

        for(int i = 0, k = 0; k  < len3; k++){
            if(!t.foundInterruptible((int)m_component.get(i++), (int)m_component.get(i++), (int)m_component.get(i++)))
                return;
        }
    }

    /**
       we don't merge
     */
    public boolean canMerge(Region r){
        return false;
    }

    /**
       we don't merge
     */
    public boolean merge(Region r){
        return false;
    }

    /**
     * Checks whether a coordinate is in the region.
     *
     * @param vc The coordinate
     */
    public boolean contains(VoxelCoordinate vc) {

        // TODO: untested and slow
        int len3 = m_component.size()/3;

        for(int i = 0; i < len3; i=i+3){
            if (vc.getX() == m_component.get(i)) {
                if (vc.getY() == m_component.get(i+1)) {
                    if (vc.getZ() == m_component.get(i+2)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
