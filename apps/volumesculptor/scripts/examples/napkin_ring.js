var TAU = (Math.sqrt(5)+1)/2;
var PI = Math.PI;

function getPlane(v1,v2){
    var n = new Vector3d();
    n.cross(v2,v1);
    n.normalize();
    return ReflectionSymmetry.getPlane(n, 0);

}



function getIcosahedralSymmetry( ){


    var v5 = new Vector3d(1,0,TAU);
    var v3 = new Vector3d(0,1/TAU,TAU);
    var p35 = new Vector3d();

    p35.cross(v3, v5);
    p35.normalize();

    var splanes = new Array();
    var count = 0;
    splanes[count++] = new ReflectionSymmetry.getPlane(new Vector3d(-1,0,0), 0.);
    splanes[count++] = new ReflectionSymmetry.getPlane(new Vector3d(0,-1,0), 0.);
    splanes[count++] = new ReflectionSymmetry.getPlane(p35, 0.);

    return new ReflectionSymmetry(splanes);

}

function getSphereBend(fixedRadius, bendAmount, offset){

    var center = fixedRadius*fixedRadius/bendAmount;
    var radius = Math.sqrt(center*center + fixedRadius*fixedRadius);

    var cp = new CompositeTransform();
    cp.add(new PlaneReflection(new Vector3d(0,0,1), new Vector3d(0,0,offset)));
    cp.add(new SphereInversion(new Vector3d(0,0,-center + offset), radius));
    return cp;
}

function getCylinderBend(fixedRadius, bendAmount){

    var center = (fixedRadius*fixedRadius - bendAmount*bendAmount)/(2*bendAmount);
    var radius = Math.sqrt(center*center + fixedRadius*fixedRadius);
    var cp = new CompositeTransform();
    cp.add(new RingWrap(radius));
    cp.add(new Translation(0,0,-(center+bendAmount)));

    return cp;

}

function getImage(radius, thickness, path){

    var s = radius/Math.sqrt(1 + 1./(TAU*TAU));
    var v5 = new Vector3d(s/TAU,0,s);
    var v3 = new Vector3d(0,s/(TAU*TAU),s);

    var union = new Union();

    var correction = 1.05;
	var xpnt = v5.x;
	var ypnt = v3.y*correction;
    //var box = new Box(xpnt, ypny, thickness);
    var image = new Image3D(path, xpnt, ypnt, thickness);
    image.setBaseThickness(0);
    var vs = 0.1*MM;
    image.setVoxelSize(vs);
    image.setUseGrayscale(false);
    image.setBlurWidth(vs);
	
	var ct = new CompositeTransform();
	ct.add(new Translation(xpnt/2,ypnt/2,0));
	ct.add(getCylinderBend(xpnt,0.9*xpnt));
	ct.add(new Translation(0,0,v5.z));
    image.setTransform(ct);

    union.add(image);

    var dt = new DataTransformer();
    dt.setSource(union);
    return dt;
}

function main(arg){

    var radius = 31.75*MM;
    var thickness = 1.2*MM;
    var voxelSize = 0.2*MM;
    var a = radius + 2*thickness;

    var path = arg[0];
    var trans;

    var image = getImage(radius,thickness, path);

    var union = new Union();

    union.add(image);
    union.setTransform(getIcosahedralSymmetry( ));

    cyl = new Cylinder(new Vector3d(-a,0,0), new Vector3d(a,0,0), 19.05*MM);

    subtract = new Subtraction(union, cyl);

    var maker = new GridMaker();

    //maker.setSource(subtract);
    maker.setSource(union);

    var dest = createGrid(-a,a,-a,a,-a,a,voxelSize);

    maker.makeGrid(dest);

    return dest;

}
