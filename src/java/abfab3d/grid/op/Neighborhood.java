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
package abfab3d.grid.op;


public class Neighborhood {

    /**
     returns array of neighbors of a point in a ball or radius @radius
     radius is expressed in voxels
     */
    public static int[] makeBall(double radius){

        double EPS = 1.e-10;
        
        double radius2 = (radius*radius+EPS); // compare against radius squared
        int iradius = (int)(radius+1);

        // calculate size needed 
        int count = 0;
        for(int y = -iradius; y <= iradius; y++){
            for(int x = -iradius; x <= iradius; x++){
                for(int z = -iradius; z <= iradius; z++){
                //for(int z = 0; z <= 0; z++){
                    double d2 = x*x + y*y + z*z;
                    if(d2 <= radius2)
                        count += 3;
                }
            }
        }
        int neig[] = new int[count];

        // store data in array
        count = 0;
        for(int y = -iradius; y <= iradius; y++){
            for(int x = -iradius; x <= iradius; x++){
                for(int z = -iradius; z <= iradius; z++){
                    //for(int z = 0; z <= 0; z++){
                    double d2 = x*x + y*y + z*z;
                    if(d2 <= radius2){
                        neig[count] = -x;
                        neig[count+1] = -y;
                        neig[count+2] = -z;
                        count += 3;
                    }
                }
            }
        }
        return neig;
    }

    /*
      these are first few ball neighborhood 
       pnt     radius  cnt diff                 
       0 0 0 :  0.00    1
       0 0 1 :  1.00    7  6
       0 1 1 :  1.41   19  12
       1 1 1 :  1.73   27  8
       0 0 2 :  2.00   33  6
       0 1 2 :  2.24   57  24
       1 1 2 :  2.45   81  24
       0 2 2 :  2.83   93  12
       1 2 2 :  3.00  123  30
       0 0 3 :  3.00  123  
       0 1 3 :  3.16  147  24 
       1 1 3 :  3.32  171  24
       2 2 2 :  3.46  179  8 
       0 2 3 :  3.61  203  24
       1 2 3 :  3.74  251  48
       0 0 4 :  4.00  257  6

    */

}