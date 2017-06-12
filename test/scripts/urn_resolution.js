var uiParams = [
  {
    name: "model",
    desc: "Model",
    type: "uri",
    defaultVal: "urn:shapeways:stockModel:sphere"
  }
];
function main(args) {
    console.log(args.model);
    var radius = 25 * MM;
    var sphere = new Sphere(radius);

    var s = 25*MM;
    return new Scene(sphere,new Bounds(-s,s,-s,s,-s,s));
}
