package com.zsf.interpreter.expressions.linking;

import com.zsf.interpreter.expressions.Expression;

/**
 * Created by hasee on 2017/1/23.
 */
public class ConcatenateExpression extends LinkingExpression {

    private Expression leftExp;
    private Expression rightExp;

    public ConcatenateExpression(Expression leftExp, Expression rightExp) {
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    @Override
    public String toString() {
        return String.format("concat(%s,%s)",leftExp.toString(),rightExp.toString());
    }

    public Expression getLeftExp() {
        return leftExp;
    }

    public void setLeftExp(Expression leftExp) {
        this.leftExp = leftExp;
    }

    public Expression getRightExp() {
        return rightExp;
    }

    public void setRightExp(Expression rightExp) {
        this.rightExp = rightExp;
    }
}
