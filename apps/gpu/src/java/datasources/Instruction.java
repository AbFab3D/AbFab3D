package datasources;

import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OpenCL instruction
 *
 * @author Alan Hudson
 */
public class Instruction {
    private static final int CHUNK_SIZE = 5;


    public final static int 
        oSPHERE = 0,
        oBOX = 1,
        oGYROID = 2,
        oINTERSECTION = 3,
        oUNION = 4,
        oTORUS = 5,
        oINTERSECTION_START = 6,
        oINTERSECTION_MID = 7,
        oINTERSECTION_END = 8,
        oUNION_START = 9,
        oUNION_MID = 10,
        oUNION_END = 11,
        oSUBTRACTION_START = 12,
        oSUBTRACTION_END = 13,
        oRESET = 1000,
        oSCALE = 1001,       
        oTRANSLATION = 1002,
        oROTATION = 1003;

    private int op;
    
    /** Float Params */
    private int fcount;
    private float[] fparams;
    
    /** Integer Params */
    private int icount;
    private int[] iparams;
    
    /** Float Vector Params */
    private int fvcount;
    private float[] fvparams;
    
    /** Boolean Params */
    private int bcount;
    private boolean[] bparams;
    
    /** Matrix Params */
    private int mcount;
    private float[] mparams;
    
    private ParameterType types[];
    private int tcount;

    private static final HashMap<String, Integer> opcodes;

    static {
        opcodes=new HashMap<String, Integer>();
        opcodes.put("sphere",oSPHERE);
        opcodes.put("box",oBOX);
        opcodes.put("gyroid",oGYROID);
        opcodes.put("intersection",oINTERSECTION);
        opcodes.put("union",oUNION);
        opcodes.put("torus",oTORUS);
        opcodes.put("intersectionStart",oINTERSECTION_START);
        opcodes.put("intersectionMid",oINTERSECTION_MID);
        opcodes.put("intersectionEnd",oINTERSECTION_END);
        opcodes.put("unionStart",oUNION_START);
        opcodes.put("unionMid",oUNION_MID);
        opcodes.put("unionEnd",oUNION_END);
        opcodes.put("subtractionStart",oSUBTRACTION_START);
        opcodes.put("subtractionEnd",oSUBTRACTION_END);

        opcodes.put("reset",oRESET);
        opcodes.put("scale",oSCALE);
        opcodes.put("translation",oTRANSLATION);
        opcodes.put("rotation",oROTATION);
    }


    public Instruction() {
        fcount = 0;
        icount = 0;
        fvcount = 0;
        bcount = 0;

        tcount = 0;
        types = new ParameterType[CHUNK_SIZE];
    }

    public Instruction(String op) {
        this();

        setOpCode(getOpCode(op));
    }

    public void setOpCode(int code) {
        op = code;
    }

    public void addFloat(float f) {
        if (fparams == null) {
            fparams = new float[CHUNK_SIZE];
        } else if (fcount == fparams.length) {
            float[] na = new float[fparams.length + CHUNK_SIZE];
            for(int i=0; i < fparams.length; i++) {
                na[i] = fparams[i];
            }

            fparams = na;
        }

        fparams[fcount++] = f;

        addType(ParameterType.FLOAT);
    }
    public void addFloatVector3(float[] f) {
        if (fvparams == null) {
            fvparams = new float[CHUNK_SIZE * 3];
        } else if (fvcount * 3 == fvparams.length) {
            float[] na = new float[fvparams.length + CHUNK_SIZE * 3];
            for(int i=0; i < fvparams.length; i++) {
                na[i] = fvparams[i];
            }

            fvparams = na;
        }

        fvparams[fvcount*3] = f[0];
        fvparams[fvcount*3+1] = f[1];
        fvparams[fvcount*3+2] = f[2];
        fvcount++;
        addType(ParameterType.VECTOR_3D);
    }

    public void addFloatVector3(Vector3d f) {
        if (fvparams == null) {
            fvparams = new float[CHUNK_SIZE * 3];
        } else if (fvcount * 3 == fvparams.length) {
            float[] na = new float[fvparams.length + CHUNK_SIZE * 3];
            for(int i=0; i < fvparams.length; i++) {
                na[i] = fvparams[i];
            }

            fvparams = na;
        }
        
        fvparams[fvcount*3] = (float)f.x;
        fvparams[fvcount*3+1] = (float)f.y;
        fvparams[fvcount*3+2] = (float)f.z;
        fvcount++;
        addType(ParameterType.VECTOR_3D);
    }

    public void addMatrix(float[] f) {
        if (mparams == null) {
            mparams = new float[CHUNK_SIZE * 16];
        } else if (mcount * 16 == mparams.length) {
            float[] na = new float[mparams.length + CHUNK_SIZE * 3];
            for(int i=0; i < mparams.length; i++) {
                na[i] = mparams[i];
            }

            mparams = na;
        }
        int ms = mcount*16;
        for(int i = 0; i < 16;i++){
            mparams[ms+i] = f[i];
        }
        mcount++;

        addType(ParameterType.MATRIX_4D);
    }

    public void addMatrix(Matrix4d f) {
        if (mparams == null) {
            mparams = new float[CHUNK_SIZE * 16];
        } else if (mcount * 16 == mparams.length) {
            float[] na = new float[mparams.length + CHUNK_SIZE * 3];
            for(int i=0; i < mparams.length; i++) {
                na[i] = mparams[i];
            }

            mparams = na;
        }
        int ms = mcount*16;
        mparams[ms] = (float)f.m00;
        mparams[ms+1] = (float)f.m01;
        mparams[ms+2] = (float)f.m02;
        mparams[ms+3] = (float)f.m03;
        mparams[ms+4] = (float)f.m10;
        mparams[ms+5] = (float)f.m11;
        mparams[ms+6] = (float)f.m12;
        mparams[ms+7] = (float)f.m13;
        mparams[ms+8] = (float)f.m20;
        mparams[ms+9] = (float)f.m21;
        mparams[ms+10] = (float)f.m22;
        mparams[ms+11] = (float)f.m23;
        mparams[ms+12] = (float)f.m30;
        mparams[ms+13] = (float)f.m31;
        mparams[ms+14] = (float)f.m32;
        mparams[ms+15] = (float)f.m33;
        
        mcount++;
        addType(ParameterType.MATRIX_4D);
    }

    public void addInt(int val) {
        if (iparams == null) {
            iparams = new int[CHUNK_SIZE];
        } else if (icount == iparams.length) {
            int[] na = new int[iparams.length + CHUNK_SIZE];
            for(int i=0; i < iparams.length; i++) {
                na[i] = iparams[i];
            }

            iparams = na;
        }

        iparams[icount++] = val;
        addType(ParameterType.INTEGER);
    }

    public void addBoolean(boolean val) {
        if (bparams == null) {
            bparams = new boolean[CHUNK_SIZE];
        } else if (bcount == bparams.length) {
            boolean[] na = new boolean[bparams.length + CHUNK_SIZE];
            for(int i=0; i < bparams.length; i++) {
                na[i] = bparams[i];
            }

            bparams = na;
        }

        bparams[bcount++] = val;
        addType(ParameterType.BOOLEAN);
    }

    /**
     * Compact all arrays to used sizes
     */
    public void compact() {
        if (fparams != null && fparams.length > fcount) {
            float[] na = new float[fcount];
            for(int i=0; i < fcount; i++) {
                na[i] = fparams[i];
            }
            fparams = na;
        }
        if (iparams != null && iparams.length > icount) {
            int[] na = new int[icount];
            for(int i=0; i < icount; i++) {
                na[i] = iparams[i];
            }
            iparams = na;
        }
        if (bparams != null && bparams.length > bcount) {
            boolean[] na = new boolean[bcount];
            for(int i=0; i < bcount; i++) {
                na[i] = bparams[i];
            }
            bparams = na;
        }
        if (fvparams != null && fvparams.length > fvcount * 3) {
            float[] na = new float[fvcount * 3];
            for(int i=0; i < fvcount * 3; i++) {
                na[i] = fvparams[i];
            }
            fvparams = na;
        }

        if (mparams != null && mparams.length > mcount * 16) {
            float[] na = new float[mcount * 16];
            for(int i=0; i < mcount * 16; i++) {
                na[i] = mparams[i];
            }
            mparams = na;
        }

        if (types != null && types.length > tcount) {
            ParameterType[] na = new ParameterType[tcount];
            for(int i=0; i < tcount; i++) {
                na[i] = types[i];
            }
            types = na;
        }
    }

    public int getFloatCount() {
        return fcount;
    }

    public int getIntCount() {
        return icount;
    }

    public int getFloatVectorCount() {
        return fvcount;
    }

    public int getMatrixCount() {
        return mcount;
    }

    public int getBooleanCount() {
        return bcount;
    }

    public float getFloatParam(int idx) {
        return fparams[idx];
    }

    public void getFloatParams(float[] arr, int start) {
        int idx = start;
        for(int i=0; i < fcount; i++) {
            arr[idx++] = fparams[i];
        }
    }

    public int getIntParam(int idx) {
        return iparams[idx];
    }

    public void getIntParams(int[] arr, int start) {
        int idx = start;
        for(int i=0; i < icount; i++) {
            arr[idx++] = iparams[i];
        }
    }

    public void getFloatVector(int idx, Vector3d dest) {
        dest.x = fvparams[idx*3];
        dest.y = fvparams[idx*3+1];
        dest.z = fvparams[idx*3+2];
    }

    public void getFloatVectorParams(float[] arr, int start) {
        int idx = start * 3;
        for(int i=0; i < fvcount * 3; i++) {
            arr[idx++] = fvparams[i];
        }
    }

    public void getMatrix(int idx, Matrix4d dest) {
        idx *= 16;
        dest.m00 = mparams[idx];
        dest.m01 = mparams[idx+1];
        dest.m02 = mparams[idx+2];
        dest.m03 = mparams[idx+3];
        dest.m10 = mparams[idx+4];
        dest.m11 = mparams[idx+5];
        dest.m12 = mparams[idx+6];
        dest.m13 = mparams[idx+7];
        dest.m20 = mparams[idx+8];
        dest.m21 = mparams[idx+9];
        dest.m22 = mparams[idx+10];
        dest.m23 = mparams[idx+11];
        dest.m30 = mparams[idx+12];
        dest.m31 = mparams[idx+13];
        dest.m32 = mparams[idx+14];
        dest.m33 = mparams[idx+15];
    }

    public void getMatrixParams(float[] arr, int start) {
        int idx = start * 16;
        for(int i=0; i < mcount * 16; i++) {
            arr[idx++] = mparams[i];
        }
    }

    public boolean getBooleanParam(int idx) {
        return bparams[idx];
    }

    public void getBooleanParams(boolean[] arr, int start) {
        int idx = start;
        for(int i=0; i < bcount; i++) {
            arr[idx++] = bparams[i];
        }
    }

    public int getOpCode() {
        return op;
    }

    private void addType(ParameterType type) {
        if (tcount >= types.length) {
            ParameterType[] na = new ParameterType[types.length + CHUNK_SIZE];
            for (int i = 0; i < types.length; i++) {
                na[i] = types[i];
            }

            types = na;
        }

        types[tcount++] = type;
    }

    public ParameterType[] getTypes() {
        return types;
    }

    public int getTypeCount() {
        return tcount;
    }

    /**
     * Add in the call params.
     * @param source
     * @param inst
     * @param exclude Params to exclude
     */
    public static void addCallParams(Parameterizable source, Instruction inst, Set<String> exclude) {
        Parameter[] params = ((Parameterizable) source).getParams();

        // generic mapper, will not work if the function has an initializer

        int len = params.length;

        for(int i=0; i < len; i++) {
            if (exclude != null && exclude.contains(params[i].getName())) continue;

            addCallParam(params[i],inst);
        }

    }

    public static void addCallParam(Parameter param, Instruction inst) {
        ParameterType type = param.getType();
        Object value = param.getValue();

        addCallParam(type, value, inst);
    }

    /**
     * Add in the call params.
     * @param inst
     */
    public static void addCallParam(ParameterType type, Object value, Instruction inst) {
        switch(type) {
            case INTEGER:
                inst.addInt((Integer) value);
                break;
            case DOUBLE:
                inst.addFloat(((Double)value).floatValue());
                break;
            case VECTOR_3D:
                Vector3d v3d = (Vector3d) value;
                float[] vec = new float[3];
                vec[0] = (float) v3d.x;
                vec[1] = (float) v3d.y;
                vec[2] = (float) v3d.z;
                inst.addFloatVector3(vec);
                break;
            case MATRIX_4D:
                Matrix4d m4d = (Matrix4d) value;
                float[] mvec = new float[16];
                mvec[0] = (float) m4d.m00;
                mvec[1] = (float) m4d.m01;
                mvec[2] = (float) m4d.m02;
                mvec[3] = (float) m4d.m03;
                mvec[4] = (float) m4d.m10;
                mvec[5] = (float) m4d.m11;
                mvec[6] = (float) m4d.m12;
                mvec[7] = (float) m4d.m13;
                mvec[8] = (float) m4d.m20;
                mvec[9] = (float) m4d.m21;
                mvec[10] = (float) m4d.m22;
                mvec[11] = (float) m4d.m23;
                mvec[12] = (float) m4d.m30;
                mvec[13] = (float) m4d.m31;
                mvec[14] = (float) m4d.m32;
                mvec[15] = (float) m4d.m33;

                inst.addMatrix(mvec);
                break;
            case BOOLEAN:
                inst.addBoolean((Boolean)value);
                break;
            default:
                throw new IllegalArgumentException("Parameter type not mapped: " + type);
        }
    }

    public static void addCallParams(Parameterizable source, Instruction inst) {
        addCallParams(source,inst,null);
    }

    public static int getOpCode(String name) {
        Integer op = opcodes.get(name);

        if (op ==  null) {
            throw new IllegalArgumentException("Undefined op: " + name);
        }

        return op.intValue();
    }

    public static String convertOpToFunction(int op) {
        for(Map.Entry<String,Integer> entry : opcodes.entrySet()) {
            if (entry.getValue().equals(op)) {
                return entry.getKey();
            }
        }

        return null;
    }


}