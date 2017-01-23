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

    public Regex getR1() {
        return r1;
    }

    public void setR1(Regex r1) {
        this.r1 = r1;
    }

    public Regex getR2() {
        return r2;
    }

    public void setR2(Regex r2) {
        this.r2 = r2;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return String.format("regPos(%s,%s,%d)",r1.toString(),r2.toString(),c);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RegPosExpression){
            if (((RegPosExpression) obj).getR1().equals(this.getR1())
                    && ((RegPosExpression) obj).getR2().equals(this.getR2())){
                return true;
            }
        }
        return false;
    }
}
