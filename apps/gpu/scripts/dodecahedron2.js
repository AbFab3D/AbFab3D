function dodecahedron(height) {
  var intersect = new Intersection();
  var rot1 = new Rotation(new Vector3d(1, 0, 0), 2.0344430402625);
  var cube = new Box(0, 0, 0, 2*height, 2*height, height);
  intersect.add(cube);
  
  for (var i = 0; i < 5; i++) {
    var trans = new CompositeTransform();
    trans.add(rot1);
    trans.add(new Rotation(new Vector3d(0, 0, 1), 1.256637061*i));
    cube = new Box(0, 0, 0, 2*height, 2*height, height);
    cube.setTransform(trans);
    intersect.add(cube);
  }
  intersect.setTransform(new Rotation(new Vector3d(1,0,0), Math.PI/10));
  return intersect;
}

function main(args) {
  var s = 25*MM;
  var vs = 0.5*MM;
  var dodec = dodecahedron(1.6*s);
    
  return new Shape(dodec,new Bounds(-s,s,-s,s,-s,s));
  
}
