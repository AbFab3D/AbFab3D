function main(args) {
  var size = 5*MM;
  var thickeness = 1*MM;

  var box = new Box(0,0,0,size,thickeness, thickeness);

  return new Shape(box,new Bounds(-2*size,2*size,-2*size,2*size,-2*size,2*size));
}

