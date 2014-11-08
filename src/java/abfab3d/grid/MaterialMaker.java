package abfab3d.grid;

/**
 * Converts attributes down to specific materials
 *
 * @author Alan Hudson
 */
public class MaterialMaker {
    private DensityMaker densityMaker;
    private String materialURN;

    public MaterialMaker(DensityMaker densityMaker) {
        this.densityMaker = densityMaker;
    }

    public MaterialMaker(DensityMaker densityMaker, String materialURN) {
        this.densityMaker = densityMaker;
        this.materialURN = materialURN;
    }

    public DensityMaker getDensityMaker() {
        return densityMaker;
    }

    public void setDensityMaker(DensityMaker densityMaker) {
        this.densityMaker = densityMaker;
    }

    public String getMaterialURN() {
        return materialURN;
    }

    public void setMaterialURN(String materialURN) {
        this.materialURN = materialURN;
    }
}
