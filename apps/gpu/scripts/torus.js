function main(args) {
    var radius = 25 * MM;
    var torus = new Torus(new Vector3d(0,0,0),20*MM,5*MM);
	var s = 25*MM;
	return new Shape(torus,new Bounds(-s,s,-s,s,-s,s));
}
