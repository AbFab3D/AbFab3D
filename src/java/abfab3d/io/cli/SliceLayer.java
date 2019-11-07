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

public class SliceLayer {
    private double layerHeight;
    private ArrayList<PolyLine> lines = new ArrayList<>();
    private ArrayList<Hatches> hatches = new ArrayList<>();

    public SliceLayer() {

    }

    public SliceLayer(double h) {
        layerHeight = h;
    }

    /*
       Start of a layer with upper surface at height z (z*units [mm]). All layers must be sorted in ascending order with respect to z. The thickness of the layer is given by the difference between the z values of the current and previous layers. A thickness for the first (lowest) layer can be specified by including a "zero-layer" with a given z value but no polyline.
     */
    public double getLayerHeight() {
        return layerHeight;
    }

    public PolyLine[] getPolyLines() {
        return (PolyLine[]) lines.toArray(new PolyLine[lines.size()]);
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
}