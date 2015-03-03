function main(args) {
    var radius = 25 * MM;

	var bx = 49*MM;
	var by = 30*MM;
	var bz = 10*MM;
    var box1 = new Box(0,0,0, bx, by, bz);
    var box2 = new Box(0,0,0, by, bz, bx);
    var box3 = new Box(0,0,0, bz, bx, by);
	var union = new Union();
	union.add(box1);
	union.add(box2);
	union.add(box3);
	var s = 25*MM;
	return new Shape(union,new Bounds(-s,s,-s,s,-s,s));
}
