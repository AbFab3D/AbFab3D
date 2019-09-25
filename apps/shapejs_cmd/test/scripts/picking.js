function main(args) {
    var radius = 25 * MM;
    var sphere = new Sphere(radius);

    var s = radius + 1*MM;
    return new Scene(sphere,new Bounds(-s,s,-s,s,-s,s));
}
