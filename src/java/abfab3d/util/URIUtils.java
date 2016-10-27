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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

import static abfab3d.core.Output.printf;

/**
 * Utils for dealing with URI.  Someday we might generalize this if the library needs more of it
 *
 * @author Alan Hudson
 */
public class URIUtils {
    private static final boolean DEBUG = false;

    public static String downloadURI(String paramName, String urlStr) throws URISyntaxException, IOException {
        String workingDirPath = Files.createTempDirectory("downloadURI").toString();
        return writeUrlToFile(paramName, urlStr, workingDirPath);
    }

    public static String writeUrlToFile(String paramName, String urlStr, String destDir) throws URISyntaxException, IOException {
        // Convert to a URI to encode any special characters
        URL yourl = new URL(urlStr);
        URI uri = new URI(yourl.getProtocol(), yourl.getUserInfo(), yourl.getHost(), yourl.getPort(), yourl.getPath(), yourl.getQuery(), yourl.getRef());
        if (DEBUG) printf("Write url to file.  urlStr: %s  convUrl: %s  %s: uri\n",urlStr,yourl,uri);


        String filename = getFileName(paramName, uri);
        if (DEBUG) printf("file is: %s\n",filename);
        File file = new File(destDir + "/" + filename);
        int retries = 0;
        int max_retries = 3;

        while(retries < max_retries) {
            try {
                // TODO: this does not handle 301 redirects which we got from Shapeways by requesting a https endpoint that only supported http
                FileUtils.copyURLToFile(new URL(uri.toASCIIString()), file, 10000, 60000);
                if (file.exists()) {

                    if (file.length() == 0) {
                        // file really failed to download, perhaps from 301 redirect.  try removing https and trying again
                        urlStr = urlStr.replace("https:","http:");
                        FileUtils.copyURLToFile(new URL(urlStr), file, 10000, 60000);
                        if (!file.exists() || file.length() == 0) {
                            printf("Failed to download file, tried http.  file: %s\n",urlStr);
                            return null;
                        }
                    }

                    System.out.println("      saved file " + file);

                    // If zip file, copy to tmp dir and unzip
                    // If not zip file, return path as is
                    if (filename.endsWith(".zip")) {
                        unzip(file, new File(destDir));
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

    public static String writeDataURIToFile(String paramName, String urlStr, String destDir) throws IOException {
        int mte = urlStr.indexOf(";");

        if (mte == -1) {
            printf("Cannot find mimetype in url: %s\n",urlStr);
            return null;
        }
        String mime = urlStr.substring(5,mte);
        printf("Mime type: %s\n",mime);
        int mts = mime.indexOf("/");

        if (mts == -1) {
            printf("Cannot find / mimetype in type: %s\n",mime);
            return null;
        }
        String ext = mime.substring(mts+1);

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
    private static String getFileName(String paramName, URI uri) {
        String filename = Paths.get(uri.getPath()).getFileName().toString();
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
}
