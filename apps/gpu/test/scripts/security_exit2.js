java.lang.System.exit(1);

function main(args) {
  var size = 5*MM;
  var thickeness = 5*MM;

  var box = new Box(0,0,0,size,thickeness, thickeness);
  box.setTransform(new Translation(0,2*MM,0));

  return new Shape(box,new Bounds(-2*size,2*size,-2*size,2*size,-2*size,2*size));
}

