package groovyx.gpars.gpu.experiments

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression

/**
 * Utility methods for examining ASTNode instances
 */
class ASTUtils
{
    /**
     * Returns whether the given node is a DeclarationExpression 
     * that declares a closure
     * 
     * @param node The node
     * @return Whether the node is a closure declaration
     */
    public static boolean isClosureDeclaration(ASTNode node)
    {
        if (!(node instanceof DeclarationExpression))
        {
            return false;
        }
        DeclarationExpression expression = (DeclarationExpression)node;
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();
        if (!(left instanceof VariableExpression))
        {
            return false;
        }
        if (!(right instanceof ClosureExpression))
        {
            return false;
        }
        return true;
    }

    /**
     * Private constructor to prevent instantiation    
     */
    private ASTUtils()
    {
    }
}
