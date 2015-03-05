function getRotation(from, to){

	var a = new Vector3d();
	a.cross(from, to);
	var sina = a.length();
	   
	var cosa = to.dot(from);
	var angle = Math.atan2(sina, cosa);

	if(Math.abs(sina) < 1.e-8)
	   return new AxisAngle4d(1,0,0,0);

	a.normalize();
	return new AxisAngle4d(a.x,a.y,a.z,angle);
}
// calculates transformation of text to be placed between point p0 and p1 wih normals n0 and n1
function getTextTransform(p0, n0, p1, n1){

	// calculation of text transform
	var nn = new Vector3d();
	nn.add(n0,n1);
	nn.normalize();
	
	var center = new Vector3d();
	center.add(p0, p1);
	center.scale(0.5);
	var xdir = new Vector3d();
	xdir.sub(p1,p0);
	var len = xdir.length();
	xdir.normalize();
	
	var xaxis = new Vector3d(1,0,0);
	
	var aa1 = getRotation(xaxis, xdir);
	
	var m1 = new Matrix3d();
	m1.set(aa1);
	var zaxis = new Vector3d(0,0,1);
	m1.transform(zaxis); // zaxis after first rotation
	
	var aa2 = getRotation(zaxis, nn);
	var trans = new CompositeTransform();
	trans.add(new Rotation(aa1.x,aa1.y,aa1.z, aa1.angle));
	trans.add(new Rotation(aa2.x,aa2.y,aa2.z, aa2.angle));
	trans.add(new Translation(center));
	return trans;
}


function main(args) {

    var radius = 25 * MM;
	
	var n0 = new Vector3d(-0.7,0.2,1);
	var n1 = new Vector3d(0.7, 0.5,1);
	n0.normalize();
	n1.normalize();
	var p0 = new Vector3d(n0);
	var p1 = new Vector3d(n1);
	p0.scale(radius);
	p1.scale(radius);
	
	var ttrans = getTextTransform(p0,n0,p1,n1);
	var p01 = new Vector3d();
	p01.sub(p1,p0);
	
	var bx = p01.length();
	var by = 5*MM;
	var bz = 10*MM;
	var s = 22*MM;

	var vs = 0.1*MM;		
    var textBox = new Text("TEXT text", "Times New Roman", bx, by, bz, vs);
	
	textBox.getParam("rounding").setValue(0.*MM);
	textBox.getParam("blurWidth").setValue(0.1*MM);
	textBox.setTransform(getTextTransform(p0,n0,p1,n1));

	
	var shape = new Union(new Sphere(radius));
	shape.add(new Sphere(p0,1*MM));
	shape.add(new Sphere(p1,1*MM));
	
	var eng = new Engraving(shape, textBox);
	eng.getParam("depth").setValue(0.5*MM);
	eng.getParam("blend").setValue(0.2*MM);

	var r = radius+1*MM;
	return new Shape(eng,new Bounds(-r,r,-r,r,-r,r));

}
