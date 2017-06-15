/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
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
import java.util.HashMap;
import java.util.UUID;

/**
 * Tests the functionality of the ScriptManager
 */
public class TestScriptManager extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestScriptManager.class);
    }

    /**
     * Test job not cached
     */
    public void testJobNotCached()  {
        URI uri = new File("test/scripts/gyrosphere_params.js").toURI();
        Script s = new Script(uri);

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();

        try {
            sm.updateParams(jobID, new HashMap<String, Object>());
        } catch (NotCachedException nce) {
            // passed
            return;
        }

        fail("Exception not thrown");
    }

    /**
     * Test job not cached
     */
    public void testJobNotCached2() {
        URI uri = new File("test/scripts/gyrosphere_params.js").toURI();
        Script s = new Script(uri);

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();

        HashMap<String,Object> params = new HashMap<>();
        params.put("period",12);

        try {
            sm.prepareScript(jobID, (String) null, params);
        } catch(Exception e) {
            return;
        }

        fail("No exception thrown");
    }
}
