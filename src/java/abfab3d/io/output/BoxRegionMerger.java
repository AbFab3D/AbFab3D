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

package abfab3d.io.output;

// External Imports
import java.util.*;

// Internal Imports
import abfab3d.grid.*;


/**
 * Merges boxes into largest available boxes.
 *
 * @author Alan Hudson
 */
public class BoxRegionMerger {
    private int pass;
    private int maxPasses;

    public BoxRegionMerger(int maxPasses) {
        this.maxPasses = maxPasses;
        pass = 0;
    }

    /**
     * Attempts to merge regions together.
     *
     * @regions The regions to merge, results placed here
     */
    public void merge(Set<Region> regions) {
        displayCounts(regions);

        pass = 0;

        if (pass >= maxPasses)
            return;

        mergeRegions(regions);
    }

    /**
     * Merge regions.
     */
    private void mergeRegions(Set<Region> regions) {
        Iterator<Region> itr = regions.iterator();
        HashSet<Region> remove = new HashSet<Region>();
        int count = 0;

        // TODO: Is remove list faster then restarting?

        while(itr.hasNext()) {
            Region r1 = itr.next();

            if (remove.contains(r1)) {
                continue;
            }

            Iterator<Region> itr2 = regions.iterator();

            while(itr2.hasNext()) {
                Region r2 = itr2.next();

                if (r1 == r2)
                    continue;

                if (remove.contains(r2)) {
                    continue;
                }

                if (r1.merge(r2)) {
System.out.println("MERGE: " + r1);
                    remove.add(r2);
                    count++;
                }
            }
        }

        itr = remove.iterator();
        while(itr.hasNext()) {
            Region r1 = itr.next();
            regions.remove(r1);
        }

        System.out.println("Mergers: " + count);
        pass++;

        if (pass < maxPasses && count > 0) {
            mergeRegions(regions);
        } else {
            System.out.println("Final Regions:");
            displayCounts(regions);
        }
    }

    public void displayCounts(Set<Region> regions) {
        Iterator<Region> itr = regions.iterator();

        int[] center = new int[3];
        int[] size = new int[3];
        int tot_vol = 0;
        HashMap<Integer, Integer> counts = new HashMap<Integer,Integer>();

        HashSet<Region> remove = new HashSet<Region>();

        while(itr.hasNext()) {
            Region r = itr.next();
            if (r instanceof BoxRegion) {
                BoxRegion box = (BoxRegion) r;

                box.getSize(size);

                Integer vol = new Integer(size[0] * size[1] * size[2]);
                tot_vol += vol.intValue();
                Integer cnt = counts.get(vol);

                if (cnt == null) {
                    counts.put(vol, new Integer(1));
                } else {
                    counts.put(vol, new Integer(cnt.intValue() + 1));
                }

/*
                if (passes == 0 && vol != 81) {
                    System.out.println("Removing!!");
                    remove.add(r);
                }
*/
            }
        }

        Iterator<Map.Entry<Integer,Integer>> itr2 = counts.entrySet().iterator();
        while(itr2.hasNext()) {
            Map.Entry<Integer,Integer> e = itr2.next();
            System.out.println("Vol: " + e.getKey() + " count: " + e.getValue());
        }

        Iterator<Region> itr3 = remove.iterator();
        while(itr3.hasNext()) {
            regions.remove(itr3.next());
        }
        System.out.println("Total Vol: " + tot_vol);
    }
}
