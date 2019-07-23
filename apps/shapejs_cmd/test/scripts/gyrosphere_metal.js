
var uiParams = [
    {
        name: "period",
        desc: "Period of the gyroid",
        label: "Period",
        type: "double",
        rangeMin: 1,
        rangeMax: 21,
        step: 1,
        defaultVal: 18
    },
    {
        name: "thickness",
        desc: "Thickness",
        type: "double",
        rangeMin: 1,
        rangeMax: 5,
        step: 0.5,
        defaultVal: 2
    }

];
function main(args) {
    var radius = 25 * MM;
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(args['period']*MM, args['thickness']*MM);
    var intersect = new Intersection();
    intersect.setBlend(2*MM);
    intersect.add(sphere);
    intersect.add(gyroid);

    var s = 25*MM;
    var mat = new FullColorMaterial();
    var bright = 5;
    var sparams = mat.getShaderParams();  // Platinum params
    sparams.setDiffuseColor(new Color(0,0,0));
    sparams.setSpecularColor(new Color(bright * 0.1016546, bright * 0.1016546, bright * 0.1016546));
//    sparams.setAlbedo(new Color(1,1,1));
    sparams.setAlbedo(new Color(0.8,0.8,0.8));
    sparams.setShininess(0.5);
    sparams.setAmbientIntensity(0);
    sparams.setRoughness(0.02);
    sparams.setGradientFactor(60);

    var shape = new Shape(intersect, mat);
    var scene =  Scene(shape,new Bounds(-s,s,-s,s,-s,s));

    var light_color = new Color(1,1,1);

    var intensity = 0.8;

    // Setup 2 point lighting to mimic Blender setup
    var rlight = new Light(new Vector3d(2,8,10),light_color,0,intensity);
    var llight = new Light(new Vector3d(-2,8,10),light_color,0,intensity * 0.3);

    rlight.setRadius(100*CM);
    llight.setRadius(25*CM);
    var lights = [rlight,llight];
    scene.setLights(lights);

    return scene;
}
