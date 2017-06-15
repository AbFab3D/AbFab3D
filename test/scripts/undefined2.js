var uiParams = [
  {
    name: "bailPos",
    desc: "Bail Pos",
    type: "location",
    onChange: "bailChanged"
  },
];
function main(args) {
  if (args.bailPos === undefined) {
    console.log("bail is undefined: " + args.image);
  }

  var s = 25 * MM;
  return new Scene(new Box(s / 2, s / 2, s / 2), new Bounds(-s, s, -s, s, -s, s));
}
