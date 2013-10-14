package groovyx.gpars.gpu.experiments;

import org.jocl.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import static org.jocl.CL.*;

/**
 * Utility class for setting up OpenCL and maintaining
 * a single kernel
 * 
 * NOTE: This class is only a dummy for the first tests!!!
 */
public class CLSetup
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(CLSetup.class.getName());
	
    /**
     * The OpenCL context
     */
    private cl_context context;
    
    /**
     * The OpenCL command queue
     */
    private cl_command_queue commandQueue;
    
    /**
     * The OpenCL kernel
     */
    private cl_kernel kernel;

    /**
     * Constructor with default initialization
     */
    public CLSetup()
    {
    	logger.fine("Setting up OpenCL");
        
        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
        System.out.println("numPlatforms = " + numPlatforms);

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];
        System.out.println("platform = " + platform);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        System.out.println("numDevices = " + numDevices);

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];
        System.out.println("device = " + device);

        // Create a context for the selected device
        context = clCreateContext(contextProperties, 1, new cl_device_id[]
        { device }, null, null, null);

        // Create a command-queue for the selected device
        long commandQueueProperties = CL_QUEUE_PROFILING_ENABLE;
        commandQueue = clCreateCommandQueue(context, device, commandQueueProperties, null);
        
        logger.fine("Setting up OpenCL DONE");
    }
    
    /**
     * Release all OpenCL resources that have been created by
     * this class.
     */
    public void shutdown()
    {
    	clReleaseKernel(kernel);
    	clReleaseCommandQueue(commandQueue);
    	clReleaseContext(context);
    }
    
    /**
     * Create a kernel from the file with the given name (where ".cl" will be appended)
     *  
     * @param kernelName The kernel name
     */
    public void createKernel(String kernelName)
    {
        String fileName = kernelName+".cl";
        
        System.out.println("Creating kernel from "+fileName);

        String programSource = readFile(fileName);
        
        // Create the program
        cl_program program = clCreateProgramWithSource(context, 1, 
       		new String[] { programSource }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, kernelName, null);
        
        clReleaseProgram(program);
        
        System.out.println("Creating kernel from "+fileName+" DONE");
    }
    
    public void callKernel(int numElements, float input0[], float input1[], float output[])
    {
        System.out.println("Calling kernel");
        
        cl_mem memInput0 = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR, 
            numElements * Sizeof.cl_float, Pointer.to(input0), null);
        cl_mem memInput1 = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR, 
            numElements * Sizeof.cl_float, Pointer.to(input1), null);
        cl_mem memOutput = clCreateBuffer(context, CL_MEM_COPY_HOST_PTR, 
            numElements * Sizeof.cl_float, Pointer.to(output), null);
        
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{numElements}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(memInput0));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(memInput1));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(memOutput));
        
        long globalWorkSize[] = new long[]{numElements};
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, 
            globalWorkSize, null, 0, null, null);
        clEnqueueReadBuffer(commandQueue, memOutput, true, 0, 
            numElements * Sizeof.cl_float, 
            Pointer.to(output), 0, null, null);
        clFinish(commandQueue);

        clReleaseMemObject(memInput0);
        clReleaseMemObject(memInput1);
        clReleaseMemObject(memOutput);

        System.out.println("Calling kernel DONE"); //, result "+Arrays.toString(output));
    }
    
    /**
     * Helper function which reads the file with the given name and returns 
     * the contents of this file as a String. 
     * 
     * @param fileName The name of the file to read.
     * @return The contents of the file
     * @throws IllegalArgumentException If the file can not be read
     */
    private String readFile(String fileName)
    {
        try
        {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true)
            {
                line = br.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Could not read file", e);
        }
    }
    

}
