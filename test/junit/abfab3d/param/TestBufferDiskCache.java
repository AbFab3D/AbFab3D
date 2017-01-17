/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2013
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Test DiskCache
 *
 * @author Alan Hudson
 */
public class TestBufferDiskCache extends TestCase {

    private static String dir = "/tmp/diskcache";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestBufferDiskCache.class);
    }

    public void testByteBuffer() throws IOException {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, false, false);
        cache.clear();

        LabeledBuffer<byte[]> bbuffer = new LabeledBuffer<byte[]>("bytebuffer",new byte[] {1,Byte.MIN_VALUE,Byte.MAX_VALUE});

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

    public void testByteBufferCompressLarge() throws IOException {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, true, false);
        cache.clear();

        String model = "test/models/Deer.stl";
        File f = new File(model);
        FileInputStream fis = new FileInputStream(f);
        byte[] buffer = new byte[(int)f.length()];
        fis.read(buffer);
        fis.close();

        LabeledBuffer<byte[]> bbuffer = new LabeledBuffer<byte[]>("bytebuffer2",buffer);

        cache.put(bbuffer);
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());

        assertNotNull("return null", ret_val);

        byte[] src = bbuffer.getBuffer();
        byte[] dest = (byte[]) ret_val.getBuffer();

        assertEquals("size wrong", src.length, dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong.  idx: " + i,src[i],dest[i]);
        }
    }


    public void testIntBuffer() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, false, false);
        cache.clear();

        LabeledBuffer<int[]> bbuffer = new LabeledBuffer<int[]>("intbuffer",new int[] {1,255,Integer.MIN_VALUE,Integer.MAX_VALUE});

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

    public void testIntBufferCompress() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, true, false);
        cache.clear();

        LabeledBuffer<int[]> bbuffer = new LabeledBuffer<int[]>("intbuffer",new int[] {1,255,Integer.MIN_VALUE,Integer.MAX_VALUE});

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

    public void testFloatBuffer() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, false, false);
        cache.clear();

        LabeledBuffer<float[]> bbuffer = new LabeledBuffer<float[]>("floatbuffer",new float[] {1.69f,255,Float.MIN_VALUE,Float.MAX_VALUE});

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

    public void testFloatBufferCompress() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, true, false);
        cache.clear();

        LabeledBuffer<float[]> bbuffer = new LabeledBuffer<float[]>("floatbuffer",new float[] {1.69f,255,Float.MIN_VALUE,Float.MAX_VALUE});

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

    public void testShortBuffer() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, false, false);
        cache.clear();

        LabeledBuffer<short[]> bbuffer = new LabeledBuffer<short[]>("shortbuffer",new short[] {1,255,Short.MIN_VALUE,Short.MAX_VALUE});

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

    public void testShortBufferCompress() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, true, false);
        cache.clear();

        LabeledBuffer<short[]> bbuffer = new LabeledBuffer<short[]>("shortbuffer",new short[] {1,255,Short.MIN_VALUE,Short.MAX_VALUE});

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

    public void _testIntBufferSpeed() {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, false, false);
        cache.clear();

        LabeledBuffer<int[]> bbuffer = new LabeledBuffer<int[]>("bigintbuffer",new int[53480952]);

        long t0 = time();
        cache.put(bbuffer);
        printf("put time: %d\n",time() - t0);

        t0 = time();
        LabeledBuffer ret_val = cache.get(bbuffer.getLabel());
        printf("get time: %d\n",time() - t0);
        assertNotNull("return null", ret_val);

        int[] src = bbuffer.getBuffer();
        int[] dest = (int[]) ret_val.getBuffer();

        assertEquals("size wrong",src.length,dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

    public void testByteBufferLazy() throws IOException {
        BufferDiskCache cache = BufferDiskCache.getInstance((int)10e6,dir, false, true);
        cache.clear();

        LabeledBuffer<byte[]> bbuffer = new LabeledBuffer<byte[]>("bytebuffer",new byte[] {1,Byte.MIN_VALUE,Byte.MAX_VALUE});

        cache.put(bbuffer);

        long t0 = time();
        LabeledBuffer ret_val = null;

        while(ret_val == null) {
            ret_val = cache.get(bbuffer.getLabel());
            if (time() - t0 > 10000) {
                // This should not take more then 10s to lazy write
                break;
            }

            try { Thread.sleep(100); } catch(Exception e) {
                // ignore
            }
        }

        assertNotNull("return null", ret_val);

        byte[] src = bbuffer.getBuffer();
        byte[] dest = (byte[]) ret_val.getBuffer();

        assertEquals("size wrong", src.length, dest.length);

        for(int i=0; i < src.length; i++) {
            assertEquals("contents wrong",src[i],dest[i]);
        }
    }

}
