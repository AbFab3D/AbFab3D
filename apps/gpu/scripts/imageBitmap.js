function main(args) {
    var radius = 25 * MM;
	var bx = 30*MM;
	var by = 20*MM;
	var bz = 2*MM;
	var s = 20*MM;
	var vs = 0.1*MM;
    var path = 	"scripts/shapeways227.png";
	var image = loadImage(path);
	
	by = bx * image.getHeight()/image.getWidth();
    var grid = createGrid(-s,s,-s,s,-s,s,vs);
    var imgBox = new ImageBitmap(image, bx, by, bz, vs);
	imgBox.getParam("rounding").setValue(0.5*MM);
    var maker = new GridMaker();
    maker.setSource(imgBox);
    maker.makeGrid(grid);

    return grid;
}
