function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	
	var cx = 5*MM;
	var R = 10*MM;
	var r = 3*MM;
	var angle = Math.PI/2;
	
	var union = new Union();
	var t1 = new Torus(new Vector3d(cx,cx,0),R, r);
	//t1.setTransform(new Translation(new Vector3d(0,0,r)));
	t1.setTransform(new Rotation(new Vector3d(0,0,r), angle));
	union.add(t1);
	union.add(new Torus(new Vector3d(-cx,cx,0),R, r));
	union.add(new Torus(new Vector3d(-cx,-cx,0),R, r));
	union.add(new Torus(new Vector3d(cx,-cx,0),R, r));
	
    var maker = new GridMaker();
    maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
