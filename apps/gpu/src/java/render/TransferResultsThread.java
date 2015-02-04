package render;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLEvent;
import com.jogamp.opencl.CLEventList;

import java.util.concurrent.ConcurrentLinkedQueue;

import static abfab3d.util.Output.printf;

/**
 *  Transfers results back from the GPU.
 *
 *  @author Alan Hudson
 */
public class TransferResultsThread extends Thread {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;
    
    private ConcurrentLinkedQueue<RenderTile> transfers;
    private boolean terminate = false;
    private TransferResultsListener listener;
    private volatile boolean done;
    private int tilesProcessed=0;
    private long imageTime;

    public TransferResultsThread(ConcurrentLinkedQueue<RenderTile> transfers) {
        this.transfers = transfers;
    }

    public void setListener(TransferResultsListener listener) {
        this.listener = listener;
    }

    public void run() {
        CLCommandQueue queue = null;
        try {
            while (true) {
                RenderTile tile = getNextTile();
                if (tile == null) {
                    if (terminate) {
                        // end of processing
                        break;
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (Exception e) {
                        }
                        continue;
                    }
                }

                tilesProcessed++;
                long t0 = System.nanoTime();

                if (DEBUG) printf("Reading buffer: x0: %d  y0: %d dest: %s\n", tile.getX0(), tile.getY0(),tile.getDest());
                CLEventList events = new CLEventList(1);

                tile.getCommandQueue().putReadBuffer(tile.getDest(), true, events); // read results back (blocking read)}
                /*
                // Create own queue to avoid threading issues
                queue = tile.getDevice().createCommandQueue();
                queue.putReadBuffer(tile.getDest(), true, events); // read results back (blocking read)}
                */
                if (listener != null) {
                    listener.tileArrived(tile);
                }

                    //System.out.printf("Returning grid: %s\n",tile.grid);
                //tile.getDest().getBuffer().rewind();  // not sure if this is necessary

                tile.getResources().getBufferPool().returnObject(tile.getDest());

                imageTime += (System.nanoTime() - t0);
                if (DEBUG_TIMING) {
                    CLDevice device = tile.getDevice();
                    long start = tile.getResources().getStartTime();
                    for (int j = 0; j < events.size(); j++) {
                        CLEvent event = events.getEvent(j);
                        printf("trnsfer cmd(%s):  total: %6.2f  start:%6.2f end: %6.2f\n",device.getName(),(event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                                        - event.getProfilingInfo(CLEvent.ProfilingCommand.START)) / 1000000.0,
                                (event.getProfilingInfo(CLEvent.ProfilingCommand.START) - start) /1000000.0,
                                (event.getProfilingInfo(CLEvent.ProfilingCommand.END) - start) / 1000000.0);
                        /*
                                System.out.println("trnsfer cmd: " + j + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                                        - event.getProfilingInfo(CLEvent.ProfilingCommand.START)) / 1000000.0);
*/
                    }
                }
            }

            if (DEBUG) printf("Finished Transfer\n");

        } catch(Exception e) {
            e.printStackTrace();
        }

        done = true;
    }

    public void terminate() {
        terminate = true;
    }

    public boolean isDone() {
        return done;
    }

    private RenderTile getNextTile() {
        return transfers.poll();
    }

    public long getImageTime() {
        return imageTime;
    }

    public int getTilesProcessed() {
        return tilesProcessed;
    }

}
