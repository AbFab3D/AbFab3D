/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

// External Imports

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileInputStream;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ColorSpace;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**


 */
public class TestICCProfile extends TestCase {

    static final boolean DEBUG = false;

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestICCProfile.class);
    }


    public void testNothing(){

    }


    public void devTestReadICC() throws Exception {

        ICC_Profile profile = ICC_Profile.getInstance(new FileInputStream("../../../PolyJet/StratasysJ750Vivid1_0.icc"));
        //ICC_Profile profile = ICC_Profile.getInstance(new FileInputStream("../../../PolyJet/doc/sRGB Color Space Profile.icm"));
        
        //printf("profile: %s\n", profile);
        printProfile(profile);

        printf("ColorSpaceType: %d (%s)\n", profile.getColorSpaceType(),getTypeName(profile.getColorSpaceType()));
        printf("PSCType: %d (%s)\n", profile.getPCSType(),getTypeName(profile.getPCSType()));

        //printf("wpt: %s\n", printArray(profile.getData(ICC_Profile.icSigMediaWhitePointTag)));
        //printf("chad: %s\n", printArray(profile.getData(ICC_Profile.icSigChromaticAdaptationTag)));
        //printf("cprt: %s\n",printArray(profile.getData(ICC_Profile.icSigCopyrightTag)));

        ICC_ColorSpace space = new ICC_ColorSpace(profile);
        printf("color space: %s\n", space);
        int numComponents = space.getNumComponents();
        printf("NumComponents: %d\n", space.getNumComponents());
        printf("type: %d (%s)\n", space.getType(), getTypeName(space.getType()));

        for(int i = 0; i < numComponents; i++){
            printf("component:%d name: %s (%7.3f %7.3f)\n", i, space.getName(i), space.getMinValue(i),space.getMaxValue(i));            
        }
        
        float colors[][] = new float[][]{{0,0,0,0},{1,0,0,0},{0,1,0,0},{0,0,1,0}};
        
        float colorsXYZ[][] = new float[][]{{0,0,0},{0.434f, 0.222f, 0.014f},{0.384f, 0.714f, 0.097f}, {0.143f, 0.060f, 0.711f}};


        for(int i = 0; i < colors.length; i++){
            printf("(%6.3f,%6.3f,%6.3f) ->", colors[i][0], colors[i][1], colors[i][2]);
            //float dcolors[] = space.fromRGB(colors[i]);
            float dcolors[] = space.toCIEXYZ(colors[i]);
            float dcolorsXYZ[] = space.fromCIEXYZ(dcolors);
            //float dcolors[] = space.toRGB(colors[i]);
            //float dcolors[] = colors[i];
            printf("toXYZ:   (%6.3f,%6.3f,%6.3f ) ", dcolors[0], dcolors[1], dcolors[2]);
            printf("fromXYZ: (%6.3f,%6.3f,%6.3f )\n", dcolorsXYZ[0], dcolorsXYZ[1], dcolorsXYZ[2]);

        }
        
    }

    static void printProfile(ICC_Profile profile){

        //printf("profile: %s\n", profile);
        byte data[] = profile.getData();
        printf("profile: %s\n", profile);
        printTagTable(data);
        
    }
    
    static void printTagTable(byte data[]){

        int offset = 128; // tag table offset 
        int tagCount = getInt(data, offset);
        printf("tagCount: %d\n", tagCount);
        offset += 4;
        for(int i = 0; i < tagCount; i++){
            printf("tag:%s(%8x) offset: %8d size: %8d\n", getTagSig(data,offset),getInt(data,offset),getInt(data, offset+4),getInt(data, offset+8));
            offset += 12; // tag table entry size 
        }
    }


    static int ubyte(byte x){
        return ((int)x) & 0xFF;
    }

    static String getTagSig(byte data[], int offset){
        byte s[] = new byte[]{data[offset],data[offset+1],data[offset+2], data[offset+3]};
        return new String(s);
    }

    static int getInt(byte data[], int offset){
        return (ubyte(data[offset]) << 24) | (ubyte(data[offset+1]) << 16) | (ubyte(data[offset+2]) << 8) | (ubyte(data[offset+3]));
    }

    String printArray(byte wpt[]){
        
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < wpt.length; i++){
            buf.append(fmt("%2x ",wpt[i]));
        }
        return buf.toString();
    }

    String getTypeName(int type){
        switch(type){
        case ColorSpace.CS_CIEXYZ: return "CS_CIEXYZ";
        case ColorSpace.CS_GRAY: return "CS_GRAY";
        case ColorSpace.CS_LINEAR_RGB: return "CS_LINEAR_RGB";
        case ColorSpace.CS_PYCC: return "CS_PYCC";
        case ColorSpace.CS_sRGB: return "CS_sRGB";
        case ColorSpace.TYPE_2CLR: return "TYPE_2CLR";
        case ColorSpace.TYPE_3CLR: return "TYPE_3CLR";
        case ColorSpace.TYPE_4CLR: return "TYPE_4CLR";
        case ColorSpace.TYPE_5CLR: return "TYPE_5CLR";
        case ColorSpace.TYPE_6CLR: return "TYPE_6CLR";
        case ColorSpace.TYPE_7CLR: return "TYPE_7CLR";
        case ColorSpace.TYPE_8CLR: return "TYPE_8CLR";
        case ColorSpace.TYPE_9CLR: return "TYPE_9CLR";
        case ColorSpace.TYPE_ACLR: return "TYPE_ACLR";
        case ColorSpace.TYPE_BCLR: return "TYPE_BCLR";
        case ColorSpace.TYPE_CCLR: return "TYPE_CCLR";
        case ColorSpace.TYPE_CMY: return "TYPE_CMY";
        case ColorSpace.TYPE_CMYK: return "TYPE_CMYK";
        case ColorSpace.TYPE_DCLR: return "TYPE_DCLR";
        case ColorSpace.TYPE_ECLR: return "TYPE_ECLR";
        case ColorSpace.TYPE_FCLR: return "TYPE_FCLR";
        case ColorSpace.TYPE_GRAY: return "TYPE_GRAY";
        case ColorSpace.TYPE_HLS: return "TYPE_HLS";
        case ColorSpace.TYPE_HSV: return "TYPE_HSV";
        case ColorSpace.TYPE_Lab: return "TYPE_Lab";
        case ColorSpace.TYPE_Luv: return "TYPE_Luv";
        case ColorSpace.TYPE_RGB: return "TYPE_Luv";
        case ColorSpace.TYPE_XYZ: return "TYPE_XYZ";
        case ColorSpace.TYPE_YCbCr: return "TYPE_YCbCr";
        case ColorSpace.TYPE_Yxy: return "TYPE_Yxy";
        default: return "TYPE_UNKNOWN";
        }
    }
    
    public static void main(String arg[]) throws Exception {
        
        new TestICCProfile().devTestReadICC();

    }
}
