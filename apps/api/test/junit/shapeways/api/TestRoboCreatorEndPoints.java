/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapeways.api;

// External Imports
import junit.framework.*;

// Internal Imports
import org.apache.commons.io.FileUtils;
import shapeways.api.models.*;
import shapeways.api.models.reservation.RESTAPIReservation;
import shapeways.api.models.reservation.RESTAPIReservationV1;
import shapeways.api.models.reservation.ReservationInsertResult;
import shapeways.api.models.reservation.ReservationUpdateResult;
import shapeways.api.price.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tests the functionality of the REST end points needed for RoboCreators
 *
 * @author Alan Hudson
 * @version
 */
public class TestRoboCreatorEndPoints extends TestCase {
    private static final String host =  "http://api.beekey.nyc.shapeways.net";
    //private static final String host = null;     // Null host means shapeways production

    /** Access token and secret */
    // ImagePopper on shapeways.com
/*
    private String CONSUMER_KEY = "fcc8983c4e72e553e19583fcfbedf8d55d5dfc24";
    private String CONSUMER_SECRET = "ae647dffab05dbabaf92f739c766cda3351664d1";
    private String ACCESS_TOKEN = "22cbc36539f31f8ecf8990a6386ebe76676bef87";
    private String ACCESS_SECRET = "8dc65aab672eedebeb691eb5bcc52e1b9a969aa9";
*/

    // ImagePopper on beekeys
    private String CONSUMER_KEY = "c1100bbeeccc7f4f2fe9a0089b052b6a03e8af54";
    private String CONSUMER_SECRET = "91fbff645d1a7ff539991f08604d5802590b1f71";
    private String ACCESS_TOKEN = "ae93af1bd3cad68b37ac05945fdad927daeac6c9";
    private String ACCESS_SECRET = "a75ff292d277e83a6d8b879f3aebec8ee9c770fb";

    private String PROXY_HOST="127.0.0.1";
    private String PROXY_PORT="8888";

    public void setUp() {
        if (PROXY_HOST != null) {
            System.out.println("Configuring proxy");

            Properties systemSettings =
                    System.getProperties();
            systemSettings.put("proxySet", "true");
//            systemSettings.put("http.proxyHost", PROXY_HOST);
//            systemSettings.put("http.proxyPort", PROXY_PORT);
            systemSettings.put("socksProxyHost", PROXY_HOST);
            systemSettings.put("socksProxyPort", PROXY_PORT);
        }
    }
    public void testInsertPrice() {
        float volume = 1000000f /(100 * 100 * 100); // 1 cm^3 in m^2
        float area = 6000f / (1000 * 1000); // 600 mm^2 (6 cm^2) in m^2
        float xBoundMin = -.01f;
        float yBoundMin = -.01f;
        float zBoundMin = -.01f;
        float xBoundMax = 10.01f; // 1 cm in m
        float yBoundMax = 10.01f;
        float zBoundMax = 10.01f;
        int[] materials = new int[] {6, 25};

        String host = null;

        RESTAPIPrice price_endpoint = new RESTAPIPriceV1(host, CONSUMER_KEY,CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_SECRET);

        Result result = price_endpoint.insert(volume, area, xBoundMin, xBoundMax, yBoundMin, yBoundMax, zBoundMin, zBoundMax, materials);
        System.out.println("Result: " + result);

        if (result instanceof ResultError) {
            fail("Failed request: " + result);
        }

        PriceInsertResult presult = (PriceInsertResult) result;
        Map<String, Price> prices = presult.getPrices();

        assertNotNull("Prices return",prices);
        assertEquals("Number of prices",2,prices.size());
        for(Price price : prices.values()) {
            assertTrue("Price > 0", price.getPrice() > 0);
        }
    }

    public void testInsertPriceReserved() {
        float volume = 1000000f /(100 * 100 * 100); // 1 cm^3 in m^2
        float area = 6000f / (1000 * 1000); // 600 mm^2 (6 cm^2) in m^2
        float xBoundMin = -.01f;
        float yBoundMin = -.01f;
        float zBoundMin = -.01f;
        float xBoundMax = 10.01f; // 1 cm in m
        float yBoundMax = 10.01f;
        float zBoundMax = 10.01f;
        int[] materials = new int[] {6, 25};
        boolean reserved = true;

        RESTAPIPrice price_endpoint = new RESTAPIPriceV1(host, CONSUMER_KEY,CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_SECRET);

        Result result = price_endpoint.insert(volume, area, xBoundMin, xBoundMax, yBoundMin, yBoundMax, zBoundMin, zBoundMax, materials, reserved);

        if (result instanceof ResultError) {
            fail("Failed request: " + result);
        }

        PriceInsertResult presult = (PriceInsertResult) result;
        Map<String, Price> prices = presult.getPrices();

        assertNotNull("Prices return",prices);
        assertEquals("Number of prices",2,prices.size());
        for(Price price : prices.values()) {
            assertTrue("Price > 0", price.getPrice() > 0);
        }

        assertNotNull("ReservationId not null", presult.getReservePriceId());
    }

    public void testFlow() {
        float volume = 1000000f /(100 * 100 * 100); // 1 cm^3 in m^2
        float area = 6000f / (1000 * 1000); // 600 mm^2 (6 cm^2) in m^2
        float xBoundMin = -.01f;
        float yBoundMin = -.01f;
        float zBoundMin = -.01f;
        float xBoundMax = 10.01f; // 1 cm in m
        float yBoundMax = 10.01f;
        float zBoundMax = 10.01f;
        int[] materials = new int[] {6, 25};
        boolean reserved = true;

        RESTAPIPrice price = new RESTAPIPriceV1(host, CONSUMER_KEY,CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_SECRET);
        price.setDebug(true);

        Result presult = price.insert(volume, area, xBoundMin, xBoundMax, yBoundMin, yBoundMax, zBoundMin, zBoundMax, materials, reserved);

        if (presult instanceof ResultError) {
            fail(presult.toString());
        }

        PriceInsertResult pir = (PriceInsertResult) presult;
        RESTAPIReservation reserve = new RESTAPIReservationV1(host, CONSUMER_KEY,CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_SECRET);
        reserve.setDebug(true);

        HashMap<String,Object> geom_parameters = new HashMap<String,Object>();
        geom_parameters.put("width",0.05f);
        geom_parameters.put("height",0.05f);
        geom_parameters.put("depth",0.05f);

        byte[] photoFile = null;

        try {
            // Everyone loves cats!
            photoFile = FileUtils.readFileToByteArray(new File("apps/imagepopper/images/cat.png"));
        } catch(IOException ioe) {
            fail(ioe.getMessage());
        }
        Result rresult = reserve.insert(pir.getReservePriceId(), geom_parameters, photoFile, null, null, null, "Cube","Cube", false, false, null, null);
        if (rresult instanceof ResultError) {
            fail(rresult.toString());
        }
        ReservationInsertResult riresult = (ReservationInsertResult) rresult;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        pw.println("#X3D V3.0 utf8");
        pw.println("Shape { geometry Box { size 0.05 0.05 0.05 } }");
        pw.close();

        byte[] x3dfile = baos.toByteArray();

        Integer modelId = riresult.getModelId();

        Result upload = reserve.update(modelId, x3dfile, "cube.x3dv", 1.0f, true, true);

        System.out.println("Upload result: " + upload);
        if (upload instanceof ResultError) {
            fail(upload.toString());
        }

        ReservationUpdateResult ruresult = ((ReservationUpdateResult) upload);

    }


    public void testInsertModels() {
        RESTAPIModels models_endpoint = new RESTAPIModelsV1(host,CONSUMER_KEY,CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_SECRET);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        pw.println("#X3D V3.0 utf8");
        pw.println("Shape { geometry Box { size 0.05 0.05 0.05 } }");
        pw.close();

        Result result = models_endpoint.insert(baos.toByteArray(), "cube.x3dv", 1.0f, true, true, "cube", "cube", false, false, null, null);

        System.out.println("Result: " + result);

        if (result instanceof ResultError) {
            fail("Failed request: " + result);
        }

        ModelsInsertResult mresult = (ModelsInsertResult) result;

    }
}
