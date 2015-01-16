function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	var bx = 49*MM;
	var by = 30*MM;
	var bz = 10*MM;
    var box1 = new Box(0,0,0, bx, by, bz);
    var box2 = new Box(0,0,0, by, bz, bx);
    var box3 = new Box(0,0,0, bz, bx, by);
	var union = new Union();
	union.add(box1);
	union.add(box2);
	union.add(box3);
	
    var maker = new GridMaker();
    maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
