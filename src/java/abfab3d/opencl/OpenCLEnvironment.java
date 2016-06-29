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
package abfab3d.opencl;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLPlatform;

import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
 * An environment to encapsulate openCL Context, Device, Queue etc
 *
 * @author Vladimir Bulatov
 */
public class OpenCLEnvironment {

    private CLContext m_context;
    private CLDevice m_device;
    private CLCommandQueue m_commandQueue;

    public OpenCLEnvironment(CLDevice device){

        m_device = device;
        m_context = CLContext.create(m_device);
        m_commandQueue = m_device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

    }


    public static OpenCLEnvironment create(String platformName, String deviceName){
    
        CLPlatform[] platforms = CLPlatform.listCLPlatforms();
        CLDevice device = null;

        for(int i=0; i < platforms.length; i++) {
            
            if(platforms[i].getName().contains(platformName)){
                CLDevice[] devices = platforms[i].listCLDevices();
                for(int j=0; j < devices.length; j++) {
                    if(devices[j].getName().contains(deviceName)){
                        device = devices[j];                        
                    }
                }
            }
        }
        if(device == null)             
            device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice(); 
        return new OpenCLEnvironment(device);

    }


    public CLDevice getDevice() {
        return m_device;
    }

    public CLCommandQueue getCommandQueue() {
        return m_commandQueue;
    }

    public CLContext getContext() {
        return m_context;
    }

    public String toString(){
        return fmt("OpenCLEnvironment(%s)", m_device.getName());
    }
    public String getInfo(){
        return fmt(" device:%s\n vendor: %s\n profile:%s\n version: %s\n driver: %s\n compUnits:%d\n localMem:%d\n globalMem:%d\n maxAlloc:%d\n ",
                   m_device.getName(), m_device.getVendor(), m_device.getProfile(), m_device.getVersion().fullversion, m_device.getDriverVersion(), 
                   m_device.getMaxComputeUnits(),m_device.getLocalMemSize(),
                   m_device.getGlobalMemSize(),
                   m_device.getMaxMemAllocSize());
    }
    
}
