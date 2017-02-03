package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.TerminalExpression;

/**
 * Created by hasee on 2017/1/22.
 */
public abstract class PosExpression extends TerminalExpression {
    @Override
    public abstract boolean equals(Object obj);

    public abstract int interpret(String inputString);
}
