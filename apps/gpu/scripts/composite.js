function main(args) {
  var size = 5*MM;
  var thickeness = 1*MM;

  var box = new Box(0,0,0,size,thickeness, thickeness);
  var trans = new Translation(0,2*MM,0);
  var rot = new Rotation(0,1,0,Math.PI/2);
  var scale = new Scale(2,1,1);
  var ct = new CompositeTransform();
  ct.add(trans);
  ct.add(rot);
  ct.add(scale);

  box.setTransform(ct);
  var s = size;
  return new Shape(box,new Bounds(-s,s,-s,s,-s,s));
  
}
