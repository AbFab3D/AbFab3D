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

package logo3d;

import java.util.Arrays;

/**
 * Draw a line in discrete 3D space.
 * 
 * Uses a modified Bresenham approach.
 * 
 * @author James Gray
 */
public class DrawLine {
    
	/**
     * Constructor does nothing (yet?).
     */
    public DrawLine() {
        
    }
    
    /**
     * Draws a line between two points using Bresenham in 3D.
     * 
     * @param p0, a point in discrete 3D space
     * @param p1, another point in discrete 3D space
     * @return an array of Point3D points describing a line
     *         connecting p0 and p1, following Bresenham criteria
     */
    public Point3D[] drawline(int[] p0, int[] p1) {
        
        /**
         * Due to dimensional independence, the 2D algorithm
         * can be applied fairly directly to find a 3D line.
         * 
         * This implementation can probably be made more efficient
         * if it starts getting used a lot and becomes important.
         */
    	
    	Point3D[] line;
    	
    	// find the dimensional displacements
        int[] dr = {Math.abs(p1[0]-p0[0]), Math.abs(p1[1]-p0[1]), Math.abs(p1[2]-p0[2])};
        
        // check if we're in a reduced-dimensionality case
        int dim = 3;
        for (int i = 0; i < 3; i++) {
        	if (dr[i] == 0) {
        		dim -= 1;
        	}
        }
        switch (dim) {
        	case 0:
        		line = new Point3D[1];
        		line[0] = new Point3D(p0);
        		return line;
        	case 1:
        		int nz = 0;
        		int[] zers = {1,2};
        		if (dr[1] > 0) {
        			nz = 1;
        			zers = new int[] {0,2};
        		} else if (dr[2] > 0) {
        			nz = 2;
        			zers = new int[] {0,1};
        		}
        		line = new Point3D[dr[nz]+1];
        		int step = 1;
        		if (p0[nz] > p1[nz]) {
        			step = -step;
        		}
        		for (int i = 0; i < dr[nz]+1; i++) {
        			int[] p = {0,0,0};
        			p[nz] += p0[nz]+i*step;
        			p[zers[0]] = p0[zers[0]];
        			p[zers[1]] = p0[zers[1]];
        			line[i] = new Point3D(p);
        		}
        		return line;
        	case 2:
        		int[] nzs;
        		int zer;
        		if (dr[0] == 0) {
        			nzs = new int[] {1,2};
        			zer = 0;
        		} else if (dr[1] == 0) {
        			nzs = new int[] {0,2};
        			zer = 1;
        		} else {
        			nzs = new int[] {0,1};
        			zer = 2;
        		}
        		Point3D[] tlin = line2D(
        				new int[] {p0[nzs[0]],p0[nzs[1]],0}, 
        				new int[] {p1[nzs[0]],p1[nzs[1]],0});
        		line = new Point3D[tlin.length];
        		for (int i = 0; i < line.length; i++) {
        			int[] tp = {0,0,0};
        			tp[nzs[0]] = tlin[i].x;
        			tp[nzs[1]] = tlin[i].y;
        			tp[zer] = p0[zer];
        			line[i] = new Point3D(tp);
        		}
        		return line;
        }
        
        // find the median-length dimension
        int[] tmp = {dr[0],dr[1],dr[2]};
        Arrays.sort(tmp);
        int nrot = 0;
        if (dr[1] == tmp[1]) {
        	nrot = 1;
        } else if (dr[2] == tmp[1]) {
        	nrot = -1;
        }
        
        // rotate so the median direction is in zeroth bin
        Point3D r0 = new Point3D(p0);
        Point3D r1 = new Point3D(p1);
        if (nrot != 0) {
        	r0.rot(nrot);
        	r1.rot(nrot);
        }
        
        // find 2D lines
        Point3D[] line0 = line2D(new int[] {r0.x,r0.y,0}, new int[] {r1.x,r1.y,0});
        Point3D[] line1 = line2D(new int[] {r0.x,r0.z,0}, new int[] {r1.x,r1.z,0});
        
        // merge result
        // the median-length dimension should be common-valued in both lines
        // but note that it may head in the wrong direction
        if (line1.length > line0.length) {
        	line = line1;
        } else {
        	line = line0;
        	line0 = line1;
        }
        int step = 1;
        int pt = 0;
        if (line[0].x != line0[0].x) {
        	step = -step;
        	pt = line0.length - 1;
        }
        for (int i = 0; i < line.length; i++) {
        	while (line[i].x != line0[pt].x) {
        		pt+=step;
        	}
        	line[i].z = line0[pt].y;
        }
        
        // rotate back to initial frame
        if (nrot != 0) {
        	for (int i = 0; i < line.length; i++) {
        		line[i].rot(-nrot);
        	}
        }
        
        return line;
    }
    
    /**
     * Draws a line between two points using Bresenham in 3D.
     * 
     * @param p0, a point in discrete 3D space
     * @param p1, another point in discrete 3D space
     * @return an array of Point3D points describing a line
     *         connecting p0 and p1, following Bresenham criteria
     */
    public Point3D[] drawline(Point3D p0, Point3D p1) {
    	return drawline(p0.p(), p1.p());
    }
    
    /**
     * Does the Bresenham line algorithm in 2D.
     * 
     * @return a line of Point3D (x=p[0], y=p[1], z=0)
     */
    private Point3D[] line2D(Point3D p0, Point3D p1) {
    	
    	/**
         * Consider the following pseudocode taken from
         * Wikipedia's page on the Bresenham line algorithm.
         * 
         * TODO: optimize?
         * 
         * boolean steep := abs(y1 - y0) > abs(x1 - x0)
         * if steep then
         *     swap(x0, y0)
         *     swap(x1, y1)
         * if x0 > x1 then
         *     swap(x0, x1)
         *     swap(y0, y1)
         * int deltax := x1 - x0
         * int deltay := abs(y1 - y0)
         * real error := 0
         * real deltaerr := deltay / deltax
         * int ystep
         * int y := y0
         * if y0 < y1 then ystep := 1 else ystep := -1
         * for x from x0 to x1
         *     if steep then plot(y,x) else plot(x,y)
         *     error := error + deltaerr
         *     if error >= 0.5 then
         *         y := y + ystep
         *         error := error - 1.0
         */
    	
    	boolean steep = Math.abs(p1.y-p0.y) > Math.abs(p1.x-p0.x);
    	if (steep) {
    		p0.set(p0.y,p0.x,0);
    		p1.set(p1.y,p1.x,0);
    	}
    	if (p0.x > p1.x) {
    		Point3D tmp = new Point3D(p0.x,p0.y,p0.z);
    		p0.set(p1.x,p1.y,0);
    		p1.set(tmp.x,tmp.y,0);
    	}
    	
    	int dx = p1.x - p0.x;
    	int dy = Math.abs(p1.y - p0.y);
    	
    	double err = 0;
    	double derr = (double) dy / (double) dx;
    	
    	int y = p0.y;
    	
    	int ystep = -1;
    	if (p0.y < p1.y) {
    		ystep = 1;
    	}
    	
    	// swapped above if x descending
    	Point3D[] line = new Point3D[1+Math.max(dx, dy)];
    	for (int i = 0; i <= dx; i++) {
    		if (steep) {
    			line[i] = new Point3D(y,p0.x+i,0); 
    		} else {
    			line[i] = new Point3D(p0.x+i,y,0);
    		}
    		
    		err += derr;
    		if (err >= 0.5) {
    			y += ystep;
    			err -= 1.0;
    		}
    	}
    	return line;
    }
    
    /**
     * Does the Bresenham line algorithm in 2D.
     * 
     * @return a line of Point3D (x=p[0], y=p[1], z=0)
     */
    private Point3D[] line2D(int[] p0, int[] p1) {
    	return line2D(new Point3D(p0),new Point3D(p1));
    }
}

class Point3D {
    public int x;
    public int y;
    public int z;
    
    /**
     * Construct point.
     */
    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Construct point.
     */
    public Point3D(int[] p) {
        set(p);
    }
    
    /**
     * Change point values using array input.
     * 
     * @param p the values {x,y,z} to set
     */
    public void set(int[] p) {
        set(p[0],p[1],p[2]);
    }
    
    /**
     * Change point values using array input.
     * 
     * @param x,y,z the values to set
     */
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Return point values as array.
     * 
     * @return {x,y,z}
     */
    public int[] p() {
        return new int[] {x,y,z};
    }
    
    /**
     * Shift the point by the input displacements.
     * 
     * @param dx, x = x + dx
     * @param dy, y = y + dy
     * @param dz, z = z + dz
     */
    public void shift(int dx, int dy, int dz) {
    	x += dx;
    	y += dy;
    	z += dz;
    }
    
    /**
     * Shift the point by the input displacement.
     * 
     * @param dr, p = p + dr
     */
    public void shift(int[] dr) {
    	shift(dr[0],dr[1],dr[2]);
    }
    
    /**
     * Shift the point by the input displacement.
     * 
     * @param dr, p = p + dr
     */
    public void shift(Point3D dr) {
    	shift(dr.p());
    }
    
    /**
     * Rotate the point according to RHR.
     * 
     * @param direction, 1 or -1
     *             if  1, x -> y -> z -> x
     *             if -1, x <- y <- z <- x
     *             else nothing happens
     */
    public void rot(int direction) {
        if (direction == 1) {
            int[] p = {y,z,x};
            set(p);
        } else if (direction == -1) {
            int[] p = {z,x,y};
            set(p);
        }
    }
    
    /**
     * Finds the (absolute, Euclidean) distance between
     * this point and another input point.
     * 
     * Note that this includes operations which may be
     * expensive in bulk. Using sd() is an option in
     * cases where speed is critical and only ordering
     * matters (e.g. similarity, clustering).
     * 
     * @param p, the point to compare this point to
     * @return the Euclidean distance
     */
    public double dist(int[] p) {
        return dist(new Point3D(p));
    }
    
    /**
     * Finds the (absolute, Euclidean) distance between
     * this point and another input point.
     * 
     * Note that this includes operations which may be
     * expensive in bulk. Using sd() is an option in
     * cases where speed is critical and only ordering
     * matters (e.g. similarity, clustering).
     * 
     * @param p, the point to compare this point to
     * @return the Euclidean distance
     */
    public double dist(int x, int y, int z) {
        return dist(new Point3D(x,y,z));
    }
    
    /**
     * Finds the (absolute, Euclidean) distance between
     * this point and another input point.
     * 
     * Note that this includes operations which may be
     * expensive in bulk. Using sd() is an option in
     * cases where speed is critical and only ordering
     * matters (e.g. similarity, clustering).
     * 
     * @param p, the point to compare this point to
     * @return the Euclidean distance
     */
    public double dist(Point3D p) {
        return Math.sqrt(Math.pow((double) (x-p.x), 2)
                        +Math.pow((double) (y-p.y), 2)
                        +Math.pow((double) (z-p.z), 2));
    }
    
    /**
     * Finds the squared distance between this point
     * and another input point. This is faster than
     * finding proper Euclidean distance as the sqrt
     * operation is omitted, and often effective if
     * only ordering or order-of-distance is needed.
     * 
     * @param p, the point to compare this point to
     * @return (x1-x0)^2 + (y1-y0)^2 + (z1-z0)^2
     */
    public double sd(int[] p) {
        return sd(new Point3D(p));
    }
    
    /**
     * Finds the squared distance between this point
     * and another input point. This is faster than
     * finding proper Euclidean distance as the sqrt
     * operation is omitted, and often effective if
     * only ordering or order-of-distance is needed.
     * 
     * @param p, the point to compare this point to
     * @return (x1-x0)^2 + (y1-y0)^2 + (z1-z0)^2
     */
    public double sd(int x, int y, int z) {
        return sd(new Point3D(x,y,z));
    }
    
    /**
     * Finds the squared distance between this point
     * and another input point. This is faster than
     * finding proper Euclidean distance as the sqrt
     * operation is omitted, and often effective if
     * only ordering or order-of-distance is needed.
     * 
     * @param p, the point to compare this point to
     * @return (x1-x0)^2 + (y1-y0)^2 + (z1-z0)^2
     */
    public double sd(Point3D p) {
        return Math.pow((double) (x-p.x), 2)
              +Math.pow((double) (y-p.y), 2)
              +Math.pow((double) (z-p.z), 2);
    }
}

