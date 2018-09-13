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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import abfab3d.core.Initializable;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.util.AbFab3DGlobals;

import static abfab3d.core.Output.printf;
import static abfab3d.util.ImageUtil.makeARGB;


/**
   class to render images via DataSource interface 
   DataSource returns ARBG values which shall be inside interval [0.,1.] for alpha, red, green, blue values
   image is rendered in the rectangle in the xy plane with z values = center of the Bounds 
   the coordinates domain is divided into imgWidth x imgHeight pixels 
   data values values are calculated in CENTERS of pixels (using half pixel shift) 
   data values are assumed to be in the range [0,1]
   data.v[0] - RED
   data.v[1] - GREEN
   data.v[2] - BLUE
   data.v[3] - ALPHA 
 */
public class ImageMaker {
    
    protected int m_threadCount = 0;
    protected int m_imgType = BufferedImage.TYPE_INT_ARGB;

    private Slice[] m_slices;
    private AtomicInteger m_slicesIdx;

    public ImageMaker(){        
    }

    public void setThreadCount(int threadCount){        
        m_threadCount = threadCount;
    }


    Slice getNextSlice(){
        if(m_slicesIdx.intValue() >= m_slices.length)
            return null;

        return m_slices[m_slicesIdx.getAndIncrement()];
    }

    /**
       creates and renders in default TYPE_INT_ARGB format 
     */
    public BufferedImage renderImage(int width, int height, Bounds bounds, DataSource imgRenderer){

        BufferedImage image =  new BufferedImage(width, height, m_imgType);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();

        if (m_threadCount == 0) {
            m_threadCount = Runtime.getRuntime().availableProcessors();
        }

        int max = (int) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY);
        if (m_threadCount > max) m_threadCount = max;

        if (m_threadCount == 1) {
            renderImage(width, height, bounds, imgRenderer, imageData);
        } else {
            renderImageMT(width, height, bounds, imgRenderer, imageData);
        }

        return image;
    }

    public void renderImage(int width, int height, Bounds bounds, DataSource imgRenderer, int [] imageData){
        if(imgRenderer instanceof Initializable) {
            ((Initializable)imgRenderer).initialize();
        }

        Vec pnt = new Vec(3);
        Vec data = new Vec(4); 
        double du = bounds.getSizeX()/width;
        double dv = bounds.getSizeY()/height;

        double umin = bounds.xmin + du/2; // half pixel shift 
        double vmin = bounds.ymin + dv/2;
        // take w plane in the middle of bounds, or shall it be at zmin ? 
        double wmin = (bounds.zmin + bounds.zmax)/2;
        
        int dataDim = imgRenderer.getChannelsCount();
        double datav[] = data.v;
        double pntv[] = pnt.v;
        for(int v = 0; v < height; v++){

            //in images y-axis pointing down 
            int offy = width*(height-1-v);
            double vvalue = vmin + v*dv;
            for(int u = 0; u < width; u++){
                int offset = u + offy;

                pnt.set(umin + u*du, vvalue, wmin);
                data.set(0,0,0,0); // init data 

                imgRenderer.getDataValue(pnt, data);
                switch(dataDim){
                default:
                case 4: imageData[offset] = makeARGB(datav[0],datav[1],datav[2],datav[3]); break;
                case 3: imageData[offset] = makeARGB(datav[0],datav[1],datav[2],1.); break;
                case 2: imageData[offset] = makeARGB(datav[0],0,       datav[1],1.);break;
                case 1: imageData[offset] = makeARGB(datav[0],datav[0],datav[0],1.);break;
                }
            }
        }
    }

    public void renderImageMT(int width, int height, Bounds bounds, DataSource imgRenderer, int [] imageData){
        if(imgRenderer instanceof Initializable) {
            ((Initializable)imgRenderer).initialize();
        }

        m_slices = new Slice[height];
        for(int v = 0; v < height; v++) {
            m_slices[v] = new Slice(v);
        }

        m_slicesIdx = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        for(int i = 0; i < m_threadCount; i++){
            Runnable runner = new ImageRunner(width,height,bounds,imgRenderer,imageData);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    class ImageRunner implements Runnable {

        int width;
        int height;
        Bounds bounds;
        DataSource imgRenderer;
        int[] imageData;

        public ImageRunner(int width, int height, Bounds bounds, DataSource imgRenderer, int[] imageData) {
            this.width = width;
            this.height = height;
            this.bounds = bounds;
            this.imgRenderer = imgRenderer;
            this.imageData = imageData;
        }

        public void run(){

            Vec pnt = new Vec(3);
            Vec data = new Vec(4);
            double du = bounds.getSizeX()/width;
            double dv = bounds.getSizeY()/height;

            double umin = bounds.xmin + du/2; // half pixel shift
            double vmin = bounds.ymin + dv/2;
            // take w plane in the middle of bounds, or shall it be at zmin ?
            double wmin = (bounds.zmin + bounds.zmax)/2;

            int dataDim = imgRenderer.getChannelsCount();
            double datav[] = data.v;

            int v = -1;
            try {
                //printf("%s:.run()\n", Thread.currentThread());

                while (true) {

                    Slice slice = getNextSlice();
                    if (slice == null) {
                        return;
                    }

                    v = slice.getHeight();

                    //in images y-axis pointing down
                    int offy = width * (height - 1 - v);
                    double vvalue = vmin + v * dv;
                    for (int u = 0; u < width; u++) {
                        int offset = u + offy;

                        pnt.set(umin + u * du, vvalue, wmin);
                        data.set(0, 0, 0, 0); // init data

                        imgRenderer.getDataValue(pnt, data);
                        switch (dataDim) {
                            default:
                            case 4:
                                imageData[offset] = makeARGB(datav[0], datav[1], datav[2], datav[3]);
                                break;
                            case 3:
                                imageData[offset] = makeARGB(datav[0], datav[1], datav[2], 1.);
                                break;
                            case 2:
                                imageData[offset] = makeARGB(datav[0], 0, datav[1], 1.);
                                break;
                            case 1:
                                imageData[offset] = makeARGB(datav[0], datav[0], datav[0], 1.);
                                break;
                        }
                    }
                }
            } catch(Throwable t) {
                printf("Problem on slice: %d / %d\n",v,height);
                t.printStackTrace();
            }
        }
    } //class ShapeDilaterRunner


    static class Slice {

        private int h;

        Slice(int h){
            this.h = h;
        }

        public int getHeight() {
            return h;
        }
    }

} // class ImageMaker