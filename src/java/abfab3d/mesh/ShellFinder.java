/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.mesh;

import java.util.*;

import javax.vecmath.Vector3d;

import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Vec;
import abfab3d.util.StructMixedData;
import abfab3d.util.StackOfInt;
import abfab3d.core.TriangleCollector;

import static abfab3d.core.Output.printf;

/**
 * Shell finding utilities for WingedEdgeMesh.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class ShellFinder {
    
    public ShellFinder(){        
    }

    /**
     * Find shells in a mesh.
     *
     * @param mesh
     * @return
     */
    public ShellInfo[] findShells(WingedEdgeTriangleMesh mesh){     
        
        Hashtable<Integer,Integer> hfaces = makeUnmarkedFaces(mesh);

        ArrayList<ShellInfo> vsi = new ArrayList<ShellInfo>();

        while(true){
            
            Enumeration<Integer> e = hfaces.elements(); 
            if(!e.hasMoreElements())
                break;
            int shellFace = e.nextElement();
            ShellInfo si = markShell(mesh, shellFace, hfaces);
            vsi.add(si);
        }

        return (ShellInfo[])vsi.toArray(new ShellInfo[vsi.size()]);
    }


    /**
     * Find shells sorted by volume.  ShellInfo will contain the volume.
     *
     * @param mesh
     * @return
     */
    public ShellInfo[] findShellsSorted(WingedEdgeTriangleMesh mesh, boolean naturalOrder){
        return findShellsSorted(mesh,naturalOrder,false);
    }

    /**
     * Find shells sorted by volume.  ShellInfo will contain the volume.
     *
     * @param mesh
     * @return
     */
    public ShellInfo[] findShellsSorted(WingedEdgeTriangleMesh mesh, boolean naturalOrder, boolean calcBounds){
        return findShellsSorted(mesh,naturalOrder,calcBounds,Integer.MAX_VALUE);
    }

    /**
     * Find shells sorted by volume.  ShellInfo will contain the volume.
     *
     * @param mesh
     * @param naturalOrder Sort via natural order or reverse
     * @param calcBounds Should we calculate bounds info per shell
     * @param maxShells Stop after finding this many shell.  Returns null
     * @return
     */
    public ShellInfo[] findShellsSorted(WingedEdgeTriangleMesh mesh, boolean naturalOrder, boolean calcBounds, int maxShells){

        Hashtable<Integer,Integer> hfaces = makeUnmarkedFaces(mesh);
        ArrayList<ShellInfo> vsi = new ArrayList<ShellInfo>();
        AreaCalculator ac = new AreaCalculator();
        BoundsCalculator bc = new BoundsCalculator();

        int cnt = 0;
        while(true){

            Enumeration<Integer> e = hfaces.elements();
            if(!e.hasMoreElements())
                break;
            int shellFace = e.nextElement();
            ShellInfo si = markShell(mesh, shellFace, hfaces);
            vsi.add(si);

            if (!calcBounds) {
                ac.reset();
                getShell(mesh, si.startFace, ac, hfaces);
                si.volume = ac.getVolume();
            } else {
                ac.reset();
                bc.reset();
                getShell(mesh, si.startFace, ac,bc, hfaces);
                si.volume = ac.getVolume();
                si.bounds = new double[6];
                bc.getBounds(si.bounds);
            }

            cnt++;

            if (cnt > maxShells) {
                return null;
            }
        }

        Collections.sort(vsi, new ShellVolumeComparator(naturalOrder));
        return (ShellInfo[])vsi.toArray(new ShellInfo[vsi.size()]);
    }

    /**

       collect triangles from the shell starting from startFace to TriangleCollector 

     */
    public void getShell(WingedEdgeTriangleMesh mesh, int startFace, TriangleCollector tc){
        
        Hashtable<Integer,Integer> unmarkedFaces = makeUnmarkedFaces(mesh); // Why table not Map?
        StackOfInt facesToCheck = new StackOfInt(100000);                

        StructMixedData faces = mesh.getFaces();
        StructMixedData halfEdges = mesh.getHalfEdges();
        int currentFace = startFace;

        while(currentFace != -1){
            
            unmarkedFaces.remove(new Integer(currentFace));   // TODO: garbage
            sendFace(currentFace, mesh, tc);
            
            
            int he = Face.getHe(faces, currentFace);

            // face as 3 adjacent faces linked via HalfEdges
            int twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {            
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {            
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);

            if(twin != -1) {  
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }
            
            currentFace = findNextFace(unmarkedFaces,facesToCheck);
            
        }    
        
        return;
    }

    /**

     collect triangles from the shell starting from startFace to TriangleCollector

     */
    public void getShell(WingedEdgeTriangleMesh mesh, int startFace, TriangleCollector tc,Hashtable<Integer,Integer> unmarkedFaces){

        StackOfInt facesToCheck = new StackOfInt(100000);

        StructMixedData faces = mesh.getFaces();
        StructMixedData halfEdges = mesh.getHalfEdges();
        int currentFace = startFace;

        while(currentFace != -1){

            unmarkedFaces.remove(new Integer(currentFace));   // TODO: garbage
            sendFace(currentFace, mesh, tc);


            int he = Face.getHe(faces, currentFace);

            // face as 3 adjacent faces linked via HalfEdges
            int twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);

            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            currentFace = findNextFace(unmarkedFaces,facesToCheck);

        }

        return;
    }

    public TriangleProducer getShell(WingedEdgeTriangleMesh mesh, int startFace) {
        return new TPWrapper(this,mesh,startFace);
    }

    static class TPWrapper implements TriangleProducer {
        ShellFinder finder;
        WingedEdgeTriangleMesh mesh;
        int startFace;

        public TPWrapper(ShellFinder finder,WingedEdgeTriangleMesh mesh, int startFace) {
            this.finder = finder;
            this.mesh = mesh;
            this.startFace = startFace;
        }

        @Override
        public boolean getTriangles(TriangleCollector tc) {
            finder.getShell(mesh,startFace,tc);

            return true;
        }
    }

    /**

     collect triangles from the shell starting from startFace to TriangleCollector

     */
    public void getShell(WingedEdgeTriangleMesh mesh, int startFace, TriangleCollector tc1, TriangleCollector tc2){

        Hashtable<Integer,Integer> unmarkedFaces = makeUnmarkedFaces(mesh); // Why table not Map?
        StackOfInt facesToCheck = new StackOfInt(100000);

        StructMixedData faces = mesh.getFaces();
        StructMixedData halfEdges = mesh.getHalfEdges();
        int currentFace = startFace;

        while(currentFace != -1){

            unmarkedFaces.remove(new Integer(currentFace));   // TODO: garbage
            sendFace(currentFace, mesh, tc1);
            sendFace(currentFace, mesh, tc2);


            int he = Face.getHe(faces, currentFace);

            // face as 3 adjacent faces linked via HalfEdges
            int twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);

            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            currentFace = findNextFace(unmarkedFaces,facesToCheck);

        }

        return;
    }

    /**

     collect triangles from the shell starting from startFace to TriangleCollector

     */
    public void getShell(WingedEdgeTriangleMesh mesh, int startFace, TriangleCollector tc1, TriangleCollector tc2,Hashtable<Integer,Integer> unmarkedFaces){

        StackOfInt facesToCheck = new StackOfInt(100000);

        StructMixedData faces = mesh.getFaces();
        StructMixedData halfEdges = mesh.getHalfEdges();
        int currentFace = startFace;

        while(currentFace != -1){

            unmarkedFaces.remove(new Integer(currentFace));   // TODO: garbage
            sendFace(currentFace, mesh, tc1);
            sendFace(currentFace, mesh, tc2);


            int he = Face.getHe(faces, currentFace);

            // face as 3 adjacent faces linked via HalfEdges
            int twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);

            if(twin != -1) {
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            currentFace = findNextFace(unmarkedFaces,facesToCheck);

        }

        return;
    }

    Hashtable<Integer,Integer> makeUnmarkedFaces(WingedEdgeTriangleMesh mesh){
        
        StructMixedData faces = mesh.getFaces();
        int f = mesh.getStartFace();
        Hashtable<Integer,Integer> hfaces = new Hashtable<Integer,Integer>();

        while(f != -1) {
            Integer key = new Integer(f);
            //printf("face: %d\n", f);
            //faceCount++;
            f = Face.getNext(faces,f);
            hfaces.put(key,key);

        }

        return hfaces; 
    }

    /**
       
     */
    ShellInfo markShell(WingedEdgeTriangleMesh mesh, int startFace, Hashtable<Integer,Integer> unmarkedFaces){
        
        ShellInfo si = new ShellInfo();
        StackOfInt facesToCheck = new StackOfInt(100000);
        
        si.startFace = startFace;
        si.faceCount = 0;
        
        StructMixedData faces = mesh.getFaces();
        StructMixedData halfEdges = mesh.getHalfEdges();
        
        int currentFace = startFace;
        while(currentFace != -1){
            
            unmarkedFaces.remove(new Integer(currentFace));
            si.faceCount++;
            
            //printf("currentFace: %d\n", currentFace);
                        
            int he = Face.getHe(faces, currentFace);

            // face as 3 adjacent faces linked via HalfEdges
            int twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {            
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);
            if(twin != -1) {            
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }

            he = HalfEdge.getNext(halfEdges, he);
            twin = HalfEdge.getTwin(halfEdges, he);

            if(twin != -1) {            
                processFace(HalfEdge.getLeft(halfEdges, twin), unmarkedFaces, facesToCheck);
            }
            
            currentFace = findNextFace(unmarkedFaces,facesToCheck);
            
        }    
        
        return si;
    }

    void processFace(int face, Hashtable<Integer,Integer> unmarkedFaces, StackOfInt facesToCheck){

        Integer fi = new Integer(face);

        if(unmarkedFaces.get(fi) != null){

            facesToCheck.push(fi.intValue());
        }                
    }

    int findNextFace(Hashtable<Integer,Integer> unmarkedFaces, StackOfInt facesToCheck){

        int face;
        //printf("findNextFace()\n");
        while((face = facesToCheck.pop()) != StackOfInt.NO_DATA){
            
            //printf("popped: %d\n", face);
            Integer f;
            if((f = unmarkedFaces.get(new Integer(face))) != null){

                //printf("unarked return %d\n", f.intValue());
                
                return f.intValue();
                
            } else {

                //printf("marked\n");
                
            }
        }
        //printf("returns -1\n");
        
        return -1;
    }


    Vector3d 
        p0 = new Vector3d(),
        p1 = new Vector3d(),
        p2 = new Vector3d();

    double[] pnt = new double[3];


    void sendFace(int face, WingedEdgeTriangleMesh mesh, TriangleCollector tc){
        
        int he = Face.getHe(mesh.getFaces(), face);

        StructMixedData hedges = mesh.getHalfEdges();
        StructMixedData vertices = mesh.getVertices();
        

        int he1 = HalfEdge.getNext(hedges,he);
        int
            v0 = HalfEdge.getStart(hedges, he),
            v1 = HalfEdge.getEnd(hedges, he),
            v2 = HalfEdge.getEnd(hedges, he1);
        
        Vertex.getPoint(vertices, v0, pnt);
        p0.set(pnt);
        Vertex.getPoint(vertices, v1, pnt);
        p1.set(pnt);
        Vertex.getPoint(vertices, v2, pnt);
        p2.set(pnt);
        
        tc.addTri(p0, p1, p2);        
        
    }


    /**
     * Represents one shell of given WETriangleMesh
     */
    public static class ShellInfo {
        public int startFace; 
        public int faceCount;

        /** The shell volume or 0 if not calculated.  m^3 */
        public double volume;

        /** The bounds or null if not calculated */
        public double[] bounds;
    }

    static class ShellVolumeComparator implements Comparator<ShellInfo> {
        private boolean naturalOrder;

        public ShellVolumeComparator(boolean naturalOrder) {
            this.naturalOrder = naturalOrder;
        }

        @Override
        public int compare(ShellInfo o1, ShellInfo o2) {
            if (naturalOrder) {
                return Double.compare(o1.volume,o2.volume);
            } else {
                return -Double.compare(o1.volume,o2.volume);
            }
        }
    }

    /**
     * Copied here from io.input.BoundsCalculator for compile order issues.  Simple class so hopefully it wont contain errors
     */

    static class BoundsCalculator implements TriangleCollector, AttributedTriangleCollector {


        double
                xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE,
                ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE,
                zmin = Double.MAX_VALUE, zmax = -Double.MAX_VALUE;

        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

            checkVertex(v0);
            checkVertex(v1);
            checkVertex(v2);

            return true; // to continue
        }

        @Override
        public boolean addAttTri(Vec v0, Vec v1, Vec v2) {
            checkVertex(v0);
            checkVertex(v1);
            checkVertex(v2);

            return true; // to continue
        }

        public void checkVertex(Vector3d v){

            double x = v.x;
            double y = v.y;
            double z = v.z;

            if(x < xmin) xmin = x;
            if(x > xmax) xmax = x;

            if(y < ymin) ymin = y;
            if(y > ymax) ymax = y;

            if(z < zmin) zmin = z;
            if(z > zmax) zmax = z;

        }

        public void checkVertex(Vec v){

            double x = v.v[0];
            double y = v.v[1];
            double z = v.v[2];

            if(x < xmin) xmin = x;
            if(x > xmax) xmax = x;

            if(y < ymin) ymin = y;
            if(y > ymax) ymax = y;

            if(z < zmin) zmin = z;
            if(z > zmax) zmax = z;

        }

        public void getBounds(double bounds[]){

            bounds[0] = xmin;
            bounds[1] = xmax;
            bounds[2] = ymin;
            bounds[3] = ymax;
            bounds[4] = zmin;
            bounds[5] = zmax;

        }

        /**
         * Reset all variables so this class can be reused;
         */
        public void reset() {
            xmin = Double.MAX_VALUE;
            xmax = -Double.MAX_VALUE;
            ymin = Double.MAX_VALUE;
            ymax = -Double.MAX_VALUE;
            zmin = Double.MAX_VALUE;
            zmax = -Double.MAX_VALUE;
        }
    }


}
