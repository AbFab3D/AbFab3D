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

package abfab3d.grid;

// External Imports
import java.lang.reflect.Constructor;

import junit.framework.Test;
import junit.framework.TestSuite;


// Internal Imports

import static abfab3d.util.Output.printf;

/**
 * Tests the functionality of a ArrayAttributeGridInt.
 *
 * @author Vladimir Bulatov
 */
public class GeneralGridTester {

    Class gclass;

    public GeneralGridTester(String className) throws Exception{

        gclass = Class.forName(className); 
        
    }
    
    public void testConstructors(){
        printf("testConstructors: %s\n", gclass.getCanonicalName());
        Constructor constr[] = gclass.getConstructors();
        for(int i = 0; i < constr.length; i++){
            printf("constr: %s\n", constr[i].getName());
        }        
        
    }
    

    
    public static void main(String args[]) throws Exception {
        GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridByte");
        tester.testConstructors();
    }
}