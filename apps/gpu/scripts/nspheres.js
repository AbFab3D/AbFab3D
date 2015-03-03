function main(args) {
    var radius = 15 * MM;
    var num = 3;
    var gs = 2*radius;

    var result;
    if (num == 1) {
        result = new Sphere(0,0,0,radius);
    } else {
        var union = new Union();
		var blend = union.getParam("blend").setValue(0.5*MM);
		var x0 = -radius;
		var dx = 2*radius/(num-1);
        for (i = 0; i < num; i++) {
            union.add(new Sphere(x0 + dx*i, 0, 0, radius));
        }
        result = union;
    }
	
	var s = gs;
	return new Shape(union,new Bounds(-s,s,-s,s,-s,s));
}
