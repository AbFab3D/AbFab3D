//union of 3 boxes arranged along x axis 
// middle box is shifted up 
// the whole shape is rotated around z 
function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	
	var cx = 10*MM;
	var R = 15*MM;
	var r = 9*MM;
	var angle = Math.PI/10;
	var bsize = new Vector3d(R,r,r);
	var union = new Union();
	
	var t1 = new Box(-cx,0,0, r,r,r);
	union.add(t1);
	var t2 = new Box(0,0,0,r,r,r);
	t2.setTransform(new Translation(0, cx/2, 0));	
	var t3 = new Box(cx,0,0, r, r, r);
	union.add(t2);
	union.add(t3);
	// rotate the whole union around Z
	union.setTransform(new Rotation(new Vector3d(0,0,1), Math.PI/4));
	
    var maker = new GridMaker();
    maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
