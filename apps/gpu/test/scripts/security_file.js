function main(args) {
  var size = 5*MM;
  var thickeness = 1*MM;

  var box = new Box(0,0,0,size,thickeness, thickeness);
  box.setTransform(new Translation(0,2*MM,0));

  var f1 = new java.io.File("c:/tmp/foo.txt");
  print("File exists: " + f1.exists());

  return new Shape(box,new Bounds(-2*size,2*size,-2*size,2*size,-2*size,2*size));
}

