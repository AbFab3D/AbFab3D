package abfab3d.io.output;

import abfab3d.datasources.*;
import abfab3d.grid.*;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.input.STSReader;
import abfab3d.util.*;
import junit.framework.TestCase;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import javax.vecmath.Vector3d;

import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Test for STS reading and writing
 *
 * @author Alan Hudson
 */
public class TestSTSReadWrite extends TestCase {
    public void testSTSOutput() {

        double vs = 0.5*MM;
        double margin = vs;
        int threadCount = 8;

        double s = 20*MM;

        double bounds[] = new double[]{-s, s, -s, s, -s, s};

        MathUtil.roundBounds(bounds, vs);
        bounds = MathUtil.extendBounds(bounds, margin);

        int nx[] = MathUtil.getGridSize(bounds, vs);

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);

        Intersection density = new Intersection();

        density.add(new Sphere(new Vector3d(0,0,0), s));
        density.add(new Sphere(new Vector3d(0,0,0), -(s-1*MM)));
        density.add(new Plane(new Vector3d(1,0,0), 0));


        DataSource color1 = new HalfGyroid(0.3*s);

        DataSourceMixer mux = new DataSourceMixer(density, color1);

        AttributeMaker attmuxer = new AttributeMakerGeneral(new int[]{8,8});

        GridMaker gm = new GridMaker();

        gm.setSource(mux);
        gm.setThreadCount(threadCount);

        gm.setAttributeMaker(attmuxer);

        AttributeGrid grid = new ArrayAttributeGridInt(nx[0], nx[1], nx[2], vs, vs);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));

        MaterialMaker[] makers = new MaterialMaker[]{new MaterialMaker(new ChannelDensityMaker(0), MaterialURN.SHAPEWAYS_STRONG_AND_FLEXIBLE_PLASTIC),
                new MaterialMaker(new ChannelDensityMaker(1),MaterialURN.SHAPEWAYS_SILVER)};

        STSWriter writer = new STSWriter();

        new File("/tmp/sts").mkdirs();
        // TODO: change this to in memory to avoid filesystem issues
        writer.write(grid,makers,new String[] {FinishURN.SHAPEWAYS_POLISHED_HAND},"/tmp/sts/foo2.sts");
    }

    public void testSTSInputFile() {
        STSReader reader = new STSReader();

        try {
            TriangleMesh[] meshes = reader.loadMeshes("/tmp/sts/foo2.sts");

            assertNotNull("Mesh array is null",meshes);
            assertEquals("Mesh count", meshes.length,2);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOError: " + ioe.getMessage());
        }
    }

    public void _testSTSInputStream() throws IOException {

        FileInputStream fis = new FileInputStream("/tmp/sts/foo2.sts");
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(bis);

        STSReader reader = new STSReader();

        try {
            TriangleMesh[] meshes = reader.loadMeshes(zis);

            assertNotNull("Mesh array is null",meshes);
            assertEquals("Mesh count", meshes.length,2);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOError: " + ioe.getMessage());
        }
    }

    public void _testSTSInput() {

        double vs = 0.5*MM;
        double margin = vs;
        int threadCount = 8;

        double s = 20*MM;

        double bounds[] = new double[]{-s, s, -s, s, -s, s};

        MathUtil.roundBounds(bounds, vs);
        bounds = MathUtil.extendBounds(bounds, margin);

        int nx[] = MathUtil.getGridSize(bounds, vs);

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);

        Intersection density = new Intersection();

        density.add(new Sphere(new Vector3d(0,0,0), s));
        density.add(new Sphere(new Vector3d(0,0,0), -(s-1*MM)));
        density.add(new Plane(new Vector3d(1,0,0), 0));


        DataSource color1 = new HalfGyroid(0.3*s);

        DataSourceMixer mux = new DataSourceMixer(density, color1);

        AttributeMaker attmuxer = new AttributeMakerGeneral(new int[]{8,8});

        GridMaker gm = new GridMaker();

        gm.setSource(mux);
        gm.setThreadCount(threadCount);

        gm.setAttributeMaker(attmuxer);

        AttributeGrid grid = new ArrayAttributeGridInt(nx[0], nx[1], nx[2], vs, vs);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));

        MaterialMaker[] makers = new MaterialMaker[]{new MaterialMaker(new ChannelDensityMaker(0), MaterialURN.SHAPEWAYS_STRONG_AND_FLEXIBLE_PLASTIC),
                new MaterialMaker(new ChannelDensityMaker(1),MaterialURN.SHAPEWAYS_SILVER)};

        ByteArrayOutputStream baos = null;

        try {
            STSWriter writer = new STSWriter();

            baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            ZipOutputStream zos = new ZipOutputStream(bos);

            writer.write(grid, makers, new String[]{FinishURN.SHAPEWAYS_POLISHED_HAND}, zos);
            zos.close();
            bos.close();
            baos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: " + ioe.getMessage());
        }

        byte[] buff = baos.toByteArray();

        assertNotNull("Byte array is null",buff);
        assertTrue("Byte array is 0",buff.length > 0);

        printf("Zip is: %d bytes\n",buff.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(buff);
        BufferedInputStream bis = new BufferedInputStream(bais);
        ZipInputStream zis = new ZipInputStream(bis);

        STSReader reader = new STSReader();

        try {
            TriangleMesh[] meshes = reader.loadMeshes(zis);

            assertNotNull("Mesh array is null",meshes);
            assertEquals("Mesh count", meshes.length,2);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("IOError: " + ioe.getMessage());
        }
    }

    static class ChannelDensityMaker implements DensityMaker {

        int mat;

        ChannelDensityMaker(int mat){

            this.mat = mat;

        }

        public double makeDensity(long attribute){

            double dens = (attribute & 0xFF)/255.;
            double mdens = ((attribute >> 8) & 0xFF) / 255.;
            switch(mat){
                case 0:
                    break;
                case 1:
                    mdens = 1-mdens;
            }

            if(mdens < dens)
                return mdens;
            else
                return dens;
        }

    }


    static class HalfGyroid  extends TransformableDataSource {

        private double period = 10*MM;
        private double level = 0;
        private double offsetX = 0,offsetY = 0,offsetZ = 0;
        private double factor = 0;

        public HalfGyroid(double period){
            this.period = period;
        }

        public void setPeriod(double value){
            this.period = value;
        }

        public void setLevel(double value){
            this.level = value;
        }

        public void setOffset(double offsetX, double offsetY,double offsetZ){
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public int initialize(){
            super.initialize();
            this.factor = 2*PI/period;

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0]-offsetX;
            double y = pnt.v[1]-offsetY;
            double z = pnt.v[2]-offsetZ;

            x *= factor;
            y *= factor;
            z *= factor;

            double vs = pnt.getScaledVoxelSize();

            double d = ( sin(x)*cos(y) + sin(y)*cos(z) + sin(z) * cos(x) - level)/factor;

            data.v[0] = step10(d, 0, vs);

            return RESULT_OK;
        }

    }

}
