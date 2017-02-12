package abfab3d.core;

import static abfab3d.core.Output.printf;

/**
 * A buffer with a unique label.  Once this buffer has been set its contents are locked.
 *
 * @author Alan Hudson
 */
public class LabeledBuffer<T> {
    public enum Type {BYTE, SHORT, INT, FLOAT, DOUBLE};

    private String label;
    private T buffer;
    private Type type;
    private int size;

    public LabeledBuffer() {

    }

    public LabeledBuffer(String label,T buffer) {
        this.label = label;
        setBuffer(buffer);
    }

    public String getLabel() {
       return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setBuffer(T buff) {
        this.buffer = buff;

        if (buff instanceof int[]) {
            type = Type.INT;
            size = ((int[]) buff).length;
        } else if (buff instanceof byte[]) {
            type = Type.BYTE;
            size = ((byte[]) buff).length;
        } else if (buff instanceof short[]) {
            type = Type.SHORT;
            size = ((short[]) buff).length;
        } else if (buff instanceof float[]) {
            type = Type.FLOAT;
            size = ((float[]) buff).length;
        } else if (buff instanceof double[]) {
            type = Type.DOUBLE;
            size = ((double[]) buff).length;
        } else {
            throw new IllegalArgumentException("Unknown type");
        }
    }

    public T getBuffer() {
        return buffer;
    }

    public void getBuffer(T dest) {
        switch(type) {
            case INT:
                int[] ibuffer = (int[]) buffer;
                System.arraycopy(ibuffer,0,dest,0,ibuffer.length);
                break;
            case BYTE:
                byte[] bbuffer = (byte[]) buffer;
                System.arraycopy(bbuffer,0,dest,0,bbuffer.length);
                break;
            case SHORT:
                short[] sbuffer = (short[]) buffer;
                System.arraycopy(sbuffer,0,dest,0,sbuffer.length);
                break;
            case FLOAT:
                float[] fbuffer = (float[]) buffer;
                System.arraycopy(fbuffer,0,dest,0,fbuffer.length);
                break;
            case DOUBLE:
                double[] dbuffer = (double[]) buffer;
                System.arraycopy(dbuffer,0,dest,0,dbuffer.length);
                break;
            default:
                throw new IllegalArgumentException("Unknown type");

        }
    }

    public int getNumElements() {
        return size;
    }

    public int getSizeBytes() {
        switch(type) {
            case INT:
                return size * 4;
            case BYTE:
                return size;
            case SHORT:
                return size * 2;
            case FLOAT:
                return size * 4;
            case DOUBLE:
                return size * 8;
            default:
                throw new IllegalArgumentException("Unknown type");

        }
    }

    public Type getType() {
        return type;
    }

    public static final void main(String[] args) {
        LabeledBuffer<int[]> lb = new LabeledBuffer<int[]>();
        lb.setBuffer(new int[] {1,3});

        LabeledBuffer<byte[]> lb2 = new LabeledBuffer<byte[]>();
        lb2.setBuffer(new byte[] {1,3});

    }
}

