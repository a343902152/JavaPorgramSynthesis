package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.Expression;

/**
 * Created by hasee on 2016/12/27.
 */
public class AbsPosExpression extends PosExpression {

    private int pos;

    public AbsPosExpression(int pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return String.format("absPos(%d)",pos);
    }

    @Override
    public Expression deepClone() {
        return new AbsPosExpression(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbsPosExpression){
            if (((AbsPosExpression) obj).getPos()==this.getPos()){
                return true;
            }
        }
        return false;
    }


    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
