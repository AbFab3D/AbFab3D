function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	var bx = 49*MM;
	var by = 30*MM;
	var bz = 10*MM;
    var box1 = new Box(0,0,0, bx, by, bz);
    var box2 = new Box(0,0,0, by, bz, bx);
    var box3 = new Box(0,0,0, bz, bx, by);
	var inter = new Intersection();
	box1.setTransform(new Rotation(new Vector3d(1,0,0), Math.PI/10));
	box2.setTransform(new Rotation(new Vector3d(0,1,0), Math.PI/10));
	box3.setTransform(new Rotation(new Vector3d(0,0,1), Math.PI/10));
	inter.add(box1);
	inter.add(box2);
	inter.add(box3);
	
    var maker = new GridMaker();
    maker.setSource(inter);
    maker.makeGrid(grid);

    return grid;
}
