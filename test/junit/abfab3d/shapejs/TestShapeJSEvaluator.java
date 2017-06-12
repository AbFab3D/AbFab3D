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


import abfab3d.param.Parameter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of the SceneImageDataSource
 */
public class TestShapeJSEvaluator extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestShapeJSEvaluator.class);
    }

    /**
     * Test basic usage
     */
    public void testBasic() throws IOException {
        URI uri = new File("test/scripts/gyrosphere_params.js").toURI();
        Script s = new Script(uri);

        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        eval.prepareScript(s.getCode(), null);

        Map<String, Parameter> params = eval.getParams();

        assertNotNull("Period param missing", params.get("period"));
    }

    public void testUndefined() {
        URI uri = new File("test/scripts/undefined.js").toURI();
        Script s = new Script(uri);

        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        eval.prepareScript(s.getCode(), null);
        EvaluatedScript result = eval.getResult();

        assertTrue("Not success", result.isSuccess());

        result = eval.executeScript("main");
        assertTrue("Not success", result.isSuccess());

        String[] prints = result.getPrintLogs();

        assertTrue("Must contain one print",prints.length == 1);
    }

    public void testUndefined2() {
        URI uri = new File("test/scripts/undefined2.js").toURI();
        Script s = new Script(uri);

        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        eval.prepareScript(s.getCode(), null);
        EvaluatedScript result = eval.getResult();

        assertTrue("Not success", result.isSuccess());

        result = eval.executeScript("main");
        assertTrue("Not success", result.isSuccess());

        String[] prints = result.getPrintLogs();

        if (prints != null) {
            printf("Prints:\n");
            for(int i=0; i < prints.length; i++) {
                printf(prints[i]);
            }
        }
        assertTrue("Must contain one print",prints.length == 1);

    }

    public void testURNResolution() {
        URI uri = new File("test/scripts/urn_resolution.js").toURI();
        Script s = new Script(uri);

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();

        ScriptResources sr = sm.prepareScript(jobID,s.getCode(),null);
        EvaluatedScript result = sr.eval.getResult();

        assertTrue("Not success", result.isSuccess());

        sr = sm.executeScript(sr);
        result = sr.eval.getResult();

        assertTrue("Not success", result.isSuccess());

        String[] prints = result.getPrintLogs();

        printf("Prints:\n");
        if (prints != null) {
            for(int i=0; i < prints.length; i++) {
                printf(prints[i]);
            }
        }
        assertTrue("Must contain one print",prints.length == 1);

        for(int i=0; i < prints.length; i++) {
            assertFalse("still in urn form",prints[i].contains("urn:shapeways:stockModel:sphere"));
        }

    }

}
