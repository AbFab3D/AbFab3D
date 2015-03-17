var uiParams = [
	{
		"id": "text",
		"displayName": "Text",
		"type": "text",
		"default" : "Alan"
	},
	{
		"id": "text_loc",
		"displayName": "Text Location",
		"type": "location",
		"related": "text"
	}

];
function main(args) {
    
	//image map size 
	var bz = 20*MM;
	var s = 20*MM;
	var radius = 19*MM;
	var bx = 2*radius;
	var by = 2*radius;
	
	var vs = 0.1*MM;
    //var path = 	"scripts/shapeways227.png";
	//var path = 	"images/shapeways755.png";
    var path = 	"images/r5-bird.png";
	var image = loadImage(path);
		
	by = bx * image.getHeight()/image.getWidth();
    var imageMap = new ImageMap(image, bx, by, bz);
	imageMap.getParam("center").setValue(new Vector3d(0,0,radius));
	imageMap.getParam("blackDisplacement").setValue(-0.5*MM);
	imageMap.getParam("whiteDisplacement").setValue(0.);
	imageMap.getParam("blurWidth").setValue(0.1*MM);
	var s1 = new Sphere(0,0,0,radius);
	
	var eng = new Engraving(s1, imageMap);

	eng.getParam("depth").setValue(-0.4*MM);
	eng.getParam("blend").setValue(0.2*MM);

	return new Shape(eng,new Bounds(-s,s,-s,s,-s,s));
}
