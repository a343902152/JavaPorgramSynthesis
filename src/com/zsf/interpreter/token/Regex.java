package com.zsf.interpreter.token;

import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代表正则token
 * Created by hasee on 2017/1/22.
 */
public class Regex {
    private String regexName;
    private String reg;
    private Pattern pattern;

    public Regex(String regexName, String reg) {
        this.regexName = regexName;
        this.reg = reg;

        this.pattern=Pattern.compile(reg);
    }

    @Override
    public String toString() {
        return String.format("%s",regexName);
    }

    public String getRegexName() {
        return regexName;
    }

    public void setRegexName(String regexName) {
        this.regexName = regexName;
    }

    public String getReg() {
        return reg;
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public List<Match> doMatch(String inputString) {
        if (pattern==null){
            return null;
        }
        List<Match> matches=new ArrayList<Match>();
        Matcher matcher=pattern.matcher(inputString);
        while (matcher.find()){
            matches.add(new Match(inputString,matcher.start(),matcher.group(),this));
        }
        return matches;
    }
}
