function main(args) {
	var r = 25*MM;
    var sphere = new Sphere(0, 0,0, -15*MM);
    var box = new Box(0,0,0, 50*MM, 40*MM, 10*MM);
    var inter = new Intersection(sphere, box);
	return new Shape(inter,new Bounds(-r,r,-r,r,-r,r));
}
