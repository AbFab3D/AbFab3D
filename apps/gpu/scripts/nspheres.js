function main(args) {
    var radius = 15 * MM;
    var num = 12;
    var gs = 2*radius;
    var grid = createGrid(-gs, gs, -gs, gs, -gs, gs, 0.1 * MM);

    var result;
    if (num == 1) {
        result = new Sphere(0,0,0,radius);
    } else {
        var union = new Union();
		
		var x0 = -radius;
		var dx = 2*radius/(num-1);
        for (i = 0; i < num; i++) {
            union.add(new Sphere(x0 + dx*i, 0, 0, radius));
        }
        result = union;
    }
    var maker = new GridMaker();
    maker.setSource(result);
    maker.makeGrid(grid);

    return grid;
}
