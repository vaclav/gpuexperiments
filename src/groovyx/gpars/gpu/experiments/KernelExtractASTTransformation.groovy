package groovyx.gpars.gpu.experiments;

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * An AST transformation that is used by the {@link Kernel}
 * annotation. It checks if the AST node is a closure 
 * declaration, and tries to translate the code from the
 * closure into OpenCL code 
 */
@GroovyASTTransformation(phase=CompilePhase.CLASS_GENERATION)
public class KernelExtractASTTransformation implements ASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ASTNode node = nodes[1]
        if (ASTUtils.isClosureDeclaration(node))
        {
            println "Found DeclarationExpression for closure in AST, starting transcription"
            node.visit(new KernelExtractGroovyCodeVisitor("/Users/vaclav/work/gparsStuff/gpu"))
        }
    }
}


