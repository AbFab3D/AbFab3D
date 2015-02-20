/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package opencl;

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;

import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Output.printf;

/**
   CL code generator for rotation
   @author Vladimir Bulatov 
 */
public class CLRotation implements CLCodeGenerator {

    static final boolean DEBUG = true;

    static int OPCODE = Opcodes.oROTATION;
    /*
    typedef struct {
        int size;  // size of struct in words 
        int opcode; // opcode to perform 
        // custom parameters of DataSource 
        float3 center; 
        float3 m0; 
        float3 m1; 
        float3 m2; 
    } sRotation;    
     */

    static int STRUCTSIZE = 20;
    

    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {
        
        if(DEBUG)printf("CLRotation(%s)\n", node);
        AxisAngle4d aa = (AxisAngle4d)node.getParam("rotation").getValue();
        Vector3d center = (Vector3d)node.getParam("center").getValue();

        if(DEBUG)printf("rotation([%7.5f,%7.5f,%7.5f,%7.5f][%7.5f,%7.5f,%7.5f,])\n", aa.x,aa.y,aa.z,aa.angle, center.x, center.y, center.z);
        
        Matrix3d mat = new Matrix3d();
        mat.set(new AxisAngle4d(aa.x,aa.y,aa.z,-aa.angle));

        if(DEBUG)printf("mat([%7.5f,%7.5f,%7.5f][%7.5f,%7.5f,%7.5f][%7.5f,%7.5f,%7.5f])\n", mat.m00,mat.m01,mat.m02,mat.m10,mat.m11,mat.m12,mat.m20,mat.m21,mat.m22);
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        c += 2;  // align to 4 words boundary 
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0;
        buffer[c++] = floatToInt(mat.m00);
        buffer[c++] = floatToInt(mat.m01);
        buffer[c++] = floatToInt(mat.m02);
        buffer[c++] = 0;
        buffer[c++] = floatToInt(mat.m10);
        buffer[c++] = floatToInt(mat.m11);
        buffer[c++] = floatToInt(mat.m12);
        buffer[c++] = 0;
        buffer[c++] = floatToInt(mat.m20);
        buffer[c++] = floatToInt(mat.m21);
        buffer[c++] = floatToInt(mat.m22);
        buffer[c++] = 0;


        codeBuffer.add(buffer, STRUCTSIZE);
        return STRUCTSIZE;
    }
}