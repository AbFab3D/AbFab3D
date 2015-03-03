function main(args) {
	
    var radius = 25 * MM;
	var s = 25*MM;
	var bx = 30*MM;
	var by = 20*MM;
	var bz = 10*MM;
	
    var box = new Box(0,0,0, bx, by, bz);
	var rot = new Rotation(0,0,1,Math.PI/4);
	rot.getParam("center").setValue(new Vector3d(-bx/2,-by/2,0));
	box.setTransform(rot);
	
	return new Shape(box, new Bounds(-s, s, -s, s, -s, s));
	
}
