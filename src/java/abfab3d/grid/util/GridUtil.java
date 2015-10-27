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

 package abfab3d.grid.util;

// External Imports
import java.util.*;

// Internal Imports
import abfab3d.grid.*;

 public class GridUtil  {

     public static void fill(Grid2D grid, long attribute){
         
         int nx = grid.getWidth();
         int ny = grid.getHeight();
         for(int y = 0; y < ny; y++){
             for(int x = 0; x < nx; x++){
                 grid.setAttribute(x,y,attribute);
             }
         }         
     }

     public static void fill(AttributeGrid grid, long attribute){
         
         int nx = grid.getWidth();
         int ny = grid.getHeight();
         int nz = grid.getDepth();
         for(int y = 0; y < ny; y++){
             for(int x = 0; x < nx; x++){
                 for(int z= 0; z < nz; z++){
                     grid.setAttribute(x,y,z,attribute);
                 }
             }
         }         
     }
 }