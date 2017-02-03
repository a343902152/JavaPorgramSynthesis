package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.model.Match;
import com.zsf.interpreter.token.Regex;

import java.util.List;

/**
 * Created by hasee on 2017/1/23.
 */
public class SubString2Expression extends StringExpression {
    private Regex regex;
    private int c;

    public SubString2Expression(Regex regex, int c) {
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
        return new SubString2Expression(regex,c);
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public String toString() {
        return String.format("subStr2(%s,%d)",regex.toString(),c);
    }

    @Override
    public String interpret(String inputString) {
        List<Match> matches=regex.doMatch(inputString);
        String ans="";
        if (c-1<matches.size()){
            ans=matches.get(c-1).getMatchedString();
        }else {
            ans=toString()+" is null";
        }
        return ans;
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
