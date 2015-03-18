var uiParams = [
    {
        name: "model",
        desc: "model",
        type: "uri"
    },
    {
        name: "models",
        desc: "models",
        type: "string[]",
        defaultVal : ["model1","model2"]
    },
    {
        name: "text",
        desc: "Text",
        type: "string",
        "onChange" : "textChanged",
        defaultVal: "Shapeways"
    },
    {
        name: "center",
        desc: "Center",
        type: "location",
        defaultVal : { point: [-10*MM,0,0], normal: [1,0,0] }
    },
    {
        name: "radius",
        desc: "Radius",
        type: "double",
        rangeMin: 0.1,
        rangeMax: 10,
        step: 0.1,
        defaultVal: 1
    },
    {
        name: "radii",
        desc: "Radii",
        type: "double[]",
        rangeMin: 0.1,
        rangeMax: 10,
        step: 0.1,
        defaultVal: 1    // test double constructor
    },
    {
        name: "sizes",
        desc: "Sizes",
        type: "double[]",
        rangeMin: 0.1,
        rangeMax: 10,
        step: 0.1,
        defaultVal: [1,2.2]  // test list constructor
    }
];

function main(args) {
    var r = args.radius * MM;
    var center = args.center.point;
    var model = args.model;
    var models = args.models;
    var radii = args.radii;
    var sizes = args.sizes;

    var gsx = r + center.x;
    var gsy = r + center.y;
    var gsz = r + center.z;

    print("center: " + center + " size: " + gsx + " " + gsy + " " + gsz);
    print("model: " + model);
    print("models: 0: " + models[0] + " 1:" + models[1]);
    print("radii: " + radii[0]);

    print("Sizes: ");
    for (var i = 0; i < sizes.length; i++) {
        print(sizes[i]);
    }
    // TODO: thought this would have crashed it but doesnt.
    if (sizes[0] != 1 && sizes[1] != 2.2) return null;
    if (radii[0] != 1) return null;

    var union = new Union(new Sphere(r), new Box(center.x,center.y,center.z,r,r,r));
    return new Shape(union, new Bounds(-gsx,gsx,-gsy,gsy,-gsz,gsz));
}