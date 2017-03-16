package com.zsf.flashextract.regex;

import com.zsf.flashextract.region.Region;
import com.zsf.flashextract.region.newregion.Field;
import com.zsf.interpreter.expressions.regex.DynamicRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;

import static com.zsf.interpreter.tool.StringTools.getCommonStr;
import static com.zsf.interpreter.tool.StringTools.getReversedStr;

/**
 * Created by hasee on 2017/3/16.
 */
public class RegexCommomTools {

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    public static List<Match> buildStringMatches(String inputString,List<Regex> usefulRegex) {
        List<Match> matches = new ArrayList<Match>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            List<Match> curMatcher = regex.doMatch(inputString);
            matches.addAll(curMatcher);
        }
        return matches;
    }

    public static List<Regex> deDuplication(List<List<Regex>> regexs, boolean isStartWith) {
        List<Regex> deDuplicatedList = new ArrayList<Regex>();

        List<Regex> baseRegexList = regexs.get(0);
        for (Regex baseRegex : baseRegexList) {
            boolean needAddIn = false;
            for (int j = 1; j < regexs.size(); j++) {
                List<Regex> regexList = regexs.get(j);

                for (Regex regex : regexList) {
                    if ((baseRegex.equals(regex))) {
                        needAddIn = true;
                        break;
                    }
                }
                if (!needAddIn) {
                    break;
                }
            }
            if (needAddIn) {
                if (isStartWith) {
                    baseRegex.setReg("^" + baseRegex.getReg());
                    baseRegex.setRegexName("startWith(" + baseRegex.getRegexName() + ")");
                } else {
                    baseRegex.setReg(baseRegex.getReg() + "$");
                    baseRegex.setRegexName("endWith(" + baseRegex.getRegexName() + ")");
                }
                deDuplicatedList.add(baseRegex);
            }
        }
        return deDuplicatedList;
    }

    /**
     * 获得符合所有examples的endWith语法
     *
     * @param curDeepth
     * @param maxDeepth
     * @param matches
     * @param endNode
     * @param lastRegex
     */
    public static List<Regex> buildEndWith(int curDeepth, int maxDeepth,
                                     List<Match> matches, int endNode, Regex lastRegex) {
        if (curDeepth > maxDeepth) {
            return null;
        }
        String connector = "+";
        if (curDeepth == 1) {
            connector = "";
        }
        List<Regex> regexList = new ArrayList<Regex>();
        for (int i = matches.size() - 1; i >= 0; i--) {
            Match match = matches.get(i);
            if ((match.getMatchedIndex() + match.getMatchedString().length()) == endNode) {

                Regex curRegex = new DynamicRegex(match.getRegex().getRegexName() + connector + lastRegex.getRegexName(),
                        match.getRegex().getReg() + lastRegex.getReg());
                regexList.add(curRegex);
                List<Regex> curList = buildEndWith(curDeepth + 1, maxDeepth,
                        matches, match.getMatchedIndex(),
                        curRegex);
                if (curList != null) {
                    regexList.addAll(curList);
                }
            }
        }
        return regexList;
    }

    /**
     * 获得符合所有examples的startWith语法
     *
     * @param curDeepth
     * @param maxDeepth
     * @param matches
     * @param beginNode
     * @param lastRegex
     */
    public static List<Regex> buildStartWith(int curDeepth, int maxDeepth,
                                       List<Match> matches, int beginNode, Regex lastRegex) {
        if (curDeepth > maxDeepth) {
            return null;
        }
        String connector = "+";
        if (curDeepth == 1) {
            connector = "";
        }
        List<Regex> regexList = new ArrayList<Regex>();

        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            if (match.getMatchedIndex() == beginNode) {
                Regex curRegex = new DynamicRegex(lastRegex.getRegexName() + connector + match.getRegex().getRegexName(),
                        lastRegex.getReg() + match.getRegex().getReg());
                regexList.add(curRegex);

                List<Regex> curList = buildStartWith(curDeepth + 1, maxDeepth,
                        matches, match.getMatchedIndex() + match.getMatchedString().length(),
                        curRegex);
                if (curList != null) {
                    regexList.addAll(curList);
                }
            }
        }
        return regexList;
    }

    public static List<Regex> filterUsefulSelector(List<Regex> regices, List<String> splitedLineDocument,
                                             List<Integer> positiveLineIndex, List<Integer> negataiveLineIndex) {
        List<Regex> usefulLineSelector = new ArrayList<Regex>();

        for (Regex regex : regices) {
            boolean needAddIn = true;
            for (int index : positiveLineIndex) {
                String str=splitedLineDocument.get(index);
                if (!canMatch(str,regex)) {
                    needAddIn = false;
                    break;
                }
            }
            for (int index : negataiveLineIndex) {
                String str=splitedLineDocument.get(index);
                if (canMatch(str,regex)) {
                    needAddIn = false;
                    break;
                }
            }
            if (needAddIn) {
                usefulLineSelector.add(regex);
            }
        }
        return usefulLineSelector;
    }

    /**
     * 从当前新选择的区域出发，分别向左&向右匹配相同str作为dynamicToken添加到usefulRegex中
     *
     * @param fieldsByUser
     */
    public static void addDynamicToken(String inputDocument,List<Field> fieldsByUser, List<Regex> usefulRegex) {
        Field field0=fieldsByUser.get(0);

        // 左匹配
        String textBeforeSelected = inputDocument.substring(0,field0.getBeginPos());
        if (textBeforeSelected.lastIndexOf("\n")>=0){
            textBeforeSelected=textBeforeSelected.substring(textBeforeSelected.lastIndexOf("\n")+1);
        }
        String leftCommonStr = textBeforeSelected;
//        System.out.println(textBeforeSelected);
        for (int i = 1; i < fieldsByUser.size(); i++) {
            Field field=fieldsByUser.get(i);
            leftCommonStr = getCommonStr(getReversedStr(leftCommonStr),
                    getReversedStr(inputDocument.substring(0,field.getBeginPos())));
            leftCommonStr = getReversedStr(leftCommonStr);
//            System.out.println("leftCommonStr:  " + leftCommonStr);
        }

        // 右匹配
        String textAfterSelected = inputDocument.substring(field0.getEndPos());
        if (textAfterSelected.indexOf("\n")>0){
            textAfterSelected=textAfterSelected.substring(0,textAfterSelected.indexOf("\n"));
        }
        String rightCommonStr = textAfterSelected;
        System.out.println(textAfterSelected);
        for (int i = 1; i < fieldsByUser.size(); i++) {
            Field field=fieldsByUser.get(i);
            rightCommonStr = getCommonStr(rightCommonStr,inputDocument.substring(field.getEndPos()));
//            System.out.println("rightCommonStr:  " + rightCommonStr);
        }

        Regex leftRegex = new DynamicRegex("DynamicTok(" + leftCommonStr + ")", leftCommonStr);
        Regex rightRegex = new DynamicRegex("DynamicTok(" + rightCommonStr + ")", rightCommonStr);

        usefulRegex.add(leftRegex);
        usefulRegex.add(rightRegex);
    }

    public static boolean canMatch(String text, Regex selector) {
        List<Match> matches=selector.doMatch(text);
        return matches.size() > 0;
    }
}
