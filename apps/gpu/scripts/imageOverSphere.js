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
	var by = 20*MM;
	var bz = 20*MM;
	var s = 22*MM;
	var radius = 15*MM;
	var r = 5*MM;
	var vs = 0.1*MM;
    //var path = 	"scripts/pattern.png";
    //var path = 	"scripts/shapeways227.png";
    var path = 	"scripts/shapeways755.png";
	var image = loadImage(path);
		
	by = bx * image.getHeight()/image.getWidth();
    var grid = createGrid(-s,s,-s,s,-s,s,vs);
    var imgBox = new ImageBitmap(image, bx, by, bz, vs);
	imgBox.setBlurWidth(0.1*MM);
	imgBox.getParam("rounding").setValue(0.*MM);
	imgBox.getParam("center").setValue(new Vector3d(0,0,radius));

    var maker = new GridMaker();
	//var shape = new Sphere(radius);
	//var shape = new Torus(radius-r, r);	shape.setTransform(new Rotation(1,0,0,Math.PI/2));
	//var shape = new Box(2*radius,2*radius,2*radius); shape.setTransform(new Rotation(0,1,0,Math.PI/6));
	var shape = spheres(7*MM,3);
	
	var union = new Union(shape, imgBox);
	var eng = new Engraving(shape, imgBox);
	eng.getParam("depth").setValue(0.5*MM);
	eng.getParam("blend").setValue(0.2*MM);

    //maker.setSource(union);
    maker.setSource(eng);
    maker.makeGrid(grid);

    return grid;
}
