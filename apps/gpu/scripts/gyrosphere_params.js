var uiParams = [
    {
        id: "period",
        desc: "Period",
        type: "range",
        "rangeMin": 1,
        "rangeMax": 21,
        "step": 1,
        defaultVal: 18
    },
    {
        id: "thickness",
        desc: "Thickness",
        type: "range",
        "rangeMin": 1,
        "rangeMax": 5,
        "step": 0.5,
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
	return new Shape(intersect,new Bounds(-s,s,-s,s,-s,s));
}
