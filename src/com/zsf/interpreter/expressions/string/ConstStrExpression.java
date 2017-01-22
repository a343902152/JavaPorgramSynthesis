package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;

/**
 * Created by hasee on 2016/12/27.
 */
public class ConstStrExpression extends Expression {
    private String constStr;

    public ConstStrExpression(String constStr) {
        this.constStr = constStr;
    }

    @Override
    public String toString() {
        return constStr;
    }
}
