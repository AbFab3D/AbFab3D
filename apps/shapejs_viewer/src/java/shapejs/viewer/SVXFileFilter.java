/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * File filter for restricting files to just X3D types.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class SVXFileFilter extends FileFilter {

    /** The valid extensions */
    private Set<String> validExts;

    /**
     * Create a new file filter instance.
     */
    public SVXFileFilter() {
        validExts = new HashSet<String>(6);

        validExts.add("svx");
    }

    /**
     * Should we accept this file
     *
     * @param f The file to test
     * @return true if acceptable
     */
    public boolean accept(File f) {
        if (f.isDirectory())
            return true;

        String extension = null;

        String s = f.getName();

        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
            extension = s.substring(i+1).toLowerCase();

        if (extension != null) {
            return validExts.contains(extension);
        }

        return false;
    }

    /**
     * The description of this filter
     */
    public String getDescription() {
        return "Just SVX Files";
    }
}
