package abfab3d.shapejs;

import static abfab3d.core.Output.printf;

/**
 * Utilities for configuring the ImageSetup in a repeatable way
 *
 * @author Alan Hudson
 */
public class ImageSetupUtils {
    public static final boolean DEBUG = false;

    /**
     * Should we force to one tile, useful for debugging
     */
    private static final boolean ONE_TILE_MODE = false;


    static {
        if (ONE_TILE_MODE) printf("**** Using only one tile, do not release ***\n");
    }

    public static int configureSetup(ImageSetup setup, boolean navigating, RenderOptions options) {
        float sshadowQuality = 0;
        switch (options.shadowQuality) {
            case DRAFT:
                sshadowQuality = 0;
                break;
            case NORMAL:
                sshadowQuality = 0.5f;
                break;
            case FINE:
                sshadowQuality = 0.75f;
                break;
            case SUPER_FINE:
                sshadowQuality = 1.0f;
                break;
        }

        float squality = 0;
        switch (options.quality) {
            case DRAFT:
                squality = 0;
                break;
            case NORMAL:
                squality = 0.5f;
                break;
            case FINE:
                squality = 0.75f;
                break;
            case SUPER_FINE:
                squality = 1.0f;
                break;
        }

        boolean qu = (options.quality == Quality.FINE || options.quality == Quality.SUPER_FINE) ? true : false;

        return configureSetup(setup, navigating, squality, sshadowQuality, options.aaSamples, options.rayBounces, qu);
    }

    public static int configureSetup(ImageSetup setup, boolean navigating, Quality renderingQuality, Quality shadowQuality, int aa, int maxRayBounces, boolean useBumpmaps) {
        float sshadowQuality = 0;
        switch (shadowQuality) {
            case DRAFT:
                sshadowQuality = 0;
                break;
            case NORMAL:
                sshadowQuality = 0.5f;
                break;
            case FINE:
                sshadowQuality = 0.75f;
                break;
            case SUPER_FINE:
                sshadowQuality = 1.0f;
                break;
        }

        float squality = 0;
        switch (renderingQuality) {
            case DRAFT:
                squality = 0;
                break;
            case NORMAL:
                squality = 0.5f;
                break;
            case FINE:
                squality = 0.75f;
                break;
            case SUPER_FINE:
                squality = 1.0f;
                break;
        }

        return configureSetup(setup, navigating, squality, sshadowQuality, aa, maxRayBounces, useBumpmaps);
    }

    public static int configureSetup(ImageSetup setup, boolean navigating, float renderingQuality, float shadowQuality, int aa, int maxRayBounces, boolean useBumpmaps) {
        int tiles = 1;
        if (navigating) {
            tiles += Math.ceil((setup.width - 512) / 512);
            tiles += Math.ceil((setup.height - 512) / 512);

            //printf("tiles: %d\n",tiles);

            setup.aa = 1;
            setup.bumpMaps = false;
            setup.shadowQuality = 0;
            setup.quality = 0.5f;
            setup.maxRayBounces = Math.min(1, maxRayBounces);

            if (ONE_TILE_MODE) {
                tiles = 1;
            }

            return tiles;
        }

        setup.aa = aa;
        tiles = tiles + (aa - 1);

        setup.bumpMaps = useBumpmaps;
        setup.shadowQuality = shadowQuality;
        setup.quality = renderingQuality;
        setup.maxRayBounces = maxRayBounces;

        if (useBumpmaps) tiles++;
        if (maxRayBounces > 0) tiles++;
        if (setup.shadowQuality > 0.5) {
            tiles++;
        }


        if (ONE_TILE_MODE) {
            tiles = 1;
        }

        if (DEBUG) printf("Number of tiles: %d\n", tiles);
        return tiles;
    }
}
