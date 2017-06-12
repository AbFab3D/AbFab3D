
var uiParams = [
  {
    name: "image",
    label: "Image",
    desc: "Image",
    type: "Uri",
    group: "Image"
  }
];
function main(args) {
    if (typeof args.image !== 'undefined') {
      var image = loadImage(args.image);
    } else {
      console.log("image is undefined: " + args.image);
    }

    var s = 25*MM;
    return new Scene(new Box(s/2,s/2,s/2),new Bounds(-s,s,-s,s,-s,s));
}
