package opencl;


import java.util.HashMap;

public class Opcodes {
    public static final HashMap<Integer, String> codes;

    //
    // these codes have to be identical to codes usen in CL code 
    //
    public static final int
            oSPHERE = 1,
            oGYROID = 2,
            oBOX = 3,
            oTORUS = 4,
            oMAX = 5,
            oMIN = 6,
            oBLEND = 7,
            oBLENDMAX = 8,
            oBLENDMIN = 9,
            oSUBTRACT = 10,
            oBLENDSUBTRACT = 11,
            // registers operations
            oCOPY_D1D2 = 12,
            oCOPY_D2D1 = 13,
            // stack operations
            oPUSH_D2 = 14,  // save D2 to stack
            oPOP_D2 = 15;   // restore D2 fom stack

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
    }

    public static String getText(int code) {
        String st = codes.get(code);

        if (st == null) {
            return "Unknown code: " + code;
        }

        return codes.get(code);
    }
}
