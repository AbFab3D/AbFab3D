
function main(args) {
    
	var s = 20*MM;
	var radius = 19*MM;
	//image map size 
	var bz = radius;
	var bx = radius;
	var by = radius;
	
	var vs = 0.1*MM;
    //var path = 	"scripts/shapeways227.png";
	//var path = 	"images/shapeways755.png";
    var path = 	"images/r5-bird.png";
	var image = loadImage(path);
		
	by = bx * image.getHeight()/image.getWidth();
    var imageMap = new ImageMap(image, bx, by, bz);
	imageMap.set("center", new Vector3d(0,0,radius));
	imageMap.set("blackDisplacement",-0.5*MM);
	imageMap.set("whiteDisplacement",0.);
	imageMap.set("blurWidth",0.1*MM);
	var s1 = new Sphere(0,0,0,radius);
	
	var eng = new Embossing(s1, imageMap);

	eng.set("minValue",-0.1*MM);
	eng.set("maxValue",0.*MM);
	eng.set("blend",0.5*MM);
	eng.set("factor",-5);
	eng.set("offset",-1*MM);
	

	return new Shape(eng,new Bounds(-s,s,-s,s,-s,s));
}
