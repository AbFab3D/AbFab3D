var uiParams = [
    {
        name: "radius",
        desc: "Radius of the sphere",
        label: "Radius",
        type: "double",
        rangeMin: 1,
        step: 1,
        defaultVal: 25,
        unit: "MM"
    },
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
    var radius = args.radius;
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(args['period']*MM, args['thickness']*MM);
    var intersect = new Intersection();
    intersect.setBlend(2*MM);
    intersect.add(sphere);
    intersect.add(gyroid);

    var s = radius + 1*MM;
    return new Scene(intersect,new Bounds(-s,s,-s,s,-s,s));
}
