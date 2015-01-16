function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(10*MM, 0.5*MM);
    var intersect = new Intersection();
    intersect.add(sphere);
    intersect.add(gyroid);
    var maker = new GridMaker();
    maker.setSource(intersect);
    maker.makeGrid(grid);

    return grid;
}
