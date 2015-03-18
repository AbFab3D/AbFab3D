var params = [
    {
        name: "radius",
        desc: "radius",
        type: "range",
        rangeMin: 1,
        rangeMax: 21,
        step: 1,
        defaultVal: 10
    }
];
function main(args) {
    var radius = args.radius * MM;
    var sphere = new Sphere(radius);

    var r = radius * 1.1;
    return new Shape(sphere,new Bounds(-r,r,-r,r,-r,r));
}
