// blending function 
float blendQuadric(float x){
	return (1.f-x)*(1.f - x)*0.25f;
}

float blendMin(float a, float b, float w){

    float dd = min(a,b);
    float d = fabs(a-b);
    if( d < w) return dd - w*blendQuadric(d/w);	
    else return dd;
}

float blendMax(float a, float b, float w){

    float dd = max(a,b);
    float d = fabs(a-b);
    if( d < w) return dd + w*blendQuadric(d/w);
    else return dd;
}


void oMax(PTRS void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = max(in1->v.x, in2->v.x);

}

void oMin(PTRS void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = min(in1->v.x, in2->v.x);

}

void oSubtract(PTRS void *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = max(in1->v.x, -in2->v.x);

}

// blend 
typedef struct {
    int size;
    int opcode;
    float width;
    float padding;
} sBlend;


void oBlendMin(PTRS sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMin(in1->v.x, in2->v.x, ptr->width);

}

void ooBlendMin(sBlend sBlend, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMin(in1->v.x, in2->v.x, sBlend.width);

}

void oBlendMax(PTRS sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMax(in1->v.x, in2->v.x, ptr->width);

}

void oBlendSubtract(PTRS sBlend *ptr, sVec *in1, sVec *in2, sVec *out){

    out->v.x = blendMax(in1->v.x, -in2->v.x, ptr->width);

}

