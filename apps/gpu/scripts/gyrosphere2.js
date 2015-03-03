function main(args) {
    var radius = 25 * MM;
    
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(10*MM, 0.5*MM);
    var intersect = new Intersection();
    intersect.add(sphere);
    intersect.add(gyroid);
	
	
	var s = 25*MM;
	return new Shape(intersect,new Bounds(-s,s,-s,s,-s,s));
	
}
