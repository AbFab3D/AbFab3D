function main(args) {
    var radius = 15 * MM;
	var s = 2*radius;
    var grid = createGrid(-s,s,-s,s,-s,s,0.1*MM);
    var box = new Box(2*radius,2*radius,2*radius);
	box.setTransform(new Translation(radius,radius,radius));
    var maker = new GridMaker();
    maker.setSource(box);
    maker.makeGrid(grid);

    return grid;
}
