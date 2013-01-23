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

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayDeque;
import java.util.Vector;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.ClassTraverser;

import abfab3d.grid.Region;

import static abfab3d.util.Output.printf;


/**
   class to count specific regions 

   @author Vladimir Bulatov
 */
public class RegionCounter {



    /**
       components counting via various algoritms 
       
       
     */
    public static int countComponents(AttributeGrid grid, int material) {
        return countComponents(grid, material, -1, false);
    }

    public static int countComponents(AttributeGrid grid, int material, int maxCount, boolean collectData) {

        return countComponents(grid, material, maxCount, collectData, ConnectedComponent.DEFAULT_ALGORITHM);

    }

    public static int countComponents(AttributeGrid grid, int material, int maxCount, boolean collectData, int algorithm) {

        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;
        
        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1);
        
        int compCount = 0;
        int volume = 0;

        printf("countComponents(material: %d, maxCount:%d)\n", material, maxCount);

        zcycle:

        for(int z = 1; z < nz1; z++){

            for(int x = 1; x < nx1; x++){
                
                for(int y = 1; y < ny1; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;

                    if(ConnectedComponent.compareMaterial(grid, x,y,z, material)){
                        
                        ConnectedComponent sc = new  ConnectedComponent(grid, mask, x,y,z,material, collectData, algorithm);

                        //printf("Component[%d] seed:(%d,%d,%d) volume: %d\n", (compCount++), x,y,z, sc.getVolume());
                        volume+= sc.getVolume();
                        compCount++;
                        if(maxCount > 0 && compCount > maxCount)
                            break zcycle;                            
                    }                                            
                }
            }
        }

        mask.release();

        return compCount;
    }

    /**
     components counting via various algoritms


     */
    public static int countComponents(Grid grid, byte state) {
        return countComponents(grid, state, -1, false);
    }

    public static int countComponents(Grid grid, byte state, int maxCount, boolean collectData) {

        return countComponents(grid, state, maxCount, collectData,ConnectedComponentState.DEFAULT_ALGORITHM);

    }

    public static int countComponents(Grid grid, byte state, int maxCount, boolean collectData, int algorithm) {

        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;

        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1);

        printf("countComponents(state: %d, macCount:%d)\n", state, maxCount);
        int compCount = 0;
        int volume = 0;

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
                        if(maxCount > 0 && compCount > maxCount)
                            break zcycle;
                    }
                }
            }
        }

        mask.release();

        return compCount;
    }


    /**
       removes components from grid of size smaller than minSize

       return total component count and small component count 
       
     */
    public static Vector<ConnectedComponent> removeSmallComponents_old(AttributeGrid grid, int material, int minSize, int algorithm){

        
        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;
        Vector<ConnectedComponent> smallComponents = new Vector<ConnectedComponent>();
        Vector<ConnectedComponent> largeComponents = new Vector<ConnectedComponent>();

        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1, GridBitIntervals.ORIENTATION_Y);
        
        printf("removeSmallComponents(%d, %d)\n", material, minSize);

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
        
        printf("totalComponentsCount: %d, voxelCount: %d\n", compCount, volume);
        printf("smallComponentsCount: %d, voxelCount: %d maxRemovedVolume: %d\n", smallComponents.size(), smallVolume, maxRemovedVolume);
        
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
        printf("removeSmallComponents(%d, %d) done\n", material, minSize);
        
        return largeComponents;        
        
    }
    
    /**
       return components with specified material and INTERIOR class 
     */
    public static Vector<ConnectedComponent> findComponents(AttributeGrid grid, int material){
        
        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(),grid.getDepth(), GridBitIntervals.ORIENTATION_Y);
        ComponentsFinder cf = new ComponentsFinder(grid, mask, material);
        grid.find(Grid.VoxelClasses.MARKED, cf);
        cf.releaseReferences();        
        return cf.getComponents();
        
    }

    /**
     return components with specified material and INTERIOR class
     */
    public static Vector<ConnectedComponentState> findComponents(Grid grid, byte state){

        GridBit mask = new GridBitIntervals(grid.getWidth(),grid.getHeight(),grid.getDepth(), GridBitIntervals.ORIENTATION_Y);
        ComponentsFinderState cf = new ComponentsFinderState(grid, mask, state);
        grid.find(Grid.VoxelClasses.MARKED, cf);
        cf.releaseReferences();
        return cf.getComponents();

    }

    public static Vector<ConnectedComponent> removeSmallComponents(AttributeGrid grid, int material, int minSize){
        
        printf("removeSmallComponents(material:%d, minSize: %d)\n", material, minSize);

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

        printf("totalComponents count: %d, voxels: %d\n", components.size(), volume);
        printf("smallComponents count: %d, voxels: %d maxSize: %d\n", smallComp.size(), smallVolume, maxRemovedVolume);
        //printf("removeSmallComponents(%d, %d) done\n", material, minSize);
        
        return largeComp;        
    }

    public static Vector<ConnectedComponentState> removeSmallComponents(Grid grid, byte state, int minSize){

        printf("removeSmallComponents(state:%d, minSize: %d)\n", state, minSize);

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

        printf("totalComponents count: %d, voxels: %d\n", components.size(), volume);
        printf("smallComponents count: %d, voxels: %d maxSize: %d\n", smallComp.size(), smallVolume, maxRemovedVolume);
        //printf("removeSmallComponents(%d, %d) done\n", material, minSize);

        return largeComp;
    }

    static class ComponentsFinder implements ClassTraverser {

        AttributeGrid grid;
        GridBit mask;
        int material;
        Vector<ConnectedComponent> components;

        ComponentsFinder(AttributeGrid grid, GridBit mask, int material){

            this.grid = grid;
            this.mask = mask;
            this.material = material;
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

            if(grid.getAttribute(x,y,z) == material){
                ConnectedComponent cc = new ConnectedComponent(grid, mask, x,y,z, material, true);
                components.add(cc);
            }

        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){
            
            if(mask.get(x,y,z) != 0)
                return true;

            if(grid.getAttribute(x,y,z) == material){
                ConnectedComponent cc = new ConnectedComponent(grid, mask, x,y,z, material, true);
                components.add(cc);
            } 
            return true; 
        }        

        public Vector<ConnectedComponent> getComponents(){
            return components; 
        }
    }

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
