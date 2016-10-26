/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
   utils to deal with files 
   
 */
public class FileUtil {
    
    /**
       return file name without extension from path 
     */
    public static String getFileName(String path){

        String name = new File(path).getName();
        int ind = name.lastIndexOf('.');
        if(ind > 0)
            return name.substring(0,ind);
        else
            return name;
    }

    /**
       returns directory for 
     */
    public static String getFileDir(String path){

        
        String dir = new File(path).getParent();
        if(dir == null) 
            dir = "";
        
        return dir.replace('\\', '/');
    }


    /**
     * Get a resource as a stream.  Deals with differences between loading from an app and applet.
     * @param filename
     * @return
     */
    public static InputStream getResourceAsStream(String filename) {
        File f = new File(filename);

        try {
            if (f.exists()) {
                return new FileInputStream(f);
            } else {

                String fname = "classes" + File.separator + filename;
                f = new File(fname);

                if (f.exists()) {
                    return new FileInputStream(f);
                } else {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    fname = filename.replace("\\","/");
                    InputStream is = classLoader.getResourceAsStream(fname);
                    return is;
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
}