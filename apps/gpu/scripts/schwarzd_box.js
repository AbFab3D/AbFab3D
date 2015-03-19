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

    var base = new Box(2*radius,2*radius,2*radius);
    var pattern = new VolumePatterns.SchwarzD();
	pattern.set("period", 35*MM);
	pattern.set("thickness", 1*MM);
	pattern.set("level", 0.);
	
    var intersect = new Intersection();
    intersect.set("blend",1*MM);
    intersect.add(base);
    intersect.add(pattern);

	var r = radius+1*MM;
	return new Shape(intersect,new Bounds(-r,r,-r,r,-r,r));

}
