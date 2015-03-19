package utils;

import static abfab3d.util.Output.printf;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.URIParameter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import shapejs.JSWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Creates images from ShapeJS
 *
 * @author Alan Hudson
 */
public class Utils {
    private static String RESULTS_DIR_PUBLIC = "http://localhost:8080/creator-kernels/results";
    private static String RESULTS_DIR = "/var/www/html/creator-kernels/results";
    private static String TMP_DIR = "/tmp";
    private static int TEMP_DIR_ATTEMPTS = 1000;


    public static void downloadURI(Map<String, Parameter> evalParams, Map<String, Object> namedParams) {
		String workingDirName = null;
		String workingDirPath = null;
		
        for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            Parameter param = evalParams.get(key);
            String json = (String) entry.getValue();
            
            try {

                if (param.getType() == ParameterType.URI) {
                	Parameter p = ((JSWrapper) val).getParameter();
                	URIParameter up = (URIParameter) p;
                	String urlStr = up.getValue();
                	System.out.println("*** uri, " + key + " : " + urlStr);
                	if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                		if (workingDirName == null) {
                			workingDirName = createTempDir(TMP_DIR);
                			workingDirPath = TMP_DIR + "/" + workingDirName;
                		}
                		
                		String localPath = writeUrlToFile(key, urlStr, workingDirPath);
                		up.setValue(localPath);
                		System.out.println("*** uri, " + key + " : " + up.getValue());
                	}
                } else if (param.getType() == ParameterType.URI_LIST) {
                	// TODO: Handle uri list
                }
                
            } catch (Exception e) {
            	printf("Error resolving uri: " + json);
            	e.printStackTrace();
            }
        }
    }
    
    public static String downloadURI(String paramName, String urlStr) throws IOException {
		String workingDirName = createTempDir(TMP_DIR);
		String workingDirPath = TMP_DIR + "/" + workingDirName;
		return writeUrlToFile(paramName, urlStr, workingDirPath);
    }

    private static String writeUrlToFile(String paramName, String urlStr, String destDir) throws IOException {
		String filename = paramName + "_" + FilenameUtils.getName(urlStr);

		File file = new File(destDir + "/" + filename);
		FileUtils.copyURLToFile(new URL(urlStr), file, 10000, 60000);

		if (file.exists()) {
			return file.getAbsolutePath();
		}
		
		return null;
    }
    
    /**
    * Atomically creates a new directory somewhere beneath the system's
    * temporary directory (as defined by the {@code java.io.tmpdir} system
    * property), and returns its name.
    *
    * <p>Use this method instead of {@link File#createTempFile(String, String)}
    * when you wish to create a directory, not a regular file.  A common pitfall
    * is to call {@code createTempFile}, delete the file and create a
    * directory in its place, but this leads a race condition which can be
    * exploited to create security vulnerabilities, especially when executable
    * files are to be written into the directory.
    *
    * <p>This method assumes that the temporary volume is writable, has free
    * inodes and free blocks, and that it will not be called thousands of times
    * per second.
    *
    * @return the newly-created directory
    * @throws IllegalStateException if the directory could not be created
    */
    public static String createTempDir(String baseDir) {
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return baseName + counter;
            }
        }

        throw new IllegalStateException("Failed to create directory within "
            + TEMP_DIR_ATTEMPTS + " attempts (tried "
            + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')' + " baseDir: " + baseDir);
    }
    
    public static File createTempFile(String baseDir, String fieldName, String fileName, String ext) {
        String baseName = fieldName + "_" + fileName + "-";//System.currentTimeMillis() + "-";
        
        String extension = ext;
        if (!extension.startsWith(".")) {
        	extension = "." + extension;
        }

        try {
            for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
//                File tempFile = new File(baseDir, fileName + baseName + counter + ext);
                File tempFile = new File(baseDir, baseName + counter + extension);
                if (tempFile.createNewFile()) {
                    return tempFile;
                }
            }
        } catch (Exception e) {}

        throw new IllegalStateException("Failed to create file within "
            + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0" + extension
            + " to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + extension + ')' + " baseDir: " + baseDir);
    }
    
    private static String resolveURN(String resultDir) {
    	String ret_val = resultDir.replace(RESULTS_DIR, RESULTS_DIR_PUBLIC);
    	return ret_val;
    }

}

