float theKernel_core(float a, float b)
{
    a *= 2;
    b *= 2;
    a += b ;
    b += a ;
    return a + b ;
}

__kernel void theKernel(
     int numElements,
     __global const float *input0,
     __global const float *input1,
     __global float *output)
{
    int gid = get_global_id(0);
    if (gid < numElements)
    {
        float a = input0[gid];
        float b = input1[gid];
        output[gid] = theKernel_core(a, b);
    }
}