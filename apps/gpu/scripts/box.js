function main(args) {
    var radius = 25 * MM;
	var bx = 30*MM;
	var by = 20*MM;
	var bz = 10*MM;
	var s  =10*MM;
    var box = new Box(bx/2,0,0, bx, by, bz);

	return new Shape(box, new Bounds(0, bx, -s,s,-s,s));
	
}
