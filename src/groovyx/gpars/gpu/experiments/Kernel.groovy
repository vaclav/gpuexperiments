package groovyx.gpars.gpu.experiments
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * Annotation of closures that should be translated into OpenCL
 * kernels
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.LOCAL_VARIABLE)
@GroovyASTTransformationClass("groovyx.gpars.gpu.experiments.KernelExtractASTTransformation")
public @interface Kernel {
}