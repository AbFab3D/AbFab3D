function main(args) {
  var size = 5*MM;
  var thickeness = 1*MM;
  var grid = createGrid(-2*size,2*size,-2*size,2*size,-2*size,2*size,0.1*MM);

  var box = new Box(0,0,0,size,thickeness, thickeness);
  box.setTransform(new Rotation(1,1,0,Math.PI / 2));

  var maker = new GridMaker();
  maker.setSource(box);
  maker.makeGrid(grid);
  return grid;
}

