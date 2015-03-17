package opencl;


import java.util.HashMap;

import static abfab3d.util.Output.fmt;


/**
   codes for OpenCL engine operations 

   operations are performed without recursive calls
   intermediate resultds are stored on stack 
   
   the code engine has fixed registers 

   shapes are calculated on fixed registers (P1 D1)  point is stored in P1, result is returned in D1 
   transforms are calculated on fixed register (P1)
   boolean operations are calculated on fixed registers (D2, D1, D2) input data are stored in (D2, D1) result is returned in D2

   P1 - current point value 
   D1 - current data value 
   D2 - intermediate data value 

   P1 and D2 can be saved on stack of some fixed size 
   
 */
public class Opcodes {
    public static final HashMap<Integer, String> codes;

    //
    // these codes have to be identical to codes in strc/opencl/SHapeJS_opcode_v3_dist.cl
    //
    public static final int
        oSPHERE          =  1,
        oGYROID          =  2,
        oBOX             =  3,
        oTORUS           =  4,
        oMAX             =  5,
        oMIN             =  6,
        oBLEND           =  7, 
        oBLENDMAX        =  8,
        oBLENDMIN        =  9,
        oSUBTRACT        = 10,
        oBLENDSUBTRACT   = 11,
    // registers operations
        oCOPY_D1D2 =       12,
        oCOPY_D2D1 =       13,
    // stack operations
        oPUSH_D2         = 14,  // save D2 to stack
        oPOP_D2          = 15,   // restore D2 fom stack
        oPUSH_P1         = 16,  // save P1 to stack  
        oPOP_P1          = 17,   // restore P1 from stack 

    // transformations 
        oTRANSLATION     = 18, 
        oROTATION        = 19,
        oSCALE           = 20,
    // 
        oENGRAVE         = 21,
        oGRID2DBYTE      = 22,
        oGRID3DBYTE      = 23,
        oIMAGEBOX        = 24,
        oREFLECT         = 25,

    // end of operations
        oEND = 0; 
        
    static {
        codes = new HashMap<Integer, String>();
        codes.put(oSPHERE, "Sphere");
        codes.put(oGYROID, "Gyroid");
        codes.put(oBOX, "Box");
        codes.put(oTORUS, "Torus");
        codes.put(oMAX, "Max");
        codes.put(oMIN, "Min");
        codes.put(oBLEND, "Blend");
        codes.put(oBLENDMAX, "BlendMax");
        codes.put(oBLENDMIN, "BlendMin");
        codes.put(oSUBTRACT, "Subtract");
        codes.put(oBLENDSUBTRACT, "BlendSubtract");
        codes.put(oCOPY_D1D2, "CopyD1D2");
        codes.put(oCOPY_D2D1, "CopyD2D1");
        codes.put(oPUSH_D2, "PushD2");
        codes.put(oPOP_D2, "PopD2");
        codes.put(oPUSH_P1, "PushP1");
        codes.put(oPOP_P1, "PopP1");
        codes.put(oTRANSLATION, "Translation");
        codes.put(oROTATION, "Rotation");
        codes.put(oSCALE, "Scale");
        codes.put(oENGRAVE, "Engrave");
        codes.put(oGRID2DBYTE, "Grid2dByte");        
        codes.put(oGRID3DBYTE, "Grid3dByte");        
        codes.put(oIMAGEBOX, "ImageBox");        
        codes.put(oREFLECT, "Reflect");        
    }

    public static String getText(int code) {

        String st = codes.get(code);

        if (st == null) {
            return fmt("UNKNOW CODE:%d",code);
        }

        return st;
    }
}
