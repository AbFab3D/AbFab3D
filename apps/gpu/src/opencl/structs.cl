// pointer to local ops buffer
#define PTROPS local 
// pointer to struct in private memory 
#define PTRS local 
// pointer to buffer of large data 
#define PTRDATA global const

// desription of the scene to render 
typedef struct {
    float worldScale;      //
    float3 worldCenter;    // center of the world box
    PTROPS int *pOps;      // operations 
    int opCount;           // count of operations 
    PTRDATA char* pgData;  // global large data 
    global const float* invvm; // inverse view matrix
} Scene; 




typedef struct{
    float4 v;  // 3D point
    float scale; // scaling factor
} sVec;

// union to "safely" convert pointers 
typedef union {
    PTRS void *pv;  
    PTRS int *w;
} CPtr;

