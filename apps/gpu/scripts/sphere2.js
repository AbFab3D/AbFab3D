function main(args) {
    var radius = 15 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var sphere1 = new Sphere(-10*MM, 0,0, radius);
    var sphere2 = new Sphere(10*MM, 0,0, radius);
    var union = new Union();
    union.add(sphere1);
    union.add(sphere2);
    var maker = new GridMaker();
    maker.setSource(union);
    maker.makeGrid(grid);

    return grid;
}
