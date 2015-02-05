function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var torus = new Torus(new Vector3d(0,0,0),12*MM,12*MM);
    var maker = new GridMaker();
    maker.setSource(torus);
    maker.makeGrid(grid);

    return grid;
}
