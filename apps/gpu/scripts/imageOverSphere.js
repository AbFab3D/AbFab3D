function main(args) {
    var radius = 25 * MM;
	var bx = 20*MM;
	var by = 20*MM;
	var bz = 20*MM;
	var s = 20*MM;
	var radius = 15*MM;
	
	var vs = 0.1*MM;
    //var path = 	"scripts/pattern.png";
    var path = 	"scripts/shapeways227.png";
	var image = loadImage(path);
		
	by = bx * image.getHeight()/image.getWidth();
    var grid = createGrid(-s,s,-s,s,-s,s,vs);
    var imgBox = new ImageBitmap(image, bx, by, bz, vs);
	imgBox.getParam("rounding").setValue(0.5*MM);
	imgBox.getParam("center").setValue(new Vector3d(0,0,radius));

    var maker = new GridMaker();
	var shape = new Sphere(radius);
	var union = new Union(shape, imgBox);
	var eng = new Engraving(shape, imgBox);
	eng.getParam("depth").setValue(0.4*MM);
	eng.getParam("blend").setValue(0.2*MM);

    //maker.setSource(union);
    maker.setSource(eng);
    maker.makeGrid(grid);

    return grid;
}
