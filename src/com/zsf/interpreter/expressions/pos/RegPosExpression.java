package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.pos.PosExpression;
import com.zsf.interpreter.token.Regex;

/**
 * Created by hasee on 2017/1/22.
 */
public class RegPosExpression extends PosExpression {

    private Regex r1;
    private Regex r2;
    private int c;

    public RegPosExpression(Regex r1, Regex r2, int c) {
        this.r1 = r1;
        this.r2 = r2;
        this.c = c;
    }

    @Override
    public String toString() {
        return null;
    }
}
