/***************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

import abfab3d.core.Output;
import abfab3d.core.ResultCodes;

import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics2D;

import java.awt.image.*;

import static java.lang.Math.max;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.MathUtil.lerp;
import static abfab3d.core.MathUtil.iround;


/**
 * Image utilities.
 *
 * @author Vladimir Bulatov
 */
public class ImageUtil {
    private static final boolean DEBUG = false;
    public static final int RESULT_OK = 1, RESULT_FAILURE = -1;
    public static int MAXC = 0xFF; //  maximal value of color component as int 
    public static final double CNORM = 255.; // maximal value of color component
    public static final double CNORM1 = 1./255.;
    // indices of component in the array of colors
    public static final int RED = 0, GREEN  = 1, BLUE = 2, ALPHA = 3;
    
    static public  final int getAlpha(int rgb){
        return ((rgb >> 24) & 0xFF);
    }
    
    static public  final int getRed(int rgb){
        return ((rgb >> 16) & 0xFF);
    }
    
    static public final  int getGreen(int rgb){
        return ((rgb >> 8) & 0xFF);
    }
    
    static public final  int getBlue(int rgb){
        return ((rgb) & 0xFF);
    }

    /**
       extract color components from packed color in ARGB format
       colro component are in range [0.,1.]
     */
    static public final void getRGBA(int argb, double color[]){
        color[RED] = getRed(argb)/255.;
        color[GREEN] = getGreen(argb)/255.;
        color[BLUE] = getBlue(argb)/255.;
        color[ALPHA] = getAlpha(argb)/255.;
    }

    /**
       makes color componets pre-multiplied by alpha
       
     */
    static public final void premult(double color[]){
        double a = color[ALPHA];
        color[RED] *= a;
        color[GREEN] *= a;
        color[BLUE] *= a;
    }

    /**
       overlay topColor over backgroundColor and saver result into backgroundColor;
       colors have to be in premult form normalized to [0,1]
     */
    static public final void overlay(double backgroundColor[], double topColor[]){
        
        double b = 1.-topColor[ALPHA];
        backgroundColor[0] = b*backgroundColor[0] + topColor[0];
        backgroundColor[1] = b*backgroundColor[1] + topColor[1];
        backgroundColor[2] = b*backgroundColor[2] + topColor[2];
        backgroundColor[3] = b*backgroundColor[3] + topColor[3];

    }


    /**
       converts color from premult form in RGBA order into full form in RGBA order 
       if input component are not in premult for result will be bad 
     */
    static public final void fullColor(double color[]){
        double a = color[ALPHA];
        if(a != 0) {
            color[RED] /= a;
            color[GREEN] /= a;
            color[BLUE] /= a;
        } else {
            color[RED] = 0.;
            color[GREEN] = 0.;
            color[BLUE] = 0.;
        }
    }

    /**
       copy color from coolorFrom into colorTo
     */
    static public final void copyColor(double colorFrom[], double[] colorTo){
        colorTo[0] = colorFrom[0];
        colorTo[1] = colorFrom[1];
        colorTo[2] = colorFrom[2];
        colorTo[3] = colorFrom[3];
    }

    /**
       scale color by given value and return it
     */
    static public final double[] scaleColor(double color[], double alpha){
        color[0] *= alpha;
        color[1] *= alpha;
        color[2] *= alpha;
        color[3] *= alpha;
        return color;
    }
    
    /**
       packs 3 color components into single int RGB (0xFFRRGGBB) 
     */
    public static final int makeRGB(int r, int g, int b){
        return 
            0xFF000000 |  // ALPHA 
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
            ((b & 0xFF) );            
    }

    /**
       combines 4 color components into single int ARGB (0xAARRGGBB) 
     */
    public static final int makeARGB(int r, int g, int b, int a ){
        return 
            ((a & 0xFF) << 24 )| 
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
            ((b & 0xFF) );    
    }

    /**
       converts rgba color into single int 
       input colors are in range [0.,1.]
     */
    public static final int makeARGB(double r, double g, double b, double a ){
        return makeARGB((int)(r*255+0.5), (int)(g*255+0.5), (int)(b*255+0.5),(int)(a*255+0.5));
    }
    

    /**
       linear interpolation of two colors packed in ARGB form 
     */
    public static final int lerpColors(int c1, int c2, double t){

        return makeARGB(iround(lerp(getRed(c1),getRed(c2),t)),
                        iround(lerp(getGreen(c1),getGreen(c2),t)),
                        iround(lerp(getBlue(c1),getBlue(c2),t)),
                        iround(lerp(getAlpha(c1),getAlpha(c2),t)));
    }

    public static void lerpColors(double c1[], double c2[], double x, double cout[]){
        
        for(int i = 0; i < 4; i++){
            cout[i] = c1[i] + x * (c2[i] - c1[i]);
        }
        
    }
    
    
    /**
       do multiplication of colors normalized to [0, 255]
    */
    public static final double mul255(double a, double b){
        return a*b * CNORM1;
    }

    public static final int mul255(int a, int b){
        return (a*b)/255;
    }
    
    /*
      combine two premultiplied colors with given alpha
      colors are normalized to [0, 255]
    */
    public static final double combinePremultDouble255(double c1, double c2, double alpha){
        
        return c1 - c1 * alpha * CNORM1 + c2;
        
    }

    public static final int combinePremultInt(int c1, int c2, int alpha){
        
        return c1 + c2 - alpha * c1 / 255;
        
    }

    
    // overlay of non-premult color components c1 over c2 
    public static final int combineInt(int c1, int c2, int alpha){
        
        return c1 + (c2 - c1)* alpha / 255;
        
    }
    
    /**
     *  formula for alpha composition works for PREMULTIPLIED colors (colors are premultiplied by alpha value) 
     *  colors are normalized to [0, 255]
     *  
     */
    public static void combinePremultColors255(double c1[], double c2[], double c3[], double alpha){
        
        for(int i = 0; i < 4; i++){
            c3[i] = combinePremultDouble255(c1[i], c2[i], alpha);
        }
    }

    public static int combinePremultColorsInt(int c1, int c2){

        int a = getAlpha(c2);
        int r = combinePremultInt(getRed(c1),   getRed(c2), a);
        int g = combinePremultInt(getGreen(c1), getGreen(c2), a);
        int b = combinePremultInt(getBlue(c1),  getBlue(c2), a);
        int a1 = combinePremultInt(getAlpha(c1),  a, a);

        return makeARGB(r,g,b,a1);

    }
        
    public static double[] getPremultColor255(Color c, double outColor[]){
        double alpha = c.getAlpha();
        outColor[ALPHA] = alpha;
        outColor[RED] = mul255(c.getRed(),alpha);
        outColor[GREEN] = mul255(c.getGreen(),alpha);
        outColor[BLUE] = mul255(c.getBlue(),alpha);
        return outColor;
    }

    public static double[] getPremultColor255(int rgba, double outColor[]){
        double alpha = getAlpha(rgba);
        outColor[ALPHA] = alpha;
        outColor[RED] = mul255(getRed(rgba),alpha);
        outColor[GREEN] = mul255(getGreen(rgba),alpha);
        outColor[BLUE] = mul255(getBlue(rgba),alpha);
        return outColor;
    }


    public static final int getPremultColorInt(int c){
        int a = getAlpha(c);
        int r = mul255(getRed(c),a);
        int g = mul255(getGreen(c),a);
        int b = mul255(getBlue(c),a);
        return makeARGB(r, g, b, a );
    }

    public static final int getGray(int c){
        return (getRed(c) + getGreen(c) + getBlue(c))/3;
    }

    /**
       calculates gray level intensity of two colors 
       
       c1 - background Color  RGBA (completely opaque) 
       c2 - overlay Color     RGBA (may have transparency) 
       
       input colors are not premultiplied. 
       
     */
    public static final int getCombinedGray(int c1, int c2){

        int r1 = getRed(c1);
        int g1 = getGreen(c1);
        int b1 = getBlue(c1);

        int r2 = getRed(c2);
        int g2 = getGreen(c2);
        int b2 = getBlue(c2);
        int a2 = getAlpha(c2);
        
        int r3 = combineInt(r1, r2, a2);
        int g3 = combineInt(g1, g2, a2);
        int b3 = combineInt(b1,b2,a2);

        return (r3 + g3 + b3)/3;
    }
    
    /*
      return scaled down image using pixel weight function 
    */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height, PixelWeight pixelWeight){
        
        int w0 = image.getWidth();
        int h0 = image.getHeight();
        
        BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for(int y = 0; y < height; y++){
            
            int y0 = (y * h0)/height;
            int y1 = ((y + 1) * h0)/height;
            //printf("y0: %d, y1: %d\n", y0, y1);
            
            for(int x = 0; x < width; x++){
                
                int x0 = (x * w0)/width;
                int x1 = ((x + 1) * w0)/width;
                
                //printf("x0: %d, x1: %d\n", x0, x1);
                
                int rs = 0, gs = 0, bs = 0, as = 0, ws = 0;
                
                for(int yy = y0; yy < y1; yy++){
                    
                    for(int xx = x0; xx < x1; xx++){
                        
                        int pixel = image.getRGB(xx,yy);
                        int r = getRed(pixel);
                        int g = getGreen(pixel);
                        int b = getBlue(pixel);
                        int a = getAlpha(pixel);
                        int weight = pixelWeight.getWeight(r,g,b,a);
                        rs += r*weight;
                        gs += g*weight;
                        bs += b*weight;
                        as += a*weight;
                        ws += weight;
                    }
                }
                
                rs /= ws;
                gs /= ws;
                bs /= ws;
                as /= ws;
                outImage.setRGB(x,y, makeARGB(rs,gs,bs,as));
                
            }
        }
        
        return outImage;
        
    }
    
    
    /**
       return scaled instance of the image 
       returned image has the same type as source image
    */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height){
        
        try {
            
            BufferedImage scaledImage = new BufferedImage(width, height, image.getType());
            //BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            // Paint scaled version of image to new image
            Graphics2D graphics2D = scaledImage.createGraphics();
            //graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            //graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            //Image scaled = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            
            graphics2D.drawImage(scaled, 0, 0, width, height, null);
            
            return scaledImage;      
            
        } catch(Exception e){
            e.printStackTrace(Output.out);
        }
        return null;
    }  

    public static String getImageTypeName(int type){
        switch(type){
        default: return "UNKNOWN";
        case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR";
        case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
        case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE";
        case BufferedImage.TYPE_BYTE_BINARY: return "BYTE_BINARY";
        case BufferedImage.TYPE_BYTE_GRAY: return "BYTE_GRAY";
        case BufferedImage.TYPE_BYTE_INDEXED: return "BYTE_INDEXED";
        case BufferedImage.TYPE_CUSTOM: return "CUSTOM";
        case BufferedImage.TYPE_INT_ARGB: return "INT_ARGB";
        case BufferedImage.TYPE_INT_ARGB_PRE: return "INT_ARGB_PRE";
        case BufferedImage.TYPE_INT_BGR: return "INT_BGR";
        case BufferedImage.TYPE_INT_RGB: return "INT_RGB";
        case BufferedImage.TYPE_USHORT_555_RGB: return "USHORT_555_RGB";
        case BufferedImage.TYPE_USHORT_565_RGB: return "USHORT_565_RGB";
        case BufferedImage.TYPE_USHORT_GRAY: return "USHORT_GRAY";            
        }
    }

    public static String getDataTypeName(int type){
        switch(type){
        default: return "Unknown";
        case DataBuffer.TYPE_BYTE: return "BYTE";
        case DataBuffer.TYPE_DOUBLE: return "DOUBLE";
        case DataBuffer.TYPE_FLOAT: return "DOUBLE";
        case DataBuffer.TYPE_INT: return "INT";
        case DataBuffer.TYPE_SHORT: return "SHORT";
        case DataBuffer.TYPE_UNDEFINED: return "UNDEFINED";
        case DataBuffer.TYPE_USHORT: return "USHORT";
        }
    }

    public static short[] getGray16Data(BufferedImage image){

        if (DEBUG) printf("image type: %s\n",ImageUtil.getImageTypeName(image.getType()));

        DataBuffer dataBuffer = image.getRaster().getDataBuffer();

        if (DEBUG) printf("image data type: %s\n", ImageUtil.getDataTypeName(dataBuffer.getDataType()));

        if (DEBUG) printf("buffer: %s\n", dataBuffer);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        int grayDataSize = imageWidth*imageHeight;

        short grayData[] = new short[grayDataSize];
        
        switch(image.getType()){
        case BufferedImage.TYPE_CUSTOM: 
            {
                switch(dataBuffer.getDataType()){
                default:
                    //throw new IllegalArgumentException(fmt("unhandled image data format: %s \n",getDataTypeName(dataBuffer.getDataType())));
                    getGray16DataGeneric(image, grayData);
                    break;

                case DataBuffer.TYPE_USHORT: 
                    if( ushort2gray16(((DataBufferUShort)dataBuffer).getData(), grayData) != RESULT_OK){
                        getGray16DataGeneric(image, grayData);
                    }
                    break;
                }
                break;
            }
            
        case BufferedImage.TYPE_4BYTE_ABGR:
            {                
                byteABGR2gray16(((DataBufferByte)dataBuffer).getData(), grayData);            
                break;
                
            }
        case BufferedImage.TYPE_3BYTE_BGR:
            {
                byteBGR2gray16(((DataBufferByte)dataBuffer).getData(), grayData);            
                break;
            }
        default:
            {
                getGray16DataGeneric(image, grayData);
                break;
            }            
        }
        
        return grayData;
        
    }


    public static int[] getImageData_INT_ARGB(BufferedImage image){

        String imageTypeName = ImageUtil.getImageTypeName(image.getType());

        if (DEBUG) printf("getImageData_INT_ABGR() image type: %s\n",imageTypeName);

        DataBuffer dataBuffer = image.getRaster().getDataBuffer();
        if (DEBUG) printf("image data type: %s\n", ImageUtil.getDataTypeName(dataBuffer.getDataType()));
        if (DEBUG) printf("buffer: %s\n", dataBuffer);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        int dataSize = imageWidth*imageHeight;

        int intData[] = new int[dataSize];
        
        switch(image.getType()){
        case BufferedImage.TYPE_CUSTOM: 
            {
                switch(dataBuffer.getDataType()){
                default:
                    throw new RuntimeException(fmt("unhandled image data type: %s",imageTypeName));
                    //throw new IllegalArgumentException(fmt("unhandled image data format: %s \n",getDataTypeName(dataBuffer.getDataType())));
                    //getGray16DataGeneric(image, grayData);
                    //break;

                case DataBuffer.TYPE_USHORT: 
                    throw new RuntimeException(fmt("unhandled image data type: %s",imageTypeName));
                    //if( ushort2gray16(((DataBufferUShort)dataBuffer).getData(), grayData) != RESULT_OK){
                    //    getGray16DataGeneric(image, grayData);
                    //}
                    // break;
                }
                //break;
            }
            
        case BufferedImage.TYPE_4BYTE_ABGR:
            {                
                BYTE_ABGR_2_INT_ARGB(((DataBufferByte)dataBuffer).getData(), intData);            
                break;
                
            }
        case BufferedImage.TYPE_INT_ARGB:
            {                
                INT_ARGB_2_INT_ARGB(((DataBufferInt)dataBuffer).getData(), intData);            
                break;
                
            }
        case BufferedImage.TYPE_3BYTE_BGR:
            {
                
                BYTE3_BGR_2_INT_ARGB(((DataBufferByte)dataBuffer).getData(), intData);            
                break;
            }
        default:
            {
                throw new RuntimeException(fmt("unhandled image data type: %s",imageTypeName));
                //getGray16DataGeneric(image, grayData);
                //break;
            }            
        }
        
        return intData;
        
    }


    public static void getGray16DataGeneric(BufferedImage image, short grayData[]){

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int grayDataSize = imageWidth * imageHeight;

        int imageData[] = new int[grayDataSize];

        image.getRGB(0,0,imageWidth, imageHeight, imageData, 0, imageWidth);            
        int len = imageData.length; 
        for(int i = 0; i < grayDataSize; i++){
            // convert data into grayscale short  
            grayData[i] = (short)ub2us(getCombinedGray(SOLID_WHITE, imageData[i])); 
        }
    }        

    /**
       convert array of byte data in ABGR form into 16 bit gray data 
     */
    public static void byteABGR2gray16(byte imageData[], short grayData[]){

        int len = grayData.length;
        for(int i = 0, k = 0; i < len; i++, k += 4){
            
            int a = ub2i(imageData[k]);
            int b = ub2i(imageData[k+1]);
            int g = ub2i(imageData[k+2]);
            int r = ub2i(imageData[k+3]);
            // overlay over solid white
            int gray = combineInt(0xFF, (r + g + b)/3,a);
            
            //r = combineInt(0xFF, r, a);
            //g = combineInt(0xFF, g, a);
            //b = combineInt(0xFF, b, a);
        
            grayData[i] = (short)ub2us(gray);
            //grayData[i] = (short)((0xFFFF & (r + g + b)/3) << 8);
    
        }        
    }

    /**
       convert array of 4 ARGB byte data in ARGB int 
     */
    public static void BYTE_ABGR_2_INT_ARGB(byte imageData[], int intData[]){

        int len = intData.length;
        for(int i = 0, k = 0; i < len; i++, k += 4){
            
            int a = ub2i(imageData[k]);
            int b = ub2i(imageData[k+1]);
            int g = ub2i(imageData[k+2]);
            int r = ub2i(imageData[k+3]);

            intData [i] = makeARGB(r,g,b,a);
    
        }        
    }

    /**
       convert array of INT_ARGB to INT_ARGB (simple copy) 
     */
    public static void INT_ARGB_2_INT_ARGB(int imageData[], int intData[]){

        int len = intData.length;
        for(int i = 0; i < len; i++){            
            intData [i] = imageData[i];
        }        
    }


    /**
       convert array of 4 ARGB byte data in ARGB int 
     */
    public static void BYTE3_BGR_2_INT_ARGB(byte imageData[], int intData[]){

        int len = intData.length;
        for(int i = 0, k = 0; i < len; i++, k += 3){
            
            int b = ub2i(imageData[k]);
            int g = ub2i(imageData[k+1]);
            int r = ub2i(imageData[k+2]);

            intData [i] = makeARGB(r,g,b,0xFF);
    
        }        
    }

    /**
       extract array of byte from array of byte in ABGR form into 8 bit 
     */
    public static void getABGRcomponent(byte imageData[], int componentOffset, byte componentData[]){

        int len = componentData.length;
        for(int i = 0, k = 0; i < len; i++, k += 4){            
            componentData[i] = imageData[k + componentOffset];
        }        
    }

    /**
     convert array of byte data in BGR form into 8 bit
     */
    public static void getBGRcomponent(byte imageData[], int componentOffset, byte componentData[]){

        int len = componentData.length;
        for(int i = 0, k = 0; i < len; i++, k += 3){
            componentData[i] = imageData[k + componentOffset];
        }
    }

    // array of BGR bytes to gray16 conversion 
    static void byteBGR2gray16(byte imageData[], short grayData[]){

        int len = grayData.length;
        for(int i = 0, k = 0; i < len; i++, k += 3){
            grayData[i] = (short)ub2us((ub2i(imageData[k]) + ub2i(imageData[k+1]) + ub2i(imageData[k+2]))/3);
        }
        
    }
        
    // convert image data into gray data
    // return ResultCodes.RESULT_OK or RESULT_FAILURE
    // 
    public static int ushort2gray16(short imageData[], short grayData[]){
        
        int len = grayData.length;
        int count = imageData.length/len;
        
        switch(count){
        default:
            return RESULT_FAILURE;
            //throw new IllegalArgumentException(fmt("unknown image data component count: %d\n",count));            
        case 3: //RGB data 
            for(int i = 0, k = 0; i < len; i++, k += 3){
                grayData[i] = (short)((us2i(imageData[k]) + us2i(imageData[k+1]) + us2i(imageData[k+2]))/3);
            }
            break;            
        case 4: // RGBA data
            for(int i = 0, k = 0; i < len; i++, k += 4){
                //TODO take alpha into account 
                //return c1 + (c2 - c1)* alpha / 255;
                int gray = ((us2i(imageData[k]) + us2i(imageData[k+1]) + us2i(imageData[k+2]))/3);
                int alpha = us2i(imageData[k+3]);
                grayData[i] = (short)(MAX_USHORT + ((gray - MAX_USHORT)*alpha)/MAX_USHORT);
            }            
            break;
        }
        return ResultCodes.RESULT_OK;
    }


    /**
     * Map one range of numbers to another range.
     *
     * @param x
     * @param inMin
     * @param inMax
     * @param outMin
     * @param outMax
     * @return
     */
    public static int linearmap(int x, int inMin, int inMax, int outMin, int outMax) {
        
        if(x <= inMin) return outMin;
        if(x >= inMax) return outMax;
        int ret_val = (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
        return ret_val;
        
    }

    /**
       get RGB color for given hue, saturation, luminance
       color components are returned in the [0,1] range 
     */
    public static double[] getColorHSL(double hue, double saturation, double luminance, double rgb[]){
        double q = 0;        
        if (luminance < 0.5)
            q = luminance * (1 + saturation);
        else
            q = (luminance + saturation) - (saturation * luminance);
        
        double p = 2 * luminance - q;
        if(rgb == null)
            rgb = new double[3];

        rgb[0] = max(0, hueToRGB(p, q, hue + (1./3.)));
        rgb[1] = max(0, hueToRGB(p, q, hue));
        rgb[2] = max(0, hueToRGB(p, q, hue - (1./3.)));
        return rgb;
    }

    /**
       get RGB color for given hue, saturation, luminance
     */
    public static int getColorHSL(double hue, double saturation, double luminance){
        
        double q = 0;        
        if (luminance < 0.5)
            q = luminance * (1 + saturation);
        else
            q = (luminance + saturation) - (saturation * luminance);
        
        double p = 2 * luminance - q;
        
        double r = max(0, hueToRGB(p, q, hue + (1./3.)));
        double g = max(0, hueToRGB(p, q, hue));
        double b = max(0, hueToRGB(p, q, hue - (1. / 3.)));
        
        return makeRGB((int)(r*MAXC),(int)(g*MAXC),(int)(b*MAXC));
    }

    private static double hueToRGB(double p, double q, double h)  {

        if (h < 0) h += 1;

        
        if (h > 1 ) h -= 1;
        
        if (6 * h < 1){
            return p + ((q - p) * 6 * h);
        }
        
        if (2 * h < 1 ){
            return  q;
        }

        if (3 * h < 2){
            return p + ( (q - p) * 6 * ((2.0f / 3.0f) - h) );
        }
        
        return p;
    }

    // unsigned byte to unsigned short conversion
    // with scaling to map 0xFF to 0xFFFF 
    final static int ub2us(int ub){
        int b = (ub & 0xFF);
        return ((b << 8) | b);
    //return (short)(0xFFFF & (( (0xFF & ub) * MAX_USHORT)/MAX_UBYTE));
    }

    // unsigned short to signed int conversion 
    public static final int us2i(short s){
        return (0xFFFF & (int)s);        
    }

    // unsigned byte to signed int conversion 
    public static final int ub2i(byte s){
        return (0xFF & (int)s);        
    }

    static final int SOLID_WHITE = 0xFFFFFFFF;
    static final int MAX_USHORT = 0xFFFF;
    static final int MAX_UBYTE = 0xFF;
    
} // class ImageUtil 

