/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package render;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLEventList;
import com.jogamp.opencl.CLKernel;
import datasources.Instruction;

import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static abfab3d.util.Output.printf;

/**
 * Issues rendering requests for tiles.  Places finished tiles onto a transfer queue to get results back
 *
 * @author Alan Hudson
 */
public class RenderThread extends Thread {
    /** Incoming slices to process */
    private ConcurrentLinkedQueue<RenderTile> tiles;

    /** Outgoing transfers to process */
    private ConcurrentLinkedQueue<RenderTile> transfers;

    private VolumeScene vscene;
    //private List<String> prog;
    //private List<Instruction> inst;
    //private String opts;
    //private String ver;
    private DeviceResources device;
    private CLEventList events;
    private int width;
    private int height;
    private float worldScale;
    private Matrix4f view;
    private volatile boolean done;
    private volatile boolean initialized;

    private int tilesProcessed=0;
    private long kernelTime;
    private long imageTime;

    /** Results listener, if set it will place results here instead of transfer queue */
    private TransferResultsListener listener;

    public RenderThread(DeviceResources device, ConcurrentLinkedQueue<RenderTile> tiles,
                        ConcurrentLinkedQueue<RenderTile> transfers, 
                        VolumeScene vscene, // List<String> prog, List<Instruction> inst, String opts,String ver,
                        Matrix4f view, int width,int height,float worldScale) {
        this.device = device;
        this.tiles = tiles;

        this.vscene = vscene; 
        //this.prog = prog;
        //this.inst = inst;
        //this.ver = ver;
        //this.opts = opts;

        this.width = width;
        this.height = height;
        this.worldScale = worldScale;
        this.view = new Matrix4f(view);
        this.view.invert();
        this.transfers = transfers;
    }

    public void cleanup() {
        if (events != null) events.release();
        events = null;

        if (device != null) device.cleanup();
        device = null;
    }

    public void run() {
        long t0 = System.nanoTime();
        // Initialize on thread

        device.init(vscene);
        //printf("Device init: %d\n",((int)((System.nanoTime() - t0) / 1e6)));

        initialized = true;
        boolean first = true;

        try {
            while (true) {
                RenderTile tile = getNextTile();
                if (tile == null) {
                    // end of processing
                    break;
                }

                t0 = System.nanoTime();
                tilesProcessed++;
                tile.setDest(device.getBufferPool().borrowObject());

                if (first) {
                    device.getRenderer().sendView(view,device.getView());
                    first = false;
                }
                device.getRenderer().renderOps(tile.getX0(),tile.getY0(),
                                               tile.getWidth(),tile.getHeight(),width,height, vscene, tile.getDest());

                // Assign device so transfer knows where it came from
                tile.setResources(device);
                tile.setDevice(device.getDevice());
                tile.setQueue(device.getQueue());
                kernelTime += (System.nanoTime() - t0);

                if (listener == null) {
                    transfers.add(tile);

                } else {
                    listener.tileArrived(tile);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        done = true;
    }

    public void setListener(TransferResultsListener listener) {
        this.listener = listener;
    }

    private RenderTile getNextTile() {
        return tiles.poll();
    }

    public boolean isDone() {
        return done;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public long getKernelTime() {
        return kernelTime;
    }

    public int getTilesProcessed() {
        return tilesProcessed;
    }
}
