package com.zsf.flashextract;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.model.Match;
import com.zsf.interpreter.model.Regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsf on 2017/2/26.
 */
public class FEMain {

    public static List<Regex> usefulRegex = initUsefulRegex();
    /**
     * 增加有效的token可以强化匹配能力
     * <p>
     * 但是每添加一个token，答案数就要乘以这个token能产生的结果数
     * token过多会导致结果爆炸增长(很容易伤几千万)
     *
     * @return
     */
    private static List<Regex> initUsefulRegex() {
        List<Regex> regexList = new ArrayList<Regex>();
        regexList.add(new Regex("SimpleNumberTok", "\\d+"));
        regexList.add(new Regex("DigitToken", "[-+]?(([0-9]+)([.]([0-9]+))?)"));
        regexList.add(new Regex("LowerToken", "[a-z]+"));
        regexList.add(new Regex("UpperToken", "[A-Z]+"));
        regexList.add(new Regex("AlphaToken", "[a-zA-Z]+"));
//        regexList.add(new Regex("WordToken","[a-z\\sA-Z]+")); // 匹配单词的token，会导致结果爆炸增长几十万倍

        // TimeToken可匹配[12:15 | 10:26:59 PM| 22:01:15 aM]形式的时间数据
        regexList.add(new Regex("TimeToken", "(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?([ ]*[aApP][mM])?"));
        // YMDToken可匹配[10/03/1979 | 1-1-02 | 01.1.2003]形式的年月日数据
        regexList.add(new Regex("YMDToken", "([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})"));
        // YMDToken2可匹配[2004-04-30 | 2004-02-29],不匹配[2004-04-31 | 2004-02-30 | 2004-2-15 | 2004-5-7]
        regexList.add(new Regex("YMDToken2", "[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))"));
        // TextDate可匹配[Apr 03 | February 28 | November 02] (PS:简化版，没处理日期的逻辑错误)
        regexList.add(new Regex("TextDate", "(Jan(uary)?|Feb(ruary)?|Ma(r(ch)?|y)|Apr(il)?|Jul(y)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)[ -]?(0[1-9]|[1-2][0-9]|3[01])"));
        regexList.add(new Regex("WhichDayToken", "(Mon|Tues|Fri|Sun)(day)?|Wed(nesday)?|(Thur|Tue)(sday)?|Sat(urday)?"));
//        regices.add(new Regex("AlphaNumToken", "[a-z A-Z 0-9]+"));

        // special tokens
        regexList.add(new Regex("TestSymbolToken", "[-]+"));
        regexList.add(new Regex("CommaToken", "[,]+"));
        regexList.add(new Regex("<","[<]+"));
        regexList.add(new Regex(">","[>]+"));
        regexList.add(new Regex("/","[/]+"));
//        regexList.add(new Regex("SpaceToken", "[ ]+")); // 加上之后就出不了结果？？
        // FIXME: 2017/2/5 如果开启这个SpTok在当前算法下会导致解过于庞大
//        regexList.add(new Regex("SpecialTokens","[ -+()\\[\\],.:]+"));

        return regexList;
    }

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    private static List<Match> buildStringMatches(String inputString) {
        // TODO: 2017/2/5 加入match次数的能力
        // TODO: 2017/2/5 加入不match的能力
        List<Match> matches = new ArrayList<Match>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            List<Match> curMatcher = regex.doMatch(inputString);
            matches.addAll(curMatcher);
        }
        return matches;
    }

    public static void main(String[] args) {
        // TODO: 2017/2/26 FilterBool的测试
        String inputDocument="<HTML>\n" +
                "<body>\n" +
                "<table>\n" +
                "<tr><td>Name</td><td>Email</td><td>Office</td></tr>\n" +
                "<tr><td>Russell Smith</td><td>Russell.Smith@contoso.com</td><td>London</td></tr>\n" +
                "<tr><td>David Jones</td><td>David.Jones@contoso.com</td><td>Manchester</td></tr>\n" +
                "<tr><td>John Cameron</td><td>John.Cameron@contoso.com</td><td>New York</td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</HTML>";
        List<String> targetLines=new ArrayList<String>();
        targetLines.add("姓名：<span class=\"name\">Ran Liu</span> <br> 职称：<span class=\"zc\">Associate Professor/Senior Engineer</span><br> 联系方式：<span class=\"lxfs\">ran.liu_cqu@qq.com</span><br> 主要研究方向:<span class=\"major\">Medical and stereo image processing; IC design; Biomedical Engineering</span><br>");
//        targetLines.add("<tr><td>David Jones</td><td>David.Jones@contoso.com</td><td>Manchester</td></tr>");

        List<Expression> expForExtractingLine=testBoolFilter(inputDocument,targetLines);

    }

    private static List<Expression> testBoolFilter(String inputDocument, List<String> targetLines) {

        for (String str:targetLines){
            List<Match> matches=buildStringMatches(str);
            System.out.println("start with:");
            buildStartWith(1,3,matches,0,"");
            System.out.println("end with:");
            buildEndWith(1,3,matches,str.length(),"");
            // TODO: 2017/2/26 dynamicToken
        }

        return null;
    }

    private static void buildEndWith(int curDeepth, int maxDeepth,
                                     List<Match> matches, int endNode, String curExpression) {
        if (curDeepth>maxDeepth){
            return;
        }
        for (int i=matches.size()-1;i>=0;i--){
            Match match=matches.get(i);
            if ((match.getMatchedIndex()+match.getMatchedString().length())==endNode){
                String curExpressionBack=curExpression;
                curExpression=match.getRegex().toString()+curExpression;
                System.out.println((curDeepth+" ")+curExpression);
                buildEndWith(curDeepth+1,maxDeepth,
                        matches,match.getMatchedIndex(),
                        curExpression);
                curExpression=curExpressionBack;
            }
        }
    }

    private static void buildStartWith(int curDeepth, int maxDeepth,
                                       List<Match> matches, int beginNode,String curExpression) {
        if (curDeepth>maxDeepth){
            return;
        }
        for (int i=0;i<matches.size();i++){
            Match match=matches.get(i);
            if (match.getMatchedIndex()==beginNode){
                String curExpressionBack=curExpression;
                curExpression+=match.getRegex().toString();
                System.out.println((curDeepth+" ")+curExpression);
                buildStartWith(curDeepth+1,maxDeepth,
                        matches,match.getMatchedIndex()+match.getMatchedString().length(),
                        curExpression);
                curExpression=curExpressionBack;
            }
        }


    }
}
