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
    var gyroid = new VolumePatterns.Gyroid();
	gyroid.set("period", 25*MM);
	gyroid.set("thickness", 2*MM);
	gyroid.set("center", new Vector3d(0*MM,0*MM,0*MM));
	gyroid.set("level", 0);
	
    var intersect = new Intersection();
    intersect.set("blend",1*MM);
    intersect.add(sphere);
    intersect.add(gyroid);

	var r = radius+1*MM;
	return new Shape(intersect,new Bounds(-r,r,-r,r,-r,r));

}
