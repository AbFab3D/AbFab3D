var params = [
    {
        id: "thickness",
        desc: "Thickness",
        type: "range",
        "rangeMin": 0.005,
        "rangeMax": 0.05,
        "step": 0.001,
        defaultVal: 0.01
    }
];
function main(args) {
    var radius = 25 * MM;

    var base = new Sphere(radius);
    var pattern = new VolumePatterns.Lidinoid();
	pattern.set("period", 20*MM);
	pattern.set("thickness", 2*MM);
	pattern.set("level", 0.5);
	
    var intersect = new Intersection();
    intersect.set("blend",1*MM);
    intersect.add(base);
    intersect.add(pattern);

	var r = radius+1*MM;
	return new Shape(intersect,new Bounds(-r,r,-r,r,-r,r));

}
