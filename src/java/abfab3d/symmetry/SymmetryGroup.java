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

package abfab3d.symmetry;

import javax.vecmath.Vector3d;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import abfab3d.util.Vec;
import abfab3d.util.ResultCodes;

import static abfab3d.symmetry.SymmetryUtil.toFundamentalDomain;

/**
   class to support calculations with symmetry group in inversive geometry 
   transformation in inversion geometry are compositions of reflectons in planes and inversions is spheres

   group is defined by fundamental domain (FD) and pairing transformations (PT)
   FD is 3D polyhedron bounded by finite number of splanes (spheres or planes)  
   Fundametal Domain may be finite or infinite
   transformed copies of FD tile the whole 3D space
   Pairiong transformation PT is trANSand pairing transformations whcih transform FD into adjacent tile
   reflections include plane reflections and sphere inversions 


   @author Vladimir Bulatov
 */
public class SymmetryGroup {
    
    static final boolean DEBUG = false;
    static int debugCount = 1000;
    
    public static final int RESULT_OK = ResultCodes.RESULT_OK;
    public static final int RESULT_ERROR = ResultCodes.RESULT_ERROR;
    public static final int RESULT_OUTSIDE = ResultCodes.RESULT_OUTSIDE;
    
    public static final int TYPE_REFLECTIONS = 0;
    public static final int TYPE_EUCLIDEAN = 1;
    public static final int TYPE_INVERSIVE = 2;
    
    
    int m_type = TYPE_REFLECTIONS;
    
    SPlane m_splanes[];
    
    int m_maxIterations = 100;
    
    public SymmetryGroup(SPlane splanes[]){
        m_type = TYPE_REFLECTIONS;
        m_splanes = new SPlane[splanes.length];
        System.arraycopy(splanes, 0, m_splanes, 0, splanes.length);
        
    }
   
    public SPlane[] getSPlanes(){
        return m_splanes;
    }

    public void setMaxIterations(int value){

        m_maxIterations = value;
        
    }

    public int toFD(Vec pnt){
        switch(m_type){
        default:
        case TYPE_REFLECTIONS:
            //return toFundamentalDomain(pnt, m_splanes, m_maxIterations);
            return toFundamentalDomain(pnt, m_splanes, m_splanes, m_maxIterations);
            
        }
    }
    
 
}
