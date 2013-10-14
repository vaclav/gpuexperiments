package groovyx.gpars.gpu.experiments

import groovy.inspect.swingui.AstNodeToScriptVisitor
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.GroovyCodeVisitor
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.Statement

import java.util.logging.Level

/**
 * Implementation of a GroovyCodeVisitor that will be called when
 * a closure declaration is encountered in the AST. It will
 * translate the code from the respective AST node into an
 * OpenCL kernel.
 */
public class KernelExtractGroovyCodeVisitor extends BasicGroovyCodeVisitor implements GroovyCodeVisitor {
    private final String pathToGeneratedClFiles

    KernelExtractGroovyCodeVisitor(String pathToGeneratedClFiles) {
        this.pathToGeneratedClFiles = pathToGeneratedClFiles
    }

    @Override
    protected void processDeclarationExpression(DeclarationExpression expression) {
        if (!ASTUtils.isClosureDeclaration(expression)) {
            return;
        }

        logger.info "Found DeclarationExpression for closure"
        //printCode(expression);

        // The left expression should be a VariableExpression containing the closure name
        Expression leftExpression = expression.getLeftExpression();
        if (!(leftExpression instanceof VariableExpression)) {
            logger.info "Expected VariableExpression but found " + leftExpression.getClass()
            return;
        }
        VariableExpression variableDeclaration = leftExpression;
        String kernelVariableName = variableDeclaration.getAccessedVariable().getName()
        logger.info "Found VariableExpression with name " + kernelVariableName

        // The right expression should be a ClosureExpression
        Expression rightExpression = expression.getRightExpression();
        if (!(rightExpression instanceof ClosureExpression)) {
            logger.info "Expected ClosureExpression but found " + rightExpression.getClass()
            return;
        }
        ClosureExpression closureExpression = rightExpression;

        // The ClosureExpression should have two parameters
        Parameter[] parameters = closureExpression.getParameters();
        if (parameters.size() != 2) {
            logger.info "ClosureExpression must have 2 parameters, but has " + parameters
            return;
        }
        String parameterName0 = parameters[0].getName();
        String parameterName1 = parameters[1].getName();
        logger.info "Found ClosureExpression with parameters " + parameterName0 + " and " + parameterName1

        // Obtain the Statement from the ClosureExpression and
        // generate the OpenCL source code. We're cheating
        // here: The code is actually generated using a
        // AstNodeToScriptVisitor, which of course only
        // works for /very/ limited, special cases. A more
        // sophisticated translation has to be developed.
        Statement statement = closureExpression.getCode()
        String sourceCode =
            "float " + kernelVariableName + "_core(float " + parameterName0 + ", float " + parameterName1 + ")" + "\n" +
                    "{" + "\n" +
                    createCoreCode(statement) +
                    "}" + "\n" +
                    "" + "\n" +
                    "__kernel void " + kernelVariableName + "(" + "\n" +
                    "     int numElements," + "\n" +
                    "     __global const float *input0," + "\n" +
                    "     __global const float *input1," + "\n" +
                    "     __global float *output)" + "\n" +
                    "{" + "\n" +
                    "    int gid = get_global_id(0);" + "\n" +
                    "    if (gid < numElements)" + "\n" +
                    "    {" + "\n" +
                    "        float " + parameterName0 + " = input0[gid];" + "\n" +
                    "        float " + parameterName1 + " = input1[gid];" + "\n" +
                    "        output[gid] = " + kernelVariableName + "_core(" + parameterName0 + ", " + parameterName1 + ");" + "\n" +
                    "    }" + "\n" +
                    "}";
        logger.info "Generated code: \n" + sourceCode;

        File file = new File(pathToGeneratedClFiles + '/' + kernelVariableName + ".cl");

        logger.log(Level.SEVERE, "Absolute path of the generated .cl file: " + file.getAbsolutePath())

        logger.info "Writing kernel file " + file

        def writer = file.newWriter()
        writer << sourceCode;
        writer.close();

//        if(true) throw new RuntimeException('test')
        logger.info "Writing kernel file " + file + " DONE"
    }

    /**
     * Create the core OpenCL code from the given ASTNode.
     * See
     * <code>http://svn.codehaus.org/groovy/tags/GROOVY_1_7_6/
     *   src/main/groovy/inspect/swingui/AstNodeToScriptAdapter.groovy</code>
     * for an "inspiration" of how this might be done for the
     * general case.
     *
     * @param node The ASTNode
     * @return The core OpenCL code
     */
    private static String createCoreCode(ASTNode node) {
        logger.info "Creating core code from " + node

        StringWriter writer = new StringWriter();
        AstNodeToScriptVisitor x = new AstNodeToScriptVisitor(writer, false, true);
        node.visit(x);
        String s = writer.toString();
        String[] lines = s.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append("    " + line + ";\n");
        }
        return sb.toString();
    }
}
