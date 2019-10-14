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

import java.awt.image.BufferedImage;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import javax.swing.JPanel;

import abfab3d.core.Color;


public class ImagePanel extends JPanel {
    private BufferedImage img1;
    private BufferedImage img2;
    private BufferedImage curr;

    public ImagePanel(BufferedImage img) {
        img1 = img;
        img2 = img;
        curr = img1;
        Color c = (Color)ViewerConfig.getInstance().get(ViewerConfig.CANVAS_BACKGROUND_COLOR);
        setBackground(c.toAWT());

        setPreferredSize(new Dimension(512,512));
    }

    public void updateImage(BufferedImage img2) {
        if (curr == img1) {
            this.img2 = img2;
        } else {
            this.img1 = img2;
        }

        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (curr == img1) {
            curr = img2;
        } else {
            curr = img1;
        }

        if (curr == null) return;

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(curr, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    /**
     * Clear the panel back to the background color
     */
    public void clear() {
        img1 = null;
        img2 = null;

        repaint();
    }
}