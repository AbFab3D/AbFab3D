var uiParams = [
	{
		"id": "model",
		"displayName": "Model",
		"type": "url",
		"required": true
	},
	{
		"id": "image1",
		"displayName": "Image",
		"type": "url",
		"required": false
	},
	{
		"id": "imagepos1_0",
		"displayName": "Image left",
		"type": "location",
		"default": "",
		"required": false
	},
	{
		"id": "imagepos1_1",
		"displayName": "Image right",
		"type": "location",
		"default": "",
		"required": false
	},
	{
		"id": "image2",
		"displayName": "Image2",
		"type": "url",
		"required": false
	},
	{
		"id": "imagepos2_0",
		"displayName": "Image left",
		"type": "location",
		"default": "",
		"required": false
	},
	{
		"id": "imagepos2_1",
		"displayName": "Image right",
		"type": "location",
		"default": "",
		"required": false
	},
	{
		"id": "engraveDepth",
		"displayName": "Engrave Depth",
		"type": "range",
		"rangeMin": -2,
		"rangeMax": 0.5,
		"step": 0.1,
		"default": -1
	}

];

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
	var vs = 0.2*MM;
	var maxDist = 2.1*MM;
	var svr = 255;

	var modelPath = args['model'];
	var imagePath1 = args['image1'];
	var imagepos1_0 = args['imagepos1_0'];
	var imagepos1_1 = args['imagepos1_1'];
	var imagePath2 = args['image2'];
	var imagepos2_0 = args['imagepos2_0'];
	var imagepos2_1 = args['imagepos2_1'];
	var engraveDepth = args.engraveDepth * MM;

	var shape = null;
	var bounds = null;
	var imgBox1 = null;
	var imgBox2 = null;

	if (modelPath) {
		var distDataKey = "distData:" + modelPath;
		var boundsKey = "gridBounds:" + modelPath;
		var distData = getCachedData(distDataKey);
		var bounds = getCachedData(boundsKey);

		if (distData == null) {
			var modelGrid = load(modelPath, vs,3*MM);
			bounds = modelGrid.getGridBounds();

			var dt = new DistanceTransformLayered(svr, maxDist, maxDist);
			var distGrid = dt.execute(modelGrid);
			distData = new DataSourceGrid(distGrid);
			// a hack to get real distance from the distance grid
			var maxDistSVR = svr * (maxDist / vs);
			distData.setMapper(new LinearMapper(-maxDistSVR, maxDistSVR, -maxDist, maxDist));
			saveCachedData(distDataKey, distData);
			saveCachedData(boundsKey, bounds);

			bounds = modelGrid.getGridBounds();
			var cx = bounds.getCenterX();
			var cy = bounds.getCenterY();
			var cz = bounds.getCenterZ();
		}

		shape = distData;
	}

	if (imagePath1 && imagepos1_0 && imagepos1_1) {
		var image1 = loadImage(imagePath1);
		var pos1_0Str = imagepos1_0.split(",");
		var pos1_1Str = imagepos1_1.split(",");

		var pos1_0 = [parseFloat(pos1_0Str[0]),parseFloat(pos1_0Str[1]),parseFloat(pos1_0Str[2])];
		var normal1_0 = [parseFloat(pos1_0Str[3]),parseFloat(pos1_0Str[4]),parseFloat(pos1_0Str[5])];
		var pos1_1 = [parseFloat(pos1_1Str[0]),parseFloat(pos1_1Str[1]),parseFloat(pos1_1Str[2])];
		var normal1_1 = [parseFloat(pos1_1Str[3]),parseFloat(pos1_1Str[4]),parseFloat(pos1_1Str[5])];

		var p1_0 = new Vector3d(pos1_0[0],pos1_0[1],pos1_0[2]);
		var p1_1 = new Vector3d(pos1_1[0],pos1_1[1],pos1_1[2]);
		var n1_0 = new Vector3d(normal1_0[0],normal1_0[1],normal1_0[2]);
		var n1_1 = new Vector3d(normal1_1[0],normal1_1[1],normal1_1[2]);
		n1_0.normalize();
		n1_1.normalize();

		var p1_01 = new Vector3d();
		p1_01.sub(p1_1,p1_0);

		var bx1 = p1_01.length();
		var by1 = bx1 * image1.getHeight()/image1.getWidth();
		var bz1 = 50*MM;
		var vs1 = 0.1*MM;

		imgBox1 = new ImageBitmap(image1, bx1, by1, bz1, vs1);
		imgBox1.setBlurWidth(0.1*MM);
		imgBox1.getParam("rounding").setValue(0.0*MM);
		imgBox1.setTransform(getTextTransform(p1_0,n1_0,p1_1,n1_1));
	}

	if (imagePath2 && imagepos2_0 && imagepos2_1) {
		var image2 = loadImage(imagePath2);
		var pos2_0Str = imagepos2_0.split(",");
		var pos2_1Str = imagepos2_1.split(",");

		var pos2_0 = [parseFloat(pos2_0Str[0]),parseFloat(pos2_0Str[1]),parseFloat(pos2_0Str[2])];
		var normal2_0 = [parseFloat(pos2_0Str[3]),parseFloat(pos2_0Str[4]),parseFloat(pos2_0Str[5])];
		var pos2_1 = [parseFloat(pos2_1Str[0]),parseFloat(pos2_1Str[1]),parseFloat(pos2_1Str[2])];
		var normal2_1 = [parseFloat(pos2_1Str[3]),parseFloat(pos2_1Str[4]),parseFloat(pos2_1Str[5])];

		var p2_0 = new Vector3d(pos2_0[0],pos2_0[1],pos2_0[2]);
		var p2_1 = new Vector3d(pos2_1[0],pos2_1[1],pos2_1[2]);
		var n2_0 = new Vector3d(normal2_0[0],normal2_0[1],normal2_0[2]);
		var n2_1 = new Vector3d(normal2_1[0],normal2_1[1],normal2_1[2]);
		n2_0.normalize();
		n2_1.normalize();

		var p2_01 = new Vector3d();
		p2_01.sub(p2_1,p2_0);

		var bx2 = p2_01.length();
		var by2 = bx2 * image2.getHeight()/image2.getWidth();
		var bz2 = 50*MM;
		var vs2 = 0.1*MM;

		imgBox2 = new ImageBitmap(image2, bx2, by2, bz2, vs2);
		imgBox2.setBlurWidth(0.1*MM);
		imgBox2.getParam("rounding").setValue(0.0*MM);
		imgBox2.setTransform(getTextTransform(p2_0,n2_0,p2_1,n2_1));
	}

	if (shape != null) {
		if (imgBox1 !== null && imgBox2 !== null) {
			var eng = new Engraving(shape, new Intersection(imgBox1, imgBox2));
			eng.getParam("depth").setValue(engraveDepth);
			eng.getParam("blend").setValue(0.2 * MM);
			return new Shape(eng, bounds);
		} else if (imgBox1 !== null) {
			var union = new Union(shape, imgBox1);
			var eng = new Engraving(shape, imgBox1);
			eng.getParam("depth").setValue(engraveDepth);
			eng.getParam("blend").setValue(0.2*MM);
			return new Shape(eng, bounds);
		} else {
			return new Shape(shape, bounds);
		}
	}

}
