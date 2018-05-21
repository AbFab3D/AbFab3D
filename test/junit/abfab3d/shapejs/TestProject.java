/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

// External Imports


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of the Project class
 */
public class TestProject extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestProject.class);
    }

    /**
     * Test saving of a Project
     */
    public void testSave()  {
    }

    /**
     * Test loading of a Project
     */
    public void testLoadManifest() {
        try {
            Project p = Project.load("test/projects/image_pendant/manifest.json", new ArrayList<String>());

            List<ProjectItem> scripts = p.getScripts();
            assertEquals("Wrong number of scripts",2,scripts.size());

            printf("Checking for: %s \n",scripts.get(0).getPath());
            assertTrue("Not there",new File(scripts.get(0).getPath()).exists());

            List<VariantItem> variants = p.getVariants();
            assertEquals("Wrong number of variants",2,variants.size());
            assertTrue("Not there: "+variants.get(0).getPath(),new File(variants.get(0).getPath()).exists());
            assertTrue("Not there",new File(variants.get(1).getPath()).exists());

            assertNotNull("Missing thumb",variants.get(0).getThumbnail());
            assertNotNull("Missing thumb",variants.get(1).getThumbnail());

            List<ProjectItem> resources = p.getResources();
            assertEquals("Wrong number of resources",2,resources.size());
            assertTrue("Not there",new File(resources.get(0).getPath()).exists());
            assertTrue("Not there",new File(resources.get(1).getPath()).exists());

            assertNotNull("Name set",p.getName());
            assertNotNull("Author set",p.getAuthor());
            assertNotNull("License set",p.getLicense());

            /*
            printf("Parent: %s\n",p.getParentDir());
            for(ProjectItem pi : scripts) {
                printf("%s\n",pi.getPath());
            }

            for(VariantItem pi : variants) {
                printf("path: %s  main: %s\n",pi.getPath(),pi.getMainScript());
            }
            */
        } catch(IOException e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * Test loading of a Project
     */
    public void testLoadZip() {
        try {
            Project p = Project.load("test/projects/image_pendant/image_pendant.shapeprj", new ArrayList<String>());

            List<ProjectItem> scripts = p.getScripts();
            assertEquals("Wrong number of scripts",1,scripts.size());

            printf("Checking for: %s \n",scripts.get(0).getPath());
            assertTrue("Not there",new File(scripts.get(0).getPath()).exists());

            List<VariantItem> variants = p.getVariants();
            assertEquals("Wrong number of variants",2,variants.size());
            assertTrue("Not there: "+variants.get(0).getPath(),new File(variants.get(0).getPath()).exists());
            assertTrue("Not there",new File(variants.get(1).getPath()).exists());

            assertNotNull("Missing thumb",variants.get(0).getThumbnail());
            assertNotNull("Missing thumb",variants.get(1).getThumbnail());

            List<ProjectItem> resources = p.getResources();
            assertEquals("Wrong number of resources",2,resources.size());
            assertTrue("Not there",new File(resources.get(0).getPath()).exists());
            assertTrue("Not there",new File(resources.get(1).getPath()).exists());

            assertNotNull("Name set",p.getName());
            assertNotNull("Author set",p.getAuthor());
            assertNotNull("License set",p.getLicense());
        } catch(IOException e) {
            e.printStackTrace();
            fail("Error: " + e.getMessage());
        }
    }

}
