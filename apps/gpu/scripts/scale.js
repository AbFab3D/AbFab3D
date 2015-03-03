function main(args) {
    var radius = 25 * MM;
	var bx = 30*MM;
	var by = 20*MM;
	var bz = 10*MM;
	    
    var box1 = new Box(bx/2,0,0, bx, by, bz);
    var box2 = new Box(-bx/2,0,0, bx, by, bz);
	var scale = new Scale(0.5, 1, 1);
	scale.getParam("center").setValue(new Vector3d(-bx/2,0,0));
	
	box2.setTransform(scale);
	
	var union = new Union();
	union.add(box1);
	union.add(box2);
	var s = 25*MM;
	return new Shape(union,new Bounds(-s,s,-s,s,-s,s));
}
