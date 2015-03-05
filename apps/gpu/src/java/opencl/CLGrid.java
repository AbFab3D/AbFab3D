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

import abfab3d.grid.Bounds;
import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.IntParameter;

import abfab3d.datasources.DataSourceGrid;
import abfab3d.datasources.LinearMapper;

import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

/**
   CL code generatror for box
   @author Vladimir Bulatov 
 */
public class CLGrid  extends CLNodeBase {

    static final boolean DEBUG = true;
    static int OPCODE = Opcodes.oGRID3DBYTE;
    /*
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode 
    // custom parameters
    // coefficients to calculate data value
    float valueOffset; // value = byteValue*valueFactor + valueOffset;
    float valueFactor; 

    int nx; // grid count in x direction
    int ny; // grid count in y direction
    int nz; // grid count in y direction

    float3 center;  // center in world units

    float3 halfsize; // size in world units

    float3 origin; // location of bottom left corner
    
    float scale; // world->grid 
    float outsideValue; // value to use outside of grid 
    int dataOffset; // location of data in the data buffer 
    PTRDATA char *pData; // actual grid data 
} sGrid3dByte;
    */

    static int STRUCTSIZE = 24;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        if(DEBUG) printf("%s.getCLCode()\n",this);

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        DataSourceGrid grid = (DataSourceGrid)node;
        
        Bounds bounds = grid.getGridBounds();
        int nx = grid.getGridWidth();
        int ny = grid.getGridHeight();
        int nz = grid.getGridDepth();
        Vector3d center = bounds.getCenter();
        Vector3d size = bounds.getSize();

        byte data[] = new byte[nx*ny*nz];
        long t0;
        if(DEBUG) t0 = time();
        if(DEBUG) printf("getting grid data\n");
        grid.getGridData(data);
        if(DEBUG) printf("getting grid data took %d ms\n", (time() - t0));
                
        int dataOffset = codeBuffer.addData(data);

        LinearMapper mapper = grid.getMapper();
        double vmin = mapper.getVmin();
        double vmax = mapper.getVmax();

        // CL code should maps (0,255) into (vmin, vmax);
        // value = byteValue*valueFactor + valueOffset;
        
        double valueOffset = vmin;
        double valueFactor = (vmax - vmin)/255.;
        //TODO - take outside values from DataSource 
        double outsideValue = vmax; // this makes sense only for distance grid
        if(DEBUG) printf("CLGrid [%d %d %d]valueOffset: %9.6f, valueFactor: %9.6f \n", nx, ny, nz, valueOffset, valueFactor);
        if(false) {
            int z = nz/2;
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    byte d = data[x + y*nx + z*nx*ny];
                    double v = (((int)d)&0xFF) * (vmax - vmin)/255 + vmin;
                    
                    //if((x > nx/4 && x < nx/2) && (y < ny/2))printf("%2x ", ((int)d)&0xFF);
                    if((x > nx/4 && x < nx/2) && (y < ny/2)) printf("%4.2f ", v*1000);
                }
                printf("\n");
            }
        }
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(valueOffset);  
        buffer[c++] = floatToInt(valueFactor); 
        buffer[c++] = nx; 
        buffer[c++] = ny; 
        buffer[c++] = nz; 
        buffer[c++] = 0; // alignment
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0; // alignment
        buffer[c++] = floatToInt(size.x/2.);
        buffer[c++] = floatToInt(size.y/2.);
        buffer[c++] = floatToInt(size.z/2.);
        buffer[c++] = 0;// alignment
        buffer[c++] = floatToInt(center.x-size.x/2.);
        buffer[c++] = floatToInt(center.y-size.y/2.);
        buffer[c++] = floatToInt(center.z-size.z/2.);
        buffer[c++] = 0;// alignment
        buffer[c++] = floatToInt(nx/size.x); //1./pixelSize 
        buffer[c++] = floatToInt(outsideValue);
        buffer[c++] = dataOffset; // data offset 

        codeBuffer.add(buffer, STRUCTSIZE);
        wcount += STRUCTSIZE;

        wcount +=  super.getMaterialCLCode(node,codeBuffer);
        
        return wcount;
    }
}