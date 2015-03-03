function main(args) {
    var radius = 25 * MM;
    var sphere = new Sphere(radius);
	var r = 25*MM;
	return new Shape(sphere,new Bounds(-r,r,-r,r,-r,r));
}
