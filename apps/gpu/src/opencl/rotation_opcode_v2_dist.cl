float3 rotation(float3 center, float16 inv_mat,float3 in) {
    float3 pos = in - center;  // TODO: benchmark compare for no center calc

    return (float3)(
       dot(inv_mat.s048, pos),
       dot(inv_mat.s159, pos),
       dot(inv_mat.s26A, pos)) + center;

}
