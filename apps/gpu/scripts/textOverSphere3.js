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
	var bx = 25*MM;
	var by = 10*MM;
	var bz = 10*MM;
	var s = 22*MM;

	var r = 13*MM;
	var vs = 0.1*MM;
		
    //var textBox = new Text("text text", "Courier", bx, by, bz, vs);
    var textBox = new Text("TEXT TEST", "Times New Roman", bx, by, bz, vs);
	
	textBox.getParam("rounding").setValue(0.*MM);
	textBox.getParam("center").setValue(new Vector3d(0,0,7*MM));
	textBox.getParam("blurWidth").setValue(0.05*MM);
	
    var maker = new GridMaker();
	var H = 3*MM;
	var h = 0.6*r;
	var h2 = Math.sqrt(r*r - h*h);
	var R = Math.sqrt(h*h + H*H);
	var shape = new Subtraction(new Sphere(r), new Sphere(0,0,(H+h2),R));
	shape.getParam("blend").setValue(0.5*MM);
	
	var eng = new Engraving(shape, textBox);
	eng.getParam("depth").setValue(0.5*MM);
	eng.getParam("blend").setValue(0.2*MM);

	var r = 13*MM;
	return new Shape(eng,new Bounds(-r,r,-r,r,-r,r));

}
