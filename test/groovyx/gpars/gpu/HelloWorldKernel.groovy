package groovyx.gpars.gpu.experiments
/**
 * A very basic test of the @Kernel annotation and the execution
 * of closure code on the GPU
 */
class HelloWorldKernel
{
    static void main(def args)
    {
        long nanoTimeBefore = 0;
        long nanoTimeAfter = 0;
        double durationMS = 0;
        
        int n = 1000000;
        float[] values0 = 1..n
        float[] values1 = 1..n

        // The @Kernel annotation will be processed by the
        // KernelExtractASTTransformation during the class generation
        // phase of the compilation. This will cause the code from 
        // the closure to be converted into the source code for an
        // OpenCL kernel (by the KernelExtractGroovyCodeVisitor).
        // The code is written into a file and compiled at runtime. 
        // NOTE: Only very basic arithmetic is supported until now!
        @Kernel
        def theKernel = { float a, float b -> a*=2; b*=2; a+=b; b+=a; a+b }

        
        // This function combines two float arrays using a closure, and 
        // stores the results in a third one (This should be part of a 
        // DSL for float arrays or collections that can be converted 
        // into float arrays, and of course it should hide the details
        // of the analogous implementation of 'combineArraysCL' that
        // is defined below...)
        def combineArrays = { float[] in0, float[] in1, float[] out, f ->
            def max = Math.min(in0.size(), in1.size());
            0.upto(max-1) {index -> out[index] = f(in0[index], in1[index]) }
            out}

        
        // Combine the two input arrays and print the first few results
        float[] results = new float[n]
        nanoTimeBefore = System.nanoTime();
        def sum = combineArrays(values0, values1, results, theKernel)
        nanoTimeAfter = System.nanoTime();
        durationMS = ((nanoTimeAfter-nanoTimeBefore)/1e6)
        println "Result from Groovy: "+sum.getAt(0..5)+", duration "+durationMS+" ms"

        // Create the OpenCL setup and prepare the Kernel 
        // (this should be hidden, of course!)
        CLSetup clSetup = new CLSetup();
        clSetup.createKernel("theKernel");
        
        // This function combines two float arrays with OpenCL, using the kernel 
        // that has been created from a closure. The usage of the kernel and 
        // OpenCL itself should be hidden, maybe inside a DSL
        def combineArraysCL = { float[] in0, float[] in1, float[] out, f ->
            def max = Math.min(in0.size(), in1.size());
            clSetup.callKernel(max, in0, in1, out)
            out}

        // Combine the two input arrays and print the first few results
        float[] resultsCL = new float[n]
        nanoTimeBefore = System.nanoTime();
        def sumCL = combineArraysCL(values0, values1, resultsCL, theKernel)
        nanoTimeAfter = System.nanoTime();
        durationMS = ((nanoTimeAfter-nanoTimeBefore)/1e6)
        println "Result from OpenCL: "+sumCL.getAt(0..5)+", duration "+durationMS+" ms";

        clSetup.shutdown();
    }
}


