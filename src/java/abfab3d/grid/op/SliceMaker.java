/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.util.ColorMapper;

import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


/**
 * class to render single slice of DataSource channel into image
 * it is mainly used for testing and measurements
 */
public class SliceMaker {

    protected int m_threadCount = 0;

    public SliceMaker() {
    }

    public void setThreadCount(int threadCount) {
        m_threadCount = threadCount;
    }


    /**
     * create and render slice image for given data source
     * render slice for given DataSource
     * <p>
     * slice is defined via 3 corners:
     * <p>
     * pntv --------------------
     * |                     |
     * |                     |
     * |                     |
     * |                     |
     * |                     |
     * origin-----------------pntu
     */
    public BufferedImage renderSlice(int nu, int nv, Vector3d origin, Vector3d pntu, Vector3d pntv, DataSource dataSource, int channelID, ColorMapper colorMapper) {

        BufferedImage image = new BufferedImage(nu, nv, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt) image.getRaster().getDataBuffer();
        int[] imageData = db.getData();

        renderSlice(nu, nv, origin, pntu, pntv, dataSource, channelID, colorMapper, imageData);
        return image;

    }

    /**
     * render slice of given channelID for given DataSource using into pre-allocated image array in ARGB format
     */
    public void renderSlice(int nu, int nv, Vector3d origin, Vector3d pntu, Vector3d pntv, DataSource dataSource, int channelID,
                            ColorMapper colorMapper, int[] imageData) {
        renderSlice(nu, nv, origin, pntu, pntv, dataSource, channelID, colorMapper, imageData, null);
    }

    /**
     * render slice of given channelID for given DataSource using into pre-allocated image array in ARGB format
     */
    public void renderSlice(int nu, int nv, Vector3d origin, Vector3d pntu, Vector3d pntv, DataSource dataSource, int channelID,
                            ColorMapper colorMapper, int[] imageData, double[] values) {

        //
        // TODO make it MT
        //
        Vec pnt = new Vec(3);
        Vec data = new Vec(4);
        double
                ux = (pntu.x - origin.x) / nu,
                uy = (pntu.y - origin.y) / nu,
                uz = (pntu.z - origin.z) / nu,
                vx = (pntv.x - origin.x) / nv,
                vy = (pntv.y - origin.y) / nv,
                vz = (pntv.z - origin.z) / nv,
                x0 = origin.x + (ux + vx) / 2, // add half pixel shift
                y0 = origin.y + (uy + vy) / 2,
                z0 = origin.z + (uz + vz) / 2;


        for (int v = 0; v < nv; v++) {
            int offy = nu * (nv - 1 - v);

            for (int u = 0; u < nu; u++) {

                pnt.v[0] = x0 + u * ux + v * vx;
                pnt.v[1] = y0 + u * uy + v * vy;
                pnt.v[2] = z0 + u * uz + v * vz;
                dataSource.getDataValue(pnt, data);
                imageData[u + offy] = colorMapper.getColor(data.v[channelID]);
                if (values != null) values[u + offy] = data.v[channelID];
            }
        }
    }

} // class SliceMaker