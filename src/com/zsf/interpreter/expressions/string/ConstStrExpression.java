package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;

/**
 * Created by hasee on 2016/12/27.
 */
public class ConstStrExpression extends StringExpression {
    private String constStr;

    public ConstStrExpression(String constStr) {
        this.constStr = constStr;
    }

    public String getConstStr() {
        return constStr;
    }

    public void setConstStr(String constStr) {
        this.constStr = constStr;
    }

    @Override
    public String toString() {
        return String.format("constStr(%s)",constStr);
    }

}
