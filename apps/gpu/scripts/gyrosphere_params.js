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
    var sphere = new Sphere(radius);
    var gyroid = new VolumePatterns.Gyroid(args['period']*MM, args['thickness']*MM);
    var intersect = new Intersection();
    intersect.add(sphere);
    intersect.add(gyroid);
	
	var s = 25*MM;
	return new Shape(union,new Bounds(-s,s,-s,s,-s,s));
}
