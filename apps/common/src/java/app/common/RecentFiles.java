package app.common;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * A class to store recently opened or saved files.
 * 
 * @author Tony Wong
 */
public class RecentFiles {
    public final static String RECENT_FILE = "recent_file.";
    
    private int maxFiles;
    private Preferences prefs;
    private List<String> recentFilesList;
    
    /**
     * Creates a RecentFiles with the specified max file count and preference.
     * @param maxFiles The max number of recent files
     * @param prefs The Java preferences
     */
    public RecentFiles(int maxFiles, Preferences prefs) {
    	this.maxFiles = maxFiles;
    	this.prefs = prefs;
    	recentFilesList = new ArrayList<String>();
        
        loadPreferences();
    }
    
    /**
     * Add a file to the recent files list.
     * @param file The file path as a string
     */
    public void add(String file) {
    	recentFilesList.remove(file);
    	recentFilesList.add(0, file);
        
    	int size = recentFilesList.size();
    	
        if (size > maxFiles) {
        	recentFilesList.remove(size - 1);
        }
        
        savePreferences();
    }
    
    /**
     * Remove a file from the recent files list.
     * @param file The file path as a string
     */
    public void remove(String file) {
    	recentFilesList.remove(file);
    	savePreferences();
    }
    
    /**
     * Get the file at a given index.
     * @param index The index
     * @return The file path as a string
     */
    public String get(int index) {
        return (String) recentFilesList.get(index);
    }
    
    /**
     * Get the list of recent files.
     * @return A list of the recent files
     */
    public List<String> getFiles() {
        return recentFilesList;
    }
    
    /**
     * Get the size of the recent files list.
     * @return The size of the recent files list
     */
    public int size() {
        return recentFilesList.size();
    }
    
    /**
     *  Load the recent files from preferences.
     */
    private void loadPreferences() {
    	// Clear all recent files in list
    	if (recentFilesList.size() > 0) {
    		recentFilesList.clear();
    	}
    	
        // Load recent files from preferences
    	for (int i=0; i<maxFiles; i++) {
            String file = prefs.get(RECENT_FILE+i, null);

            if (file != null) {
            	recentFilesList.add(file);
            } else {
                break;
            }
        }
    }
    
    /**
     * Save the recent files to preferences.
     */
    private void savePreferences() {
    	int size = recentFilesList.size();
    	
    	// Save recent files from list to preferences,
    	// and remove extras from preferences
        for (int i=0; i<maxFiles; i++) {
            if (i < size) {
                prefs.put(RECENT_FILE+i, (String)recentFilesList.get(i));
            } else {
            	prefs.remove(RECENT_FILE+i);
            }
        }
    }
}