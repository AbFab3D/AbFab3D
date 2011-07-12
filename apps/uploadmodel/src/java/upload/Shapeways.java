/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package upload;

// External Imports
import java.util.*;
import java.io.*;
import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.ErrorHandler;

// Internal Imports
import abfab3d.util.ApplicationParams;
import abfab3d.geom.*;
import abfab3d.geom.CubeCreator.Style;
import abfab3d.grid.*;
import abfab3d.util.*;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.shapeways.*;
import abfab3d.validate.VolumeChecker;

/**
 * Example of uploading a model to the Shapeways site
 *
 * @author Alan Hudson
 */
public class Shapeways {
    /** Resolution of the voxels in meters.  */
    public static final double RESOLUTION = 0.001;

    /** The volume calculator */
    private VolumeChecker calc;

    /**
     * Upload the file to Shapeways.
     */
    public void upload(String filename, String name, String modelType, float scale) {
        try {
//            String soap_server = "http://www.shapeways.com";
            String soap_server = "http://test5.shapeways.com";
            String soap_path = "/modules/shapeways_api/webservice/v1/soap.php";
            //String soap_path = "/modules/udesign/webservice/soap.php";

//            String wsdl_server = "http://api.shapeways.com";
            String wsdl_server = "http://api.shapeways.com";
            String wsdl_path = "/v1/wsdl.php";
            //String wsdl_path = "/modules/udesign/webservice/wsdl.php";
            String urn = "urn:SW.wsdl";

            ApplicationParams.put("SOAP_SERVER", soap_server);
            ApplicationParams.put("SOAP_PATH", soap_path);
            ApplicationParams.put("WSDL_SERVER", wsdl_server);
            ApplicationParams.put("WSDL_PATH", wsdl_path);
            ApplicationParams.put("SERVICE_URN", urn);

            IOManager site = IOManager.getIOManager();
            site.setDebug(true);

            site.login("swapitest", "testme!");
//            site.login("tonytest", "yumetech");

            byte[] file = site.readFile(new File(filename));
            SWModelType model = new SWModelType();
            model.setSoapElementName("model");
            model.setSoapElementType("SWModel");
            model.setModelType(modelType);
            model.setDesc("Spring7");
            model.setFile(file);
            model.setScale(scale);
            model.setHasColor(false);
            model.setFilename(name);
            model.setTitle(name);
            model.setAvailability(0);

            site.saveNewModel(model);


            //float volume = calcVolume(new File(filename));
            float volume = 1;

            //displayPrice(volume, 1);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * Display the printers available.
     */
    public void displayPrinters() {
        IOManager site = IOManager.getIOManager();

        ArrayList<MaterialType> materials = null;

        PrinterArrayType p = site.getPrinters();
        PrinterType[] pt = p.getPrinters();

        if((pt != null) && (pt.length > 0)) {
            // get all the materials from all printers
            materials = new ArrayList<MaterialType>();

            for (int i = 0; i < pt.length; i++) {

                displayPrinter(pt[i]);

                MaterialType[] materialList = pt[i].getMaterials();

                if(materialList == null) {
                    materials = null;
                } else {
                    for (int j = 0; j < materialList.length; j++) {
                        MaterialType mat = materialList[j];

                        if(mat != null) {
                            displayMaterial(mat);
                            materials.add(mat);
                        }
                    }

                    if(materials.size() <= 0) {
                        materials = null;
                    }
                }
            }
        }
    }

    /**
     * Calculate Volume of an IndexedTriangleSet.
     */
    private float calcVolume(File input) {
        VolumeChecker calc = new VolumeChecker();

        calc.processFile(input);

        float vol = calc.calculateVolume(100f);  // Calculate volume in cm^3

        System.out.println("Volume is: " + vol);

        return vol;
    }

    /**
     * Display a printer.
     *
     * @param pt The printer
     */
    private void displayPrinter(PrinterType pt) {
        System.out.println("Printer: ");
        System.out.println("   title: " + pt.getTitle());
        System.out.println("   volume: " + pt.getVolume());
        System.out.println("   wallthickness: " + pt.getWallthickness());
    }

    /**
     * Display a material.
     *
     * @param mt The material
     */
    private void displayMaterial(MaterialType mt) {
        System.out.println("Material: ");
        System.out.println("   id: " + mt.getId());
        System.out.println("   title: " + mt.getTitle());
    }

    /**
     * Display the price.
     *
     * @param volume The volume in cm^3
     * @param material The materialID
     */
    private void displayPrice(float volume, int material) {
        IOManager site = IOManager.getIOManager();
        SWModelPriceType price = site.getModelPrice(volume, material);

        System.out.println("Price: " + price.getPrice() + " " +
            price.getCurrency());
    }

    public static void main(String[] args) {
        Shapeways c = new Shapeways();
        //c.upload("../../test/models/sphere_10cm_rough.x3dv","sphere_10cm_rough.x3dv", "x3dv",1);
        //c.upload("../../test/models/sphere_10cm_smooth.x3dv","sphere_10cm_smooth.x3dv", "x3dv",1);

        //c.upload("../../test/models/cube-1cm3.stl", "cube-1cm3.stl", "STL", 1);
//        c.upload("../../test/models/toxic.stl",0.001f);
//        c.upload("../../test/models/toxic.x3dv",0.001f);
//        c.upload("../../test/models/Spring.stl","STL", 10f);
          c.upload("../../test/models/ball-r5.stl", "ball-r5.stl", "STL", 1f);

    }
}