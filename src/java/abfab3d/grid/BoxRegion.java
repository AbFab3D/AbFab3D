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

/**
 * A box shaped region of a voxel space.
 *
 * @author Alan Hudson
 */
public class BoxRegion implements Region {
    /** The origin of the region in voxel coordinates */
    private int[] origin;

    /** The size of the region in voxel coordinates */
    private int[] size;

    /**
     * Constructor.
     *
     */
    public BoxRegion(int[] origin, int[] size) {
        this.origin = origin.clone();
        this.size = size.clone();
    }

    /*
     * Traverse a region and call the RegionTraverser per voxel coordinate.
     *
     * @param t The traverer to call for each voxel
     */
    public void traverse(RegionTraverser t) {
        for(int i=0; i < size[0]; i++) {
            for(int j=0; j < size[1]; j++) {
                for(int k=0; k < size[2]; k++) {
                    t.found(origin[0] + i, origin[1] + j, origin[2] + k);
                }
            }
        }
    }

    /*
     * Traverse a region and call the RegionTraverser per voxel coordinate.  Can be
     * interupted.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void traverseInterruptible(RegionTraverser t) {
        loop:
        for(int i=0; i < size[0]; i++) {
            for(int j=0; j < size[1]; j++) {
                for(int k=0; k < size[2]; k++) {
                    if (!t.foundInterruptible(origin[0] + i, origin[1] + j, origin[2] + k))
                        break loop;
                }
            }
        }
    }

    /**
     * Get the volume covered by this region.
     *
     * @return The volume
     */
    public long getVolume() {
        return size[0] * size[1] * size[2];
    }

    /**
     * Can this region be merged with another.  The region type must remain
     * the same.
     *
     * @param r The region to merge
     */
    public boolean canMerge(Region r) {
        // To merge we must remain a box.
        // start or end points must touch
        // at least 2 dimensions must match size

        if (!(r instanceof BoxRegion)) {
            return false;
        }

        BoxRegion br = (BoxRegion) r;
        int[] r_origin = new int[3];
        int[] r_size = new int[3];

        br.getOrigin(r_origin);
        br.getSize(r_size);

        int same_count = 0;
        int non_equal_dir = -1;  // Axis not equal, 0 = x, 1 = y, 2 = z

        if (size[0] == r_size[0]) {
            same_count++;
        } else {
            non_equal_dir = 0;
        }

        if (size[1] == r_size[1]) {
            same_count++;
        } else {
            non_equal_dir = 1;
        }

        if (size[2] == r_size[2]) {
            same_count++;
        } else {
            non_equal_dir = 2;
        }

        if (same_count < 2)
            return false;

/*
        int[] r_end = new int[3];
        int[] end = new int[3];


        // Should we store this for faster merges?
        end[0] = origin[0] + size[0];
        end[1] = origin[1] + size[1];
        end[2] = origin[2] + size[2];

        r_end[0] = r_origin[0] + r_size[0];
        r_end[1] = r_origin[1] + r_size[1];
        r_end[2] = r_origin[2] + r_size[2];
*/
        if (same_count == 3) {
            // All equal, must be adj in some direction

            if (origin[0] + size[0] == r_origin[0]) {
                return true;
            }

            if (origin[0] - size[0] == r_origin[0] + r_size[0]) {
                return true;
            }

            if (origin[1] + size[1] == r_origin[1]) {
                return true;
            }

            if (origin[1] - size[1] == r_origin[1] + r_size[1]) {
                return true;
            }

            if (origin[2] + size[2] == r_origin[2]) {
                return true;
            }

            if (origin[2] - size[2] == r_origin[2] + r_size[2]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Merge this region with another.
     *
     * @return true if successful.  If false no changes will be made
     */
    public boolean merge(Region r) {
        if (!(r instanceof BoxRegion)) {
            return false;
        }

        BoxRegion br = (BoxRegion) r;
        int[] r_origin = new int[3];
        int[] r_size = new int[3];

        br.getOrigin(r_origin);
        br.getSize(r_size);

        int same_count = 0;
        int non_equal_dir = -1;  // Axis not equal, 0 = x, 1 = y, 2 = z

//System.out.println("merge: " + this + " to: " + r);
        if (size[0] == r_size[0]) {
            same_count++;
        } else {
            non_equal_dir = 0;
        }

        if (size[1] == r_size[1]) {
            same_count++;
        } else {
            non_equal_dir = 1;
        }

        if (size[2] == r_size[2]) {
            same_count++;
        } else {
            non_equal_dir = 2;
        }

        if (same_count < 2)
            return false;

        if (same_count == 3) {
//System.out.println("pot1");
            // All equal, must be adj in some direction
            if (origin[0] == r_origin[0]) {
                if (origin[1] == r_origin[1]) {
                    // Right side grow
                    if (origin[2] + size[2] == r_origin[2]) {
                        size[2] += r_size[2];
                        return true;
                    }

                    // Left side grow
                    if (origin[2] == r_origin[2] + r_size[2]) {
                        origin[2] = r_origin[2];
                        size[2] += r_size[2];
                        return true;
                    }
                }
            }

            if (origin[0] == r_origin[0]) {
                if (origin[2] == r_origin[2]) {
                    // Right side grow
                    if (origin[1] + size[1] == r_origin[1]) {
                        size[1] += r_size[1];
                        return true;
                    }

                    // Left side grow
                    if (origin[1] == r_origin[1] + r_size[1]) {
                        origin[1] = r_origin[1];
                        size[1] += r_size[1];
                        return true;
                    }
                }
            }

            if (origin[1] == r_origin[1]) {
                if (origin[2] == r_origin[2]) {
                    // Right side grow
                    if (origin[0] + size[0] == r_origin[0]) {
                        size[0] += r_size[0];
                        return true;
                    }

                    // Left side grow
                    if (origin[0] == r_origin[0] + r_size[0]) {
                        origin[0] = r_origin[0];
                        size[0] += r_size[0];
                        return true;
                    }
                }
            }
        } else if (same_count == 2) {
//            System.out.println("pot2: " + non_equal_dir);
            if (non_equal_dir == 0) {
                switch(non_equal_dir) {
                    case 0:
                        if (origin[1] == r_origin[1]) {
                            if (origin[2] == r_origin[2]) {
                                // Right side grow
                                if (origin[0] + size[0] == r_origin[0]) {
                                    size[0] += r_size[0];
                                    return true;
                                }

                                // Left side grow
                                if (origin[0] == r_origin[0] + r_size[0]) {
                                    origin[0] = r_origin[0];
                                    size[0] += r_size[0];
                                    return true;
                                }
                            }
                        }
                        break;
                    case 1:
                        if (origin[0] == r_origin[0]) {
                            if (origin[2] == r_origin[2]) {
                                // Right side grow
                                if (origin[1] + size[1] == r_origin[1]) {
                                    size[1] += r_size[1];
                                    return true;
                                }

                                // Left side grow
                                if (origin[1] == r_origin[1] + r_size[1]) {
                                    origin[1] = r_origin[1];
                                    size[1] += r_size[1];
                                    return true;
                                }
                            }
                        }
                        break;
                    case 2:
                        if (origin[0] == r_origin[0]) {
                            if (origin[1] == r_origin[1]) {
                                // Right side grow
                                if (origin[2] + size[2] == r_origin[2]) {
                                    size[2] += r_size[2];
                                    return true;
                                }

                                // Left side grow
                                if (origin[2] == r_origin[2] + r_size[2]) {
                                    origin[2] = r_origin[2];
                                    size[2] += r_size[2];
                                    return true;
                                }
                            }
                        }
                        break;
                }
            }
        }

        return false;
    }

    public void getOrigin(int[] origin) {
        origin[0] = this.origin[0];
        origin[1] = this.origin[1];
        origin[2] = this.origin[2];
    }

    public void getSize(int[] size) {
        size[0] = this.size[0];
        size[1] = this.size[1];
        size[2] = this.size[2];
    }

    public String toString() {
        return "BoxRegion@" + hashCode() + " origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size);
    }

    /**
     * Get the extents of the region
     *
     * @param min The preallocated min
     * @param max The preallocated max
     */
    public void getExtents(int[] min, int[] max) {
        min[0] = origin[0];
        min[1] = origin[1];
        min[2] = origin[2];
        max[0] = origin[0] + size[0];
        max[1] = origin[1] + size[1];
        max[2] = origin[2] + size[2];
    }

}