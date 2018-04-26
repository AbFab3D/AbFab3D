/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2018
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.core;

//External Imports
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.BaseTestCase;

// Internal Imports

/**
 * Tests the functionality of Units
 *
 * @author Tony Wong
 * @version
 */
public class TestUnits extends BaseTestCase  {
	
	private static final double EPS_RATIO = 0.000001;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestUnits.class);
    }
    
    public void testConvertLength() {
        UnitName[] lNames = {UnitName.M, UnitName.CM, UnitName.MM};
        double[][] expected = {
        		{1, 100, 1000},
        		{0.01, 1, 10},
        		{0.001, 0.1, 1}
        };
        
        double val = 1;
        
        for (int i=0; i<lNames.length; i++) {
        	for (int j=0; j<lNames.length; j++) {
        		double newVal = Units.convertToUnit(val, lNames[i], lNames[j]);
        		assertTrue(expected[i][j] + " != " + newVal, equals(expected[i][j], newVal, EPS_RATIO));
        	}
        }
    }
    
    public void testConvertArea() {
        UnitName[] lNames = {UnitName.M2, UnitName.CM2, UnitName.MM2};
        double[][] expected = {
        		{1, 100*100, 1000*1000},
        		{0.01*0.01, 1, 10*10},
        		{0.001*0.001, 0.1*0.1, 1}
        };
        
        double val = 1;
        
        for (int i=0; i<lNames.length; i++) {
        	for (int j=0; j<lNames.length; j++) {
        		double newVal = Units.convertToUnit(val, lNames[i], lNames[j]);
        		assertTrue(expected[i][j] + " != " + newVal, equals(expected[i][j], newVal, EPS_RATIO));
        	}
        }
    }
    
    public void testConvertVolume() {
        UnitName[] lNames = {UnitName.M3, UnitName.CM3, UnitName.MM3};
        double[][] expected = {
        		{1, 100*100*100, 1000*1000*1000},
        		{0.01*0.01*0.01, 1, 10*10*10},
        		{0.001*0.001*0.001, 0.1*0.1*0.1, 1}
        };
        
        double val = 1;
        
        for (int i=0; i<lNames.length; i++) {
        	for (int j=0; j<lNames.length; j++) {
        		double newVal = Units.convertToUnit(val, lNames[i], lNames[j]);
        		assertTrue(expected[i][j] + " != " + newVal, equals(expected[i][j], newVal, EPS_RATIO));
        	}
        }
    }
    
    private boolean equals(double val1, double val2, double ratioDiff) {
    	return ( Math.abs(1 - val2 / val1) <= ratioDiff);
    }
}
