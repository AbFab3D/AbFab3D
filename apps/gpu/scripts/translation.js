function main(args) {
    var radius = 15 * MM;
	var s = 2*radius;

    var box = new Box(2*radius,2*radius,2*radius);
	box.setTransform(new Translation(radius,radius,radius));
	
	return new Shape(box,new Bounds(-s,s,-s,s,-s,s));

}
