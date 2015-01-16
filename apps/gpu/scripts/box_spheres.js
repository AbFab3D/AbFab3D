function main(args) {

    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
	var c = 25*MM;
	var rs = c*Math.sqrt(2);
    var box = new Box(0,0,0, 50*MM, 50*MM, 50*MM);
    var inter = new Intersection();
	inter.add(box);
	inter.add(new Sphere(c,c,c, -rs));	
	inter.add(new Sphere(-c,c,c, -rs));	
	inter.add(new Sphere(-c,-c,c, -rs));	
	inter.add(new Sphere(c,-c,c, -rs));	
	inter.add(new Sphere(c,c,-c, -rs));	
	inter.add(new Sphere(-c,c,-c, -rs));	
	inter.add(new Sphere(-c,-c,-c, -rs));	
	inter.add(new Sphere(c,-c,-c, -rs));	
    var maker = new GridMaker();
    maker.setSource(inter);
    maker.makeGrid(grid);

    return grid;
}
