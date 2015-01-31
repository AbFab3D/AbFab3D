float box(float vs, float3 minv, float3 maxv, float3 pnt) {
	float x = pnt.x;
	float y = pnt.y;
	float z = pnt.z;
	float w = BlendWidth;
	float dx = max(minv.x-x, x-maxv.x);
	float dy = max(minv.y-y, y-maxv.y);
	float dz = max(minv.z-z, z-maxv.z);
	float d = blendMax(dx, dy,w);
    d = blendMax(d, dz,w);
    return d;

}
