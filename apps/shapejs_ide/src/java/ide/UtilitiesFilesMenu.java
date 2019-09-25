package ide;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import static abfab3d.core.Output.printf;

public abstract class UtilitiesFilesMenu extends JMenu {
    private static final String LOC = "scripts/utils";
    private int maxFileHistorySize;
    private List<String> fileHistory = new ArrayList<String>();

    public UtilitiesFilesMenu(String name) {
        super(name);

        // scan
        File dir = new File(LOC);
        File[] dirs = dir.listFiles();

        for(int i=0; i < dirs.length; i++) {
            if (dirs[i].isDirectory()) {
                printf("Checking dir for manifest\n");
                File[] files = dirs[i].listFiles();
                File projFile = null;
                for(int j=0; j < files.length; j++) {
                    if (files[j].getName().equals("manifest.json")) {
                        projFile = files[j];
                        break;
                    }
                }

                if (projFile != null) {
                    printf("Adding util: %s\n", projFile.getParentFile().getName());
                    addFileToFileHistory(projFile.getAbsolutePath());
                }
            }
        }
    }

    public void addFileToFileHistory(String fileFullPath) {
        if (!this.getShouldIgnoreFile(fileFullPath)) {
            int index = this.fileHistory.indexOf(fileFullPath);
            JMenuItem menuItem;
            if (index > -1) {
                menuItem = (JMenuItem)this.getMenuComponent(index);
                this.remove(index);
                this.add(menuItem);
                String temp = (String)this.fileHistory.remove(index);
                this.fileHistory.add(0, temp);
            } else {
                menuItem = new JMenuItem(this.createOpenAction(fileFullPath));
                this.add(menuItem);
                this.fileHistory.add(0, fileFullPath);
            }
        }
    }

    protected abstract Action createOpenAction(String var1);

    public String getFileFullPath(int index) {
        return (String)this.fileHistory.get(index);
    }

    public List<String> getFileHistory() {
        return new ArrayList(this.fileHistory);
    }

    public int getMaximumFileHistorySize() {
        return this.maxFileHistorySize;
    }

    protected boolean getShouldIgnoreFile(String fileFullPath) {
        return false;
    }

    public void setMaximumFileHistorySize(int newSize) {
        if (newSize >= 0) {
            this.maxFileHistorySize = newSize;

            while(this.getItemCount() > this.maxFileHistorySize) {
                this.remove(this.getItemCount() - 1);
                this.fileHistory.remove(this.fileHistory.size() - 1);
            }

        }
    }
}

