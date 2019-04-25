package abfab3d.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static abfab3d.core.Output.printf;

/**
 * Utils for dealing with URI.  Someday we might generalize this if the library needs more of it
 *
 * @author Alan Hudson
 */
public class URIUtils {
    private static final boolean DEBUG = false;
    public static final String DOWNLOAD_PREFIX = "downloadURI";
	public static final Map<String, String> mimeTypeMapper;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("image/png", "png");
		tmpMap.put("image/jpeg", "jpg");
		tmpMap.put("image/gif", "gif");
		tmpMap.put("model/x3d+xml", "x3d");
		tmpMap.put("model/x3d+fastinfoset", "x3db");
		tmpMap.put("model/x3d-vrml", "x3dv");
		tmpMap.put("model/vrml", "wrl");
		tmpMap.put("application/sla", "stl");
		tmpMap.put("text/csv", "csv");
		tmpMap.put("application/zip", "zip");
		mimeTypeMapper = Collections.unmodifiableMap(tmpMap);
	}

    /**
     * Downloads a URI and places the result in a temporary file
     * @param paramName
     * @param urlStr
     * @return The string
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String downloadURI(String paramName, String urlStr) throws URISyntaxException, IOException {
        String workingDirPath = Files.createTempDirectory(DOWNLOAD_PREFIX).toString();
        return writeUrlToFile(paramName, urlStr, workingDirPath, false);
    }

    public static String downloadURIToString(String urlStr) throws URISyntaxException, IOException {

        URL yourl = new URL(urlStr);
        URI uri = new URI(yourl.getProtocol(), yourl.getUserInfo(), yourl.getHost(), yourl.getPort(), yourl.getPath(), yourl.getQuery(), yourl.getRef());
        if (DEBUG) printf("Write url to string.  urlStr: %s  convUrl: %s  %s: uri\n",urlStr,yourl,uri);


        int retries = 0;
        int max_retries = 2;

        InputStream input = null;

        while(retries < max_retries) {
            try {
                // TODO: this does not handle 301 redirects which we got from Shapeways by requesting a https endpoint that only supported http
                URL source = new URL(uri.toASCIIString());
                int connectionTimeout = 10000;
                int readTimeout = 10000;

                URLConnection connection = source.openConnection();
                connection.setConnectTimeout(connectionTimeout);
                connection.setReadTimeout(readTimeout);
                input = connection.getInputStream();
                return IOUtils.toString(input);

            } catch (IOException ioe) {
                retries++;
                if (retries >= max_retries) {
                    throw ioe;
                }
                try {
                    Thread.sleep(500 * (retries * retries));
                } catch (InterruptedException ie) {
                }
            } finally {
                IOUtils.closeQuietly(input);
            }
        }

        throw new IOException("Failed to download, too many retries");
    }

    public static String writeUrlToFile(String paramName, String urlStr, String destDir, boolean fixedNaming) throws URISyntaxException, IOException {
        // Convert to a URI to encode any special characters
        URL yourl = new URL(urlStr);
        URI uri = new URI(yourl.getProtocol(), yourl.getUserInfo(), yourl.getHost(), yourl.getPort(), yourl.getPath(), yourl.getQuery(), yourl.getRef());
        if (DEBUG) printf("Write url to file.  urlStr: %s  convUrl: %s  %s: uri\n",urlStr,yourl,uri);


        String filename = getFileName(paramName, uri, fixedNaming);
        if (DEBUG) printf("file is: %s\n",filename);
        File file = new File(destDir + "/" + filename);
        int retries = 0;
        int max_retries = 2;

        while(retries < max_retries) {
            try {
                // TODO: this does not handle 301 redirects which we got from Shapeways by requesting a https endpoint that only supported http
                FileUtils.copyURLToFile(new URL(uri.toASCIIString()), file, 10000, 60000);
                if (file.exists()) {

                    if (file.length() == 0) {
                        // file really failed to download, perhaps from 301 redirect.  try removing https and trying again
						if (urlStr.startsWith("http://")) {
							urlStr = urlStr.replace("http:","https:");
						} else if (urlStr.startsWith("https://")) {
							urlStr = urlStr.replace("https:","http:");
						}

                        FileUtils.copyURLToFile(new URL(urlStr), file, 10000, 60000);
                        if (!file.exists() || file.length() == 0) {
                            printf("Failed to download file, tried http.  file: %s\n",urlStr);
                            return null;
                        }
                    }

                    if (retries > 0) {
                        System.out.println("      saved file " + file + " retried: " + retries);
                    } else {
                        System.out.println("      saved file " + file);
                    }
                    // If zip file, copy to tmp dir and unzip
                    // If not zip file, return path as is
                    if (filename.endsWith(".zip")) {
                        unzip(file, new File(destDir));
                        file.delete();

                        return destDir;
                    } else {
                        return file.getAbsolutePath();
                    }
                } else {
                    printf("File does not exist after copy.  url: %s  file: %s\n",urlStr,file);
                    return null;
                }
            } catch (IOException ioe) {
                retries++;
                if (retries >= max_retries) {
                    throw ioe;
                }
                try { Thread.sleep(500 * (retries * retries)); } catch(InterruptedException ie) {}
            }
        }

        return null;
    }

    public static String getUrlFilename(String paramName, String urlStr, String destDir, boolean fixedNaming) throws URISyntaxException, IOException {
        // Convert to a URI to encode any special characters
        URL yourl = new URL(urlStr);
        URI uri = new URI(yourl.getProtocol(), yourl.getUserInfo(), yourl.getHost(), yourl.getPort(), yourl.getPath(), yourl.getQuery(), yourl.getRef());
        if (DEBUG) printf("Write url to file.  urlStr: %s  convUrl: %s  %s: uri\n",urlStr,yourl,uri);


        String filename = getFileName(paramName, uri, fixedNaming);
        if (DEBUG) printf("file is: %s\n",filename);
        File file = new File(destDir + "/" + filename);

        if (file.exists()) {
            // If zip file, copy to tmp dir and unzip
            // If not zip file, return path as is
            if (filename.endsWith(".zip")) {
                return destDir;
            } else {
                return file.getAbsolutePath();
            }
        } else {
            printf("Couldn't find file at: %s\n",file.getAbsolutePath());
            return null;
        }
    }

    public static String writeDataURIToFile(String paramName, String urlStr, String destDir) throws IOException {
        int mte = urlStr.indexOf(";");

        if (mte == -1) {
            printf("Cannot find mimetype in url: %s\n",urlStr);
            return null;
        }
        String mime = urlStr.substring(5,mte);
        printf("Mime type: %s\n",mime);

        String ext = mimeTypeMapper.get(mime);
        String filename = paramName + "_" + "datafile." + ext;
        printf("Saving file: %s\n",filename);

        int encs = urlStr.indexOf(",", mte+1);
        String enc = urlStr.substring(mte+1,encs);
        if (!enc.equals("base64")) {
            printf("Unhandled encoding: %s\n",enc);
        }

        byte[] bytes = Base64.decodeBase64(urlStr.substring(encs + 1, urlStr.length()));
        File file = new File(destDir + "/" + filename);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }

        if (file.exists()) {
            return file.getAbsolutePath();
        }

        return null;
    }

    /**
     * Unzip a file into a destination directory
     *
     * @param src
     * @param dest
     */
    private static void unzip(File src, File dest) throws IOException {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(src);

            for (Enumeration e = zipFile.getEntries(); e.hasMoreElements(); ) {
                ZipArchiveEntry entry = (ZipArchiveEntry) e.nextElement();
                unzipEntry(zipFile, entry, dest);
            }
        } finally {
            if (zipFile != null) zipFile.close();
        }
    }

    private static void unzipEntry(ZipFile zipFile, ZipArchiveEntry entry, File dest) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(dest, entry.getName()));
            return;
        }

        File outputFile = new File(dest, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            if (outputStream != null ) outputStream.close();
            if (inputStream != null) inputStream.close();
        }
    }

    public static String resolveURN(String resultDir, String localResultsDir, String publicResultsDir) {
        String ret_val = resultDir.replace(localResultsDir, publicResultsDir);
        return ret_val;
    }

    /**
     * Checks if a file or directory is in a subdirectory of a base directory.
     * @param base The base directory
     * @param child The child file or directory
     * @return True if child is in a subdirectory of base. False otherwise.
     * @throws IOException
     */
    public static boolean isInSubDirectory(File base, File child) throws IOException {
    	if (child == null) return false;
    	
    	base = base.getCanonicalFile();
    	child = child.getCanonicalFile();

	    File parentFile = child;
	    while (parentFile != null) {
	    	if (base.equals(parentFile)) {
	    		return true;
	        }
	        parentFile = parentFile.getParentFile();
	    }
	    return false;
    }
    
    private static void createDir(File dir) {
        if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
    }

    /**
     * Get the file name and extension from a url string.
     * Format of url should be:  http://server/path/to/file/filename.ext
     * TODO: Get the extension from the mime type.
     *
     * @param paramName
     * @param urlStr
     * @return
     * @throws Exception
     */
    private static String getFileName(String paramName, String urlStr) {
        String filename = FilenameUtils.getName(urlStr);

        String ext = FilenameUtils.getExtension(filename);
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        try {
            return Files.createTempFile(paramName, ext).getFileName().toString();
        } catch(IOException ioe) {
            throw new IllegalStateException("Failed to create temp file for: " + paramName + " ext: " + ext);
        }
    }

    /**
     * Get the file name and extension from a url string.
     * Format of url should be:  http://server/path/to/file/filename.ext
     * TODO: Get the extension from the mime type.
     *
     * @param paramName
     * @param uri
     * @return
     * @throws Exception
     */
    private static String getFileName(String paramName, URI uri, boolean fixedNaming) {
        String filename = Paths.get(uri.getPath()).getFileName().toString();
        String ext = FilenameUtils.getExtension(filename);

        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        try {
            if (!fixedNaming) {
                return Files.createTempFile(paramName, ext).getFileName().toString();
            } else {
                return filename;
            }
        } catch(IOException ioe) {
            throw new IllegalStateException("Failed to create temp file for: " + paramName + " ext: " + ext);
        }
    }
}
