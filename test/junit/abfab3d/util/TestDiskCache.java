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

package abfab3d.util;

// External Imports

import abfab3d.param.FileDiskCache;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static abfab3d.core.Output.printf;

/**
 * Test DiskCache
 *
 * @author Alan Hudson
 */
public class TestDiskCache extends TestCase {

    private static String dir = "/tmp/diskcache";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDiskCache.class);
    }

    public void testConvKeyToFilename() {
        FileDiskCache dc = new FileDiskCache(dir);
        String key = "http://www.shapeways.com/models/get-base/9999999/9999999.v0.visual_mesh_converter.sh.x3db.zip?token=abdcefghijklmnopqrstuvwxyz1234567890";

        String newKey = dc.convKeyToFilename(key,null);

        printf("newKey: %s\n", newKey);
        assertNotNull("key null", newKey);

        printf("Key len: %d\n",newKey.length());
        assertTrue("key len", newKey.length() <= 108);
    }

    public void testConvKeyToFilenameKeepExt() {
        FileDiskCache dc = new FileDiskCache(dir);
        String key = "C:\\Users\\giles\\AppData\\Local\\Temp\\downloaduri5690294239212912741\\model8708582718597325402.x3db";

        String newKey = dc.convKeyToFilename(key,"x3db");

        printf("newKey: %s\n", newKey);
        assertNotNull("key null", newKey);

        printf("Key len: %d\n",newKey.length());
        assertTrue("key len", newKey.length() <= 108);

        assertTrue("file ending",newKey.endsWith(".x3db"));
    }

    public void testAddFile() throws IOException {
        FileDiskCache dc = new FileDiskCache(dir);
        dc.clear();

        String path = Files.createTempFile("test", "file").toString();
        File origFile = new File(path);

        FileOutputStream fos = new FileOutputStream(path);
        fos.write("HelloWorld".getBytes());
        fos.close();

        assertTrue("orig file", origFile.exists());
        String key = "helloworld";

        String newPath = dc.put(key, path);

        assertNotNull("No file returned", newPath);

        File ffile = new File(newPath);
        assertTrue(ffile.exists());
        assertFalse(origFile.exists());

        String rpath = dc.get(key);

        assertNotNull("Entry null", rpath);
        assertTrue("File doesnt exist", new File(rpath).exists());

        assertTrue("Size too small",dc.getCurrentSize() >= 10);

    }

    public void testAddDirectory() throws IOException {
        FileDiskCache dc = new FileDiskCache(dir);
        dc.clear();

        String path = Files.createTempDirectory("dirtest").toString();
        File origFile = new File(path);

        File file1 = new File(path,"file1.x3d");
        FileOutputStream fos = new FileOutputStream(file1);
        fos.write("HelloWorld".getBytes());
        fos.close();

        File file2 = new File(path,"file2.x3db");
        FileOutputStream fos2 = new FileOutputStream(file2);
        fos2.write("HelloWorld2".getBytes());
        fos2.close();

        assertTrue("orig file", origFile.exists());
        String key = "disktest";

        String newPath = dc.put(key, path);

        assertNotNull("No file returned", newPath);

        File ffile = new File(newPath);
        assertTrue(ffile.exists());
        assertFalse(origFile.exists());

        String rpath = dc.get(key);
        File result = new File(rpath);

        assertNotNull("Entry null", rpath);
        assertTrue("Not directory", result.isDirectory());
        assertEquals("Needs 2 files", result.listFiles().length, 2);

        printf("Current Size: %d\n", dc.getCurrentSize());
        assertTrue("Size too small",dc.getCurrentSize() > 20);
    }

    public void testMaxSize() throws IOException {
        FileDiskCache dc = new FileDiskCache(dir, 60);
        dc.clear();

        String path = Files.createTempFile("test", "file").toString();
        File origFile = new File(path);

        for(int i=0; i < 10; i++) {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write("1234567890".getBytes());
            fos.close();

            assertTrue("orig file", origFile.exists());
            String key = "key" + i;

            String newPath = dc.put(key, path);

            assertNotNull("No file returned", newPath);
            File ffile = new File(newPath);
            assertTrue(ffile.exists());
            assertFalse(origFile.exists());
        }


        // The first 4 should be gone
        for(int i=0; i < 4; i++) {
            assertNull("key" + i + " should be null", dc.get("key" + i));
        }

        // We should have the last 6 left
        for(int i=4; i < 10; i++) {
            assertNotNull("key" + i + " should exist", dc.get("key" + i));
        }

        dc.clear();
    }

    public void testRemove() throws IOException {
        FileDiskCache dc = new FileDiskCache(dir, 60);
        dc.clear();


        String path = Files.createTempFile("test", "file").toString();
        File origFile = new File(path);

        FileOutputStream fos = new FileOutputStream(path);
        fos.write("HelloWorld".getBytes());
        fos.close();

        assertTrue("orig file", origFile.exists());
        String key = "helloworld";

        String newPath = dc.put(key, path);
        assertTrue("file there", new File(newPath).exists());
        File meta = new File(newPath + ".meta");
        assertTrue("meta there", meta.exists());

        assertNotNull("key kept", dc.get(key));
        dc.remove(key);

        assertNull("key removed", dc.get(key));
        assertFalse("file still there", new File(newPath).exists());
        assertFalse("meta still there", meta.exists());
    }

    public void testRemoveDirectory() throws IOException {
        FileDiskCache dc = new FileDiskCache(dir);
        dc.clear();

        String path = Files.createTempDirectory("dirtest").toString();
        File origFile = new File(path);

        File file1 = new File(path,"file1.x3d");
        FileOutputStream fos = new FileOutputStream(file1);
        fos.write("HelloWorld".getBytes());
        fos.close();

        File file2 = new File(path,"file2.x3db");
        FileOutputStream fos2 = new FileOutputStream(file2);
        fos2.write("HelloWorld2".getBytes());
        fos2.close();

        assertTrue("orig file", origFile.exists());
        String key = "disktest";

        String newPath = dc.put(key, path);

        assertNotNull("No file returned", newPath);

        File ffile = new File(newPath);
        assertTrue(ffile.exists());
        assertFalse(origFile.exists());

        String rpath = dc.get(key);
        File result = new File(rpath);

        assertNotNull("Entry null", rpath);
        assertTrue("Not directory", result.isDirectory());
        assertEquals("Needs 2 files", result.listFiles().length, 2);

        printf("Current Size: %d\n", dc.getCurrentSize());
        assertTrue("Size too small",dc.getCurrentSize() > 20);

        dc.remove(key);

        File meta = new File(newPath + ".meta");
        assertNull("key removed", dc.get(key));
        assertFalse("file still there", new File(newPath).exists());
        assertFalse("meta still there", meta.exists());

    }

    public void testAddExtra() throws IOException {
        FileDiskCache dc = new FileDiskCache(dir);
        dc.clear();

        HashMap<String,Object> extra1 = new HashMap<>();
        extra1.put("param1_key","param1_value");

        String path = Files.createTempFile("test", "file").toString();
        File origFile = new File(path);

        FileOutputStream fos = new FileOutputStream(path);
        fos.write("HelloWorld".getBytes());
        fos.close();

        assertTrue("orig file", origFile.exists());
        String key = "helloworld";

        String newPath = dc.put(key, extra1,path);

        assertNotNull("No file returned", newPath);

        File ffile = new File(newPath);
        assertTrue(ffile.exists());
        assertFalse(origFile.exists());

        HashMap<String,Object> extra2 = new HashMap<>();

        String rpath = dc.get(key, extra2);

        assertNotNull("Entry null", rpath);
        assertTrue("File doesnt exist", new File(rpath).exists());

        assertTrue("Size too small",dc.getCurrentSize() >= 10);

        assertEquals("Extra entries",extra1.size(),extra2.size());
        assertEquals("param1",extra1.get("param1_key"),extra2.get("param1_key"));

    }


}
