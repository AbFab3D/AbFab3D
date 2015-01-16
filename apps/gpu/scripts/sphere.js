function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var sphere = new Sphere(radius);
    var maker = new GridMaker();
    maker.setSource(sphere);
    maker.makeGrid(grid);

    return grid;
}
