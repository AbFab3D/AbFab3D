package program;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLProgram;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static abfab3d.util.Output.printf;

/**
 * Utilities for loading OpenCL programs
 *
 * @author Alan Hudson
 */
public class ProgramLoader {
    public static CLProgram load(CLContext context, String filename) throws IOException {
        return context.createProgram(getStreamFor(filename));
    }

    public static CLProgram load(CLContext context, String[] filename) throws IOException {
        StringBuilder bldr = new StringBuilder();
        int len = filename.length;

        for(int i=0; i < len; i++) {
            InputStream is = getStreamFor(filename[i]);
            String st = IOUtils.toString(is, "UTF-8");
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

    public static CLProgram load(CLContext context, String[] filename, String[] prog) throws IOException {
        StringBuilder bldr = new StringBuilder();
        int len = filename.length;

        for(int i=0; i < len; i++) {
            InputStream is = getStreamFor(filename[i]);
            String st = IOUtils.toString(is, "UTF-8");
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

    public static CLProgram load(CLContext context, List list) throws IOException {
        StringBuilder bldr = new StringBuilder();
        int len = list.size();

        for(int i=0; i < len; i++) {
            Object o = list.get(i);
            if (o instanceof File){
                String name = ((File)o).getName();
                InputStream is = getStreamFor(name);
                String st = IOUtils.toString(is, "UTF-8");
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

        if (is == null) {
            String path = "classes" + File.separator + filename;
            //printf("Loading openCL Script: %s\n", path);
            try {
                FileInputStream fis = new FileInputStream(path);

                return fis;
            } catch (IOException ioe) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream input = classLoader.getResourceAsStream(filename);

                return input;
            }
        } else {
            //printf("Loading openCL Script: %s\n", filename);
        }

        return is;
    }

}
