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
    var size = 0.04;
    var thickness = 0.01;
    var grid = createGrid(-16*MM,16*MM,-16*MM,16*MM,-16*MM,16*MM,0.1*MM);
	var sphere = new Sphere(15*MM);
	
	var box = new Box(0,0,15*MM, 25*MM, 10*MM, 25*MM);
	
	var eng = new Engraving(sphere, box);
	
	eng.getParam("depth").setValue(0.5*MM);
	eng.getParam("blend").setValue(0.1*MM);
	
    var maker = new GridMaker();
    maker.setSource(eng);
    maker.makeGrid(grid);
    return grid;
}
