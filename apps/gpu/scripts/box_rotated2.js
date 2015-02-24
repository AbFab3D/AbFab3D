function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	var bx = 15*MM;
	var by = 15*MM;
	var bz = 5*MM;
	var angle = Math.PI*(0.5 + 2./3);
	
    var box = new Box(0,-by/2,0, bx, by, bz);
	var rot = new Rotation(0,0,1,-angle);
	rot.getParam("center").setValue(new Vector3d(-bx/2,0,0));
	box.setTransform(rot);
	box.getParam("rounding").setValue(0.5*MM);

    var box2 = new Box(0,-by/2,0, bx, by, bz);
	var rot2 = new Rotation(0,0,1,angle);
	rot2.getParam("center").setValue(new Vector3d(bx/2,0,0));
	box2.setTransform(rot2);
	box2.getParam("rounding").setValue(0.5*MM);
	
	var box1 = new Box(0,-by/2,0, bx, by, bz);
	box1.getParam("rounding").setValue(0.5*MM);
	
	var union = new Union();
	union.getParam("blend").setValue(1*MM);
	union.add(box);
	union.add(box1);
	union.add(box2);
	
    var maker = new GridMaker();
    maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
