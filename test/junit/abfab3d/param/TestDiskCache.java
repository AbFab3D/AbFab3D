/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.param;

// External Imports


import abfab3d.core.LabeledBuffer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Tests the functionality of DiskCache
 *
 * @version
 */
public class TestDiskCache extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDiskCache.class);
    }

    public void testByteBuffer() {
        LabeledBuffer<byte[]> bbuffer = new LabeledBuffer<byte[]>("bytebuffer",new byte[] {1,Byte.MIN_VALUE,Byte.MAX_VALUE});
        String basedir = "/tmp/diskcache";

        DiskCache cache = DiskCache.getInstance((int)10e6,basedir);

        cache.put(bbuffer);
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());

        assertNotNull("return null", ret_val);

        byte[] src = bbuffer.getBuffer();
        byte[] dest = (byte[]) ret_val.getBuffer();

        assertEquals("size wrong", src.length, dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

    public void testIntBuffer() {
        LabeledBuffer<int[]> bbuffer = new LabeledBuffer<int[]>("intbuffer",new int[] {1,255,Integer.MIN_VALUE,Integer.MAX_VALUE});
        String basedir = "/tmp/diskcache";

        DiskCache cache = DiskCache.getInstance((int)10e6,basedir);

        cache.put(bbuffer);
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());

        assertNotNull("return null", ret_val);

        int[] src = bbuffer.getBuffer();
        int[] dest = (int[]) ret_val.getBuffer();

        assertEquals("size wrong",src.length,dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

    public void testShortBuffer() {
        LabeledBuffer<short[]> bbuffer = new LabeledBuffer<short[]>("shortbuffer",new short[] {1,255,Short.MIN_VALUE,Short.MAX_VALUE});
        String basedir = "/tmp/diskcache";

        DiskCache cache = DiskCache.getInstance((int)10e6,basedir);

        cache.put(bbuffer);
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());

        assertNotNull("return null", ret_val);

        short[] src = bbuffer.getBuffer();
        short[] dest = (short[]) ret_val.getBuffer();

        assertEquals("size wrong",src.length,dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

    public void testFloatBuffer() {
        LabeledBuffer<float[]> bbuffer = new LabeledBuffer<float[]>("floatbuffer",new float[] {1.69f,255,Float.MIN_VALUE,Float.MAX_VALUE});
        String basedir = "/tmp/diskcache";

        DiskCache cache = DiskCache.getInstance((int)10e6,basedir);

        cache.put(bbuffer);
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());

        assertNotNull("return null", ret_val);

        float[] src = bbuffer.getBuffer();
        float[] dest = (float[]) ret_val.getBuffer();

        assertEquals("size wrong",src.length,dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

    public void testDoubleBuffer() {
        LabeledBuffer<double[]> bbuffer = new LabeledBuffer<double[]>("doublebuffer",new double[] {1.69,255,Double.MIN_VALUE,Double.MAX_VALUE});
        String basedir = "/tmp/diskcache";

        DiskCache cache = DiskCache.getInstance((int)10e6,basedir);

        cache.put(bbuffer);
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());

        assertNotNull("return null", ret_val);

        double[] src = bbuffer.getBuffer();
        double[] dest = (double[]) ret_val.getBuffer();

        assertEquals("size wrong",src.length,dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

}
