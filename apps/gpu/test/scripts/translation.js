function main(args) {
  var size = 5*MM;
  var thickeness = 1*MM;
  var grid = createGrid(-size,size,-size,size,-size,size,0.1*MM);

  var box = new Box(0,0,0,size,thickeness, thickeness);
  box.setTransform(new Translation(0,2*MM,0));

  var maker = new GridMaker();
  maker.setSource(box);
  maker.makeGrid(grid);
  return grid;
}

