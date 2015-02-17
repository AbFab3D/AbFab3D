
function main(args) {
	var r = 15*MM;
	var bx = 30*MM;
	var by = 15*MM;
	var bz = 15*MM;
	
    var grid = createGrid(-r,r,-r,r,-r,r,0.1*MM);
    var diff = new Subtraction(new Sphere(r), new Box(bx, by, bz));	
	//var diff = new Subtraction(new Box(bx, by, bz),new Sphere(r));
	diff.getParam("blend").setValue(0.5*MM);
    var maker = new GridMaker();
    maker.setSource(diff);
    maker.makeGrid(grid);
    return grid;
}
