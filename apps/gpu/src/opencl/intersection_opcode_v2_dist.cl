float intersectionOp(float a, float b) {

	float w = BlendWidth;// blending width 0.2mm       
    return blendMax(a,b,w);
	
}
