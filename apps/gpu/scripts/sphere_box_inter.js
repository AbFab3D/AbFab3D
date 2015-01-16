function main(args) {

    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var sphere = new Sphere(0, 0,0, 25*MM);
    var box = new Box(0,0,0, 50*MM, 40*MM, 10*MM);
    var inter = new Intersection(sphere, box);
    var maker = new GridMaker();
    maker.setSource(inter);
    maker.makeGrid(grid);

    return grid;
}
