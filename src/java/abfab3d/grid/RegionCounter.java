/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import abfab3d.core.AttributeGrid;
import abfab3d.core.ClassTraverser;
import abfab3d.core.Grid;

import abfab3d.core.VoxelClasses;
import abfab3d.core.Units;

import static abfab3d.core.Output.printf;


/**
   class to count specific regions

   @author Vladimir Bulatov
 */
public class RegionCounter {

    public static final boolean DEBUG = false;

    /**
       components counting via various algoritms


     */
    public static RegionCounterResults countComponents(AttributeGrid grid, AttributeTester materialTester) {
        return countComponents(grid, materialTester, -1, false,ConnectedComponent.DEFAULT_ALGORITHM);        
    }
    public static RegionCounterResults countComponents(AttributeGrid grid, long material) {
        return countComponents(grid, new AttributeTesterValue(material), -1, false,ConnectedComponent.DEFAULT_ALGORITHM);
    }

    public static RegionCounterResults countComponents(AttributeGrid grid, long material, int minSize) {
        return countComponents(grid, new AttributeTesterValue(material), minSize, -1, false, ConnectedComponent.DEFAULT_ALGORITHM);
    }

    public static RegionCounterResults countComponents(AttributeGrid grid, long material, int maxCount, boolean collectData) {

        return countComponents(grid, new AttributeTesterValue(material), maxCount, collectData, ConnectedComponent.DEFAULT_ALGORITHM);

    }

    public static RegionCounterResults countComponents(AttributeGrid grid, AttributeTester materialTester, int maxCount, boolean collectData, int algorithm) {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        GridBit mask = new GridBitIntervals(nx,ny, nz);

        int compCount = 0;
        int volume = 0;
        long largestVoxels = 0;

        if(DEBUG)printf("countComponents(tester: %s, maxCount:%d)\n", materialTester, maxCount);

        zcycle:

        for(int z = 0; z < nz; z++){
            for(int x = 0; x < nx; x++){
                for(int y = 0; y < ny; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;
                    if(materialTester.test(x,y,z,grid.getAttribute(x,y,z))){
                        
                        ConnectedComponent sc = new  ConnectedComponent(grid, mask, x,y,z, materialTester, collectData, algorithm);

                        compCount++;
                        if (DEBUG) printf("Component[%d] seed:(%d,%d,%d) voxels: %d volume: %4.4f cm^3\n", compCount, x,y,z, sc.getVolume(), sc.getVolume() * Math.pow(grid.getVoxelSize(),3) / Units.CM3);
                        volume+= sc.getVolume();
                        if (sc.getVolume() > largestVoxels) {
                            largestVoxels = sc.getVolume();
                        }
                        if(maxCount > 0 && compCount > maxCount)
                            break zcycle;
                    }
                }
            }
        }

        mask.release();

        RegionCounterResults ret_val = new RegionCounterResults();
        ret_val.numRegions = compCount;
        ret_val.totalVolume = volume;
        ret_val.maxedCount = (maxCount > 0 && compCount > maxCount);
        ret_val.largestRegionVoxels = largestVoxels;
        ret_val.largestRegionVolume = largestVoxels * Math.pow(grid.getVoxelSize(),3);
        ret_val.voxelSize = grid.getVoxelSize();

        return ret_val;
    }

    public static RegionCounterResults countComponents(AttributeGrid grid, AttributeTester tester, int minSize, int maxCount, boolean collectData, int algorithm) {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        GridBit mask = new GridBitIntervals(nx,ny, nz);

        int compCount = 0;
        int volume = 0;
        long largestVoxels = 0;

        if(DEBUG)printf("countComponents(material: %s, minSize: %d maxCount:%d)\n", tester, minSize, maxCount);

        zcycle:

        for(int z = 0; z < nz; z++){

            for(int x = 0; x < nx; x++){

                for(int y = 0; y < ny; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;

                    if(tester.test(x,y,z,grid.getAttribute(x,y,z))){
                        
                        ConnectedComponent sc = new  ConnectedComponent(grid, mask, x,y,z, tester, collectData, algorithm);

                        //printf("Component[%d] seed:(%d,%d,%d) volume: %d\n", (compCount++), x,y,z, sc.getVolume());
                        if (sc.getVolume() >= minSize) {
                            compCount++;
                            volume+= sc.getVolume();
                            if (sc.getVolume() > largestVoxels) {
                                largestVoxels = sc.getVolume();
                            }

                            if(maxCount > 0 && compCount > maxCount)
                                break zcycle;
                        }
                    }
                }
            }
        }

        mask.release();

        RegionCounterResults ret_val = new RegionCounterResults();
        ret_val.numRegions = compCount;
        ret_val.totalVolume = volume;
        ret_val.maxedCount = (maxCount > 0 && compCount > maxCount);
        ret_val.largestRegionVoxels = largestVoxels;
        ret_val.largestRegionVolume = largestVoxels * Math.pow(grid.getVoxelSize(),3);
        ret_val.voxelSize = grid.getVoxelSize();

        return ret_val;
    }

    /**
     components counting via various algoritms


     */
    public static RegionCounterResults countComponents(Grid grid, byte state) {
        return countComponents(grid, state, -1, false);
    }

    public static RegionCounterResults countComponents(Grid grid, byte state,int minSize) {
        return countComponents(grid, state, minSize, -1, false, ConnectedComponentState.DEFAULT_ALGORITHM);
    }

    public static RegionCounterResults countComponents(Grid grid, byte state, int maxCount, boolean collectData) {

        return countComponents(grid, state, maxCount, collectData,ConnectedComponentState.DEFAULT_ALGORITHM);

    }

    public static RegionCounterResults countComponents(Grid grid, byte state, int maxCount, boolean collectData, int algorithm) {

        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;

        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1);

        if(DEBUG)printf("countComponents(state: %d, macCount:%d)\n", state, maxCount);
        int compCount = 0;
        int volume = 0;
        long largestVoxels = 0;

        zcycle:

        for(int z = 1; z < nz1; z++){

            for(int x = 1; x < nx1; x++){

                for(int y = 1; y < ny1; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;


                    if(ConnectedComponentState.compareState(grid, x,y,z, state)){

                        ConnectedComponentState sc = new  ConnectedComponentState(grid, mask, x,y,z,state, collectData, algorithm);

                        volume+= sc.getVolume();
                        compCount++;
                        if (sc.getVolume() > largestVoxels) {
                            largestVoxels = sc.getVolume();
                        }

                        if(maxCount > 0 && compCount > maxCount)
                            break zcycle;
                    }
                }
            }
        }

        mask.release();

        RegionCounterResults ret_val = new RegionCounterResults();
        ret_val.numRegions = compCount;
        ret_val.totalVolume = volume;
        ret_val.maxedCount = (maxCount > 0 && compCount > maxCount);
        ret_val.largestRegionVoxels = largestVoxels;
        ret_val.largestRegionVolume = largestVoxels * Math.pow(grid.getVoxelSize(),3);
        ret_val.voxelSize = grid.getVoxelSize();

        return ret_val;
    }

    public static RegionCounterResults countComponents(Grid grid, byte state, int minSize, int maxCount, boolean collectData, int algorithm) {

        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;

        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1);

        if(DEBUG)printf("countComponents(state: %d, minSize: %d macCount:%d)\n", state, minSize, maxCount);
        int compCount = 0;
        int volume = 0;
        long largestVoxels = 0;

        zcycle:

        for(int z = 1; z < nz1; z++){

            for(int x = 1; x < nx1; x++){

                for(int y = 1; y < ny1; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;


                    if(ConnectedComponentState.compareState(grid, x,y,z, state)){

                        ConnectedComponentState sc = new  ConnectedComponentState(grid, mask, x,y,z,state, collectData, algorithm);

                        if (sc.getVolume() >= minSize) {
                            compCount++;
                            volume+= sc.getVolume();
                            if (sc.getVolume() > largestVoxels) {
                                largestVoxels = sc.getVolume();
                            }

                            if(maxCount > 0 && compCount > maxCount)
                                break zcycle;
                        }
                    }
                }
            }
        }

        mask.release();

        RegionCounterResults ret_val = new RegionCounterResults();
        ret_val.numRegions = compCount;
        ret_val.totalVolume = volume;
        ret_val.maxedCount = (maxCount > 0 && compCount > maxCount);
        ret_val.largestRegionVoxels = largestVoxels;
        ret_val.largestRegionVolume = largestVoxels * Math.pow(grid.getVoxelSize(),3);
        ret_val.voxelSize = grid.getVoxelSize();

        return ret_val;
    }

    /**
       removes components from grid of size smaller than minSize

       return total component count and small component count

     */
    public static Vector<ConnectedComponent> removeSmallComponents_old(AttributeGrid grid, long material, int minSize, int algorithm){


        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;
        Vector<ConnectedComponent> smallComponents = new Vector<ConnectedComponent>();
        Vector<ConnectedComponent> largeComponents = new Vector<ConnectedComponent>();

        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1, GridBitIntervals.ORIENTATION_Y);

        if(DEBUG)printf("removeSmallComponents(%d, %d)\n", material, minSize);

        int compCount = 0;
        int volume = 0;
        int smallVolume = 0;
        long maxRemovedVolume = 0;

    zcycle:

        for(int z = 1; z < nz1; z++){

            for(int x = 1; x < nx1; x++){

                for(int y = 1; y < ny1; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;
                    if(ConnectedComponent.compareMaterial(grid, x,y,z, material)){
                        ConnectedComponent sc = new  ConnectedComponent(grid, mask, x,y,z,material, false, algorithm);
                        //printf("Component[%d] seed:(%d,%d,%d) volume: %d\n", (compCount++), x,y,z, sc.getVolume());
                        if( sc.getVolume() < minSize){
                            smallComponents.add(sc);
                            smallVolume += sc.getVolume();
                            if(sc.getVolume() > maxRemovedVolume)
                                maxRemovedVolume = sc.getVolume();
                        } else {
                            largeComponents.add(sc);
                        }
                        compCount++;
                        volume += sc.getVolume();
                    }
                }
            }
        }

        if(DEBUG)printf("totalComponentsCount: %d, voxelCount: %d\n", compCount, volume);
        if(DEBUG)printf("smallComponentsCount: %d, voxelCount: %d maxRemovedVolume: %d\n", smallComponents.size(), smallVolume, maxRemovedVolume);

        if(smallComponents.size() == 0)
            return largeComponents;

        mask.clear();

        int voxel[] = new int[3];

        for(int i = 0; i < smallComponents.size(); i++){

            ConnectedComponent cc = smallComponents.get(i);
            ConnectedComponent sc = new ConnectedComponent(grid, mask, cc.seedX, cc.seedY, cc.seedZ, material, true, algorithm);
            long v = sc.getVolume();
            //printf("remove small component:");
            for(int j =0; j < v; j++){
                sc.getVoxelCoord(j, voxel);
                grid.setState(voxel[0],voxel[1],voxel[2],Grid.OUTSIDE);
            }
        }
        if(DEBUG)printf("removeSmallComponents(%d, %d) done\n", material, minSize);

        return largeComponents;

    }

    /**
       return connected  components with specified material and INSIDE voxels 
     */
    public static Vector<ConnectedComponent> findComponents(AttributeGrid grid, long material){

        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(),grid.getDepth(), GridBitIntervals.ORIENTATION_Y);
        ComponentsFinder cf = new ComponentsFinder(grid, mask, new AttributeTesterValue(material));
        grid.find(VoxelClasses.INSIDE, cf);
        cf.releaseReferences();
        return cf.getComponents();

    }

    /**
       return connected components with specific material tester 
    */
    public static Vector<ConnectedComponent> findComponents(AttributeGrid grid, AttributeTester materialTester){

        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(),grid.getDepth(), GridBitIntervals.ORIENTATION_Y);
        ComponentsFinder cf = new ComponentsFinder(grid, mask, materialTester);
        grid.find(VoxelClasses.ALL, cf);
        cf.releaseReferences();
        return cf.getComponents();

    }

    /**
     return components with specified material and INSIDE class
     */
    public static Vector<ConnectedComponentState> findComponents(Grid grid, byte state){

        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(),grid.getDepth(), GridBitIntervals.ORIENTATION_Y);
        ComponentsFinderState cf = new ComponentsFinderState(grid, mask, state);
        grid.find(VoxelClasses.INSIDE, cf);
        cf.releaseReferences();
        return cf.getComponents();

    }

    public static Vector<ConnectedComponent> removeSmallComponents(AttributeGrid grid, long material, int minSize){

        if(DEBUG)printf("removeSmallComponents(material:%d, minSize: %d)\n", material, minSize);

        int compCount = 0;
        int volume = 0;
        int smallVolume = 0;
        long maxRemovedVolume = 0;

        Vector<ConnectedComponent> components = findComponents(grid, material);

        Vector<ConnectedComponent> smallComp = new Vector<ConnectedComponent>();
        Vector<ConnectedComponent> largeComp = new Vector<ConnectedComponent>();

        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(), grid.getDepth(),
                                            GridBitIntervals.ORIENTATION_Y);
        int voxel[] = new int[3];

        for(int i =0; i <  components.size(); i++){

            ConnectedComponent c = components.get(i);
            long cv = c.getVolume();
            volume += cv;

            if( cv < minSize){

                smallComp.add(c);
                smallVolume += c.getVolume();

                if(cv > maxRemovedVolume)
                    maxRemovedVolume = cv;
                // remove small component
                ConnectedComponent sc = new ConnectedComponent(grid, mask, c.seedX, c.seedY, c.seedZ, material, true);
                for(int j = 0; j < cv; j++){
                    c.getVoxelCoord(j, voxel);
                    grid.setState(voxel[0],voxel[1],voxel[2],Grid.OUTSIDE);
                }

            } else {
                largeComp.add(c);
            }
        }

        if(DEBUG)printf("totalComponents count: %d, voxels: %d\n", components.size(), volume);
        if(DEBUG)printf("smallComponents count: %d, voxels: %d maxSize: %d\n", smallComp.size(), smallVolume, maxRemovedVolume);
        //printf("removeSmallComponents(%d, %d) done\n", material, minSize);

        return largeComp;
    }

    public static Vector<ConnectedComponentState> removeSmallComponents(Grid grid, byte state, int minSize){

        if(DEBUG)printf("removeSmallComponents(state:%d, minSize: %d)\n", state, minSize);

        int compCount = 0;
        int volume = 0;
        int smallVolume = 0;
        long maxRemovedVolume = 0;

        Vector<ConnectedComponentState> components = findComponents(grid, state);

        Vector<ConnectedComponentState> smallComp = new Vector<ConnectedComponentState>();
        Vector<ConnectedComponentState> largeComp = new Vector<ConnectedComponentState>();

        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(), grid.getDepth(),
                GridBitIntervals.ORIENTATION_Y);
        int voxel[] = new int[3];

        for(int i =0; i <  components.size(); i++){

            ConnectedComponentState c = components.get(i);
            long cv = c.getVolume();
            volume += cv;
            if( cv < minSize){

                smallComp.add(c);
                smallVolume += c.getVolume();

                if(cv > maxRemovedVolume)
                    maxRemovedVolume = cv;
                // remove small component
                ConnectedComponentState sc = new ConnectedComponentState(grid, mask, c.seedX, c.seedY, c.seedZ, state, true);
                for(int j = 0; j < cv; j++){
                    c.getVoxelCoord(j, voxel);
                    grid.setState(voxel[0],voxel[1],voxel[2],Grid.OUTSIDE);
                }

            } else {
                largeComp.add(c);
            }
        }

        if(DEBUG)printf("totalComponents count: %d, voxels: %d\n", components.size(), volume);
        if(DEBUG)printf("smallComponents count: %d, voxels: %d maxSize: %d\n", smallComp.size(), smallVolume, maxRemovedVolume);
        //printf("removeSmallComponents(%d, %d) done\n", material, minSize);

        return largeComp;
    }

    public static List<int[]> getComponentBoundsByVolume(AttributeGrid grid, long material, int maxCount, int minSize, boolean collectData) {
        return getComponentBoundsByVolume(grid, material, maxCount, minSize, collectData, ConnectedComponent.DEFAULT_ALGORITHM);
    }

    /**
     * Get bounds of component regions by material and sorted by largest first.
     *
     * @param grid The grid
     * @param material The material
     * @param maxCount The max number of components to get
     * @param collectData Whether to get the components
     * @param algorithm The algorithm to use
     * @return
     */
    public static List<int[]> getComponentBoundsByVolume(AttributeGrid grid, long material, int maxCount,
            int minSize, boolean collectData, int algorithm) {

        Vector<ConnectedComponent> components = findComponents(grid, material);

        Collections.sort(components);

        ArrayList<int[]> boundsList = new ArrayList<int[]>();

        int[] min = new int[3];
        int[] max = new int[3];

        int compSize = components.size();
        int compCount = maxCount >= compSize ? compSize : maxCount;
        int lastCompIndex = maxCount >= compSize ? 0 : compSize - compCount;

        for (int i=compSize-1; i>=lastCompIndex; i--) {
            if (minSize > 0 && components.get(i).getVolume() < minSize)
                break;

            components.get(i).getExtents(min, max);
//          System.out.println("==> mat min: " + java.util.Arrays.toString(min));
//          System.out.println("==> mat max: " + java.util.Arrays.toString(max));
//          System.out.println("==> mat vol: " + components.get(i).getVolume());
            boundsList.add(new int[] {min[0], min[1], min[2], max[0], max[1], max[2]});
        }

        return boundsList;
/*
        if (maxCount > components.size()) {
            return components;
        } else {
            Vector<ConnectedComponent> largest = new Vector<ConnectedComponent>();

            for (int i=0; i<maxCount; i++) {
                largest.add(components.get(i));
            }

            return largest;
        }
*/
    }

    public static List<int[]> getComponentBoundsByVolume(Grid grid, byte state, int maxCount, int minSize, boolean collectData) {
        return getComponentBoundsByVolume(grid, state, maxCount, minSize, collectData, ConnectedComponent.DEFAULT_ALGORITHM);
    }

    /**
     * Get bounds of component regions by state and sorted by largest first.
     *
     * @param grid The grid
     * @param state The state
     * @param maxCount The max number of components to get
     * @param collectData Whether to get the components
     * @param algorithm The algorithm to use
     * @return
     */
    public static List<int[]> getComponentBoundsByVolume(Grid grid, byte state, int maxCount,
            int minSize, boolean collectData, int algorithm) {

        Vector<ConnectedComponentState> components = findComponents(grid, state);

        Collections.sort(components);

        ArrayList<int[]> boundsList = new ArrayList<int[]>();

        int[] min = new int[3];
        int[] max = new int[3];

        int compSize = components.size();
        int compCount = maxCount >= compSize ? compSize : maxCount;
        int lastCompIndex = maxCount >= compSize ? 0 : compSize - compCount;

        for (int i=compSize-1; i>=lastCompIndex; i--) {
            if (minSize > 0 && components.get(i).getVolume() < minSize)
                break;

            components.get(i).getExtents(min, max);
//          System.out.println("==> state min: " + java.util.Arrays.toString(min));
//          System.out.println("==> state max: " + java.util.Arrays.toString(max));
//          System.out.println("==> state vol: " + components.get(i).getVolume());
            boundsList.add(new int[] {min[0], min[1], min[2], max[0], max[1], max[2]});
        }

        return boundsList;
    }

    static class ComponentsFinder implements ClassTraverser {

        AttributeGrid grid;
        GridBit mask;
        AttributeTester materialTester;
        Vector<ConnectedComponent> components;

        ComponentsFinder(AttributeGrid grid, GridBit mask, AttributeTester materialTester){

            this.grid = grid;
            this.mask = mask;
            this.materialTester = materialTester;
            this.components = new Vector<ConnectedComponent>();

        }

        public void releaseReferences(){
            this.grid = null;
            this.mask.release();
            this.mask = null;
        }

        public void found(int x, int y, int z, byte _state){

            if(mask.get(x,y,z) != 0)
                return;

            if(materialTester.test(x,y,z,grid.getAttribute(x,y,z))){
                ConnectedComponent cc = new ConnectedComponent(grid, mask, x,y,z, materialTester, true);
                components.add(cc);
            }

        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){

            if(mask.get(x,y,z) != 0)
                return true;

            if(materialTester.test(x,y,z,grid.getAttribute(x,y,z))){
                ConnectedComponent cc = new ConnectedComponent(grid, mask, x,y,z, materialTester, true);
                components.add(cc);
            }
            return true;
        }

        public Vector<ConnectedComponent> getComponents(){
            return components;
        }
    }

    //
    // componet finder for specific state 
    // 
    static class ComponentsFinderState implements ClassTraverser {

        Grid grid;
        GridBit mask;
        byte state;
        Vector<ConnectedComponentState> components;

        ComponentsFinderState(Grid grid, GridBit mask, byte state){

            this.grid = grid;
            this.mask = mask;
            this.state = state;
            this.components = new Vector<ConnectedComponentState>();

        }

        public void releaseReferences(){
            this.grid = null;
            this.mask.release();
            this.mask = null;
        }

        public void found(int x, int y, int z, byte _state){

            if(mask.get(x,y,z) != 0)
                return;

            if(grid.getState(x,y,z) == state){
                ConnectedComponentState cc = new ConnectedComponentState(grid, mask, x,y,z, state, true);
                components.add(cc);
            }
        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){

            if(mask.get(x,y,z) != 0)
                return true;

            if(grid.getState(x,y,z) == state){
                ConnectedComponentState cc = new ConnectedComponentState(grid, mask, x,y,z, state, true);
                components.add(cc);
            }
            return true;
        }

        public Vector<ConnectedComponentState> getComponents(){
            return components;
        }
    }

}

