typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of DataSource 
    float3 translation; 
} sTranslation;

void oTranslation(PTRS sTranslation *trans, sVec *inout){
    // inverse translation 
    inout->v.xyz -= trans->translation;

}

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of sScale
    float averageFactor; // inverse of average scale 
    float3 factor; // inverse of scale 
    float3 center; // center of scale 
} sScale;

void oScale(PTRS sScale *pS, sVec *inout){
    
    inout->v.xyz -= pS->center;
    inout->v.xyz *= pS->factor;
    inout->v.xyz += pS->center;
    // TODO - adjustment of total scale 
}

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of sRotation 
    float3 center; 
    float3 m0; 
    float3 m1; 
    float3 m2; 
} sRotation;

void oRotation(PTRS sRotation *rot, sVec *inout){

    float3 vec = inout->v.xyz;
    vec -= rot->center;   
    vec = (float3){dot(rot->m0,vec),dot(rot->m1,vec),dot(rot->m2,vec)};
    vec += rot->center;    
    
    inout->v.xyz = vec;
}

typedef struct {
    int type;
    float radius;
    float3 center; 
} sSPlane;
#define PLANE  0
#define SPHERE  1


void oReflect(PTRS sSPlane *s, sVec *inout){

    switch(s->type){

    case PLANE: 
        {
            float3 center = s->center;
            float radius = s->radius;
            //float vn = dot( inout->v.xyz - center * radius, center);
            inout->v.xyz -= (2.f*dot( inout->v.xyz - center * radius, center))* center;
            
        } break;
    case SPHERE:
        {
            float3 v = inout->v.xyz;
            v -= s->center;
            float len2 = dot(v,v);
            float r2 = s->radius;
            r2 *= r2;
            float factor = (r2/len2);
            v *= factor;
            v += s->center; 
            
            inout->scale *= factor;
            inout->v.xyz = v;           
        } break;
    } 
}

