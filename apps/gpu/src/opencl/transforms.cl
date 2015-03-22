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

