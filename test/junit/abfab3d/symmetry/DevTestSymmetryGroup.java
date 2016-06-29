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

package abfab3d.symmetry;

import javax.vecmath.Vector3d;
import abfab3d.core.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridDataWriter;
import abfab3d.grid.op.GridMaker;

import abfab3d.core.Bounds;

import abfab3d.transforms.FriezeSymmetry;
import abfab3d.transforms.WallpaperSymmetry;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.BaseTransform;

import abfab3d.datasources.TransformableDataSource;
import abfab3d.datasources.Union;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Box;


import abfab3d.core.Vec;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;

public class DevTestSymmetryGroup {
    
    static SymmetryGroup getTwoPlanes(double x1, double x2){
        
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new EPlane(new Vector3d(-1,0,0), -x1), // right of  plane 1
                                 new EPlane(new Vector3d(1,0,0), x2),   // left of plane 2
                             }
                             );
        
    }

    static SymmetryGroup getCube(){
        
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new EPlane(new Vector3d(-1,0,0), 1), 
                                 new EPlane(new Vector3d(1,0,0), 1),  
                                 new EPlane(new Vector3d(0,1,0), 1),  
                                 new EPlane(new Vector3d(0,-1,0), 1),  
                                 new EPlane(new Vector3d(0,0,1), 1),  
                                 new EPlane(new Vector3d(0,0,-1), 1),  
                             }
                             );
        
    }

    static SymmetryGroup getCubeE(){
        
        EPlane sides[] = new EPlane[] { 
            new EPlane(new Vector3d(-1,0,0), 1), 
            new EPlane(new Vector3d(1,0,0), 1),  
            new EPlane(new Vector3d(0,1,0), 1),  
            new EPlane(new Vector3d(0,-1,0), 1),  
            new EPlane(new Vector3d(0,0,1), 1),  
            new EPlane(new Vector3d(0,0,-1), 1),  
        };

        return new SymmetryGroup(sides, 
                                 new ETransform[]{
                                     new ETransform(sides[0]),
                                     new ETransform(sides[1]),
                                     new ETransform(sides[2]),
                                     new ETransform(sides[3]),
                                     new ETransform(sides[4]),
                                     new ETransform(sides[5])
                                 }
                                 );
        
    }

    static SymmetryGroup getSphere(){
        
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new ESphere(new Vector3d(0,0,0),1), 
                             }
                             );
        
    }

    static SymmetryGroup getLens(){
        double x = 1 - Math.sqrt(3)/2;
        double r = 0.5;
        double C = (r*r - x*x)/(2*x);
        double R = Math.sqrt(C*C+r*r);
        printf("lens: x: %7.5f C:%7.5f R:%7.5f\n",x,C,R);
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new ESphere(new Vector3d(C,0,0),R), 
                                 new ESphere(new Vector3d(-C,0,0),R), 
                             }
                             );
        
    }
    
    public void devTestPlanes(){

        //SymmetryGroup group = getTwoPlanes(1., 2.);
        SymmetryGroup group = getCubeE();
        //SymmetryGroup group = getSphere();
        //SymmetryGroup group = getLens();
        Vec pnt = new Vec(3);
        for(int i = 0; i < 100; i++){
        //for(int i = 0; i < 1; i++){
            double x = 0.1*i;
            double y = x;
            double z = y;
            pnt.v[0] = x;
            pnt.v[1] = y;
            pnt.v[2] = z;
            group.toFD(pnt);
            printf("(%7.3f, %7.3f, %7.3f) -> (%7.3f,%7.3f,%7.3f)\n", x,y,z,pnt.v[0],pnt.v[1],pnt.v[2]);
        }
    }

    public void devTestSphere(){

        ESphere s = new ESphere(new Vector3d(-0.8,0,0),1);
        Vec pnt = new Vec(3);
        //for(int i = 1; i <= 100; i++){
        for(int i = 0; i < 1; i++){
            double x = 0.1*i;
            double y = 0;
            double z = 0;
            pnt.v[0] = x;
            pnt.v[1] = y;
            pnt.v[2] = z;
            s.transform(pnt);
            printf("(%7.3f, %7.3f, %7.3f) -> (%7.3f,%7.3f,%7.3f)\n", x,y,z,pnt.v[0],pnt.v[1],pnt.v[2]);
        }
    }

    TransformableDataSource makeShape(double width){

        Box box = new Box(width, 0.1*width,0.1*width);
        box.setTransform(new Rotation(0,0,1,Math.PI/6));
        Union ds = new Union(box,
                             new Sphere(new Vector3d(width*0.3, 0,0),0.2*width)
                             );
        return ds;
    }

    TransformableDataSource makeShape2(double width){
        double s = 0.0;
        Box box = new Box(0, width*0.5, 0, width*0.9, 0.1*width,0.1*width);
        Box box2 = new Box(width*0.3, width*0.25, 0,0.2*width, 0.4*width, 0.1*width);

        Union ds = new Union(box, box2);
        return ds;
    }

    public void devTestGridRendering(){
        double vs = 1.;
        double s = 500*vs;
        double fdwidth = 0.2*s;
        AttributeGrid grid = new ArrayAttributeGridInt(new Bounds(-s,s,-s,s,0, vs), vs,vs);
        GridMaker gmaker = new GridMaker();
        BaseTransform trans[] = new BaseTransform[]{
            
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_II,fdwidth),
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_IX,fdwidth),
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_IS,fdwidth),
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_SII,fdwidth),
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_22I,fdwidth),
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_2SI,fdwidth),
            new FriezeSymmetry(FriezeSymmetry.FRIEZE_S22I,fdwidth),
            
            new WallpaperSymmetry(WallpaperSymmetry.WP_O,fdwidth,1.1*fdwidth,0.1),
            new WallpaperSymmetry(WallpaperSymmetry.WP_XX,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_SX,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_SS,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_632,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_S632,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_333,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_S333,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_3S3,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_442,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_S442,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_4S2,fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_2222,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_22X,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_22S,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_S2222,fdwidth,1.1*fdwidth),
            new WallpaperSymmetry(WallpaperSymmetry.WP_2S22,fdwidth,1.1*fdwidth),        
        };

        GridDataWriter writer = new GridDataWriter();
        writer.set("type",GridDataWriter.TYPE_DENSITY);
        
        for(int i = 0; i < trans.length; i++){
            
            TransformableDataSource ds = makeShape2(fdwidth);        
            ds.setTransform(trans[i]);
            gmaker.setSource(ds);
            long t0 = time();
            gmaker.makeGrid(grid);
            printf("grid ready: %d ms\n", (time() - t0));
            String symName = trans[i].getClass().getSimpleName() + "." + (String)trans[i].getParam("symmetryType").getValue();
            writer.writeSlices(grid, grid.getDataChannel(), fmt("/tmp/slices/%s.png",symName));
            
        }        
    }

    public static void main(String arg[]){
        //new DevTestSymmetryGroup().devTestPlanes();
        //new DevTestSymmetryGroup().devTestSphere();
        new DevTestSymmetryGroup().devTestGridRendering();
    }

}

