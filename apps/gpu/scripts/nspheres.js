function main(args) {
    var radius = 15 * MM;
    var num = 2;
    var gs = 60 * MM;
    var grid = createGrid(-gs, gs, -gs, gs, -gs, gs, 0.1 * MM);

    var result;
    if (num == 1) {
        result = new Sphere(0,0,0,radius);
    } else {
        var union = new Union();
        for (i = 1; i < num + 1; i++) {
            union.add(new Sphere(-i * radius / 2, 0, 0, radius));
            union.add(new Sphere(i * radius / 2, 0, 0, radius));
        }
        result = union;
    }
    var maker = new GridMaker();
    maker.setSource(result);
    maker.makeGrid(grid);

    return grid;
}
