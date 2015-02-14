function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	
	var cx = 5*MM;
	var R = 20*MM;
	var r = 0.1*MM;
	var angle = Math.PI/20;
	
	var union = new Union();
	var t1 = new Torus(new Vector3d(cx,cx,0),R, r);
	//t1.setTransform(new Translation(new Vector3d(0,0,r)));
	t1.setTransform(new Rotation(new Vector3d(1,0,0), angle));
	union.add(t1);
	var t2 = new Torus(new Vector3d(-cx,cx,0),R, r);
	t2.setTransform(new Rotation(new Vector3d(1,0,0), 2*angle));
	var t3 = new Torus(new Vector3d(-cx,-cx,0),R, r);
	t3.setTransform(new Rotation(new Vector3d(1,0,0), 3*angle));
	
	var t4 = new Torus(new Vector3d(cx,-cx,0),R, r);
	//t4.setTransform(new Rotation(new Vector3d(1,0,0), 4*angle));
	union.add(t2);
	union.add(t3);
	union.add(t4);
	
    var maker = new GridMaker();
    maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
