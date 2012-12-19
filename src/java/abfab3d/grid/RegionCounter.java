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

        return countComponents(grid, material, maxCount, collectData);

    }

    public static int countComponents(AttributeGrid grid, int material, int maxCount, boolean collectData, int algorithm) {

        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;
        
        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1);
        
        printf("countComponents(%d, %d)\n", material, maxCount);
        int compCount = 0;
        int volume = 0;

    zcycle:

        for(int z = 1; z < nz1; z++){

            for(int x = 1; x < nx1; x++){
                
                for(int y = 1; y < ny1; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;

                    if(ConnectedComponent.compareMaterial(grid, x,y,z, material)){
                        
                        ConnectedComponent sc = new  ConnectedComponent(grid, mask, x,y,z,material, collectData, algorithm);

                        printf("Component[%d] seed:(%d,%d,%d) volume: %d\n", (compCount++), x,y,z, sc.getVolume());
                        volume+= sc.getVolume();
                        if(maxCount > 0 && compCount > maxCount)
                            break zcycle;                            
                    }                                            
                }
            }
        }

        mask.release();

        printf("components_count: %d, total volume: %d\n", compCount, volume);
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
        grid.findInterruptible(Grid.VoxelClasses.MARKED, cf);
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

            foundInterruptible(x,y,z,_state);

        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){
            
            if(mask.get(x,y,z) != 0)
                return true;

            if(grid.getAttribute(x,y,z) == material){
                ConnectedComponent cc = new ConnectedComponent(grid, mask, x,y,z, material, false);
                components.add(cc);
            } 
            return true; 
        }        

        public Vector<ConnectedComponent> getComponents(){
            return components; 
        }
    }    
}
