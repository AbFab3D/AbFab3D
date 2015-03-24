package gpu;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static java.lang.Math.min;

/**
 * CLPlatform utilities
 *
 * @author Alan Hudson
 */
public class PlatformUtil {
    public static CLDevice getCPUDevice() {
        CLPlatform platform = CLPlatform.getDefault(type(GPU));

        if (platform.getName().contains("Apple")) {
            // Apple does not get the GPU maxFlops right, just find the nvidia card
            CLDevice[] devices = platform.listCLDevices();
            boolean found = false;
            for(int i=0; i < devices.length; i++) {
                printf("Checking device: %s %s\n",devices[i],devices[i].getVendor());
                if (devices[i].getVendor().contains("NVIDIA")) {
                    return devices[i];
                }
            }
        }
        return CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
    }

    public static int getLocalWorkSize(CLDevice device, int size) {
        boolean isApple = device.getPlatform().getName().contains("Apple");

        if (isApple) return 0;
        int localWorkSize = min(device.getMaxWorkGroupSize(), size);  // Local work size dimensions

        return localWorkSize;

    }

}
