package com.zsf.interpreter.expressions;

/**
 * Created by hasee on 2017/2/3.
 */
public abstract class NonTerminalExpression extends Expression {
    public abstract String interpret(String inputString);
}
