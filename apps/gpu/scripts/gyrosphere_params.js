var params = [
    {
        "id": "period",
        "displayName": "Period",
        "type": "range",
        "rangeMin": 1,
        "rangeMax": 21,
        "step": 1,
        "default": 18
    },
    {
        "id": "thickness",
        "displayName": "Thickness",
        "type": "range",
        "rangeMin": 1,
        "rangeMax": 5,
        "step": 0.5,
        "default": 2
    }

];
function main(args) {
    var radius = 25 * MM;
    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(args['period']*MM, args['thickness']*MM);
    var intersect = new Intersection();
    intersect.add(sphere);
    intersect.add(gyroid);
    var maker = new GridMaker();
    maker.setSource(intersect);
    maker.makeGrid(grid);

    return grid;
}
