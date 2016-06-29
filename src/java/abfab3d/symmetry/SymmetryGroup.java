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

import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;


import static abfab3d.core.Output.printf;

import abfab3d.core.Vec;
import abfab3d.core.ResultCodes;

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
    PairingTransform m_transforms[];
    ETransform m_etransforms[];    // euclidean transforms 
    //ITransform m_itransforms[]; // inversive transforms
    
    int m_maxIterations = 20;
    
    public SymmetryGroup(SPlane splanes[]){
        m_type = TYPE_REFLECTIONS;
        m_splanes = new SPlane[splanes.length];
        System.arraycopy(splanes, 0, m_splanes, 0, splanes.length);
        
    }
    public SymmetryGroup(Vector4d planes[], Matrix4d transforms[]){
        m_type = TYPE_EUCLIDEAN;
        
        m_splanes = new SPlane[planes.length];
        m_etransforms = new ETransform[planes.length];
        for(int i = 0; i < planes.length; i++){
            m_splanes[i] = new EPlane(planes[i]);
            m_etransforms[i] = new ETransform(transforms[i]);
        }
        // general transform are ETransform
        m_transforms = m_transforms;
        
    }

    public SymmetryGroup(SPlane planes[], ETransform transforms[]){
        m_type = TYPE_EUCLIDEAN;
        m_splanes = planes;
        m_transforms = transforms;
        m_etransforms = transforms;
        
    }

    public int getType(){
        return m_type;
    }

    public SPlane[] getFundamentalDomain(){
        return m_splanes;
    }

    public ETransform[] getETransforms(){
        return m_etransforms;
    }

    public void setMaxIterations(int value){

        m_maxIterations = value;
        
    }
    public int getMaxIterations(){

        return m_maxIterations;
        
    }

    public int toFD(Vec pnt){

        switch(m_type){
        default:
        case TYPE_REFLECTIONS:
            return toFundamentalDomain(pnt, m_splanes, m_maxIterations);
        case TYPE_EUCLIDEAN:
            return toFundamentalDomain(pnt, m_splanes, m_transforms, m_maxIterations);
            
        }
    }
    
 
}
