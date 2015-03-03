
function main(args) {
	var r = 15*MM;
	var bx = 30*MM;
	var by = 15*MM;
	var bz = 15*MM;
	
    var diff = new Subtraction(new Sphere(r), new Box(bx, by, bz));	
	diff.getParam("blend").setValue(0.5*MM);
	return new Shape(diff,new Bounds(-r,r,-r,r,-r,r));
}
