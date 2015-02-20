function main(args) {
    var radius = 25 * MM;
	var bx = 30*MM;
	var by = 20*MM;
	var bz = 10*MM;
	
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var box = new Box(bx/2,0,0, bx, by, bz);
    var maker = new GridMaker();
    maker.setSource(box);
    maker.makeGrid(grid);

    return grid;
}
