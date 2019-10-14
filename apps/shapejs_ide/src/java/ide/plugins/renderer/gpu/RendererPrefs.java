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
package ide.plugins.renderer.gpu;

import abfab3d.shapejs.Quality;
import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

import static abfab3d.core.Output.printf;


/**
 * Preferences for the Renderer plugin.
 *
 * @author Alan Hudson
 * @version 1.0
 */
public class RendererPrefs extends Prefs {

    /**
     * Whether the GUI plugin window is active (visible).
     */
    public boolean windowVisible;

    /**
     * The location of the dockable console output window.
     */
    public int windowPosition;

    public String renderEngine;  // gpu,software,net
    public String shapeJSServer;

    public int maxWidth;
    public int maxHeight;

    // Params while moving, usually lower quality
    public int movingAaSamples;
    public String movingQuality;
    public String movingShadowQuality;
    public int movingRayBounces;
    public double movingRenderScale;

    // Params while still
    public int stillAaSamples;
    public String stillQuality;
    public String stillShadowQuality;
    public int stillRayBounces;
    public double stillRenderScale;

    // Params while saving
    public int saveAaSamples;
    public String saveQuality;
    public String saveShadowQuality;
    public int saveRayBounces;
    public int saveWidth;
    public int saveHeight;

    public String libPath;  // semicolon separated list of directories

    public String lastSaveModelDir;
    public String lastSaveRenderDir;

    /**
     * Key stroke that toggles the console window's visibility.
     */
    public KeyStroke windowVisibilityAccelerator;


    /**
     * Overridden to validate the dockable window position value.
     */
    @Override
    public void load(InputStream in) throws IOException {
        super.load(in);
        // Ensure window position is valid.
        if (!DockableWindow.isValidPosition(windowPosition)) {
            windowPosition = DockableWindow.RIGHT;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaults() {

        windowVisible = true;
        windowPosition = DockableWindow.RIGHT;
        windowVisibilityAccelerator = null;
        renderEngine = "cpu";
        shapeJSServer = "https://gpu-us-west-2b.shapeways.com";
//		shapeJSServer="http://localhost:8080";

        movingAaSamples = 1;
        movingQuality = String.valueOf(Quality.DRAFT);
        movingShadowQuality = String.valueOf(Quality.DRAFT);
        movingRayBounces = 0;
        movingRenderScale = 0.5;

        stillAaSamples = 1;
        stillQuality = String.valueOf(Quality.NORMAL);
        stillShadowQuality = String.valueOf(Quality.DRAFT);
        stillRayBounces = 0;
        stillRenderScale = 1.0;

        saveAaSamples = 3;
        saveQuality = String.valueOf(Quality.SUPER_FINE);
        saveShadowQuality = String.valueOf(Quality.DRAFT);
        saveRayBounces = 0;
        saveWidth = 1024;
        saveHeight = 1024;

        maxWidth = 1024;
        maxHeight = 1024;
        libPath = "";
        lastSaveModelDir = "/tmp";
        lastSaveRenderDir = "/tmp";
    }
}
