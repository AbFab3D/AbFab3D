/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
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

/**
 * Utilities for color coding
 *
 * @author Alan Hudson
 */
public class ColorCoding {
    private static final float[][] kelleyColorsAll = new float[][]{
            {242 / 255f, 243 / 255f, 244 / 255f},
            {34 / 255f, 34 / 255f, 24 / 255f},
            {243 / 255f, 195 / 255f, 0 / 255f},
            {135 / 255f, 86 / 255f, 146 / 255f},
            {243 / 255f, 132 / 255f, 0 / 255f},
            {161 / 255f, 202 / 255f, 241 / 255f},
            {161 / 255f, 202 / 255f, 241 / 255f},
            {190 / 255f, 0 / 255f, 50 / 255f},
            {194 / 255f, 178 / 255f, 128 / 255f},
            {132 / 255f, 132 / 255f, 130 / 255f},
            {0 / 255f, 136 / 255f, 86 / 255f},
            {230 / 255f, 143 / 255f, 172 / 255f},
            {0 / 255f, 103 / 255f, 165 / 255f},
            {249 / 255f, 147 / 255f, 121 / 255f},
            {96 / 255f, 78 / 255f, 151 / 255f},
            {246 / 255f, 166 / 255f, 0 / 255f},
            {179 / 255f, 68 / 255f, 108 / 255f},
            {220 / 255f, 211 / 255f, 0 / 255f},
            {136 / 255f, 45 / 255f, 23 / 255f},
            {141 / 255f, 182 / 255f, 0 / 255f},
            {101 / 255f, 69 / 255f, 34 / 255f},
            {226 / 255f, 88 / 255f, 34 / 255f},
            {43 / 255f, 61 / 255f, 38 / 255f}
    };

    private static final float[][] kelleyColorsMinusBlack = new float[][]{
            {242 / 255f, 243 / 255f, 244 / 255f},
            {243 / 255f, 195 / 255f, 0 / 255f},
            {135 / 255f, 86 / 255f, 146 / 255f},
            {243 / 255f, 132 / 255f, 0 / 255f},
            {161 / 255f, 202 / 255f, 241 / 255f},
            {161 / 255f, 202 / 255f, 241 / 255f},
            {190 / 255f, 0 / 255f, 50 / 255f},
            {194 / 255f, 178 / 255f, 128 / 255f},
            {132 / 255f, 132 / 255f, 130 / 255f},
            {0 / 255f, 136 / 255f, 86 / 255f},
            {230 / 255f, 143 / 255f, 172 / 255f},
            {0 / 255f, 103 / 255f, 165 / 255f},
            {249 / 255f, 147 / 255f, 121 / 255f},
            {96 / 255f, 78 / 255f, 151 / 255f},
            {246 / 255f, 166 / 255f, 0 / 255f},
            {179 / 255f, 68 / 255f, 108 / 255f},
            {220 / 255f, 211 / 255f, 0 / 255f},
            {136 / 255f, 45 / 255f, 23 / 255f},
            {141 / 255f, 182 / 255f, 0 / 255f},
            {101 / 255f, 69 / 255f, 34 / 255f},
            {226 / 255f, 88 / 255f, 34 / 255f},
            {43 / 255f, 61 / 255f, 38 / 255f}
    };

    public static float[][] getKelleyColors(boolean excludeBlack) {
        if (excludeBlack) return kelleyColorsMinusBlack;
        else return kelleyColorsAll;
    }
}
