function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var box = new Box(0,0,0, 49*MM, 35*MM, 25*MM);
    var maker = new GridMaker();
    maker.setSource(box);
    maker.makeGrid(grid);

    return grid;
}
