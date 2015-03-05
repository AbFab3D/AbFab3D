function spheres(radius, count){

	var union = new Union();
	var blend = union.getParam("blend").setValue(0.5*MM);
	var x0 = -radius;
	var dx = 2*radius/(count-1);
	for (i = 0; i < count; i++) {
		union.add(new Sphere(x0 + dx*i, 0, 0, radius));
	}
	return union;
}

function main(args) {
    var radius = 25 * MM;
	var bx = 20*MM;
	var by = 10*MM;
	var bz = 20*MM;
	var s = 22*MM;

	var radius = 12*MM;
	var r = 5*MM;
	var vs = 0.1*MM;
	var fontName = "Helvetica Rounded LT Std";
    var text1 = new Text("Shape", fontName, bx, by, bz, vs/2);	
    //var text1 = new Text("TEXT1", "Times New Roman", bx, by, bz, vs);	
	text1.getParam("rounding").setValue(0.*MM);
	text1.getParam("center").setValue(new Vector3d(0,by/4,radius));
	text1.getParam("blurWidth").setValue(0.1*MM);

    var text2 = new Text("SHAPE2", fontName, bx, by, bz, vs);	
	text2.getParam("rounding").setValue(0.*MM);
	text2.getParam("center").setValue(new Vector3d(0,-by/4,radius));
	text2.getParam("blurWidth").setValue(0.1*MM);
	text2.setTransform(new Rotation(0,0,1,Math.PI/2));
	
	
	var texts = new Intersection(text1, text2);
	texts.getParam("blend").setValue(0.);
	var shape = new Sphere(radius);
	
	var eng = new Engraving(shape, texts);
	
	eng.getParam("depth").setValue(0.5*MM);
	eng.getParam("blend").setValue(0.2*MM);

	var r = 13*MM;
	return new Shape(eng,new Bounds(-r,r,-r,r,-r,r));

}
