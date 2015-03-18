
function main(args) {
    
	var s = 20*MM;
	var radius = 19*MM;
	//image map size 
	var bz = radius;
	var bx = radius;
	var by = radius;
	
	var vs = 0.1*MM;
		
    var pattern = new VolumePatterns.Gyroid(5*MM, 0.5*MM);

	var s1 = new Sphere(0,0,0,radius);
	
	var eng = new Embossing(s1, pattern);

	eng.set("minValue",-0.*MM);
	eng.set("maxValue",0.1*MM);
	eng.set("blend",0.5*MM);
	eng.set("factor",1);
	eng.set("offset",0*MM);
	

	return new Shape(eng,new Bounds(-s,s,-s,s,-s,s));
}
