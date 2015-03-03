function cross3D(size, thickness){
    var union = new Union();
    var boxX = new Box(0,0,0,size,thickness, thickness);
    var boxY = new Box(0,0,0, thickness, size, thickness);
    var boxZ = new Box(0,0,0,thickness, thickness,size);
    union.add(boxX);
    union.add(boxY);
    union.add(boxZ);
	union.getParam("blend").setValue(thickness*0.0);
    return union;
}

function main(args) {
    var size = 30*MM;
    var thickness = 10*MM;
	var b = 25*MM;
	var cross1 = cross3D(size, thickness);
	var cross2 = cross3D(size*1.5, 0.6*thickness);
	cross2.setTransform(new Translation(0.19*thickness,0.19*thickness,0.19*thickness));
    var diff = new Subtraction(cross1, cross2);
	diff.getParam("blend").setValue(thickness*0.0);
	
	var s = 16*MM;
	return new Shape(diff,new Bounds(-s,s,-s,s,-s,s));
}
