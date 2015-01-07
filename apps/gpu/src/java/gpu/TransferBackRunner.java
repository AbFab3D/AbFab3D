package gpu;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLEvent;
import com.jogamp.opencl.CLEventList;

import java.util.concurrent.ConcurrentLinkedQueue;
import static abfab3d.util.Output.printf;

/**
 * Transfers data back from OpenCL
 *
 * @author Alan Hudson
 */
public class TransferBackRunner implements Runnable {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;

    private ConcurrentLinkedQueue<Slice> transfers;
    private DeviceResources device;
    private boolean terminate = false;
    private TransferBackListener listener;

    public TransferBackRunner(DeviceResources device) {
        this.transfers = device.getTransferQueue();
        this.device = device;
    }

    public void setListener(TransferBackListener listener) {
        this.listener = listener;
    }

    public void run() {
        CLCommandQueue queue = null;
        try {
            queue = device.getQueue();

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
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

                if (DEBUG) printf("Reading buffer: %f --> %f\n", slice.ymin, slice.ymax);
                CLEventList events = new CLEventList(1);
                queue.putReadBuffer(slice.buffer.getBufferDest(), true, events); // read results back (blocking read)}

                if (listener != null) {
                    listener.sliceArrived(slice.buffer.getGrid(),slice.ymin,slice.ymax,slice.idx);
                }

                try {
                    //System.out.printf("Returning grid: %s\n",slice.grid);
                    device.getBufferPool().returnObject(slice.buffer);
                } catch(Exception e ) {e.printStackTrace();};

                if (DEBUG_TIMING) {
                    long start = device.getStartTime();
                    for (int j = 0; j < events.size(); j++) {
                        CLEvent event = events.getEvent(j);
                        printf("trnsfer cmd(%s):  total: %6.2f  start:%6.2f end: %6.2f\n",device.getDevice().getName(),(event.getProfilingInfo(CLEvent.ProfilingCommand.END)
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
        } finally {
            queue.release();
        }
    }

    public void terminate() {
        terminate = true;
    }

    private Slice getNextSlice() {
        return transfers.poll();
    }

}
