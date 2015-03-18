var uiParams = [
    {
        id: "model",
        desc: "Model",
        type: "uri",
        "required": true
    },
    {
        id: "image",
        desc: "Image",
        type: "uri",
        "required": false
    },
    {
        id: "textpos0",
        desc: "Image left",
        type: "location",
        "required": false
    },
    {
        id: "textpos1",
        desc: "Image right",
        type: "location",
        "required": false
    },
	{
		id: "engraveDepth",
		desc: "Engrave Depth",
		type: "double",
		"rangeMin": -2,
		"rangeMax": 0.5,
		"step": 0.1,
		defaultVal: -1
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
	var maxDist = 1*MM;
	var svr = 255;

	var modelPath = args['model'];
	var imagePath = args['image'];
	var textpos0 = args['textpos0'];
	var textpos1 = args['textpos1'];
	var engraveDepth = args.engraveDepth * MM;

	var shape = null;
	var bounds = null;
	var imgBox = null;

	if (modelPath) {
		var distDataKey = "distData:" + modelPath;
		var boundsKey = "gridBounds:" + modelPath;
		var distData = getCachedData(distDataKey);
		var bounds = getCachedData(boundsKey);

		if (distData == null) {
			var modelGrid = load(modelPath, vs,2*MM);
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

	if (imagePath && textpos0 && textpos1) {
	  var image = loadImage(imagePath);
	  var pos0Str = textpos0.split(",");
	  var pos1Str = textpos1.split(",");

	  var pos0 = [parseFloat(pos0Str[0]),parseFloat(pos0Str[1]),parseFloat(pos0Str[2])];
	  var normal0 = [parseFloat(pos0Str[3]),parseFloat(pos0Str[4]),parseFloat(pos0Str[5])];
	  var pos1 = [parseFloat(pos1Str[0]),parseFloat(pos1Str[1]),parseFloat(pos1Str[2])];
	  var normal1 = [parseFloat(pos1Str[3]),parseFloat(pos1Str[4]),parseFloat(pos1Str[5])];

	  var p0 = new Vector3d(pos0[0],pos0[1],pos0[2]);
	  var p1 = new Vector3d(pos1[0],pos1[1],pos1[2]);
	  var n0 = new Vector3d(normal0[0],normal0[1],normal0[2]);
	  var n1 = new Vector3d(normal1[0],normal1[1],normal1[2]);
	  n0.normalize();
	  n1.normalize();

	  var ttrans = getTextTransform(p0,n0,p1,n1);
	  var p01 = new Vector3d();
	  p01.sub(p1,p0);

	  var bx = p01.length();
	  var by = bx * image.getHeight()/image.getWidth();
	  var bz = 50*MM;
	  var vs = 0.1*MM;

	  imgBox = new ImageBitmap(image, bx, by, bz, vs);
	  imgBox.setBlurWidth(0.1*MM);
	  imgBox.getParam("rounding").setValue(0.*MM);
	  imgBox.setTransform(getTextTransform(p0,n0,p1,n1));
	}

	if (shape != null) {
	  if (imgBox !== null) {
	    var union = new Union(shape, imgBox);
	    var eng = new Engraving(shape, imgBox);
	    eng.getParam("depth").setValue(engraveDepth);
	    eng.getParam("blend").setValue(0.2*MM);
	    return new Shape(eng, bounds);
	  } else {
	    return new Shape(shape, bounds);
	  }
	}

}
