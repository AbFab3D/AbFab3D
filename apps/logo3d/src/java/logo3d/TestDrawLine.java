package logo3d;

import junit.framework.TestCase;

/**
 * Tests the functionality of DrawLine.
 *
 * @author James Gray
 * @version
 */
public class TestDrawLine extends TestCase {

	/**
     * Test that Point3D works
     */
    public void testPoint3D() {
	    Point3D p = new Point3D(0,0,0);
    	
    	assertEquals("Result should be ", 0, p.x);
    	assertEquals("Result should be ", 0, p.y);
    	assertEquals("Result should be ", 0, p.z);
	    
    	p.shift(1,2,3);
    	
    	assertEquals("Result should be ", 1, p.x);
    	assertEquals("Result should be ", 2, p.y);
    	assertEquals("Result should be ", 3, p.z);
    	
    	p.rot(1);
    	
    	assertEquals("Result should be ", 2, p.x);
    	assertEquals("Result should be ", 3, p.y);
    	assertEquals("Result should be ", 1, p.z);
    	
    	p.shift(new Point3D(-2,-3,-1));
    	
    	assertEquals("Result should be ", 0, p.x);
    	assertEquals("Result should be ", 0, p.y);
    	assertEquals("Result should be ", 0, p.z);
    	
    	assertEquals("Result should be ", 5, (int) p.dist(3,4,0));
    	
    	assertEquals("Result should be ", 25, (int) p.sd(3,4,0));
    }
    
    /**
     * Test that drawline gives us a good 3D line
     */
    public void testDrawline() {
    	
    	// test a simple line
    	Point3D[] line = new DrawLine().drawline(new Point3D(0,0,0), new Point3D(7,3,7));
    	for (int i = 0; i < line.length; i++) {
    		assertEquals("x: ", i,   line[i].x);
    		assertEquals("y: ", i/2, line[i].y);
    		assertEquals("z: ", i,   line[i].z);
    	}
    	
    	// test a more general line
    	line = new DrawLine().drawline(new Point3D(5,7,0), new Point3D(8,0,7));
    	for (int i = 0; i < line.length; i++) {
    		assertEquals("x: ", 5+i/2, line[line.length-1-i].x);
    		assertEquals("y: ", 7-i,   line[line.length-1-i].y);
    		assertEquals("z: ", i,     line[line.length-1-i].z);
    	}
    	
    	// test a line in the negative quad
    	line = new DrawLine().drawline(new Point3D(-5,-5,-5), new Point3D(0,0,0));
    	for (int i = 0; i < line.length; i++) {
    		assertEquals("x: ", -i, line[line.length-1-i].x);
    		assertEquals("y: ", -i, line[line.length-1-i].y);
    		assertEquals("z: ", -i, line[line.length-1-i].z);
    	}
    	
    	// test a 2D line
    	line = new DrawLine().drawline(new Point3D(0,0,0), new Point3D(10,0,10));
    	for (int i = 0; i < line.length; i++) {
    		assertEquals("x: ", i, line[i].x);
    		assertEquals("y: ", 0, line[i].y);
    		assertEquals("z: ", i, line[i].z);
    	}
    	
    	// test a 1D line
    	line = new DrawLine().drawline(new Point3D(0,0,0), new Point3D(10,0,0));
    	for (int i = 0; i < line.length; i++) {
    		assertEquals("x: ", i, line[i].x);
    		assertEquals("y: ", 0, line[i].y);
    		assertEquals("z: ", 0, line[i].z);
    	}
    	
    	// test a point, i.e. "wtf, user?" case
    	line = new DrawLine().drawline(new Point3D(5,5,-5), new Point3D(5,5,-5));
    	assertEquals("x: ", 5, line[0].x);
		assertEquals("y: ", 5, line[0].y);
		assertEquals("z: ", -5, line[0].z);
		assertEquals("len: ", 1, line.length);
    }
}
