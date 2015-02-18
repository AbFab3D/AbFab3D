function cross3D(size, thickeness){
    var union = new Union();
    var boxX = new Box(0,0,0,size,thickeness, thickeness);
    var boxY = new Box(0,0,0, thickeness, size, thickeness);
    var boxZ = new Box(0,0,0,thickeness, thickeness,size);
    union.add(boxX);
    union.add(boxY);
    union.add(boxZ);
    return union;
}

function main(args) {
    var size = 30*MM;
    var thickness = 10*MM;
	var b = 25*MM;
    var grid = createGrid(-16*MM,16*MM,-16*MM,16*MM,-16*MM,16*MM,0.1*MM);
    var diff = new Subtraction(cross3D(size, thickness), cross3D(size, 0.7*thickness));
	
    var maker = new GridMaker();
    maker.setSource(diff);
    maker.makeGrid(grid);
    return grid;
}
