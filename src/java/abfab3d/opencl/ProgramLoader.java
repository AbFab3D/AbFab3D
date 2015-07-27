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

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLProgram;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
 * Utilities for loading OpenCL programs
 *
 * @author Alan Hudson
 */
public class ProgramLoader {

    static final boolean DEBUG = true;

    public static CLProgram load(CLContext context, String filename) throws IOException {

        InputStream  is = getStreamFor(filename);
        if(is == null) 
            throw new RuntimeException(fmt("can't find file: %s", filename));
        return context.createProgram(is);
    }

    public static CLProgram load(CLContext context, String[] filename, boolean unroll) throws IOException {
        StringBuilder bldr = new StringBuilder();
        int len = filename.length;

        for(int i=0; i < len; i++) {
            InputStream is = getStreamFor(filename[i]);
            String st = IOUtils.toString(is, "UTF-8");
            if (unroll) {
                st = unrollIncludes(st);
            }
            bldr.append("// Include: ");
            bldr.append(filename[i]);
            bldr.append("\n");
            bldr.append(st);
            bldr.append("\n");
            bldr.append("// End Include: ");
            bldr.append(filename[i]);
            bldr.append("\n");

        }
        return context.createProgram(bldr.toString());
    }

    public static CLProgram load(CLContext context, String[] filename, String[] prog, boolean unroll) throws IOException {
        StringBuilder bldr = new StringBuilder();
        int len = filename.length;

        for(int i=0; i < len; i++) {
            InputStream is = getStreamFor(filename[i]);
            String st = IOUtils.toString(is, "UTF-8");
            if (unroll) {
                st = unrollIncludes(st);
            }
            bldr.append("// Include: ");
            bldr.append(filename[i]);
            bldr.append("\n");
            bldr.append(st);
            bldr.append("\n");
            bldr.append("// End Include: ");
            bldr.append(filename[i]);
            bldr.append("\n");

        }
        len = prog.length;
        for(int i=0; i < len; i++) {
            if (prog[i] != null) {
                bldr.append(prog[i]);
                bldr.append("\n");
            }
        }

        return context.createProgram(bldr.toString());
    }

    public static CLProgram load(CLContext context, List list, boolean unroll) throws IOException {
        StringBuilder bldr = new StringBuilder();
        int len = list.size();

        for(int i=0; i < len; i++) {
            Object o = list.get(i);
            if (o instanceof File){
                String name = ((File)o).getName();
                InputStream is = getStreamFor(name);
                String st = IOUtils.toString(is, "UTF-8");
                if (unroll) {
                    st = unrollIncludes(st);
                }

                bldr.append("// Include: ");
                bldr.append(name);
                bldr.append("\n");
                bldr.append(st);
                bldr.append("\n");
                bldr.append("// End Include: ");
                bldr.append(name);
                bldr.append("\n");
            } else {
                bldr.append((String)o);
            }
            bldr.append("\n");
        }

        //printf("Final OpenCL: \n%s\n",bldr.toString());
        return context.createProgram(bldr.toString());
    }

    public static InputStream getStreamFor(String filename) {
        InputStream is = ProgramLoader.class.getResourceAsStream(filename);
        if(is != null) {
            if(DEBUG) printf("abfab3d.opencl.ProgramLoader.getStreamFor(%s) found in resources returns: %s \n", filename, is);
            return is;
        }

        // resources not found     
        String path = "classes" + File.separator + filename;
        //printf("Loading openCL Script: %s\n", path);
        try {

            FileInputStream fis = new FileInputStream(path);
            if(DEBUG) printf("abfab3d.opencl.ProgramLoader.getStreamFor(%s) found in files returns: %s\n", filename, fis);
            
            return fis;
        } catch (IOException ioe) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream(filename);

            if(DEBUG) printf("abfab3d.opencl.ProgramLoader.getStreamFor(%s) found in classLoader returns: %s \n", filename, input);
            
            return input;
        }
        
    }

    /**
     * Remove #include statements to inline the file.
     *
     * @param file
     * @return
     */
    protected static String unrollIncludes(String file) throws IOException {
        StringBuilder sb = new StringBuilder();
        int pos = file.indexOf("#include");

        int len = "#include \"".length();
        int last = 0;

        while(pos != -1) {
            sb.append(file.substring(last,pos));
            int pos2 = file.indexOf("\"",pos+len+1);
            String fname = file.substring(pos+len,pos2);
            sb.append("//Inlined file: " +fname + "\n");
            InputStream is = getStreamFor(fname);
            String inline = IOUtils.toString(is, "UTF-8");
            last = pos2+1;
            sb.append(inline);

            pos = file.indexOf("#include",last);
        }

        sb.append(file.substring(last));

        return sb.toString();
    }
}
