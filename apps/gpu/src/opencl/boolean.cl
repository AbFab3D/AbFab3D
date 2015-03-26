
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

