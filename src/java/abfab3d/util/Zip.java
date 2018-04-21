/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility to zip nested directories recursively.
 * https://hubpages.com/technology/Zipping-and-Unzipping-Nested-Directories-in-Java-using-Apache-Commons-Compress
 *
 * @author Tony Wong
 */
public class Zip {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Zip() {}

    /**
     * Creates a zip file at the specified path with the contents of the specified directory.
     * NB:
     *
     * @param directoryPath The path of the directory where the archive will be created. eg. c:/temp
     * @param zipPath The full path of the archive to create. eg. c:/temp/archive.zip
     * @param includeParent should the parent directory be part of the archive
     * @throws IOException If anything goes wrong
     */
    public static void createZip(String directoryPath, String zipPath, boolean includeParent) throws IOException {
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        ZipArchiveOutputStream tOut = null;

        try {
            fOut = new FileOutputStream(new File(zipPath));
            bOut = new BufferedOutputStream(fOut);
            tOut = new ZipArchiveOutputStream(bOut);
            addFileToZip(tOut, directoryPath, "", includeParent);
        } finally {
            tOut.finish();
            tOut.close();
            bOut.close();
            fOut.close();
        }

    }

    /**
     * Creates a zip entry for the path specified with a name built from the base passed in and the file/directory
     * name. If the path is a directory, a recursive call is made such that the full directory is added to the zip.
     *
     * @param zOut The zip file's output stream
     * @param path The filesystem path of the file/directory being added
     * @param base The base prefix to for the name of the zip file entry
     *
     * @throws IOException If anything goes wrong
     */
    private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base, boolean includeParent) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();

        if (includeParent) {
            ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);

            zOut.putArchiveEntry(zipEntry);
        }

        if (f.isFile()) {
            FileInputStream fInputStream = null;
            try {
                fInputStream = new FileInputStream(f);
                IOUtils.copy(fInputStream, zOut);
                zOut.closeArchiveEntry();
            } finally {
                IOUtils.closeQuietly(fInputStream);
            }

        } else {
            if (includeParent) zOut.closeArchiveEntry();
            File[] children = f.listFiles();

            if (children != null) {
                for (File child : children) {
                    String newbase = "";
                    if (includeParent) newbase = entryName + "/";
                    addFileToZip(zOut, child.getAbsolutePath(), newbase,true);
                }
            }
        }
    }
}
