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

package abfab3d.datasources;

// External Imports


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;

        import abfab3d.core.Bounds;


        import static abfab3d.core.Output.printf;

        import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of DataSourceGrid
 *
 * @version
 */
public class TestDataSourceGrid extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDataSourceGrid.class);
    }

    public void testNothing(){
    }
    
    public void devTestBytePacking(){
        
        testGrid( makeGridByte());
        testGrid( makeGridShort());
        testGrid( makeGridInt());

    }
    
    void testGrid(AttributeGrid grid){
        printf("test grid: %s\n", grid);
    
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    grid.setAttribute(x,y,z,(z+1) | ((x+1) << 4) | ((y+1) << 8));
                }
            }        
        }
        
        DataSourceGrid ds = new DataSourceGrid(grid);
        int data[] = new int[ds.getBufferSize()];
        ds.getBuffer(data);
        for(int i = 0; i < data.length; i++){
            printf("%08x\n", data[i]);
        }
    }

    AttributeGrid makeGridByte(){

        double vs = 1*MM;
        double s = 2*vs;
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(new Bounds(-s,s,-s,s,-s,s), vs, vs);
        grid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DISTANCE, "dist", 8,0,-1*MM, 1*MM)));
        return grid;

    }

    AttributeGrid makeGridShort(){

        double vs = 1*MM;
        double s = 2*vs;
        ArrayAttributeGridShort grid = new ArrayAttributeGridShort(new Bounds(-s,s,-s,s,-s,s), vs, vs);
        grid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DISTANCE, "dist", 16,0,-1*MM, 1*MM)));
        return grid;

    }

    AttributeGrid makeGridInt(){

        double vs = 1*MM;
        double s = 2*vs;
        ArrayAttributeGridInt grid = new ArrayAttributeGridInt(new Bounds(-s,s,-s,s,-s,s), vs, vs);
        grid.setDataDesc(GridDataDesc.getDistBGR(2*vs));
        return grid;

    }

   public static void main(String[] args) {
        new TestDataSourceGrid().devTestBytePacking();
    }
}