package abfab3d.io.output;


import abfab3d.grid.*;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class ConnectDiagonal implements Operation, ClassTraverser, ClassAttributeTraverser {
    private Grid grid;
    private Grid dest;
    private AttributeGrid gridAtt;
    private AttributeGrid destAtt;

    private int[] gcoords;
    private long cnt;
    int[][] corners = new int[][] {
            {-1,-1,0},
            {-1,1,0},
            {1,-1,0},
            {1,1,0},
    };
    int[][] touching = new int[][] {
            {-1,0,0,-1,1,0},
            {-1,0,0,0,1,0},
            {1,0,0,0,-1,0},
            {0,1,0,1,0,0}
    };

    public ConnectDiagonal(Grid dest) {
        if (dest instanceof AttributeGrid) {
            destAtt = (AttributeGrid) dest;
        } else {
            this.dest = dest;
        }
    }

    @Override
    public Grid execute(Grid grid) {
        if (grid instanceof AttributeGrid) {
            gridAtt = (AttributeGrid) grid;
        }
        this.grid = grid;

        if (dest == null) {
            System.out.println("Not dest, put into original grid");
            dest = grid;
        }

        gcoords = new int[3];
        cnt = 0;

        if (grid instanceof AttributeGrid) {
            ((AttributeGrid)grid).findAttribute(VoxelClasses.INSIDE, this);
        } else {
            grid.find(VoxelClasses.INSIDE, this);
        }

        System.out.println("Voxels fixed: " + cnt);
        return grid;
    }

    @Override
    public void found(int x, int y, int z, VoxelData vd) {
        for(int i=0; i < corners.length; i++) {
            int dx = x + corners[i][0];
            int dy = y + corners[i][1];
            int dz = z + corners[i][2];
            int tx1 = x + touching[i][0];
            int ty1 = y + touching[i][1];
            int tz1 = z + touching[i][2];
            int tx2 = x + touching[i][3];
            int ty2 = y + touching[i][4];
            int tz2 = z + touching[i][5];

            if (grid.insideGrid(dx,dy,dz)) {
                if (grid.getState(dx,dy,dz) != Grid.OUTSIDE) {
                    // Diagonal is occupied, verify no touching otherwise
//System.out.println("d: " + dx + " " + dy + " " + dz + " tx1: " + tx1 + " " + ty1 + " " + tz1 + " tx2: " + tx2 + " " + ty2 + " " + tz2);
                    if (grid.getState(tx1,ty1,tz1) == Grid.OUTSIDE) {
                        if (grid.getState(tx2,ty2,tz2) == Grid.OUTSIDE) {
                            // Diagonal detected, fix

                            destAtt.setData(dx,dy,dz, Grid.OUTSIDE, vd.getMaterial());

                            // Square, doesn't work
/*
                            dest.setData(tx1,ty1,tz1, Grid.INSIDE, vd.getAttribute());
                            dest.setData(tx2,ty2,tz2, Grid.INSIDE, vd.getAttribute());
*/
                            //dest.setData(dx,dy,dz, Grid.INSIDE, vd.getAttribute());

                            cnt++;
                        }
                    }

                }
            }
        }

    }

    @Override
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return false;
    }

    @Override
    public void found(int x, int y, int z, byte state) {
        for(int i=0; i < corners.length; i++) {
            int dx = x + corners[i][0];
            int dy = y + corners[i][1];
            int dz = z + corners[i][2];
            int tx1 = x + touching[i][0];
            int ty1 = y + touching[i][1];
            int tz1 = z + touching[i][2];
            int tx2 = x + touching[i][3];
            int ty2 = y + touching[i][4];
            int tz2 = z + touching[i][5];

            if (grid.insideGrid(dx,dy,dz)) {
                if (grid.getState(dx,dy,dz) != Grid.OUTSIDE) {
                    // Diagonal is occupied, verify no touching otherwise
//System.out.println("d: " + dx + " " + dy + " " + dz + " tx1: " + tx1 + " " + ty1 + " " + tz1 + " tx2: " + tx2 + " " + ty2 + " " + tz2);
                    if (grid.getState(tx1,ty1,tz1) == Grid.OUTSIDE) {
                        if (grid.getState(tx2,ty2,tz2) == Grid.OUTSIDE) {
                            // Diagonal detected, fix

                            dest.setState(dx,dy,dz, Grid.OUTSIDE);

                            // Square, doesn't work
/*
                            dest.setData(tx1,ty1,tz1, Grid.INSIDE, vd.getAttribute());
                            dest.setData(tx2,ty2,tz2, Grid.INSIDE, vd.getAttribute());
*/
                            //dest.setData(dx,dy,dz, Grid.INSIDE, vd.getAttribute());

                            cnt++;
                        }
                    }

                }
            }
        }

    }

    @Override
    public boolean foundInterruptible(int x, int y, int z, byte state) {
        // ignore
        return false;
    }

    public long getCount() {
        return cnt;
    }
}
