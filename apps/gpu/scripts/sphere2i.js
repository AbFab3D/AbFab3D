function main(args) {

    var diskRadius = 24 * MM;
	var r = 25*MM;
	var sc1 = diskRadius;
	var sc2 = 2*diskRadius;
    var sphere1 = new Sphere(-sc1, 0,0, Math.sqrt(sc1*sc1 + diskRadius*diskRadius));
    var sphere2 = new Sphere(sc2, 0,0, Math.sqrt(sc2*sc2 + diskRadius*diskRadius));
    var inter = new Intersection();
    inter.add(sphere1);
    inter.add(sphere2);
	return new Shape(inter,new Bounds(-r,r,-r,r,-r,r));
}
