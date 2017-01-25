package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.token.Regex;

/**
 * Created by hasee on 2017/1/23.
 */
public class SubString2Expression extends StringExpression {
    private String inputString;
    private Regex regex;
    private int c;

    public SubString2Expression(String inputString, Regex regex, int c) {
        this.inputString = inputString;
        this.regex = regex;
        this.c = c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubString2Expression){
            if (((SubString2Expression) obj).getRegex().getReg().equals(this.getRegex().getReg())){
                return true;
            }
        }
        return false;
    }

    @Override
    public Expression deepClone() {
        return new SubString2Expression(inputString,regex,c);
    }

    @Override
    public String toString() {
        return String.format("subStr2(%s,%d)",regex.toString(),c);
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public Regex getRegex() {
        return regex;
    }

    public void setRegex(Regex regex) {
        this.regex = regex;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }


}
