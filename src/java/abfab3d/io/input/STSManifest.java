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
 * Manifest information for STS files.
 *
 * @author Alan Hudson
 */
public class STSManifest {
    /** The material mappings */
    private Map<String,String> materials;
    private List<STSPart> parts;
    /** The user metadata */
    private Map<String,String> metadata;


    public List<STSPart> getParts() {
        return parts;
    }

    public void setParts(List<STSPart> parts) {
        this.parts = parts;
    }

    public Map<String,String> getMaterials() {
        return materials;
    }

    public void setMaterials(Map<String,String> materials) {
        this.materials = materials;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}

class STSPart {
    private String file;
    private String materialID;

    STSPart(String file, String materialID) {
        this.file = file;
        this.materialID = materialID;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMaterialID() {
        return materialID;
    }

    public void setMaterialID(String materialID) {
        this.materialID = materialID;
    }
}
