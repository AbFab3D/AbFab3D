function main(args) {
    var radius = 15 * MM;
	var r = 25*MM;
    var sphere1 = new Sphere(-10*MM, 0,0, radius);
    var sphere2 = new Sphere(10*MM, 0,0, radius);
    var union = new Union();
    union.add(sphere1);
    union.add(sphere2);
	return new Shape(union,new Bounds(-r,r,-r,r,-r,r));
}
