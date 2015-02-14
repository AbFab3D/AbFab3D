package opencl;


public class Opcodes{
    
    //
    // these codes have to be identical to codes usen in CL code 
    //
    public static final int 
        oSPHERE = 1001,
        oGYROID = 1002,
        oBOX = 1003,
        oTORUS = 1004,
        oMAX = 1005,
        oMIN = 1006,
        oBLEND  = 1007,
        oBLENDMAX = 1008,
        oBLENDMIN = 1009,
        oSUBTRACT = 1010,
        oBLENDSUBTRACT = 1011,
    // registers operations 
        oCOPY_D1D2 = 1012,
        oCOPY_D2D1 = 1013,
    // stack operations 
        oPUSH_D2 = 1014,  // save D2 to stack      
        oPOP_D2 = 1015;   // restrore D2 fom stack      
    
}
