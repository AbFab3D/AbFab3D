var uiParams = [
    {
        name: "period",
        desc: "Period",
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
    },
    {
        name: "period2",
        desc: "Period2",
        type: "double",
        rangeMin: 0.1,
        rangeMax: 10,
        step: 1,
        defaultVal: 1
    },
    {
        name: "thickness2",
        desc: "Thickness2",
        type: "double",
        rangeMin: 0.1,
        rangeMax: 3,
        step: 0.1,
        defaultVal: 0.1
    }
];
function main(args) {
    var radius = 25 * MM;
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.SchwarzP(args['period']*MM, args['thickness']*MM);
    var intersect = new Intersection();
    intersect.setBlend(2*MM);
    intersect.add(sphere);
    intersect.add(gyroid);
    var s = 25*MM;

    var eng = new Embossing(intersect, new VolumePatterns.Gyroid(args['period2']*MM,args['thickness2']*MM));
//  var eng = new Embossing(intersect, new VolumePatterns.Gyroid(1*MM,0.1*MM));
    eng.set("minValue",-0.5*MM);
    eng.set("maxValue",0.5*MM);
    eng.set("blend",0.2*MM);

    //eng.getParam("depth").setValue(engraveDepth);
    return new Shape(eng,new Bounds(-s,s,-s,s,-s,s));


//	return new Shape(intersect,new Bounds(-s,s,-s,s,-s,s));
}

