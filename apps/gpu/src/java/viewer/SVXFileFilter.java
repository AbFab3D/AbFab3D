package viewer;

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
            if (validExts.contains(extension))
                return true;
            else
                return false;
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
