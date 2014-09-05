/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.input;

import java.util.List;
import java.util.Map;

/**
 * Manifest information for SVX files.
 *
 * @author Alan Hudson
 */
public class SVXManifest {
    /** The number of x voxels */
    private int gridSizeX;

    /** The number of y voxels */
    private int gridSizeY;

    /** The number of z voxels */
    private int gridSizeZ;

    /** The voxel size in meters */
    private double voxelSize;

    /** The number of bits for subvoxel specification */
    private int subvoxelBits;

    /** The user metadata */
    private Map<String,String> metadata;

    /** The channels of information */
    private List<Channel> channels;

    /** The material mappings */
    private List<MaterialReference> materials;

    /** The lower left corner of the grid in world coordinates */
    private double originX;

    /** The lower left corner of the grid in world coordinates */
    private double originY;

    /** The lower left corner of the grid in world coordinates */
    private double originZ;

    public int getGridSizeX() {
        return gridSizeX;
    }

    public void setGridSizeX(int x) {
        this.gridSizeX = x;
    }

    public int getGridSizeY() {
        return gridSizeY;
    }

    public void setGridSizeY(int y) {
        this.gridSizeY = y;
    }

    public int getGridSizeZ() {
        return gridSizeZ;
    }

    public void setGridSizeZ(int z) {
        this.gridSizeZ = z;
    }

    public double getVoxelSize() {
        return voxelSize;
    }

    public void setVoxelSize(double voxelSize) {
        this.voxelSize = voxelSize;
    }

    public int getSubvoxelBits() {
        return subvoxelBits;
    }

    public void setSubvoxelBits(int subvoxelBits) {
        this.subvoxelBits = subvoxelBits;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<MaterialReference> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialReference> materials) {
        this.materials = materials;
    }

    public double getOriginX() {
        return originX;
    }

    public void setOriginX(double originX) {
        this.originX = originX;
    }

    public double getOriginY() {
        return originY;
    }

    public void setOriginY(double originY) {
        this.originY = originY;
    }

    public double getOriginZ() {
        return originZ;
    }

    public void setOriginZ(double originZ) {
        this.originZ = originZ;
    }
}

class Channel {
    /** Definiton of channel types */
    public enum Type {
        DENSITY(0), COLOR(1), NORMAL(2), CUSTOM(3),
        MATERIAL_ID_1(4), MATERIAL_DENSITY_1(5), MATERIAL_ID_2(6), MATERIAL_DENSITY_2(7),
        MATERIAL_ID_3(8), MATERIAL_DENSITY_3(9), MATERIAL_ID_4(10), MATERIAL_DENSITY_4(11);

        private final int id;

        private Type(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    };

    /** The type of information in the channel */
    private Type type;

    /** The naming pattern for the image slices, can include the directory */
    private String slices;
}

class MaterialReference {
    /** The value used in voxel values */
    private short id;

    /** The registered urn */
    private String urn;
}
