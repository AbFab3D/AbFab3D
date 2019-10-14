/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

/**
 * Real time viewer for Volumes
 *
 * @author Alan Hudson
 */
public class VolumeViewer extends BaseVolumeViewer  {

    public static final void main(String[] args) {
        VolumeViewer vv = new VolumeViewer();
        vv.parseArgs(args);
        vv.init();

        if (vv.initialFile != null) {
            try {
                vv.loadFile(vv.initialFile, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}