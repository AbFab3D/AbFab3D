/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

import javax.vecmath.Vector3d;

import abfab3d.core.Bounds;
import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.MaterialType;
import abfab3d.datasources.ShapeList;
import abfab3d.param.Shape;
import abfab3d.datasources.ImageColorMap;
import abfab3d.datasources.TransformableDataSource;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;


import java.util.ArrayList;
import java.util.List;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * ShapeJS script scene available to script author
 *
 * @author Alan Hudson
 */
public class Scene extends BaseParameterizable {

    final static boolean DEBUG = false;

    final public static double DEFAULT_VOXEL_SIZE = 0.1*MM;
    final public static Vector3d DEFAULT_SIZE = new Vector3d(0.1,0.1,0.1);
    final public static Vector3d DEFAULT_CENTER = new Vector3d(0.,0.,0.);
    final public static Bounds DEFAULT_BOUNDS = new Bounds(0, 0.1, 0, 0.1, 0, 0.1);    
    final public static double DEFAULT_SMOOTHING_WIDTH = 0.5;
    final public static double DEFAULT_ERROR_FACTOR = 0.1;
    final public static int DEFAULT_MAX_PARTS_COUNT = Integer.MAX_VALUE;

    public static enum LightingRig {
        AUTO,THREE_POINT, THREE_POINT_COLORED,
    };

    final public static LightingRig DEFAULT_LIGHTING_RIG = LightingRig.AUTO;

    protected Bounds m_bounds = DEFAULT_BOUNDS;
    protected String m_name = "ShapeJS";
    protected SceneLights m_lights = new SceneLights();

    // we support up to 4 materials
    // material[0] is base material
    // material[1,2,3] correspond to material channels
    protected SceneMaterials m_materials = new SceneMaterials();
    protected SceneViewpoints m_viewpoints = new SceneViewpoints();
    protected RenderingParams m_renderingParams;
    protected ArrayList<Shape> m_shapes = new ArrayList<>();
    protected LightingRig m_lightingRig = DEFAULT_LIGHTING_RIG;
    protected Camera camera = new SimpleCamera();
    protected boolean m_userSetLights = false;
    protected Background m_background = new Background();
    protected int m_lastMaterial = 0;

    protected MaterialType m_materialType = MaterialType.SINGLE_MATERIAL;

    DoubleParameter mp_gradientStep = new DoubleParameter("gradientStep", "gradient step (in meters)", 0.001);
    DoubleParameter mp_distanceStepFactor = new DoubleParameter("distanceStepFactor", "relative step size in ray surface intersection", 0.95);
    DoubleParameter mp_surfacePrecision = new DoubleParameter("surfacePrecision", "surface precision (in meters)", 1.e-4);
    DoubleParameter mp_meshErrorFactor = new DoubleParameter("meshErrorFactor", "relative mesh error factor", DEFAULT_ERROR_FACTOR);
    DoubleParameter mp_meshSmoothingWidth = new DoubleParameter("meshSmoothingWidth", "relative mesh smooting width", DEFAULT_SMOOTHING_WIDTH);
    DoubleParameter mp_minShellVolume = new DoubleParameter("minShellVolume", "min shell volume to export", 0);
    IntParameter mp_maxPartsCount = new IntParameter("maxPartsCount", "max parts count to export", DEFAULT_MAX_PARTS_COUNT);

    ShapeList root = new ShapeList();
    Material mats[] = new Material[SceneMaterials.MAX_MATERIALS];

    // local params 
    protected Parameter m_aparam[] = new Parameter[]{
        mp_gradientStep,
        mp_distanceStepFactor,
        mp_surfacePrecision,
        mp_meshErrorFactor,
        mp_meshSmoothingWidth,
        mp_minShellVolume,
        mp_maxPartsCount,

    };
    
    public Scene(String name){

        m_name = name;
        initParams();
        initRendering();
        if(DEBUG)printf("Scene(%s)\n",name);
    }

    public Scene(Parameterizable source, Bounds bounds){

        this(source, bounds, bounds.getVoxelSize());
        if(DEBUG)printf("Scene(%s, %s)\n",source, bounds);
        //if(DEBUG)printf("Shape(%s, source:%s, bounds:%s)\n", this, source, bounds);
    }

    public Scene(Shape shape, Bounds bounds){
        this(shape,bounds,bounds.getVoxelSize());
    }

    public Scene(Parameterizable source, Bounds bounds, double voxelSize){
        if(DEBUG)printf("Scene(%s, %s, %s)\n",source, bounds, voxelSize);
        //if(DEBUG)printf("Shape(%s, source:%s bounds:%s vs:%7.5f)\n", this, source, bounds, voxelSize);
        Shape s = new Shape((DataSource)source, DefaultMaterial.getInstance());
        addShape(s);

        m_bounds = bounds;
        bounds.setVoxelSize(voxelSize);

        initParams();
        initRendering();
    }

    public Scene(Shape shape, Bounds bounds, double voxelSize){
        //if(DEBUG)printf("Shape(%s, source:%s bounds:%s vs:%7.5f)\n", this, source, bounds, voxelSize);
        addShape(shape);
        m_bounds = bounds;
        bounds.setVoxelSize(voxelSize);

        initParams();
        initRendering();
    }

    private void initParams() {
        buildParams();
    }

    /**
     * Setup a default rendering setup
     */
    protected void initRendering() {
        setLightingRig(m_lightingRig);
    }

    public static Light[] getColoredLighting() {
        Light[] lights = new Light[3];

        double intensity = 0.9;
        lights[0] = new Light(new Vector3d(10,0,20),new Color(1,0,0),0.1,intensity);
        lights[1] = new Light(new Vector3d(10,10,20),new Color(0,1,0),0,intensity);
        lights[2] = new Light(new Vector3d(0,10,20),new Color(0,0,1),0,intensity);
        return lights;
    }

    public static Light[] getTwoPointLighting() {
        double a0 = 0.4;
        double a1 = 0.8;

        Light[] lights = new Light[2];        
        lights[0] = new Light(new Vector3d(20,0,20),new Color(a0,a0,a0),0.1,1);
        lights[1] = new Light(new Vector3d(-10,0,20),new Color(a1,a1,a1),0,1);
        return lights;
    }

    public static Light[] getThreePointLighting() {

        double ambient = 0;

        double lscale = 1;
        double key = 0.8 * lscale;
        double fill = 0.4 * lscale;
        double rim = 0.25 * lscale;

        double intensity = 0.9;
        Color key_light_color = new Color(key,key,key );  // high noon
        Color fill_light_color = new Color(fill,fill,fill);  // high noon
        Color rim_light_color = new Color(rim,rim,rim);  // high noon
/*
        Light key_light = new Light(new Vector3d(10,-10,1000),key_light_color,ambient,1);
        Light fill_light = new Light(new Vector3d(1000,100,100),fill_light_color,0,1);
        Light rim_light = new Light(new Vector3d(-1000,900,-200),rim_light_color,0,1);

        Light key_light = new Light(new Vector3d(10,-10,20),key_light_color,ambient,intensity);
        Light fill_light = new Light(new Vector3d(10,10,20),fill_light_color,0,intensity);
        Light rim_light = new Light(new Vector3d(-10,90,-20),rim_light_color,0,intensity);
*/        
        Light key_light = new Light(new Vector3d(10,-10,100),key_light_color,ambient,1);
        Light fill_light = new Light(new Vector3d(1000,100,100),fill_light_color,0,1);
        Light rim_light = new Light(new Vector3d(-1000,900,-200),rim_light_color,0,1);
        
        Light lights[] = new Light[] {key_light,fill_light,rim_light};

        for(int i=0; i < lights.length; i++) {
            lights[i].setCastShadows(true);
            lights[i].setSamples(4);
            //lights[i].setRadius(50);

            // TODO: revisit this when area lights work better
            lights[i].setRadius(0);
        }
        return lights;
    }

    public void setCamera(Camera val) {
        this.camera = val;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setName(String val) {
        m_name = val;
    }

    public String getName() {
        return m_name;
    }

    public Bounds getBounds(){
        return m_bounds;
    }
    public void setBounds(Bounds bounds) {
        m_bounds = bounds.clone();
    }

    public List<Shape> getShapes() {
        return m_shapes;
    }

    /**
     * Get the sources.  No rendering tricks will be applied.
     * @return
     */
    public final List<Parameterizable> getSource(){
        ArrayList<Parameterizable> ret_val = new ArrayList<>(m_shapes.size());

        for(Shape shape : m_shapes) {
            ret_val.add((Parameterizable)shape);
        }

        return ret_val;
    }

    /**
     * Get the source for rendering.  This will include rendering tricks to approximate materials
     *
     * @draftMode Use low quality approximation
     * @return
     */
    public Parameterizable getRenderingSource(boolean draftMode) {
        if (root == null) {
            root = new ShapeList();
        } else {
            root.clear();
        }

        for(int i=0; i < mats.length; i++) {
            mats[i] = null;
        }

        int midx = 0;
        for(Shape shape : m_shapes) {
            Parameterizable p = (Parameterizable) shape.getSource();

            TransformableDataSource tds = (TransformableDataSource) p;
            if (!tds.isEnabled()) continue;

            Shape ds = null;
            if (draftMode) {
                ds = shape;
            } else {
                Material rm = shape.getMaterial();
                MaterialShader ms = rm.getShader();
                ds = new Shape(ms.getRenderingSource(tds),rm,shape.getShader());
            }

            Material dsm = ds.getMaterial();
            boolean found = false;
            for(int i=0; i < mats.length; i++) {
                if (dsm == mats[i]) {
                    ds.setMaterialID(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                mats[midx] = ds.getMaterial();
                ds.setMaterialID(midx);
                midx++;
            }

            root.add(ds);
        }

        for(int i=midx; i < SceneMaterials.MAX_MATERIALS; i++) {
            mats[i] = DefaultMaterial.getInstance();
        }

        m_materials.setMaterials(mats);

        root.initialize();
        return root;
    }


    public void addShape(Shape shape) {
        m_shapes.add(shape);

        int matId = addMaterial(shape.getMaterial());
    }

    public Shape removeShape(Shape shape) {
        Shape toremove = null;

        for(Shape s : m_shapes) {
            if (s.getSource() == shape) {
                toremove = s;
            }
        }

        if (toremove != null) {
            Material mat = toremove.getMaterial();
            m_shapes.remove(toremove);

            boolean found = false;
            for(Shape s : m_shapes) {
                if (s.getMaterial() == mat) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                removeMaterial(mat);
            }
        }

        return toremove;
    }

    // Keep for backwards compat
    public void setSource(DataSource source) {
        m_shapes.clear();
        m_lastMaterial = 0;
        Shape shape = new Shape(source, DefaultMaterial.getInstance());
        m_shapes.add(shape);
        addMaterial(shape.getMaterial());
    }

    public void setShape(Shape[] shapes) {
        m_shapes.clear();
        m_lastMaterial = 0;

        for(Shape s : shapes) {
            m_shapes.add(s);
            addMaterial(s.getMaterial());
        }
    }

    public void setShape(int idx, Shape shape) {
        m_shapes.set(idx,shape);
    }

    public void setVoxelSize(double voxelSize) {
        m_bounds.setVoxelSize(voxelSize);
    }
    public double getVoxelSize() {
        return m_bounds.getVoxelSize();
    }

    public void setMeshErrorFactor(double value){
        mp_meshErrorFactor.setValue(value);
    }

    public double getMeshErrorFactor(){
        return mp_meshErrorFactor.getValue();
    }

    public void setMeshSmoothingWidth(double value){
        mp_meshSmoothingWidth.setValue(value);
    }

    public double getMeshSmoothingWidth(){
        return mp_meshSmoothingWidth.getValue();
    }

    public void setGradientStep(double val) {
        mp_gradientStep.setValue(val);
    }

    public double getGradientStep() {
        return mp_gradientStep.getValue();
    }

    public void setMinShellVolume(double value){
        mp_minShellVolume.setValue(value);
    }

    public double getMinShellVolume(){
        return mp_minShellVolume.getValue();
    }

    public int getMaxPartsCount() {
        return mp_maxPartsCount.getValue();
    }

    public void setMaxPartsCount(int maxPartsCount) {
        mp_maxPartsCount.setValue(maxPartsCount);
    }

    /**
     * @noRefGuide
     */
    public void reinitialize() {
        // reinitialize all properties.  Done after param changes.
        m_background.initialize();
    }

    /**
       set rendering material for given index 
     */
    public void setMaterial(int index,Material mat) {
        if (index > SceneMaterials.MAX_MATERIALS - 1) throw new IllegalArgumentException("Cannot exceed " + SceneMaterials.MAX_MATERIALS + " materials");
        m_shapes.get(index).setMaterial(mat);
        addMaterial(mat);
        buildParams();
    }

    /**
     * Adds a material to scene.  Duplicates will be reused
     * @param mat
     * @return
     */
    private int addMaterial(Material mat) {
        List<Material> mats = m_materials.getMaterials();
        int idx = 0;
        for(Material m : mats) {
            if (m.equals(mat)) {
                return idx;
            }
            idx++;
        }

        m_lastMaterial++;

        m_materials.setMaterial(m_lastMaterial, mat);
        buildParams();

        return m_lastMaterial;
    }

    private void removeMaterial(Material mat) {
        m_materials.removeMaterial(mat);
        m_lastMaterial--;
    }

    /**
       return type of material used in scene 
     */
    public MaterialType getMaterialType() {
        return m_materialType;
    }

    /**
     set type of material used in scene
     SINGLE_MATERIAL
     MULTI_MATERIAL
     COLOR_MATERIAL
     */
    public void setMaterialType(MaterialType type) {

       if (type == null) throw new IllegalArgumentException("Type cannot be null");

        m_materialType = type;
        Shape shape = m_shapes.get(0);
        Material mat = shape.getMaterial();
        Parameterizable sp = (Parameterizable)mat.getShader().getShaderParams();
        if(DEBUG)printf("Scene.setMaterialType(%s, %s)\n",sp,type);
        if(type == MaterialType.COLOR_MATERIAL){
            sp.set("materialType", "full color");
        } 

    }

    public void setLights(Light[] lights) {
        m_userSetLights = true;
        m_lights.setLights(lights);

        buildParams();
    }

    public SceneLights getLights() {
        return m_lights;
    }

    public SceneMaterials getMaterials() {
        return m_materials;
    }

    public void setViewpoints(Viewpoint[] viewpoints) {
        m_viewpoints.setViewpoints(viewpoints);

        buildParams();
    }

    public SceneViewpoints getViewpoints() {
        return m_viewpoints;
    }

    public int getMaterialID(Material mat) {
        // TODO: Should we use a map instead of a list for faster access?

        List<Material> mats = m_materials.getMaterials();
        int idx = 0;
        for(Material m : mats) {
            if (m.equals(mat)) {
                return idx;
            }
            idx++;
        }

        throw new IllegalArgumentException("Cannot find material: " + mat);
    }

    public void setLightingRig(LightingRig rig) {
        // Ignore lighting rig if the user explicitly sets lights
        if (m_userSetLights) return;
        m_lightingRig = rig;

        switch(m_lightingRig) {
            case AUTO:
                // do nothing yet
                break;
            case THREE_POINT_COLORED:
                m_lights.setLights(getColoredLighting());
                break;
            case THREE_POINT:
                m_lights.setLights(getThreePointLighting());
                break;
            default:
                throw new IllegalArgumentException("Unhandled lighting rig: " + rig);
        }

    }

    public LightingRig getLightingRig() {
        return m_lightingRig;
    }

    public void setRenderingParams(RenderingParams params) {
        m_renderingParams = params;
    }

    public RenderingParams getRenderingParams() {
        return m_renderingParams;
    }

    public void setBackground(Background val) {
        m_background = val;
    }

    public Background getBackground() {
        return m_background;
    }

    /**
     * Build up params from underlying children
     */
    private void buildParams() {

        // TODO: Dodgy make real

        
        clearParams();
        addParams(m_lights.getParams());
        addParams(m_materials.getParams());
        addParams(m_viewpoints.getParams());
        addParams(m_background.getParams());
        addParams(m_aparam);

        if(DEBUG)printf("%s buildParams()\n", this);

    }

    public String toString(){
        return fmt("Shape(\"%s\",%s, vs: %7.5f)", m_name, m_bounds, m_bounds.getVoxelSize());
    }

}