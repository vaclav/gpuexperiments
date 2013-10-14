
package groovyx.gpars.gpu.experiments;

import java.util.logging.Logger
import java.util.logging.Level

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.GroovyCodeVisitor
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.classgen.BytecodeExpression

/**
 * Basic implementation of a GroovyCodeVisitor. Traverses
 * the whole AST and calls a processTYPE method at the 
 * beginning of each visitTYPE method.
 */
class BasicGroovyCodeVisitor implements GroovyCodeVisitor
{
    /**
     * The log level to use
     */
    private static final Level LEVEL = Level.FINE;
    
    /**
     * The logger used in this class
     */
    static final Logger logger =
        Logger.getLogger(BasicGroovyCodeVisitor.class.getName());

    
    @Override
    public void visitBlockStatement(BlockStatement block)
    {
        processBlockStatement(block);
        for (Statement statement : block.getStatements())
        {
            statement.visit(this);
        }
    }

    /**
     * Process the given node
     *
     * @param block The node
     */
    protected void processBlockStatement(BlockStatement block)
    {
        logger.log(LEVEL, "Calling visitBlockStatement"+" "+block);
        printCode(block);
    }

    @Override
    public void visitForLoop(ForStatement forLoop)
    {
        processForLoop(forLoop);
        forLoop.getCollectionExpression().visit(this);
        forLoop.getLoopBlock().visit(this);
    }

    /**
     * Process the given node
     *
     * @param forLoop The node
     */
    protected void processForLoop(ForStatement forLoop)
    {
        logger.log(LEVEL, "Calling visitForLoop"+" "+forLoop);
        printCode(forLoop);
    }

    @Override
    public void visitWhileLoop(WhileStatement loop)
    {
        processWhileLoop(loop);
        loop.getBooleanExpression().visit(this);
        loop.getLoopBlock().visit(this);
    }

    /**
     * Process the given node
     *
     * @param loop The node
     */
    protected void processWhileLoop(WhileStatement loop)
    {
        logger.log(LEVEL, "Calling visitWhileLoop"+" "+loop);
        printCode(loop);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop)
    {
        processDoWhileLoop(loop);
        loop.getLoopBlock().visit(this);
        loop.getBooleanExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param loop The node
     */
    protected void processDoWhileLoop(DoWhileStatement loop)
    {
        logger.log(LEVEL, "Calling visitDoWhileLoop"+" "+loop);
        printCode(loop);
    }

    @Override
    public void visitIfElse(IfStatement ifElse)
    {
        processIfElse(ifElse);
        ifElse.getBooleanExpression().visit(this);
        ifElse.getIfBlock().visit(this);

        Statement elseBlock = ifElse.getElseBlock();
        if (elseBlock instanceof EmptyStatement)
        {
            // dispatching to EmptyStatement will not call back visitor,
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) elseBlock);
        } else
        {
            elseBlock.visit(this);
        }
    }

    /**
     * Process the given node
     *
     * @param ifElse The node
     */
    protected void processIfElse(IfStatement ifElse)
    {
        logger.log(LEVEL, "Calling visitIfElse"+" "+ifElse);
        printCode(ifElse);
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement)
    {
        processExpressionStatement(statement);
        statement.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processExpressionStatement(ExpressionStatement statement)
    {
        logger.log(LEVEL, "Calling visitExpressionStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement)
    {
        processReturnStatement(statement);
        statement.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processReturnStatement(ReturnStatement statement)
    {
        logger.log(LEVEL, "Calling visitReturnStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitAssertStatement(AssertStatement statement)
    {
        processAssertStatement(statement);
        statement.getBooleanExpression().visit(this);
        statement.getMessageExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processAssertStatement(AssertStatement statement)
    {
        logger.log(LEVEL, "Calling visitAssertStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement)
    {
        processTryCatchFinally(statement);
        statement.getTryStatement().visit(this);
        for (CatchStatement catchStatement : statement.getCatchStatements())
        {
            catchStatement.visit(this);
        }
        Statement finallyStatement = statement.getFinallyStatement();
        if (finallyStatement instanceof EmptyStatement)
        {
            // dispatching to EmptyStatement will not call back visitor,
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) finallyStatement);
        } else
        {
            finallyStatement.visit(this);
        }
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processTryCatchFinally(TryCatchStatement statement)
    {
        logger.log(LEVEL, "Calling visitTryCatchFinally"+" "+statement);
        printCode(statement);
    }

    protected void visitEmptyStatement(EmptyStatement statement)
    {
        // noop
    }

    @Override
    public void visitSwitch(SwitchStatement statement)
    {
        processSwitch(statement);
        statement.getExpression().visit(this);
        for (CaseStatement caseStatement : statement.getCaseStatements())
        {
            caseStatement.visit(this);
        }
        statement.getDefaultStatement().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processSwitch(SwitchStatement statement)
    {
        logger.log(LEVEL, "Calling visitSwitch"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitCaseStatement(CaseStatement statement)
    {
        processCaseStatement(statement);
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processCaseStatement(CaseStatement statement)
    {
        logger.log(LEVEL, "Calling visitCaseStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitBreakStatement(BreakStatement statement)
    {
        processBreakStatement(statement);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processBreakStatement(BreakStatement statement)
    {
        logger.log(LEVEL, "Calling visitBreakStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement)
    {
        processContinueStatement(statement);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processContinueStatement(ContinueStatement statement)
    {
        logger.log(LEVEL, "Calling visitContinueStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement)
    {
        processSynchronizedStatement(statement);
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processSynchronizedStatement(SynchronizedStatement statement)
    {
        logger.log(LEVEL, "Calling visitSynchronizedStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement)
    {
        processThrowStatement(statement);
        statement.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processThrowStatement(ThrowStatement statement)
    {
        logger.log(LEVEL, "Calling visitThrowStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call)
    {
        processMethodCallExpression(call);
        call.getObjectExpression().visit(this);
        call.getMethod().visit(this);
        call.getArguments().visit(this);
    }

    /**
     * Process the given node
     *
     * @param call The node
     */
    protected void processMethodCallExpression(MethodCallExpression call)
    {
        logger.log(LEVEL, "Calling visitMethodCallExpression"+" "+call);
        printCode(call);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call)
    {
        processStaticMethodCallExpression(call);
        call.getArguments().visit(this);
    }

    /**
     * Process the given node
     *
     * @param call The node
     */
    protected void processStaticMethodCallExpression(StaticMethodCallExpression call)
    {
        logger.log(LEVEL, "Calling visitStaticMethodCallExpression"+" "+call);
        printCode(call);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call)
    {
        processConstructorCallExpression(call);
        call.getArguments().visit(this);
    }

    /**
     * Process the given node
     *
     * @param call The node
     */
    protected void processConstructorCallExpression(ConstructorCallExpression call)
    {
        logger.log(LEVEL, "Calling visitConstructorCallExpression"+" "+call);
        printCode(call);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression)
    {
        processBinaryExpression(expression);
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processBinaryExpression(BinaryExpression expression)
    {
        logger.log(LEVEL, "Calling visitBinaryExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression)
    {
        processTernaryExpression(expression);
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processTernaryExpression(TernaryExpression expression)
    {
        logger.log(LEVEL, "Calling visitTernaryExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression)
    {
        processShortTernaryExpression(expression);
        visitTernaryExpression(expression);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processShortTernaryExpression(ElvisOperatorExpression expression)
    {
        logger.log(LEVEL, "Calling visitShortTernaryExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression)
    {
        processPostfixExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processPostfixExpression(PostfixExpression expression)
    {
        logger.log(LEVEL, "Calling visitPostfixExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression)
    {
        processPrefixExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processPrefixExpression(PrefixExpression expression)
    {
        logger.log(LEVEL, "Calling visitPrefixExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression)
    {
        processBooleanExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processBooleanExpression(BooleanExpression expression)
    {
        logger.log(LEVEL, "Calling visitBooleanExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitNotExpression(NotExpression expression)
    {
        processNotExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processNotExpression(NotExpression expression)
    {
        logger.log(LEVEL, "Calling visitNotExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression)
    {
        processClosureExpression(expression);
        expression.getCode().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processClosureExpression(ClosureExpression expression)
    {
        logger.log(LEVEL, "Calling visitClosureExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression)
    {
        processTupleExpression(expression);
        visitListOfExpressions(expression.getExpressions());
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processTupleExpression(TupleExpression expression)
    {
        logger.log(LEVEL, "Calling visitTupleExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitListExpression(ListExpression expression)
    {
        processListExpression(expression);
        visitListOfExpressions(expression.getExpressions());
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processListExpression(ListExpression expression)
    {
        logger.log(LEVEL, "Calling visitListExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression)
    {
        processArrayExpression(expression);
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processArrayExpression(ArrayExpression expression)
    {
        logger.log(LEVEL, "Calling visitArrayExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitMapExpression(MapExpression expression)
    {
        processMapExpression(expression);
        visitListOfExpressions(expression.getMapEntryExpressions());
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processMapExpression(MapExpression expression)
    {
        logger.log(LEVEL, "Calling visitMapExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression)
    {
        processMapEntryExpression(expression);
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processMapEntryExpression(MapEntryExpression expression)
    {
        logger.log(LEVEL, "Calling visitMapEntryExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression)
    {
        processRangeExpression(expression);
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processRangeExpression(RangeExpression expression)
    {
        logger.log(LEVEL, "Calling visitRangeExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression)
    {
        processSpreadExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processSpreadExpression(SpreadExpression expression)
    {
        logger.log(LEVEL, "Calling visitSpreadExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression)
    {
        processSpreadMapExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processSpreadMapExpression(SpreadMapExpression expression)
    {
        logger.log(LEVEL, "Calling visitSpreadMapExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression)
    {
        processMethodPointerExpression(expression);
        expression.getExpression().visit(this);
        expression.getMethodName().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processMethodPointerExpression(MethodPointerExpression expression)
    {
        logger.log(LEVEL, "Calling visitMethodPointerExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression)
    {
        processUnaryMinusExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processUnaryMinusExpression(UnaryMinusExpression expression)
    {
        logger.log(LEVEL, "Calling visitUnaryMinusExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression)
    {
        processUnaryPlusExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processUnaryPlusExpression(UnaryPlusExpression expression)
    {
        logger.log(LEVEL, "Calling visitUnaryPlusExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression)
    {
        processBitwiseNegationExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processBitwiseNegationExpression(BitwiseNegationExpression expression)
    {
        logger.log(LEVEL, "Calling visitBitwiseNegationExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression)
    {
        processCastExpression(expression);
        expression.getExpression().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processCastExpression(CastExpression expression)
    {
        logger.log(LEVEL, "Calling visitCastExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression)
    {
        processConstantExpression(expression);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processConstantExpression(ConstantExpression expression)
    {
        logger.log(LEVEL, "Calling visitConstantExpression"+" "+expression);
        printCode(expression);
        //println "Constant type is "+expression.getType()
    }

    @Override
    public void visitClassExpression(ClassExpression expression)
    {
        processClassExpression(expression);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processClassExpression(ClassExpression expression)
    {
        logger.log(LEVEL, "Calling visitClassExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression)
    {
        processVariableExpression(expression);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processVariableExpression(VariableExpression expression)
    {
        logger.log(LEVEL, "Calling visitVariableExpression "+expression+" "+expression);
        printCode(expression);
        //println "Variable type is " + expression.getType()
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression)
    {
        processDeclarationExpression(expression);
        visitBinaryExpression(expression);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processDeclarationExpression(DeclarationExpression expression)
    {
        logger.log(LEVEL, "Calling visitDeclarationExpression"+" "+expression);
        printCode(expression);
    }


    @Override
    public void visitPropertyExpression(PropertyExpression expression)
    {
        processPropertyExpression(expression);
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processPropertyExpression(PropertyExpression expression)
    {
        logger.log(LEVEL, "Calling visitPropertyExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression)
    {
        processAttributeExpression(expression);
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processAttributeExpression(AttributeExpression expression)
    {
        logger.log(LEVEL, "Calling visitAttributeExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression)
    {
        processFieldExpression(expression);
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processFieldExpression(FieldExpression expression)
    {
        logger.log(LEVEL, "Calling visitFieldExpression"+" "+expression);
        printCode(expression);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression)
    {
        processGStringExpression(expression);
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    /**
     * Process the given node
     *
     * @param expression The node
     */
    protected void processGStringExpression(GStringExpression expression)
    {
        logger.log(LEVEL, "Calling visitGStringExpression"+" "+expression);
        printCode(expression);
    }

    protected void visitListOfExpressions(List<? extends Expression> list)
    {
        if (list == null) return;
        for (Expression expression : list)
        {
            if (expression instanceof SpreadExpression)
            {
                Expression spread = ((SpreadExpression) expression).getExpression();
                spread.visit(this);
            } else
            {
                expression.visit(this);
            }
        }
    }

    @Override
    public void visitCatchStatement(CatchStatement statement)
    {
        processCatchStatement(statement);
        statement.getCode().visit(this);
    }

    /**
     * Process the given node
     *
     * @param statement The node
     */
    protected void processCatchStatement(CatchStatement statement)
    {
        logger.log(LEVEL, "Calling visitCatchStatement"+" "+statement);
        printCode(statement);
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale)
    {
        processArgumentlistExpression(ale);
        visitTupleExpression(ale);
    }

    /**
     * Process the given node
     *
     * @param ale The node
     */
    protected void processArgumentlistExpression(ArgumentListExpression ale)
    {
        logger.log(LEVEL, "Calling visitArgumentlistExpression"+" "+ale);
        printCode(ale);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression cle)
    {
        processClosureListExpression(cle);
        visitListOfExpressions(cle.getExpressions());
    }

    /**
     * Process the given node
     *
     * @param cle The node
     */
    protected void processClosureListExpression(ClosureListExpression cle)
    {
        logger.log(LEVEL, "Calling visitClosureListExpression"+" "+cle);
        printCode(cle);
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression cle)
    {
        processBytecodeExpression(cle);
    }

    /**
     * Process the given node
     *
     * @param cle The node
     */
    protected void processBytecodeExpression(BytecodeExpression cle)
    {
        logger.log(LEVEL, "Calling visitBytecodeExpression"+" "+cle);
        printCode(cle);
    }
    
    /**
     * Print the code of the given node as debug information
     * 
     * @param e The node
     */
    protected void printCode(ASTNode e)
    {
        if (logger.isLoggable(LEVEL))
        {
            StringWriter writer = new StringWriter();
            groovy.inspect.swingui.AstNodeToScriptVisitor delegate = 
                new groovy.inspect.swingui.AstNodeToScriptVisitor(writer, false, true);
            e.visit(delegate);
            logger.log(LEVEL, "Code of "+e+" is \n---"+writer.toString()+"\n---\n");
        }
    }

}

