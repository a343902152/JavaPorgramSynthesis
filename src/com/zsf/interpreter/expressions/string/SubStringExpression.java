package com.zsf.interpreter.expressions.string;

import com.sun.deploy.util.StringUtils;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.pos.PosExpression;
import com.zsf.interpreter.token.Regex;

/**
 * Created by hasee on 2017/1/22.
 */
public class SubStringExpression extends StringExpression {
    private String inputString;
    private PosExpression posExpression1;
    private PosExpression posExpression2;

    public SubStringExpression(String inputString, PosExpression posExpression1, PosExpression posExpression2) {
        this.inputString = inputString;
        this.posExpression1 = posExpression1;
        this.posExpression2 = posExpression2;
    }

    @Override
    public String toString() {
        return String.format("substr(%s,%s)", posExpression1.toString(), posExpression2.toString());
    }

    @Override
    public Expression deepClone() {
        return new SubStringExpression(inputString, (PosExpression) posExpression1.deepClone(), (PosExpression) posExpression2.deepClone());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public PosExpression getPosExpression1() {
        return posExpression1;
    }

    public void setPosExpression1(PosExpression posExpression1) {
        this.posExpression1 = posExpression1;
    }

    public PosExpression getPosExpression2() {
        return posExpression2;
    }

    public void setPosExpression2(PosExpression posExpression2) {
        this.posExpression2 = posExpression2;
    }
}
