package com.zsf;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.zsf.interpreter.expressions.*;
import com.zsf.interpreter.expressions.linking.ConcatenateExpression;
import com.zsf.interpreter.expressions.loop.LoopExpression;
import com.zsf.interpreter.expressions.pos.AbsPosExpression;
import com.zsf.interpreter.expressions.pos.PosExpression;
import com.zsf.interpreter.expressions.pos.RegPosExpression;
import com.zsf.interpreter.expressions.string.ConstStrExpression;
import com.zsf.interpreter.expressions.string.SubString2Expression;
import com.zsf.interpreter.expressions.string.SubStringExpression;
import com.zsf.interpreter.model.Match;
import com.zsf.interpreter.token.Regex;
import com.zsf.interpreter.tool.RunTimeMeasurer;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

public class Main {


    /**
     * 根据I->O的examples，利用generateStr()+generatePatrition()...得到能够正确处理I->0转换的表达式
     * 整个过程类似中缀表达式求值：中缀表达式->后缀表达式->求值
     * 对应本程序的inputString->[★根据examples得到的expression★]->outputString
     * <p>
     * 难点：
     * 1. 如何找到能够正确映射I->0的表达式集合？
     * 2. 如何给这些表达式排序找出最优解？
     * <p>
     * generateExpressionByEaxmples求得expression之后返回这个表达式，之后的所有I利用这个E来求得O即可
     *
     * @param exampleSet
     */
    public static void generateExpressionByExamples(HashMap<String, String> exampleSet) {

    }

    /**
     * generate阶段要调用的函数
     * 返回一个能够从input中生产output的expressions集合
     *
     * @param inputString
     * @param outputString
     */
    public static Set<Expression> generateStr(String inputString, String outputString) {
        // 论文中记作W W指能产生outputString[i，j]的所有方法集合,包括constStr[s[i,j]]以及动态获得子串方法generateSubString().
        HashMap<Pair<Integer, Integer>, Set<Expression>> W = new HashMap<Pair<Integer, Integer>, Set<Expression>>();

        RunTimeMeasurer.startTiming();
        int len = outputString.length();
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j <= len; j++) {
                String subString=outputString.substring(i,j);
                if (needBeAddedIn(subString,inputString)){
                    W.put(new Pair<Integer, Integer>(i, j), mergeSet(new ConstStrExpression(outputString.substring(i, j)),
                            generateSubString(inputString, outputString.substring(i, j))));
                }else{
                    W.put(new Pair<Integer, Integer>(i,j),generateSubString(inputString,subString));
                }

            }
        }
        RunTimeMeasurer.endTiming("generateSubString");

        // FIXME: 2017/2/3 此方法过于耗时，当item数和每个item的长度增加时，解会爆炸增长
        // FIXME: 2017/2/3 初步推测这和constStr过多有关
        RunTimeMeasurer.startTiming();
        W.put(new Pair<Integer, Integer>(0,len),concatResExp(W, 0,len));
        RunTimeMeasurer.endTiming("concatResExp");


        RunTimeMeasurer.startTiming();
        // FIXME: 2017/1/25 BUG:如果类似IBMHW，输出为IBM,HW，其中IBM是一个Loop，HW是一个LOOP但是现在程序不能产生这种Loop
        // FIXME 原因应该是处在generateLoop的位置，应该和论文一样把他放到generateStr的每个循环中
        // FIXME: 2017/2/3 当前的方法也比较耗时(约为concatRes的20%)
        for (int i=0;i<len;i++){
            for (int j=i+1;j<=len;j++){
                W.put(new Pair<Integer, Integer>(i, j),
                        mergeSet(W.get(new Pair<Integer, Integer>(i, j)), generateLoop(i,j,W)));
            }
        }
        RunTimeMeasurer.endTiming("generateLoop");

        // TODO: 2016/12/27 return dag(....,W2)；
        Set<Expression> resExps=W.get(new Pair<Integer, Integer>(0,len));
        return resExps;
    }


    /**
     * 新方法：应对IBM这种跳跃式的output，可以先用Concate把o[0],o[1],o[2]连接起来
     * 之后generateLoop中只要把表达式全都一样的concate合并成一个Loop即可。
     *
     * 算法思想：dfs
     */
    private static Set<Expression> concatResExp(HashMap<Pair<Integer, Integer>, Set<Expression>> w, int start,int end) {
        // TODO: 2017/1/25 需要修改concat的规则，比如两个constStr合并应该可以直接变成一个constStr

        if (start+1==end){
            return w.get(new Pair<Integer, Integer>(start,end));
        }
        Set<Expression> newExpressions=new HashSet<Expression>();
        newExpressions=mergeSet(newExpressions,w.get(new Pair<Integer, Integer>(start,end)));
        for (int j=start+1;j<end;j++){
            Set<Expression> curExpressions=w.get(new Pair<Integer, Integer>(start,j));
            if (curExpressions.size()>0){
                Set<Expression> anss=ConcatenateExpression.concatenateExp(curExpressions,concatResExp(w,j,end));
                newExpressions=mergeSet(newExpressions,anss);
            }
        }

        return newExpressions;
    }

    /**
     * 已经在concatResExp()中处理过跳跃性的res(如首字母提取)
     * generateLoop()中只要找到是拼接起来的，而且左右表达式一致的exp即可。
     */
    private static Set<Expression> generateLoop(int passbyNode, int endNode, HashMap<Pair<Integer, Integer>, Set<Expression>> W){
        // TODO: 2017/1/23 效率存在问题，output一旦变长，程序就运行不出来了

        Set<Expression> outputExpressions=W.get(new Pair<Integer, Integer>(passbyNode,endNode));
        Set<Expression> loopExpressions=new HashSet<Expression>();
        for (Expression exp:outputExpressions){
            if(exp instanceof ConcatenateExpression){
                if(isSameExpression(((ConcatenateExpression) exp).getLeftExp(),((ConcatenateExpression) exp).getRightExp())){
//                    System.out.println("same:");
//                    System.out.println(exp);
                    LoopExpression loop= new LoopExpression(LoopExpression.LINKING_MODE_CONCATENATE,exp,passbyNode,endNode);
//                    System.out.println(loop.toString());
                    loopExpressions.add(loop);
                }
            }
        }
        return loopExpressions;
    }

    /**
     * 返回从intputString中得到target的所有方式
     * 如：
     * inputString=123-456-123,targetString=123
     * 那么就返回s[0：3]+s[-3：-1]...
     * <p>
     * 返回一组对subStr(s,p1,p2)方法的引用，其中p1,p2则是通过generatePos()得到。
     *
     * @param inputString  输入数据
     * @param targetString 要从intputString中截取的字符串
     */
    public static Set<Expression> generateSubString(String inputString, String targetString) {
        Set<Expression> result = new HashSet<Expression>();

        Set<Expression> substr2Expressions=generateSubStr2(inputString,targetString);
        mergeSet(result,substr2Expressions);

        int targetLen = targetString.length();
        for (int k = 0; k < inputString.length() - targetLen; k++) {
            // 如果input中的某一段能够和target匹配(因为target定长，所以遍历input，每次抽取I中长度为targetLen的一段进行比较)，那么就把此时的posExpression添加到res中
            // TODO: 2017/1/22 这里可能也可以利用matches加速处理
            if (inputString.substring(k, k + targetLen).equals(targetString)) {
                Set<PosExpression> res1 = generatePos(inputString, k);
                Set<PosExpression> res2 = generatePos(inputString, k + targetLen);

                // 把找到的pos转换为subString
                for (PosExpression expression1 : res1) {
                    for (PosExpression expression2 : res2) {
                        mergeSet(new SubStringExpression(expression1, expression2),
                                result);
                    }
                }
                break;
            }
        }
        return result;
    }
    private static Set<Expression> generateSubStr2(String inputString, String targetString) {
        Set<Expression> res=new HashSet<Expression>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            Matcher matcher=regex.getPattern().matcher(inputString);
            int count=0;
            while (matcher.find()){
                count++;
                if (matcher.group().equals(targetString)){
                    res.add(new SubString2Expression(regex,count));
                }
            }
        }
        return res;
    }

    /**
     * 返回一组能够取得相应位置的'表达式!'，如Pos(r1,r2,c),其中r是正则表达式(在这里是token)，c代表第c个匹配
     * <p>
     * 注：r=TokenSeq(T1,T2..Tn)表示Str要符合[T1]+[T2]+...[Tn]+这种形式
     * 如：r=TokenSea(num，letter)，那么str必须是123abc或1Abb这种形式才能和r匹配
     * <p>
     * 所以
     *
     * @param inputString
     * @param k
     * @return 一个表示能够从input中截取出targetString的位置集合。
     * 如：input=“123-abc-456-zxc" target="abc" 那么一个有效的起点pos(即a的位置)=POS(hyphenTok(即‘-’),letterTok,1||-2)
     * 这个POS表示第一个或倒数第二个左侧为‘-’，右侧为字母的符号的位置
     */
    public static Set<PosExpression> generatePos(String inputString, int k) {
        Set<PosExpression> result = new HashSet<PosExpression>();
        // 首先把k这个位置(正向数底k个，逆向数第-(inputString.length()-k)个)加到res中
        if (k==0){
            result.add(new AbsPosExpression(k));
        }
        if (k==inputString.length()){
            result.add(new AbsPosExpression(PosExpression.END_POS));
        }

        /**
         * 新方法：
         * TODO 重构代码
         */
        List<Match> matches = buildStringMatches(inputString);
        for (int k1 = k - 1; k1 >= 0; k1--) {
            for (int m1 = 0; m1 < matches.size(); m1++) {
                Match match1 = matches.get(m1);
                // TODO 确定r1，把if改成TokenSeq形式，要能根据match的起点和终点进行跳跃
                if (match1.getMatchedIndex() == k1 && (match1.getMatchedIndex() + match1.getMatchedString().length()) == k) {
                    Regex r1 = match1.getRegex();
                    for (int k2 = k + 1; k2 <= inputString.length(); k2++) {
                        for (int m2 = 0; m2 < matches.size(); m2++) {
                            Match match2 = matches.get(m2);
                            // TODO 确定r2，把if改成TokenSeq形式，要能根据match的起点和终点进行跳跃
                            if (match2.getMatchedIndex() == k && (k + match2.getMatchedString().length()) == k2) {
                                Regex r2 = match2.getRegex();

                                // TODO: 2017/1/22 用更好的方法合并r1和r2
                                Regex r12 = new Regex("r12", r1.getReg() + r2.getReg());
                                List<Match> totalMatches = r12.doMatch(inputString);
                                int curOccur = -1;
                                String sk1k2 = inputString.substring(k1, k2);
                                for (int i = 0; i < totalMatches.size(); i++) {
                                    if (sk1k2.equals(totalMatches.get(i).getMatchedString())) {
                                        curOccur = i + 1;
                                        break;
                                    }
                                }
                                result.add(new RegPosExpression(r1, r2, curOccur));
                                result.add(new RegPosExpression(r1, r2, -(totalMatches.size() - curOccur + 1)));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private static List<Regex> usefulRegex = initUsefulRegex();

    /**
     * 增加有效的token可以强化匹配能力
     * @return
     */
    private static List<Regex> initUsefulRegex() {
        List<Regex> regexList = new ArrayList<Regex>();
        regexList.add(new Regex("SimpleNumberTok","[0-9]+"));
        regexList.add(new Regex("DigitToken", "(([0-9]+)([.]([0-9]+))?)"));
        regexList.add(new Regex("LowerToken", "[a-z]+"));
        regexList.add(new Regex("UpperToken", "[A-Z]+"));
        regexList.add(new Regex("AlphaToken", "[a-zA-Z]+"));

        // TimeToken可匹配[12:15 | 10:26:59 PM| 22:01:15 aM]形式的时间数据
        regexList.add(new Regex("TimeToken","(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?([ ]*[aApP][mM])?"));
        // YMDToken可匹配[10/03/1979 | 1-1-02 | 01.1.2003]形式的年月日数据
        regexList.add(new Regex("YMDToken","([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})"));
        // YMDToken2可匹配[2004-04-30 | 2004-02-29],不匹配[2004-04-31 | 2004-02-30 | 2004-2-15 | 2004-5-7]
        regexList.add(new Regex("YMDToken2","[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))"));
        // TextDate可匹配[Apr 03 | February 28 | November 02] (PS:简化版，没处理日期的逻辑错误)
        regexList.add(new Regex("TextDate","(Jan(uary)?|Feb(ruary)?|Ma(r(ch)?|y)|Apr(il)?|Jul(y)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)[ -]?(0[1-9]|[1-2][0-9]|3[01])"));
        regexList.add(new Regex("WhichDayToken","(Mon|Tues|Fri|Sun)(day)?|Wed(nesday)?|(Thur|Tue)(sday)?|Sat(urday)?"));
//        regices.add(new Regex("AlphaNumToken", "[a-z A-Z 0-9]+"));

        // special tokens
        regexList.add(new Regex("TestSymbolToken", "[-]+"));
        regexList.add(new Regex("CommaToken", "[,]+"));
//        regexList.add(new Regex("SpaceToken", "[ ]+")); // 加上之后就出不了结果？？
//        regexList.add(new Regex("spcialTokens","[-+()[],.:]+"));

        return regexList;
    }

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    private static List<Match> buildStringMatches(String inputString) {
        List<Match> matches = new ArrayList<Match>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            List<Match> curMatcher = regex.doMatch(inputString);
            // TODO: 2017/1/22 addAll时要做一个去重复
            matches.addAll(curMatcher);
        }
        return matches;
    }

    /**
     * 注：r=TokenSeq(T1,T2..Tn)表示Str要符合[T1]+[T2]+...[Tn]+这种形式
     * 如：r=TokenSea(num，letter)，那么str必须是123abc或1Abb这种形式才能和r匹配
     * TokenSeq中可能存在的token，如多次123abc456zxc会表示成{numToken，letterToken，numtoken，letterToken}形势，
     * 上面例子得到的经过本方法去重复之后得到{numtoken，letterToken}
     * <p>
     * △ 但是：123abc456->{numToken，letterToken，numtoken}不可以变成{numToken，letterToken}
     * <p>
     * 不太理解，不知道是不是可以略去
     * <p>
     * 对于一个某次字符串s匹配，token1和token2会取得一样的效果，此时token1和token2就没有区别(indistinguishable)
     *
     * @param regExpression
     * @param inputString
     */
    public static void generateRegex(RegExpression regExpression, String inputString) {

    }

    /**
     * same的定义：
     * constStr要求str相同
     * 普通Expression要求token相同
     * linkingExpression要求左右两边的普通Expression相同(如果linkingExpression左右均为LinkingExpression，)
     * @param leftExp
     * @param rightExp
     * @return
     */
    private static boolean isSameExpression(Expression leftExp, Expression rightExp) {
        Expression left=leftExp;
        Expression right=rightExp;
        if (leftExp instanceof ConcatenateExpression){
            if (isSameExpression(((ConcatenateExpression) leftExp).getLeftExp(),
                    ((ConcatenateExpression) leftExp).getRightExp())){
                while (((ConcatenateExpression) leftExp).getLeftExp() instanceof ConcatenateExpression){
                    leftExp=((ConcatenateExpression) leftExp).getLeftExp();
                }
                left=((ConcatenateExpression) leftExp).getLeftExp();
            }else {
                return false;
            }
        }
        if (rightExp instanceof ConcatenateExpression){
            if (isSameExpression(((ConcatenateExpression) rightExp).getLeftExp(),
                    ((ConcatenateExpression) rightExp).getRightExp())){
                while (((ConcatenateExpression) rightExp).getLeftExp() instanceof ConcatenateExpression){
                    rightExp=((ConcatenateExpression) rightExp).getLeftExp();
                }
                right=((ConcatenateExpression) rightExp).getLeftExp();
            }else {
                return false;
            }
        }
        return left.equals(right);
    }


    private static boolean needBeAddedIn(String subString, String inputString) {
        // 如果是原字符串中存在的str，那么就不需要添加(可能会有特例，需要注意一下)
        // TODO: 2017/2/2 (在最终调整之前不修改这个，以防万一)字符串是否存在要修改一下 ，去掉subString的分隔符，然后用LSC比较subString是否全都出现过
        boolean existedString = inputString.indexOf(subString) >= 0;

        // 如果是分界符，那么就添加进去
        // FIXME: 2017/2/3 delimiterReg增大会导致答案急剧增多(比如在[-,]+时为10W个，增大到[-, ]+时可能就会有1000W个)，还不知道怎么解决
        String delimiterReg = "[-,]+";
        return !existedString || subString.matches(delimiterReg);
    }

    /**
     * 把表达式整合起来，能够去重复
     * <p>
     * 要求：
     * 1. 去掉直接重复
     * 2. 去掉间接(值恒相等)重复
     *
     * @param set1
     * @param set2
     * @return 合并后的集合
     */
    public static Set<Expression> mergeSet(Set<Expression> set1, Set<Expression> set2) {
        // TODO: 2017/1/23 null还没彻底检查，现在默认set1不为空，set2有可能为空
        if(set1!=null&&set2!=null) {
            set1.addAll(set2);
        }
        return set1;
    }

    /**
     * 合并单个表达式和表达式集合
     *
     * @param expression 新添加的单个表达式
     * @param set        已有的表达式集合
     * @return
     */
    public static Set<Expression> mergeSet(Expression expression, Set<Expression> set) {
        if(expression!=null){
            set.add(expression);
        }
        return set;
    }

    private static void verifyResult(Set<Expression> resExps, String testString, String target) {
        System.out.println(resExps.size());
        try {
            FileWriter fileWriter = new FileWriter("C:\\Users\\hasee\\Desktop\\tempdata\\string-processor\\ans.txt");
            for (Expression exp:resExps){
//            if (exp instanceof LoopExpression)
//                System.out.println(exp.toString());
                if (exp instanceof NonTerminalExpression){
                    String result=((NonTerminalExpression) exp).interpret(testString);
                    if (result.equals(target)){
                        if (exp.deepth()<=4)
                            System.out.println(String.valueOf(exp.deepth())+" "+exp.toString());
                    }
                }
                fileWriter.write(exp.toString());
                fileWriter.write("\n");
            }
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 对于提取IBM形式的句子，最后W的规模大致为3*(len(o))^2
        // 其他的subStr问题W的规模会小很多
//        String inputString="Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012";
//        String inputString = "Hello World Zsf the Program Synthesis Intellij Idea";
        String inputString="2017年2月4日";
//        String outputString="HWZPSII";
        String outputString="2";
        // FIXME 当前concatExp算法为指数型函数，一旦output中item数(比如用逗号隔开)增加以及每个item的长度变长，计算时间会爆炸增长。
        // FIXME: 2017/2/3 初步估计每个item延长一位会让concatResExp耗时翻倍，每增加一个item，就会导致concatResExp耗时乘以n倍
//        String outputString="Electronics Store,18:08:57,abcd,Apr 03";
        HashMap<String, String> exampleSet = new HashMap<String, String>();
        exampleSet.put(inputString, outputString);

        // TODO : 程序入口，根据examples求得expression
//        generateExpressionByExamples(exampleSet);

        // TODO :每当有新的inputS，利用上面求得的expression将I->O
        // region # testCodeRegion
        Set<Expression> resExps=generateStr(inputString, outputString);
        String testString="2002.02.28";
        String target="02";
        // TODO 输出所有结果，等待排序
        if (true){
            verifyResult(resExps, testString, target);
        }
        // endregion

    }
}
