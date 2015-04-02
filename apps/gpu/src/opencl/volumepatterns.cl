// gyroid 
typedef struct {
    int size;
    int opcode;
    float level;
    float thickness;
    float factor;
    float3 offset;
} sGyroid;

void oGyroid(PTRS sGyroid *g, sVec *in, sVec *out){
    float3 pnt = in->v.xyz;
    pnt -= g->offset;
    pnt *= g->factor;
    
    float d = fabs((sin(pnt.x) * cos(pnt.y) + sin(pnt.y) * cos(pnt.z) + sin(pnt.z) * cos(pnt.x) - g->level) / g->factor) - (g->thickness);

    out->v.x = d;    

} // oGyroid

// SchwarzP
typedef struct {
    int size;
    int opcode;
    float level;
    float thickness;
    float factor;
} sSchwarzP;

void oSchwarzP(PTRS sSchwarzP *g, sVec *in, sVec *out){
    float3 pnt = in->v.xyz;
    pnt *= g->factor;
    
    out->v.x = fabs((cos(pnt.x) + cos(pnt.y) + cos(pnt.z) - g->level) / g->factor) - (g->thickness);
    
}
// SchwarzP

typedef struct {
    int size;
    int opcode;
    float level;
    float thickness;
    float factor;
} sSchwarzD;

void oSchwarzD(PTRS sSchwarzD *g, sVec *in, sVec *out){
    float3 pnt = in->v.xyz;
    pnt *= g->factor;
    
    float 
        x = pnt.x,
        y = pnt.y,
        z = pnt.z,
        sinx = sin(x),
        siny = sin(y),
        sinz = sin(z),
        cosx = cos(x),
        cosy = cos(y),
        cosz = cos(z);

    out->v.x = fabs(sinx * siny * sinz + sinx * cosy * cosz + cosx * siny * cosz + cosx * cosy * sinz - g->level) / g->factor - g->thickness;
    
} // oSchwarzD

typedef struct {
    int size;
    int opcode;
    float level;
    float thickness;
    float factor;
} sLidinoid;

void oLidinoid(PTRS sLidinoid *g, sVec *in, sVec *out){
    float3 pnt = in->v.xyz;
    pnt *= g->factor;
    
    float 
        x = pnt.x,
        y = pnt.y,
        z = pnt.z,
        s2x = sin(2*x),
        s2y = sin(2*y),
        s2z = sin(2*z),
        c2x = cos(2*x),
        c2y = cos(2*y),
        c2z = cos(2*z),
        sx = sin(x),
        sy = sin(y),
        sz = sin(z),
        cx = cos(x),
        cy = cos(y),
        cz = cos(z);


    out->v.x = fabs(((s2x*cy*sz + s2y*cz*sx + s2z*cx*sy) - (c2x*c2y + c2y*c2z + c2z*c2x) 
                     + 0.3f - g->level)/g->factor)  - g->thickness;
    
} // oLidinoid

