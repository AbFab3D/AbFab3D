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
import abfab3d.core.ResultCodes;
import abfab3d.datasources.ShapeList;
import abfab3d.param.BaseSNodeFactory;
import abfab3d.param.SNodeListParameter;
import abfab3d.param.SNodeParameter;
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
public class Scene extends BaseParameterizable implements Initializable {

    final static boolean DEBUG = false;

    final public static double DEFAULT_VOXEL_SIZE = 0.1*MM;
    final public static Vector3d DEFAULT_SIZE = new Vector3d(0.1,0.1,0.1);
    final public static Vector3d DEFAULT_CENTER = new Vector3d(0.,0.,0.);
    final public static Bounds DEFAULT_BOUNDS = new Bounds(0, 0.1, 0, 0.1, 0, 0.1);    
    final public static double DEFAULT_SMOOTHING_WIDTH = 0.5;
    final public static double DEFAULT_ERROR_FACTOR = 0.1;
    final public static int DEFAULT_MAX_PARTS_COUNT = Integer.MAX_VALUE;

    public static enum LightingRig {
        THREE_POINT, THREE_POINT_COLORED,
    };

    final public static LightingRig DEFAULT_LIGHTING_RIG = LightingRig.THREE_POINT_COLORED;

    protected Bounds m_bounds = DEFAULT_BOUNDS;
    protected String m_name = "ShapeJS";

    protected SceneMaterials m_materials = new SceneMaterials();
    protected ArrayList<Shape> m_shapes = new ArrayList<>();
    protected LightingRig m_lightingRig = DEFAULT_LIGHTING_RIG;

    protected int m_lastMaterial = 0;

    protected MaterialType m_materialType = MaterialType.SINGLE_MATERIAL;

    DoubleParameter mp_gradientStep = new DoubleParameter("gradientStep", "gradient step (in meters)", 0.001);
    DoubleParameter mp_meshErrorFactor = new DoubleParameter("meshErrorFactor", "relative mesh error factor", DEFAULT_ERROR_FACTOR);
    DoubleParameter mp_meshSmoothingWidth = new DoubleParameter("meshSmoothingWidth", "relative mesh smooting width", DEFAULT_SMOOTHING_WIDTH);
    DoubleParameter mp_minShellVolume = new DoubleParameter("minShellVolume", "min shell volume to export", 0);
    IntParameter mp_maxPartsCount = new IntParameter("maxPartsCount", "max parts count to export", DEFAULT_MAX_PARTS_COUNT);

    SNodeParameter mp_background = new SNodeParameter("background","Background Params", new Background());
    SNodeParameter mp_camera = new SNodeParameter("camera", "Camera params", new SimpleCamera());
    SNodeListParameter mp_viewpoints = new SNodeListParameter("viewpoints", "Viewpoints", new BaseSNodeFactory(new String[]{"viewpoint"}, new String[]{"abfab3d.shapejs.Viewpoint"}));
    SNodeListParameter mp_lights = new SNodeListParameter("lights", "Lights", new BaseSNodeFactory(new String[]{"light"}, new String[]{"abfab3d.shapejs.Light"}));
    SNodeListParameter mp_tracingParams = new SNodeListParameter("tracingParams", "Tracing Params", makeDefaultTracingParams(),new BaseSNodeFactory(new String[]{"tracingParams"}, new String[]{"abfab3d.shapejs.TracingParams"}));

    // TODO: Not ready to migrate this yet
    //SNodeListParameter mp_materials = new SNodeListParameter("materials", "Materials", new BaseSNodeFactory(new String[]{"material"}, new String[]{"abfab3d.core.Material"}));

    // TODO: Not certain about this concept now, it would need to be per material
    //SNodeParameter mp_renderingParams = new SNodeParameter("renderingParams", "Rendering params", new RenderingParams());

    ShapeList root = new ShapeList();
    //Material mats[] = new Material[SceneMaterials.MAX_MATERIALS];

    // local params 
    protected Parameter m_aparam[] = new Parameter[]{
        mp_gradientStep,
        mp_meshErrorFactor,
        mp_meshSmoothingWidth,
        mp_minShellVolume,
        mp_maxPartsCount,
        mp_background,
        mp_camera,
        mp_viewpoints,
        mp_lights,
        mp_tracingParams
    };

    public Scene(String name){
        m_name = name;
        initParams();
        initDefaults();
    }

    public Scene(Parameterizable source, Bounds bounds){

        this(source, bounds, bounds.getVoxelSize());
        //if(DEBUG)printf("Shape(%s, source:%s, bounds:%s)\n", this, source, bounds);
    }

    public Scene(Shape shape, Bounds bounds){
        this(shape,bounds,bounds.getVoxelSize());
    }

    public Scene(Parameterizable source, Bounds bounds, double voxelSize){
        //if(DEBUG)printf("Shape(%s, source:%s bounds:%s vs:%7.5f)\n", this, source, bounds, voxelSize);
        Shape s = new Shape((DataSource)source, DefaultMaterial.getInstance());
        addShape(s);

        m_bounds = bounds;
        bounds.setVoxelSize(voxelSize);

        initParams();
        initDefaults();
    }

    public Scene(Shape shape, Bounds bounds, double voxelSize){
        //if(DEBUG)printf("Shape(%s, source:%s bounds:%s vs:%7.5f)\n", this, source, bounds, voxelSize);
        addShape(shape);
        m_bounds = bounds;
        bounds.setVoxelSize(voxelSize);

        initParams();
        initDefaults();
    }

    public Scene(Bounds bounds, double voxelSize){

        m_bounds = bounds;
        bounds.setVoxelSize(voxelSize);

        initParams();
        initDefaults();
    }

    public Scene(Bounds bounds){

        m_bounds = bounds;

        initParams();
        initDefaults();
    }

    private void initParams() {
        buildParams();
    }
    
    
    protected ArrayList<TracingParams> makeDefaultTracingParams(){

        ArrayList<TracingParams> tp = new ArrayList<TracingParams>();
        tp.add(new TracingParams(TracingParams.ModeType.DRAFT,5e-3,1.0));
        tp.add(new TracingParams(TracingParams.ModeType.NORMAL,1e-3,0.95));
        tp.add(new TracingParams(TracingParams.ModeType.FINE,6e-4,0.95));
        tp.add(new TracingParams(TracingParams.ModeType.SUPER_FINE,3e-4,0.95));
        return tp;
    }


    @Override
    public int initialize() {
        List<Parameterizable> list = getSource();
        for (Parameterizable ds : list) {
            if (ds instanceof Initializable) {
                ((Initializable) ds).initialize();
            }
        }

        return ResultCodes.RESULT_OK;
    }

    /**
     * Setup a default rendering setup
     */
    protected void initDefaults() {
        setLightingRig(m_lightingRig);
    }

    public static List<Light> getColoredLighting() {
        ArrayList<Light> lights = new ArrayList<>();

        double intensity = 0.9;
        lights.add(new Light(new Vector3d(10,0,20),new Color(1,0,0),0.1,intensity));
        lights.add(new Light(new Vector3d(10,10,20),new Color(0,1,0),0,intensity));
        lights.add(new Light(new Vector3d(0,10,20),new Color(0,0,1),0,intensity));
        return lights;
    }

    public static List<Light> getTwoPointLighting() {
        double a0 = 0.4;
        double a1 = 0.8;

        ArrayList<Light> lights = new ArrayList<>();
        lights.add(new Light(new Vector3d(20,0,20),new Color(a0,a0,a0),0.1,1));
        lights.add(new Light(new Vector3d(-10,0,20),new Color(a1,a1,a1),0,1));
        return lights;
    }

    public static List<Light> getThreePointLighting() {

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

        ArrayList<Light> lights = new ArrayList<>();
        lights.add(key_light);
        lights.add(fill_light);
        lights.add(rim_light);


        for(int i=0; i < lights.size(); i++) {
            lights.get(i).setCastShadows(true);
            lights.get(i).setSamples(4);
            //lights[i].setRadius(50);

            // TODO: revisit this when area lights work better
            lights.get(i).setRadius(0);
        }
        return lights;
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
        
        if(DEBUG)printf("%s.getRenderingSource(%s)\n", this.getClass().getName(), draftMode); 
        if (root == null) {
            root = new ShapeList();
        } else {
            root.clear();
        }

        m_materials.clear();


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
            ds.setMaterialID(m_materials.addMaterial(dsm));
            root.add(ds);
        }

        root.initialize();
        return root;
    }


    public void addShape(Shape shape) {
        m_shapes.add(shape);
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
        addShape(new Shape(source, DefaultMaterial.getInstance()));
    }

    public void setShape(Shape[] shapes) {
        m_shapes.clear();
        m_lastMaterial = 0;

        for(Shape s : shapes) {
            addShape(s);
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
        //m_background.initialize();
    }

    /**
       set rendering material for given index 
     */
    public void setMaterial(int index,Material mat) {
        printf("*** scene.setMaterial(int index,Material mat) is obsolete *** \n Use Shape.setMaterial(Material) instead\n");
    }

    /**
     * Adds a material to scene.  Duplicates will be reused
     * @param mat
     * @return
     */
    /*
    private  int addMaterial(Material mat) {
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
    */
    private void removeMaterial(Material mat) {
        m_materials.removeMaterial(mat);
        m_lastMaterial--;
    }

    public SceneMaterials getMaterials() {
        return m_materials;
    }

    public void setViewpoints(Viewpoint[] viewpoints) {
        mp_viewpoints.clear();

        for (int i = 0; i < viewpoints.length; i++) {
            mp_viewpoints.add(viewpoints[i]);
        }

        clearParams();
        addParam(mp_viewpoints);
    }

    public List<Viewpoint> getViewpoints() {
        return mp_viewpoints.getValue();
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
        m_lightingRig = rig;

        switch(m_lightingRig) {
            case THREE_POINT_COLORED:
                mp_lights.setValue(getColoredLighting());
                break;
            case THREE_POINT:
                mp_lights.setValue(getThreePointLighting());
                break;
            default:
                throw new IllegalArgumentException("Unhandled lighting rig: " + rig);
        }

    }

    public LightingRig getLightingRig() {
        return m_lightingRig;
    }

    /*
    public void setRenderingParams(RenderingParams params) {
        m_renderingParams = params;
    }

    public RenderingParams getRenderingParams() {
        return m_renderingParams;
    }
    */

    public void setBackground(Background val) {
        mp_background.setValue(val);
    }

    public Background getBackground() {
        return (Background) mp_background.getValue();
    }

    public void setTracingParams(List<TracingParams> val) {
        mp_tracingParams.setValue(val);
    }

    /**
       set tracing param for the scene 
     */
    public void setTracingParams(TracingParams tparam[]) {
        
        mp_tracingParams.clear();

        for (int i = 0; i < tparam.length; i++) {
            mp_tracingParams.add(tparam[i]);
        }
        
    }

    public List<TracingParams> getTracingParams() {
        return (List<TracingParams>) mp_tracingParams.getValue();
    }

    public void setCamera(Camera val) {
        mp_camera.setValue(val);
    }

    public Camera getCamera() {
        return (Camera) mp_camera.getValue();
    }

    public void setLights(Light[] lights) {
        mp_lights.clear();

        for (int i = 0; i < lights.length; i++) {
            mp_lights.add(lights[i]);
        }
    }

    public List<Light> getLights() {
        return mp_lights.getValue();
    }

    /**
     * Build up params from underlying children
     */
    private void buildParams() {

        // TODO: Dodgy make real

        
        clearParams();
        addParams(m_materials.getParams());
        addParams(m_aparam);

        if(DEBUG)printf("%s buildParams()\n", this);

    }

    public String toString(){
        return fmt("Shape(\"%s\",%s, vs: %7.5f)", m_name, m_bounds, m_bounds.getVoxelSize());
    }

}