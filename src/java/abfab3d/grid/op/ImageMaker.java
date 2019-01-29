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
import abfab3d.core.Color;
import abfab3d.core.ImageProducer;
import abfab3d.core.ResultCodes;

import abfab3d.util.AbFab3DGlobals;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ColorParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;

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
public class ImageMaker extends BaseParameterizable implements ImageProducer {
    
    protected int m_imgType = BufferedImage.TYPE_INT_ARGB;

    private Slice[] m_slices;
    private AtomicInteger m_slicesIdx;
    
    private BufferedImage m_image;

    
    IntParameter mp_width = new IntParameter("width", 100);
    IntParameter mp_height = new IntParameter("height", 100);
    IntParameter mp_threadCount = new IntParameter("threadCount", 0);
    DoubleParameter mp_xmin = new DoubleParameter("xmin", -1.);
    DoubleParameter mp_xmax = new DoubleParameter("xmax", 1.);
    DoubleParameter mp_ymin = new DoubleParameter("ymin", -1.);
    DoubleParameter mp_ymax = new DoubleParameter("ymax", 1.);
    DoubleParameter mp_zmin = new DoubleParameter("zmin", -1.);
    DoubleParameter mp_zmax = new DoubleParameter("zmax", 1.);

    // image renderer, by default - solid red 
    SNodeParameter mp_imgRenderer = new SNodeParameter("imgRenderer", new SolidColor(new Color(1,0,0,1)));
    
    Parameter m_params[] = new Parameter[]{
        mp_width,
        mp_height,
        mp_threadCount,
        mp_imgRenderer,
        mp_xmin, 
        mp_xmax, 
        mp_ymin, 
        mp_ymax, 
        mp_zmin, 
        mp_zmax, 
        
    };

    public ImageMaker(){        

        addParams(m_params);
        
    }


    public BufferedImage getImage(){

        prepareImage();
        return m_image;
    }


    public void setBounds(Bounds bounds){

        set("xmin",bounds.xmin);
        set("xmax",bounds.xmax);
        set("ymin",bounds.ymin);
        set("ymax",bounds.ymax);
        set("zmin",bounds.zmin);
        set("zmax",bounds.zmax);

    }

    public Bounds getBounds(){

        return new Bounds(mp_xmin.getValue(), mp_xmax.getValue(), 
                          mp_ymin.getValue(), mp_ymax.getValue(), 
                          mp_zmin.getValue(), mp_zmax.getValue());
    }


    public void setThreadCount(int threadCount){        

        set("threadsCount", threadCount);

    }


    Slice getNextSlice(){
        int idx = m_slicesIdx.getAndIncrement();
        if(idx >= m_slices.length) {
            return null;
        }
        return m_slices[idx];
    }

    
    /**
       creates and renders in default TYPE_INT_ARGB format 
       backward compatibility methods        
       new code should use getImage();

     */
    public BufferedImage renderImage(int width, int height, Bounds bounds, DataSource imgRenderer){

        set("width", width);
        set("height", height);        
        set("imgRenderer", imgRenderer);
        setBounds(bounds);

        return getImage();

    }
    


    protected void prepareImage(){
                
        int width = mp_width.getValue();
        int height = mp_height.getValue();
        DataSource imgRenderer = (DataSource)mp_imgRenderer.getValue();
        Bounds bounds = getBounds();

        BufferedImage image =  new BufferedImage(width, height, m_imgType);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();
        
        int threadCount = mp_threadCount.getValue();
        if (threadCount == 0) {
            threadCount = Runtime.getRuntime().availableProcessors();
        }

        int max = (int) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY);
        if (threadCount > max) threadCount = max;

        if (threadCount == 1) {
            renderImage(width, height, bounds, imgRenderer, imageData);
        } else {
            renderImageMT(width, height, bounds, imgRenderer, imageData, threadCount);
        }


        m_image = image;
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
        // take z plane in the middle of bounds, or shall it be at zmin ? 
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

    protected void renderImageMT(int width, int height, Bounds bounds, DataSource imgRenderer, int [] imageData, int threadCount){

        if(imgRenderer instanceof Initializable) {
            ((Initializable)imgRenderer).initialize();
        }

        m_slices = new Slice[height];
        for(int v = 0; v < height; v++) {
            m_slices[v] = new Slice(v);
        }

        m_slicesIdx = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; i++){
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
            int offset = -1;
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
                        offset = u + offy;

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
                printf("Problem on slice: %d / %d  off: %d / %d\n",v,height,offset,imageData.length);
                t.printStackTrace();
                System.out.flush();
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

    static class SolidColor extends BaseParameterizable implements DataSource {
        
        double m_red, m_green, m_blue, m_alpha;

        ColorParameter mp_color = new ColorParameter("color", new Color(1,0,0,1));
        Parameter m_params[] = new Parameter[]{
            mp_color
        };

        public SolidColor(Color color){
            
            addParams(m_params);
            set("color", color);

        }

        public int initialize(){
            Color color = mp_color.getValue();
            m_red = color.getr();
            m_green = color.getg();
            m_blue = color.getb();
            m_alpha = color.geta();
            return ResultCodes.RESULT_OK;
        }

        /**
           data value at the given point 
           @param pnt Point where the data is calculated 
           @param dataValue - storage for returned calculated data 
           @return result code 
        */
        public int getDataValue(Vec pnt, Vec dataValue){

            dataValue.v[0] = m_red;
            dataValue.v[1] = m_green;
            dataValue.v[2] = m_blue;
            dataValue.v[3] = m_alpha;
            return ResultCodes.RESULT_OK;
        }
        
        /**
           @returns count of data channels, 
           it is the count of data values returned in  getDataValue()        
        */
        public int getChannelsCount(){
            return 4;
        }
        
        /**
           @return bounds of this data source. It may be null for data sources without bounds 
           
        */
        public Bounds getBounds(){
            return null;
        }
        
        
    }
    

} // class ImageMaker