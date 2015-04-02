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
	var s = 0.5*radius;
    var pattern = new Noise(new Vector3d(s,s,s),2,2,2);
	pattern.set("factor", 0.001);
	pattern.set("offset", 0.);	
    var shape = new Embossing(base, pattern);
	shape.set("minValue", -10*MM);
	shape.set("maxValue", 10*MM);
	shape.set("factor", 1);
	shape.set("blend", 0.1*MM);
	
	var r = radius+1*MM;
	return new Shape(shape,new Bounds(-r,r,-r,r,-r,r));

}
