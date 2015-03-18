var uiParams = [
	{
		name: "text",
		desc: "Text",
		type: "string"
	},
	{
		name: "textpos0",
		desc: "Text Pos1",
		type: "location"
	},
	{
		name: "textpos1",
		desc: "Text Pos2",
		type: "location"
	},
	{
		name: "image",
		desc: "Image",
		type: "uri"
	},
	{
		name: "imagepos0",
		desc: "Image Pos1",
		type: "location"
	},
	{
		name: "imagepos1",
		desc: "Image Pos2",
		type: "location"
	},
	{
		"name": "engraveDepth",
		"desc": "Engrave Depth",
		"type": "double",
		"rangeMin": -1,
		"rangeMax": 1,
		"step": 0.1,
		"defaultVal": 0.5
	}
];

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
	var textpos0 = args.textpos0;
	var textpos1 = args.textpos1;
	var text = args.text;
	var textBox = null;
	var imagePath = args.image;
	var imagepos0 = args.imagepos0;
	var imagepos1 = args.imagepos1;
	var imgBox = null;
	var engraveDepth = args.engraveDepth * MM;


	if (text != null && textpos0 && textpos1) {
		var tp0 = textpos0.point;
		var tn0 = textpos0.normal;
		var tp1 = textpos1.point;
		var tn1 = textpos1.normal;

		tn0.normalize();
		tn1.normalize();

		var tp01 = new Vector3d();
		tp01.sub(tp1,tp0);

		var tbx = tp01.length();
		var tby = 5*MM;
		var tbz = 10*MM;
		var tvs = 0.1*MM;

		textBox = new Text(text, "Times New Roman", tbx, tby, tbz, tvs);

		textBox.getParam("rounding").setValue(0.*MM);
		textBox.getParam("blurWidth").setValue(0.1*MM);
		textBox.setTransform(getTextTransform(tp0,tn0,tp1,tn1));
	}
	if (imagePath && imagepos0 && imagepos1) {
		var image = loadImage(imagePath);

		var p0 = imagepos0.point;
		var n0 = imagepos0.normal;
		var p1 = imagepos1.point;
		var n1 = imagepos1.normal;
		n0.normalize();
		n1.normalize();

		var p01 = new Vector3d();
		p01.sub(p1,p0);

		var bx = p01.length();
		var by = bx * image.getHeight()/image.getWidth();
		var bz = 20*MM;
		var vs = 0.1*MM;

		imgBox = new ImageBitmap(image, bx, by, bz, vs);
		imgBox.setBlurWidth(0.1*MM);
		imgBox.getParam("rounding").setValue(0.0*MM);
		imgBox.setTransform(getTextTransform(p0,n0,p1,n1));
	}

	var radius = 7*MM;
	var num = 3;

	r = radius * num * 0.7;
	var shape = new Union(spheres(radius,3), new Box(0,-r * 0.9,0,r,r,r));

	var bump = null;
	if (textBox !== null){
		if(imgBox === null) {
			bump = textBox;
		} else {
			bump = new Intersection(imgBox, textBox);
		}
	} else { // textBox == null
		if(imgBox !== null) {
			bump = imgBox;
		}
	}

	if(bump === null){
		return new Shape(shape,new Bounds(-r,r,-r,r,-r,r));
	} else {
		var eng = new Engraving(shape, bump);
		eng.getParam("depth").setValue(engraveDepth);
		eng.getParam("blend").setValue(0.2*MM);
		return new Shape(eng,new Bounds(-r,r,-r,r,-r,r));
	}
}
