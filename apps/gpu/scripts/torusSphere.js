function main(args) {
    var R = 20 * MM;
	var r = 5*MM;
	
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	
    var torus = new Torus(new Vector3d(0,0,0),R,r);
    var sphere = new Sphere(new Vector3d(0,0,0), R-r);
	
	var union = new Union(torus, sphere);
	
    var maker = new GridMaker();
    
	maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
