var params = [
  {
    name: "image",
    type: "uri",
    defaultVal: "buttonedsquareleatheroxblood_height_invert.png"
  },
    { name: "embossing", type: "double", rangeMin: 1, rangeMax: 10,step: 0.1,defaultVal: 4.25, unit: "mm"}, 
    { name: "thickness", type: "double", rangeMin: 0.1, rangeMax: 3,step: 0.1,defaultVal: 1, unit: "mm"}, 
    { name: "voxelSize", type: "double", rangeMin: 0.1, rangeMax: 1,step: 0.01,defaultVal: 0.5, unit: "mm"}, 
    
];

function makeLayer(path, x0, y0, z0, width, height, thickness, placement, useGray) {

  var img = new Image3D(path, width, height, thickness);
  //img.setBlurWidth(0.1 * MM);
  img.setImagePlace(placement);
  img.setUseGrayscale(useGray);
  img.setTransform(new Translation(x0, y0, z0));
  img.setBaseThickness(0);
  img.setBaseThickness(0.2);
  img.setBaseThreshold(0.01);

  return img;
}

function main(args) {
  var embossing = args.embossing;//4.25 * MM;   // height of embossing,
  var thickness = args.thickness;// desired overall thickness
  var middleThick = thickness - 2 * embossing;
  
  var width = 8 * IN;  
  var height = 8 * IN;
  var depth = embossing*2;
  var margin = 2*MM;
  
  
  var plane = new Plane(new Vector3d(0,0,1),embossing/2);
  
  var x = width/2 + margin;
  var y = height/2 + margin;
  var z = depth/2 + margin;
  var bounds = new Bounds(-x, x, -y, y, -z, z);
  var map = new ImageMap(args.image, width, height, 10*depth);
  map.set("whiteDisplacement", embossing);
  map.set("blackDisplacement", 0);
  map.set("repeatX", true);
  map.set("repeatY", true);
  //var topImg = makeLayer(args.top, 0, 0, embossing + middleThick, width, height, embossing, Image3D.IMAGE_PLACE_TOP, true);
  
  //result.add(topImg);

//  var subImg = makeLayer(args.top, 0, 0, embossing + middleThick, width, height, embossing*2, Image3D.IMAGE_PLACE_BOTTOM, true);
  //var subImg = makeLayer(args.top, 0, 0, embossing + middleThick, width, height, embossing*2, Image3D.IMAGE_PLACE_TOP, true);
  //subImg.translate(new Vector3d(0,0,-args.loc));
  //result = new Subtraction(result,subImg);
  var result = plane;
  var eplane = new Add(plane, map);
  
  var splane = new DataTransformer(eplane);
  splane.addTransform(new Translation(0,0,-thickness));
  
  var result = new Subtraction(eplane, splane);
  var result = new Intersection(result, new Box(width, height, 2*depth));
  
  var scene = Scene(result, bounds, args.voxelSize);
  scene.setMaxPartsCount(1);
  
  return scene;
}