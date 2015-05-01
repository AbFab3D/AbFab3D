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
import java.util.Random;

import abfab3d.util.Bounds;


// Internal Imports
import abfab3d.util.Output;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
 * Tests the functionality of a ArrayAttributeGridInt.
 *
 * @author Vladimir Bulatov
 */
public class GeneralGridTester {

    Class gclass; // grid class to be tested     
    long datamask; // data mask whcih can be stored in grid 
    int bitcount; // bit count of daa mask 

    public GeneralGridTester(String className, int bitcount) throws Exception{

        gclass = Class.forName(className); 
        this.bitcount = bitcount;
        this.datamask = getDataMask(bitcount);
    }
    
    public void testAll()throws Exception {

        printf("testConstructors: %s\n", gclass.getCanonicalName());        
        Constructor c = getBoundsConstructor(gclass);
        if(c != null)testBoundsConstructor(c);
        c = getIntConstructor(gclass);
        if(c != null)testIntConstructor(c);
        
        //Constructor constr[] = gclass.getConstructors();
        //for(int i = 0; i < constr.length; i++){
        //    printf("constr: %s\n", constr[i].toGenericString());
        //    Class arg[] = constr[i].getParameterTypes();
        //    printf("  params count:%d\n", arg.length); 
        //}          
    }
 

    void testIntConstructor(Constructor c) throws Exception {
        
        printf("testIntConstructor(%s)\n", c.toGenericString());  
        int nx = 21, ny = 25, nz = 29;
        double vs = 0.1;
        AttributeGrid ag = (AttributeGrid)c.newInstance(nx, ny, nz, vs, vs);
        printf("ag:%s\n", ag);
        
        Random data = new Random(10);
        //ag.setAttribute(0,0,0,1);
        //ag.setAttribute(0,0,1,2);                    
        //ag.setAttribute(0,0,2,4);                    

        //if(true)return;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz;z++){  
                    long a0 = data.nextLong() & datamask;
                    //long a0 = d | (1 << b++);
                    //printf("%d %d %d: %x\n", x, y, z, a0);
                    ag.setAttribute(x,y,z,a0);
                    long a = ag.getAttribute(x,y,z);
                    if(a != a0)
                        throw new RuntimeException(fmt("setAttribute(%X) != getAttribute() return %X",a0, a));
                }
            }
        } 
        
    }

    void testBoundsConstructor(Constructor c) throws Exception {

        printf("testBounsConstructor(%s)\n", c.toGenericString());  
        
        int nx = 21, ny = 25, nz = 29;
        double vs = 0.1;
        AttributeGrid ag = (AttributeGrid)c.newInstance(new Bounds(vs*nx, vs*ny, vs*nz), vs, vs);
        printf("ag:%s\n", ag);
        Random data = new Random(10);
        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz;z++){
                    long a0 = data.nextLong() & datamask;
                    ag.setAttribute(x,y,z,a0);
                    long a = ag.getAttribute(x,y,z);
                    if(a != a0)
                        throw new RuntimeException(fmt("setAttribute(%X) != getAttribute() return %X",a0, a));                    
                }
            }
        }        
    }

    static Constructor getBoundsConstructor(Class gclass){
        try {
            return gclass.getDeclaredConstructor(Bounds.class,Double.TYPE,Double.TYPE);
        } catch (Exception e){
        }
        return null;
    }

    static Constructor getIntConstructor(Class gclass){

        try {
            return gclass.getDeclaredConstructor(Integer.TYPE,Integer.TYPE,Integer.TYPE,Double.TYPE,Double.TYPE);
        } catch (Exception e){
        }
        return null;
    }

    static long getDataMask(int bitcount){
        long mask = 0;
        for(int b = 0; b < bitcount; b++){
            mask = mask | (1L << b);
        }
        return mask;
    }
    

    static void testGrids() throws Exception {
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridByteIndexLong", 8);
        GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridByte", 8);
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.NIOAttributeGridByte", 8);
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.GridBitIntervals",1); 
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.GridShortIntervals",16);
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.GridIntIntervals",32); 
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridLong", 64);
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridInt", 32); 
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridShort", 16);
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.ArrayAttributeGridShortIndexLong", 16);
        //GeneralGridTester tester = new GeneralGridTester("abfab3d.grid.BlockBasedAttributeGridByte", 8);  // bad 
        
        try {
        tester.testAll();
        } catch (Exception e){
            e.printStackTrace(Output.out);
        }        
    }

    static void testMakeCode(){
        printf("maxValue: %d, minValue: %d", IntIntervals.MAX_VALUE,IntIntervals.MIN_VALUE);
    }

    public static void main(String args[]) throws Exception {
        //testMakeCode();
        testGrids();
    }
}