function main(args) {
    var R = 20 * MM;
	var r = 8*MM;
	var s = R + 2*r;
		
	var union = new Union();

    var torus = new Torus(new Vector3d(0,0,0),R,r);

    var sphere1 = new Sphere(new Vector3d(0,0,0), 2*r);
	sphere1.setTransform(new Translation(R,0,0));
    var sphere2 = new Sphere(new Vector3d(0,0,0), 2*r);
	sphere2.setTransform(new Translation(-R,0,0));
    var sphere3 = new Sphere(new Vector3d(0,0,0), 2*r);
	sphere3.setTransform(new Translation(0,R,0));
    var sphere4 = new Sphere(new Vector3d(0,0,0), 2*r);
	sphere4.setTransform(new Translation(0,-R,0));
	
	//union.setTransform(new Translation(-r,0,0));

	union.add(torus);
	union.add(sphere1);
	union.add(sphere2);
	union.add(sphere3);
	union.add(sphere4);
	
	return new Shape(union,new Bounds(-s,s,-s,s,-s,s));

}