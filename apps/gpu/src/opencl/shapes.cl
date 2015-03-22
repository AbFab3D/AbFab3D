

// torus 
typedef struct {
    int size;
    int opcode;
    float r;
    float R;
    float3 center;
} sTorus;

void oTorus(PTRS sTorus *torus, sVec *in, sVec *out){

    float3 p = in->v.xyz;
    p -= torus->center;

    p.y = length(p.xy) - torus->R;

    float d = length(p.yz) - torus->r;

    out->v.x = d;    

}

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom patrameters of DataSource 
    float radius;  // sphere radius (positive radius - interior of sphere, negative - exterior) 
    float3 center; // center   
} sSphere;

void oSphere(PTRS sSphere *sphere, sVec *in, sVec *out){

    float3 v = in->v.xyz;
    v -= sphere->center;
    float len = length(v);
    float radius = sphere->radius;
    float d = sign(radius) * len - radius;

    out->v.x = d;

}

void ooSphere(sSphere sphere, sVec *in, sVec *out){

    float3 v = in->v.xyz;
    v -= sphere.center;
    float len = length(v);
    float rad = sphere.radius;
    float d = sign(rad) * len - rad;

    out->v.x = d;

}

// box
typedef struct {
    int size;
    int opcode;
    float rounding;  // rounding of box edges in 
    float3 center;   // center of the box 
    float3 halfsize; // half size of the box 
} sBox;

void oBox(PTRS sBox *box, sVec *in, sVec *out){

    float3 v = in->v.xyz;
    v -= box->center;    
    v = fabs(v);
    v -= box->halfsize;
    
    float d = blendMax(blendMax(v.x,v.y,box->rounding),v.z,box->rounding);

    out->v.x = d;
}

// embossing
typedef struct {
    int size;
    int opcode;
    float blendWidth;
    float minValue;  // min threshold
    float maxValue;  // max threshold
    float factor;   // displacement = vFactor*v + vOffset
    float offset;  
} sEmbossing;

// emboss shape1 with shape2 
void oEmbossing(PTRS sEmbossing *ptr, sVec *in1, sVec *in2, sVec *out){

    // bump mapping version 
    //float eng = max(0.f,min(ptr->depth, -in2->v.x));
    float v = ptr->factor * in2->v.x + ptr->offset;
    float e = blendMax(ptr->minValue,blendMin(ptr->maxValue, v, ptr->blendWidth ),ptr->blendWidth);
    out->v.x = in1->v.x - e;

    /*
    // subtraction version 
    float d = in1->v.x;
    // sub surface layer of shape     
     d = max(d, -d - ptr->depth);
    // intersection of subsurface layer and engraver
    d = blendMax(d, in2->v.x, ptr->blendWidth);
    // subtraction of shape and intersected engraver 
    out->v.x = blendMax(in1->v.x, -d, ptr->blendWidth);
    */
}

