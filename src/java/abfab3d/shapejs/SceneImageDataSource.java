/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

import java.util.List;

import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import abfab3d.core.Bounds;
import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.MaterialType;
import abfab3d.core.ResultCodes;

import abfab3d.param.Parameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.BaseParameterizable;

import abfab3d.core.MathUtil;


import static abfab3d.core.MathUtil.step01;
import static java.lang.Math.*;

import static abfab3d.shapejs.VecUtils.interpolate;
import static abfab3d.shapejs.VecUtils.normalize;
import static abfab3d.shapejs.VecUtils.reflect;
import static abfab3d.shapejs.VecUtils.str;
import static abfab3d.shapejs.VecUtils.addSet;
import static abfab3d.shapejs.VecUtils.mul;
import static abfab3d.shapejs.VecUtils.sub;
import static abfab3d.shapejs.VecUtils.dot;
import static abfab3d.shapejs.VecUtils.clamp;
import static abfab3d.shapejs.VecUtils.mulVV;
import static abfab3d.shapejs.VecUtils.minVV;
import static abfab3d.shapejs.VecUtils.maxVV;
import static abfab3d.shapejs.VecUtils.exp;
import static abfab3d.shapejs.VecUtils.vec4;
import static abfab3d.shapejs.VecUtils.vec3;
import static abfab3d.shapejs.VecUtils.isZero;
import static abfab3d.shapejs.VecUtils.getLayerTransmittance;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**
 *  Take (x,y) point on physical screen and return (r,g,b) color for that point
 *
 *  image is formed in relative bounds X in (-1,1) and Y in (-1,1)
 *  @author Alan Hudson
 *  @author Vladimir Bulatov
 */
public class SceneImageDataSource extends BaseParameterizable implements DataSource, Initializable {

    static boolean DEBUG = false;

    static final Vector3d UNIT3 = new Vector3d(1,1,1);

    static final int NO_INTERSECTION = 1,HAS_INTERSECTION = 2,INSIDE = 3;

    BooleanParameter mp_draftMode = new BooleanParameter("draftMode", true);
    SNodeParameter mp_scene = new SNodeParameter("scene");
    SNodeParameter mp_camera = new SNodeParameter("camera");
    IntParameter mp_shadowsQuality = new IntParameter("shadowsQuality", 0);
    IntParameter mp_raytracingDepth = new IntParameter("raytracingDepth", 0);
    IntParameter mp_maxIntersections = new IntParameter("maxIntersections", 1);
    DoubleParameter mp_surfaceJump = new DoubleParameter("surfaceJump", 0.005);

    DoubleParameter mp_volumeRendererLayerThickness = new DoubleParameter("volumeRendererLayerThickness", 0.1*MM);
    Parameter aparam[]= {

        mp_scene,
        mp_camera,
        mp_draftMode,
        mp_shadowsQuality,
        mp_raytracingDepth,
        mp_maxIntersections,
        mp_surfaceJump,
        mp_volumeRendererLayerThickness,
    };

    private Scene m_scene;
    private Camera m_camera;
    private DataSource m_root;
    private Bounds m_imageBounds;
    private Bounds m_sceneBounds;

    Vector3d m_eyeOrigin;

    Matrix4f m_viewMatrix;
    double m_cameraDepth = 4;
    double m_sceneScale;
    Vector3d m_sceneCenter;
    double m_gradientStep = 1.e-4;

    MaterialData m_materials[];
    LightData m_lights[];
    int m_shadowsQuality = 0;
    int m_raytracingDepth = 0;
    int m_maxIntersections = 1;
    double m_surfaceJump = 0.01;
    

    // size of playbox [-1,1;-1,1;-1,1]
    double m_boxSize = 2;

    double m_volumeRendererLayerThickness = 1*MM;

    static Vector4d BAD_COLOR = new Vector4d(00,1,1,1);


    public SceneImageDataSource(Scene scene, Camera camera) {

        super.addParams(aparam);
        mp_scene.setValue(scene);
        mp_camera.setValue(camera);

        m_cameraDepth = 1.0 / Math.tan(camera.getCameraAngle());
    }

    void setDebug(boolean value){
        this.DEBUG = value;
    }
    
    @Override
    public int initialize(){
        
        if(DEBUG)printf("%s.initialize()\n", this);
        m_scene = (Scene)mp_scene.getValue();
        m_camera = (Camera)mp_camera.getValue();
        m_root = (DataSource) m_scene.getRenderingSource(mp_draftMode.getValue());        
        m_shadowsQuality = mp_shadowsQuality.getValue();

        // default scene image bounds 
        m_imageBounds = new Bounds(-1.,1.,-1.,1.,-1.,1.);

        m_viewMatrix = new Matrix4f();
        m_camera.getViewMatrix(m_viewMatrix);
        m_eyeOrigin = getEyeOrigin();
       
        if(DEBUG) printf("view Matrix:\n%s",str("%7.3f",m_viewMatrix));        

        m_sceneBounds = m_scene.getBounds();
        m_sceneCenter = m_sceneBounds.getCenter();
        m_sceneScale = m_sceneBounds.getSizeMax()/m_boxSize;
        m_volumeRendererLayerThickness = mp_volumeRendererLayerThickness.getValue();
        
        m_materials = getMaterialsData(m_scene,m_volumeRendererLayerThickness);
        m_lights = getLightData(m_scene);
        m_raytracingDepth = mp_raytracingDepth.getValue();
        m_maxIntersections = mp_maxIntersections.getValue();
        m_surfaceJump = mp_surfaceJump.getValue();

        return ResultCodes.RESULT_OK;

    }

    /**
       return array of material data for the scene 
     */
    static MaterialData[] getMaterialsData(Scene scene, double layerThickness){

        List<Material> mats = scene.getMaterials().getMaterials();
        MaterialData materials[] = new MaterialData[mats.size()];        

        if(DEBUG) printf("materialsCount: %d\n",materials.length);
        for(int i = 0; i < materials.length; i++){
            materials[i] = new MaterialData((PhongParams)mats.get(i).getShader().getShaderParams(), layerThickness);
            if(DEBUG)printf("material[%d]:%s\n",i, materials[i].toString());        
        }
        return materials;
    }

    /**
       
     */
    static LightData[] getLightData(Scene scene){

        List<Light> lights = scene.getLights();
        
        LightData ld[] = new LightData[lights.size()];
        if(DEBUG)printf("lights count:%d\n", ld.length);
        
        for(int i = 0; i < ld.length; i++){
            ld[i] = new LightData(lights.get(i));
            if(DEBUG)printf("light[%d]: %s\n", i, ld[i].toString());
        }
        return ld;
    }


    /**
       return image pixel color at given point 
       @Override
    */
    public int getDataValue(Vec pnt, Vec dataValue) {


        Vector3d direction = getEyeDirection(pnt.v[0], pnt.v[1]);
        
        double intersection[] = new double[2];
        
        if(!intersectBox(m_eyeOrigin, direction, boxMin(), boxMax(), intersection)){
            // did not hit the box - return background 
            Vector4d c = getEnviroMapColor(direction);
            dataValue.v[0] = c.x;
            dataValue.v[1] = c.y;
            dataValue.v[2] = c.z;
            dataValue.v[3] = c.w;
            
        } else {

            String f = "%7.3f";            
            //if(DEBUG)printf("box hit: orig:%s dir:%s tnear:%7.3f tfar:%7.3f\n", str(f,m_eyeOrigin), str(f,direction), intersection[0],intersection[1]);
            TracingData td = new TracingData(intersection[0], intersection[1], m_eyeOrigin, direction);
            Vector4d color = raytracePixel(td, m_raytracingDepth);
            dataValue.v[0] = color.x;
            dataValue.v[1] = color.y;
            dataValue.v[2] = color.z;
            dataValue.v[3] = color.w;            
        }
        
        return ResultCodes.RESULT_OK;
    }

    
    /**
       calculates distance to surface in box coordinates 
     */
    double getDistance(double x, double y, double z, Vec data){
        
        Vec pnt = new Vec(x * m_sceneScale + m_sceneCenter.x, 
                          y * m_sceneScale + m_sceneCenter.y, 
                          z * m_sceneScale + m_sceneCenter.z
                          );
        m_root.getDataValue(pnt, data);
        
        // distance is scaled to box scale 
        return data.v[0]/m_sceneScale;

    }
    
    double getDistance(Vector3d p, Vec data){

        return getDistance(p.x, p.y, p.z, data);

    }
    

    static int debugCount = 100;
    
    /**
       return current material 
     */
    MaterialData getMaterial(Vec data){

        return m_materials[data.materialIndex];

    }
    
    
    /**
       
       return color of pixel generated by ray with given origina and direction 
       
     */
    Vector4d raytracePixel(TracingData td, int tracingDepth) {

        if(DEBUG) printf("raytracePixel(tnear:%7.3f, tfar:%7.3f, origin:%s,direction:%s,depth:%d\n",
                         td.tStart, td.tEnd, str("%5.3f", td.rayOrigin), str("%5.3f", td.rayDirection), tracingDepth);

        Vector3d pos_world = new Vector3d();
        Vector3d normal = new Vector3d();  // surface normal at the intersection
        Vector3d pos_box = new Vector3d(); // position in box units
        Vec data = new Vec(4);    // data value at the intersection point 
        
        int maxIntersections = m_maxIntersections;

        //
        // accumulated color (initally transparent) 
        //

        Vector3d color = new Vector3d(0,0,0); 
        Vector3d alpha = new Vector3d(0,0,0); 

        for(int i = 0; i < maxIntersections; i++){

            if(DEBUG)printf("step:%d tStart: %7.4f\n", i, td.tStart); 

            int res = getIntersection(td, pos_box,pos_world,normal,data);  

            if (res == INSIDE){
                if(DEBUG)printf("got INSIDE\n"); 
                // this is bad on first step only 
                if(i == 0) {
                    return m_intersectionColor; 
                } else {
                    //color = color + env_color * env_alpha*(1-alpha)
                    //alpha = alpha  + (1-alpha) * env_alpha
                    Vector4d ecolor = new Vector4d(0,0,0,0);
                    Vector3d alpha1 = sub(UNIT3, alpha);
                    addSet(color, mul(alpha1, mul(vec3(ecolor),ecolor.w)));
                    addSet(alpha, mul(alpha1, ecolor.w));                
                    break;
                    // this should not happen  
                    //return new Vector4d(0,0,0,1);
                }
            } else if(res == NO_INTERSECTION) { 

                if(DEBUG)printf("got NO_INTERSECTION\n"); 
                // nothing found on this step 
                Vector4d ecolor = getEnviroMapColor(td.rayDirection);  
                // compose current color over envmap color 
                Vector3d alpha1 = sub(UNIT3, alpha);
                //color = color + env_color * env_alpha*(1-alpha)
                //alpha = alpha  + (1-alpha) * env_alpha
                addSet(color, mul(alpha1, mul(vec3(ecolor),ecolor.w)));
                addSet(alpha, mul(alpha1, ecolor.w));                
                break;

            }
            
            if(DEBUG)printf("got INTERSECTION  t:%7.4f\n", td.tCurrent); 

            // got surface intersection 
            MaterialData mat = getMaterial(data);            
            Vector4d scolor = shadeSurface(pos_world, pos_box, normal, td.rayDirection, data, tracingDepth);             
            
            if(mat.isOpaque) {

                if(DEBUG)printf("isOpaque surface: color:[%s] alpha:[%s] scolor:[%s]\n", str("%5.2f", color), str("%5.2f", alpha), str("%5.2f", scolor)); 
                // compose accumulated color over opaque surface color
                // color += scolor*(1-alpha)
                // alpha = UNIT3;
                addSet(color, mul(vec3(scolor), sub(UNIT3, alpha)));
                alpha = new Vector3d(UNIT3);
                break;
                       
            } else {
                
                // compose accumulated color over the layer color 
                Vector3d alpha1 = sub(UNIT3,alpha);
                double surfaceAlpha = mat.surfaceAlpha; 
                addSet(color, mul(mul(vec3(scolor),surfaceAlpha),alpha1));
                addSet(alpha,mul(alpha1, surfaceAlpha));
                
                td.tStart = td.tCurrent; 
                renderVolume(td, color, alpha, data);
                if(DEBUG)printf("renderVolume returned: color:[%s] alpha:[%s]\n", str("%5.2f", color), str("%5.2f", alpha)); 
                
            }
            td.tStart = td.tCurrent; 
            td.tStart += m_surfaceJump; 
        }
        
        
        return new Vector4d(color.x, color.y, color.z, (alpha.x+alpha.y+alpha.z)/3);

        
    }

    
    /**
       starting from given point on the surface of shape does marching along the ray accumulating the color and transparency along the ray 
       until the ray leaves the interior of the shape 

       Color and Alpha are accumulated along the ray layer by layer using composition 

       each point alontg the ray may have its own color c_point and opacity a_point 

       color and alpha are composed using the following 

       color = c_pnt * a_point * (1-alpha) + color
       alpha = a_pnt * (1-alpha) + alpha 

       marching may terminate if opacity exceeds opacityThreshold (close to 1) which means that the total layer of material along the ray transmitts no light behind it. 

     */
    void renderVolume(TracingData td, Vector3d color, Vector3d alpha, Vec data){
        
        int iter = 1000;

        double dt = m_volumeRendererLayerThickness / m_sceneScale; // 0.001; // step in box units 
        //if(debugCount-- > 0)printf("dt:%8.5f\n", dt);
        double tStart = td.tStart;
        double tEnd = td.tEnd;

        double t0 = tStart, t1 = t0;
        
        Vector3d pos = new Vector3d();
        interpolate(td.rayOrigin, td.rayDirection, t0, pos);
        double dist0 = getDistance(pos, data);

                
        MaterialData material = getMaterial(data);
        // diffuse color of the layer 
        Vector3d c_pnt = vec3(material.diffuseColor);
        // alpha of the the layer 
        //Vector3d a_pnt = new Vector3d(0.005,0.005,0.005);
        //Vector3d a_pnt = new Vector3d(0.0025,0.0025,0.0025);
        //Vector3d a_pnt = new Vector3d(1,1,1);
        Vector3d a_pnt = material.getLayerAlpha();

        Vector3d unit = new Vector3d(1,1,1);

        int hit = 0;

        for(int i = 0; i < iter; i++) {
            
            t1 = t0 + dt;
            interpolate(td.rayOrigin, td.rayDirection, t1, pos);
            double dist1 = getDistance(pos, data);
            
            if(dist1 >= 0.) {// we are outside
                if( dist1 != dist0){
                    // linear interpolation of last steps 
                    // final adjustment to get point where distance is 0
                    double delta = dist0 / (dist0 - dist1);
                    t1 = t0 + dt * delta;
                    interpolate(td.rayOrigin, td.rayDirection, t1, pos);
                    // save last hit in the tracing data 
                    td.tCurrent = t1;
                    hit = 1;
                    break;                 
                }
            } else {
                Vector3d alpha1 = sub(UNIT3,alpha);
                // compose accumulated color over layer color 
                addSet(color, mul(c_pnt,a_pnt,alpha1));
                addSet(alpha,mul(a_pnt, alpha1));
                
            }
            
            if(t1 > tEnd) {
                break;
            }
            t0 = t1;
            dist0 = dist1;
        }
        
        //Vector4d c = mul(color, t1);//new Vector4d(1., 0.5, 0.8, 1);                 
        //Vector4d c = mul(color, alpha);

        //c.w = 1;

        return;
    }


    /**
       find intersection of ray with the surface 
       returns position and gradient at the found intersection 
       
    */
    int getIntersection(TracingData td, Vector3d boxPos,Vector3d scenePos, Vector3d normal, Vec data){

        //if(DEBUG)printf("getIntersection({tnear:%7.3f, tfar:%7.3f rayOrigin:%s)\n",tnear, tfar, str("%6.3f",rayOrigin),str("%6.3f",rayDirection));
        
        // march along ray from tnear till we hit something
        double t0 = td.tStart;
        double t1 = t0;
        Vector3d pos = new Vector3d();
        
        interpolate(td.rayOrigin, td.rayDirection, t0, pos);
        double dist0 = getDistance(pos, data);

        //if(DEBUG) printf("   pos:%s, dist0:%7.3f\n", str("%7.3f", pos), dist0);
        //if(DEBUG && debugCount > 0) printf("dist0: %7.3f\n", dist0);        
        
        if (dist0 < 0){  // we are inside the solid
            return INSIDE;
        }
        
        int hit = -1;

        //
        // TODO - make all these into param
        //
        double rayStep = 0.1;
        int iter = 500; // max count of iterations
        double minStep  = 1.e-3; // minimal step to do 
        double precision  = 1.e-4;//scene.precision;
        double factor = 0.9;//scene.factor;
        double normalFactor = 10; // maximal relative size of last adjustment
        
        for(int i = 0; i < iter; i++) {
            
            double dt = max(minStep, min(rayStep,abs(dist0*factor)));                
            t1 = t0 + dt;
            interpolate(td.rayOrigin, td.rayDirection, t1, pos);
            double dist1 = getDistance(pos, data);
            //if(DEBUG && debugCount > 0) printf("i:%d dist1: %7.3f\n", i, dist1); 
            if(dist1 < precision ) {// we are close
                if( dist1 != dist0){
                    // linear interpolation of last steps 
                    // final adjustment to get point where distance is 0
                    double delta = dist0 / (dist0 - dist1);
                    if(abs(delta) < normalFactor){
                        t1 = t0 + dt * delta;                    
                        interpolate(td.rayOrigin, td.rayDirection, t1, pos);
                        hit = 1;
                        break;
                    } 
                }
            }
            if(t1 > td.tEnd) {
                break;
            }
            t0 = t1;
            dist0 = dist1;
        }
        if(hit == -1) {
            return NO_INTERSECTION;
        }

        td.tCurrent = t1;
        
        double dt = m_gradientStep;
        // x
        double dx0 = getDistance(pos.x + dt, pos.y, pos.z, data);
        double dx1 = getDistance(pos.x - dt, pos.y, pos.z, data);
        // y
        double dy0 = getDistance(pos.x,pos.y + dt, pos.z, data);
        double dy1 = getDistance(pos.x,pos.y - dt, pos.z, data);
        // z
        double dz0 = getDistance(pos.x,pos.y, pos.z + dt, data);
        double  dz1 = getDistance(pos.x,pos.y, pos.z - dt, data);
        
        // second order precision formula for gradient, good for smooth gradients 
        normal.set((dx0-dx1),(dy0-dy1),(dz0-dz1));
        normal.normalize();
        
        boxPos.set(pos);
        
        // transform position into scene units
        boxToScene(pos, scenePos);
        

        return HAS_INTERSECTION;
              
    }


    /**
       convert box coordinates into scene coordinates 
     */
    void boxToScene(Vector3d boxCoord, Vector3d sceneCoord){

        sceneCoord.x = boxCoord.x * m_sceneScale + m_sceneCenter.x;
        sceneCoord.y = boxCoord.y * m_sceneScale + m_sceneCenter.y;
        sceneCoord.z = boxCoord.z * m_sceneScale + m_sceneCenter.z;
    }

    @Override
    public int getChannelsCount() {
        // rgba channels 
        return 4;
    }

    @Override
    public Bounds getBounds() {
        return m_imageBounds;
    }


    /**
      return eye origin in playbox units 
    */
    Vector3d getEyeOrigin(){
        // translational part of view
        return new Vector3d(m_viewMatrix.m03, m_viewMatrix.m13, m_viewMatrix.m23);
    }

    /**
       return view direction in playbox units
    */
    Vector3d getEyeDirection(double u, double v){
        //
        return normalize(mul(m_viewMatrix, new Vector3d(u,v,-m_cameraDepth)));
    }

    
    /**
       return pixel color generated by given surface point 
     */
    Vector4d shadeSurface(Vector3d posWorld, Vector3d posBox, Vector3d normal, Vector3d eyeRay, Vec data, int tracingDepth) {
    
        Vector3d mat_diffuse;
        Vector4d color = null;

        MaterialData material = getMaterial(data);

        switch(material.materialType){
        default:
        case MaterialType.SINGLE:
            color = material.diffuseColor;
            break;
        case MaterialType.COLOR:
            {
                //TODO color material             
            }
            break;
        case MaterialType.MIXED:
            {
                //TODO mixed material                             
            }
            break;
            
        }
        
        if(tracingDepth > 0 ){

            // combine contribution from reflected, transmitted rays and diffuse color 
            tracingDepth--;

            
            if(material.albedo.x != 0.0){


                normalize(normal);
                //if(DEBUG) printf("normal:%s posBox:%s\n", str("%5.3f",normal),str("%5.3f",posBox));
                Vector3d reflectedRay = reflect(eyeRay, normal);
                double intersection[] = new double[2];
                if(intersectBox(posBox, reflectedRay, boxMin(), boxMax(), intersection)){
                    double tnear = 0.001; 
                    double tfar = intersection[1];
                    //if(DEBUG) printf("eyeRay:%s reflectedRay:%s\n", str("%5.3f",eyeRay),str("%5.3f",reflectedRay));
                    TracingData td = new TracingData(tnear, tfar, posBox, reflectedRay);
                    Vector4d reflectedColor = raytracePixel(td, tracingDepth);             
                    return reflectedColor;
                }
            }
            
            //reflectedRay

            Vector4d surfaceColor = getPhongShading(material,color,posWorld,posBox,normal,eyeRay,data);

            return surfaceColor;
        }  else {
            return getPhongShading(material,color,posWorld,posBox,normal,eyeRay,data);
        }
        
    }

    /**
       
     */
    Vector4d getPhongShading(MaterialData material, Vector4d diffuseColor, Vector3d posWorld, Vector3d posBox, Vector3d normal, Vector3d eyeRayD, Vec data) {
            
        //if(DEBUG)printf("getPhongShading({posWorld:%s,posBox:%snormal:%s})\n", str("%6.3f",posWorld), str("%4.2f",posBox), str("%6.2f",normal));
        if(DEBUG)printf("getPhongShading({normal:%s})\n", str("%5.2f",normal));
                        
        Vector4d pixel_color = vec4(material.ambientIntensity,material.ambientIntensity,material.ambientIntensity,1.);
        
        int lightCount = m_lights.length;
        
        for(int i=0; i < lightCount; i++) {
            
            LightData light = m_lights[i];
            
            Vector3d light_pos;

            if (light.fixedPosition) {
                light_pos = light.position;
            } else {
                // TODO: Stop doing this every calc
                light_pos = mul(m_viewMatrix,light.position);
            }            
            // TODO area light 
            Vector4d light_color = light.color;
            
            Vector3d light_dir = normalize(sub(light_pos,posBox));
            double lit = dot(light_dir,normal);
            
            if (lit > 0.) {
                double shadow;                
                if (m_shadowsQuality == 0) {
                    shadow = 1;
                } else if (m_shadowsQuality <= 5) {
                    shadow = hardShadow(posBox,light_pos);
                } else {                 
                    shadow = softShadow(posBox,light_pos,light.angularSize);
                }
                if (shadow > 0.) {
                    //if(debugCount-- > 0 && shadow < 1.) printf("shadow: %7.3f\n", shadow);
                    addSet(pixel_color,mul(mul(light_color,diffuseColor),(shadow * lit * light.intensity)));

                    if (material.shininess > 0.) {
                        // direction to reflected light x
                        Vector3d ref_light = mul(normalize(reflect(light_dir, normal)),-1);
                        double s = pow(max(dot(ref_light, mul(eyeRayD,-1)), 0.0), material.shininess * 128);
                        addSet(pixel_color,  mul(mul(material.specularColor,light_color),(s * light.intensity)));
                    }
                    clamp(pixel_color, 0., 1.); 
                }
            }
        }

        if(DEBUG)printf("pixel_color: %s\n", str("%4.2f", pixel_color));
        return pixel_color;
        
    }
        
    boolean intersectBox(Vector3d origin, Vector3d dir, Vector3d boxMin, Vector3d boxMax, double inter[]){

        // compute intersection of ray with all six bbox planes
        Vector3d invR = new Vector3d(1./dir.x, 1./dir.y, 1./dir.z);
        Vector3d tbot = mulVV(sub(boxMin, origin), invR);
        Vector3d ttop = mulVV(sub(boxMax, origin), invR);

        // re-order intersections to find smallest and largest on each axis
        Vector3d tmin = minVV(ttop, tbot);
        Vector3d tmax = maxVV(ttop, tbot);
        
        // find the largest tmin and the smallest tmax
        double largest_tmin = max(max(tmin.x, tmin.y), tmin.z);
        double smallest_tmax = min(min(tmax.x, tmax.y), tmax.z);
        
        inter[0] = largest_tmin;
        inter[1] = smallest_tmax;
        
        return smallest_tmax > largest_tmin;
    
        
    }

    /**
       min of bounding box in playbox units 
     */
    Vector3d boxMin(){
        //TODO make real box 
        return new Vector3d(-1, -1, -1);
    }

    /**
       max of bounding box in playbox units 
     */
    Vector3d boxMax(){
        //TODO make real box 
        return new Vector3d(1, 1, 1);
    }

    Vector4d m_intersectionColor = new Vector4d(1,0,0,1);
    Vector4d m_backgroundColor = new Vector4d(1,1,1,1);

    Vector4d getEnviroMapColor(Vector3d direction){
        
        return m_backgroundColor;
    }



    /**
       Hard shadows is calculated by casting a ray from point on the surface toward light source
       if distance along the ray became negative - ray intersect the surface 
    */
    double hardShadow(Vector3d p0, Vector3d p1) {
        
        Vector3d pos = new Vector3d();
        Vector3d rayDir = new Vector3d(p1);
        Vec data = new Vec(4);
        rayDir.sub(p0);
        rayDir.normalize();
                
        double rayStep = 0.1;
        double precision = 3.e-4;
        int iter = 2000;
        double factor = 0.9;
        double tfar = 3; // max distance to travel (in playbox units)
        double tnear = 0.001; // move a little from the surface 
                
        double t0 = tnear;        
        
        interpolate(p0, rayDir, t0, pos);

        double dist0 = getDistance(pos, data);
        for(int i = 0; i < iter; i++) {

            double dt = min(rayStep,dist0*factor);
            double t1 = t0 + dt;
            if(t1 > tfar) {
                return 1.0;
            }
            interpolate(p0, rayDir, t1, pos);
            double dist1 = getDistance(pos, data);
            if(dist1 < 0 || abs(dist1) < precision) {
                // got intersection 
                return 0;
            }            
            t0 = t1;
            dist0 = dist1;
        }
        
        return 1.0;
    }
    
    
    /**
       simulates soft shadow using by blurring the shadow 
       minimal distance to the surface along the ray is used as blurring factor 
       p0 - point on the surface 
       p1 - directiuomn to the light source 
       lightWidth - visible angular size of the light in radians 
     */
    double softShadow(Vector3d p0, Vector3d p1, double lightWidth) {
        //printf("softShadow()\n");
        
        Vector3d pos = new Vector3d();
        Vector3d rayDir = new Vector3d(p1);
        Vec data = new Vec(4);
        rayDir.sub(p0);
        rayDir.normalize();
        
        double rayStep = 0.1;
        double minStep = 1.e-2;
        double precision = 0;// 3.e-4;
        int iter = 2000;
        double factor = 0.9;
        double tfar = 3; // max distance to travel (in playbox units)
        double tnear = 0.001; // move a little from the surface 
        //double distThreshold = 0.001; // threshold for distance to avoid self shading 
        double t0 = tnear;        
        
        interpolate(p0, rayDir, t0, pos);

        double minAngle = 10.;

        double dist0 = getDistance(pos, data);
        //if(debugCount-- > 0) printf("--- \n");
        for(int i = 0; i < iter; i++) {
            
            double dt = max(minStep, min(rayStep,dist0*factor));
            
            double t1 = t0 + dt;
            
            if(t1 > tfar) {
                break;
            }
            interpolate(p0, rayDir, t1, pos);
            double dist1 = getDistance(pos, data);
            // distance to the surface normalized to light source width 
            //double angle = (dist1/t1)/lightWidth+0.5;
            double angle = (dist1/t1);
            //printf("t1:%7.5f, dist1:%7.5f angle:%5.3f\n", t1, dist1, angle);
            //if(dist1 < 0 || abs(dist1) < precision) {
                // got intersection 
            //    printf("got intersection\n");
            //    return 0.;
            //}            
            if(angle < -lightWidth) {
                // got intersection 
                //printf("got angle intersection\n");
                return 0.;
            }            
            t0 = t1;
            dist0 = dist1;
            minAngle = min(minAngle, angle);
            //if(debugCount-- > 0) printf("dist: %7.3f\n",dist0);            
        }
        double res = step01(minAngle, 0, lightWidth);
        //printf("  res:%6.2f\n",res);

        return res;
    
        //return MathUtil.clamp(res,0,1);

    }



    //
    // internal material data used for rendering 
    //
    static class MaterialData {
        
        int materialType;

        Vector4d diffuseColor;
        Vector4d emissiveColor;
        Vector4d specularColor;
        Vector4d albedo;

        double shininess;
        double ambientIntensity;
        double roughness;
        double surfaceAlpha;
        Vector3d layerTransmittance;
        Vector3d layerAlpha;
        boolean isOpaque = true;

        MaterialData(PhongParams ppar, double layerThickness){
            
            this.materialType = ppar.getMaterialTypeIndex();
            this.diffuseColor = vec4(ppar.getDiffuseColor());
            this.emissiveColor = vec4(ppar.getEmissiveColor());
            this.specularColor = vec4(ppar.getSpecularColor());
            this.albedo = vec4(ppar.getAlbedo());
            this.roughness = ppar.getRoughness();
            this.shininess = ppar.getShininess();
            this.ambientIntensity = ppar.getAmbientIntensity();
            this.surfaceAlpha = (Double)ppar.getParam("surfaceAlpha").getValue();
            this.layerTransmittance = getLayerTransmittance((Vector3d)(ppar.getParam("transmittanceCoeff").getValue()),layerThickness);
            this.layerAlpha = sub(new Vector3d(1,1,1), this.layerTransmittance);

            this.isOpaque = isZero(this.layerTransmittance);

        }

        Vector3d getLayerAlpha(){

            return layerAlpha;

        }

        public String toString(){
            String f = "%4.2f";
            return fmt("MaterialData:{type:%d,diffuse:%s,emissive:%s,specular:%s,albedo:%s,shininess:%4.2f,ambient:%4.2f,roughness:%4.2f}\n",
                       materialType, str(f,diffuseColor),str(f,emissiveColor),str(f,specularColor),str(f,albedo),shininess,ambientIntensity,roughness);
        }

    } // static class MaterialData

    static class LightData {
        Vector3d position;
        Vector4d color;
        double intensity;
        double ambientIntensity;
        boolean castShadows;
        int samples;
        double radius;
        boolean fixedPosition; 
        double angularSize;

        LightData(Light light){
            position = light.getPosition();
            color = vec4(light.getColor());            
            intensity = light.getIntensity();
            ambientIntensity = light.getAmbientIntensity();
            castShadows = light.getCastShadows();
            samples = light.getSamples();
            radius = light.getRadius();
            fixedPosition = light.getFixedPosition();
            angularSize = light.getAngularSize();
        }

        public String toString(){
            return fmt("LightData({position:[%7.3f,%7.3f,%7.3f],color:[%4.2f,%4.2f,%4.2f,%4.2f]})",
                       position.x,position.y, position.z, color.x,color.y,color.z,color.w);
        }
        
    }// static class LightData

    
    static class TracingData {

        double tStart, tEnd, tCurrent = 0.;
        
        Vector3d rayOrigin;
        Vector3d rayDirection;

        TracingData(double tStart, double tEnd, Vector3d rayOrigin, Vector3d rayDirection){
            this.tStart = tStart;
            this.tEnd = tEnd;
            this.rayOrigin = rayOrigin;
            this.rayDirection = rayDirection;
            
        }

    } // static class TracingData

}
