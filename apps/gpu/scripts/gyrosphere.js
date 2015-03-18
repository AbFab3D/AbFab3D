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

    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(25*MM, 2*MM);
    var intersect = new Intersection();
    intersect.setBlend(2*MM);
    intersect.add(sphere);
    intersect.add(gyroid);
	intersect.setBlend(2*MM);

	var r = 25*MM;
	return new Shape(intersect,new Bounds(-r,r,-r,r,-r,r));

}
