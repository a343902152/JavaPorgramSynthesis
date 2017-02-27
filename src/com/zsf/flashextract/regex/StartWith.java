package com.zsf.flashextract.regex;

import com.zsf.interpreter.model.Regex;

/**
 * Created by hasee on 2017/2/27.
 */
public class StartWith extends Regex implements LineSelector {
    public StartWith(String regexName, String reg) {
        super(regexName, reg);
    }
}
