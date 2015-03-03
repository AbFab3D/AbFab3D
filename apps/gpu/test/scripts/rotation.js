function main(args) {
  var size = 5*MM;
  var thickeness = 1*MM;

  var box = new Box(0,0,0,size,thickeness, thickeness);
  box.setTransform(new Rotation(1,1,0,Math.PI / 2));

  return new Shape(box,new Bounds(-2*size,2*size,-2*size,2*size,-2*size,2*size));
}

