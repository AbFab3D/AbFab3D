package abfab3d.shapejs;

import java.awt.image.BufferedImage;

/**
 *
 */
public class ImageUtilTest {
    /**
     * Determines image equality based on size and color of each pixel.
     *
     * @param image1 The first image
     * @param image2 The second image
     * @return true if images are equal, and false otherwise
     */
    public static boolean isImageEqual(BufferedImage image1, BufferedImage image2) {
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();
//System.out.println(width1 + " x " + height1 + " : " + width2 + " x " + height2);
        if ( width1 != width2 || height1 != height2) {
            return false;
        }

        int color;
        int red1, green1, blue1;
        int red2, green2, blue2;

        for (int x=0; x<width1; x++) {
            for (int y=0; y<height1; y++) {
                color =  image1.getRGB(x, y);
                red1   = (color & 0x00ff0000) >> 16;
                green1 = (color & 0x0000ff00) >> 8;
                blue1  =  color & 0x000000ff;

                color =  image2.getRGB(x, y);
                red2   = (color & 0x00ff0000) >> 16;
                green2 = (color & 0x0000ff00) >> 8;
                blue2  =  color & 0x000000ff;

                if (red1 != red2 || green1 != green2 || blue1 != blue2) {
//System.out.println("Image 1, [" + x + ", " + y + "]: " + red1 + " " + green1 + " " + blue1);
//System.out.println("Image 2, [" + x + ", " + y + "]: " + red2 + " " + green2 + " " + blue2);
                    return false;
                }
            }
        }

        return true;
    }

    // Check that the image is not totally one color
    public static boolean isConstantImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int color;
        int fr,fg,fb;

        color =  image.getRGB(0, 0);
        fr   = (color & 0x00ff0000) >> 16;
        fg = (color & 0x0000ff00) >> 8;
        fb  =  color & 0x000000ff;

        int red, green, blue;

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                color =  image.getRGB(x, y);
                red   = (color & 0x00ff0000) >> 16;
                green = (color & 0x0000ff00) >> 8;
                blue  =  color & 0x000000ff;

//System.out.println(x + ", " + y + " : " + red + " " + green + " " + blue);
                if (red != fr || green != fg || blue != fb) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get the average color of a rendering.
     * @param image
     * @param bgColor  The background color to ignore
     * @param result
     */
    public static void getAverageColor(BufferedImage image, float[] bgColor, float[] result) {
        int width = image.getWidth();
        int height = image.getHeight();

        int color;
        int br,bg,bb;
        long rtot=0,gtot=0,btot=0;
        long count = 0;

        int EPS = 2;

        br = (int)(255 * bgColor[0]);
        bg = (int)(255 * bgColor[1]);
        bb = (int)(255 * bgColor[2]);

        int red, green, blue;

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                color =  image.getRGB(x, y);
                red   = (color & 0x00ff0000) >> 16;
                green = (color & 0x0000ff00) >> 8;
                blue  =  color & 0x000000ff;

                //System.out.println(x + ", " + y + " : " + red + " " + green + " " + blue);
                if (Math.abs(br - red) > EPS || Math.abs(bg - green) > EPS || Math.abs(bb - blue) > EPS) {
                    // non background color
                    count++;
                    rtot += red;
                    gtot += green;
                    btot += blue;
                }
            }
        }

        if (count == 0) {
            // all background
            result[0] = bgColor[0];
            result[1] = bgColor[1];
            result[2] = bgColor[2];

            return;
        }
        result[0] = (rtot / count) / 255.0f;
        result[1] = (gtot / count) / 255.0f;
        result[2] = (btot / count) / 255.0f;
    }
}
