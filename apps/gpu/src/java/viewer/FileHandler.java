package viewer;

// External imports
import java.io.IOException;

// Local imports
// None

/**
 * Interface representing code that can open a file or URL in the browser.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface FileHandler {

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param url The URL to open
     */
    void loadURL(String url) throws IOException;
}
