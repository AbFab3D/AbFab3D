/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import java.util.ArrayList;
import java.util.List;

public class SliceLayer {
    private double layerHeight;   // Layer height in meters
    private ArrayList<PolyLine> lines = new ArrayList<>();
    private ArrayList<Hatches> hatches = new ArrayList<>();
    private int totalPointCnt = -1;

    public SliceLayer() {

    }

    public SliceLayer(double h) {
        layerHeight = h;
    }

    /*
       Start of a layer with upper surface at height z (z*units [mm]). 
       All layers must be sorted in ascending order with respect to z. 
       The thickness of the layer is given by the difference between the z values of the current and previous layers. 
       A thickness for the first (lowest) layer can be specified by including a "zero-layer" with a given z value but no polyline.
     */
    public double getLayerHeight() {
        return layerHeight;
    }

    public PolyLine[] getPolyLines() {
        return (PolyLine[]) lines.toArray(new PolyLine[lines.size()]);
    }
    public int getPolyLineCount() {
        return lines.size();
    }

    public PolyLine getPolyLine(int index) {
        return lines.get(index);
    }

    public int getTotalPointCount() {
        if (totalPointCnt > -1) {
            return totalPointCnt;
        }

        int cnt = 0;
        for(int i=0; i < lines.size(); i++) {
            cnt += lines.get(i).getPoints().length;
        }

        totalPointCnt = cnt;
        return totalPointCnt;
    }

    public Hatches[] getHatches() {
        return (Hatches[]) hatches.toArray(new Hatches[hatches.size()]);
    }

    public void addPolyLine(PolyLine l) {
        lines.add(l);
    }

    public void addHatches(Hatches h) {
        hatches.add(h);
    }

    public void setLayerHeight(double layerHeight) {
        this.layerHeight = layerHeight;
    }

    public ArrayList<PolyLine> getLines() {
        return lines;
    }

    public void setLines(List<PolyLine> lines) {
        this.lines = new ArrayList<>(lines);
    }

    public void setLines(PolyLine[] lines) {
        this.lines = new ArrayList<>();
        for(PolyLine pl : lines) {
            this.lines.add(pl);
        }
    }

    public void setHatches(List<Hatches> hatches) {
        this.hatches = new ArrayList<>(hatches);
    }

    public void setHatches(ArrayList<Hatches> hatches) {
        this.hatches = new ArrayList<>();
        for(Hatches pl : hatches) {
            this.hatches.add(pl);
        }
    }
}