
function main(args) {

	var bx = 39*MM;
	var by = 20*MM;
	var bz = 10*MM;
	var s = 20*MM;
	var vs = 0.1*MM;
    //var path = 	"images/woodInterlockingTriangles_piece.png";
    //var path = 	"images/r4-unicorn.png";
    //var path = 	"images/r5-bird.png";
    //var path = 	"images/unicursal.png";
    var path = 	"images/sw_logo.png";
	
	var image = loadImage(path);
	if(image.getHeight() <= image.getWidth()){
		by = bx * image.getHeight()/image.getWidth();
	} else {		
		bx = by * image.getWidth()/image.getHeight();
	}
    var imgBox = new ImageBox(image, bx, by, bz, vs);
	imgBox.getParam("blurWidth").setValue(0.5*MM);
	imgBox.getParam("baseThreshold").setValue(0.1);
	imgBox.getParam("rounding").setValue(0.1*MM);
	imgBox.getParam("baseThickness").setValue(0.9);
	imgBox.getParam("tilesX").setValue(10);
	imgBox.getParam("tilesY").setValue(10);
	imgBox.getParam("imagePlace").setValue(2);
	
	return new Shape(imgBox,new Bounds(-s,s,-s,s,-s,s));

}
