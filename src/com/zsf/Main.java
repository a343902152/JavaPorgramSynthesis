package com.zsf;

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
import javafx.util.Pair;

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
    public static DAG generateStr(String inputString, String outputString) {
        // 论文中记作W W指能产生outputString[i，j]的所有方法集合,包括constStr[s[i,j]]以及动态获得子串方法generateSubString().
        HashMap<Pair<Integer, Integer>, Set<Expression>> W = new HashMap<Pair<Integer, Integer>, Set<Expression>>();

        int len = outputString.length();
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j <= len; j++) {
                W.put(new Pair<Integer, Integer>(i, j), mergeSet(new ConstStrExpression(outputString.substring(i, j)),
                        generateSubString(inputString, outputString.substring(i, j))));
            }
        }
        W=concatResExp(W, len);
        for (int i=0;i<len;i++){
            for (int j=i+1;j<=len;j++){
                W.put(new Pair<Integer, Integer>(i, j),
                        mergeSet(W.get(new Pair<Integer, Integer>(i, j)), generateLoop(i,j,W)));
            }
        }

        Set<Expression> resExps=W.get(new Pair<Integer, Integer>(0,len));
        for (Expression exp:resExps){
            if (exp instanceof LoopExpression)
                System.out.println(exp.toString());
        }

        // TODO: 2016/12/27 return dag(....,W2)；
        return new DAG();
    }

    /**
     * 新方法：应对IBM这种跳跃式的output，可以先用Concate把o[0],o[1],o[2]连接起来
     * 之后generateLoop中只要把表达式全都一样的concate合并成一个Loop即可。
     * 为避免重复计算，就需要倒序连接exp
     */
    private static HashMap<Pair<Integer, Integer>, Set<Expression>> concatResExp(HashMap<Pair<Integer, Integer>, Set<Expression>> w, int len) {
        for (int j = len; j > 0; j--) {
            for (int i = j - 2; i >= 0; i--) {
                // TODO: 2017/1/23 注意直接这样子做会导致W急剧爆炸，必须要去重
                Set<Expression> linkedExpressions = concatenateExp(w.get(new Pair<Integer, Integer>(i, i + 1)),
                        w.get(new Pair<Integer, Integer>(i + 1, j)));

                w.put(new Pair<Integer, Integer>(i, j),
                        mergeSet(w.get(new Pair<Integer, Integer>(i, j)), linkedExpressions));
            }
        }
        return w;
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
                        mergeSet(new SubStringExpression(inputString, expression1, expression2),
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
                    res.add(new SubString2Expression(inputString,regex,count));
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
        result.add(new AbsPosExpression(k));
        result.add(new AbsPosExpression(-(inputString.length() - k)));

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

    private static List<Regex> initUsefulRegex() {
        List<Regex> regices = new ArrayList<Regex>();
        regices.add(new Regex("NumToken", "[0-9]+"));
        regices.add(new Regex("LowerToken", "[a-z]+"));
        regices.add(new Regex("UpperToken", "[A-Z]+"));
        regices.add(new Regex("TestSymbolToken", "[-]+"));

        return regices;
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

    private static Set<Expression> concatenateExp(Set<Expression> expressions1, Set<Expression> expressions2) {
        Set<Expression> linkedExpressions=new HashSet<Expression>();
        for(Expression exp1:expressions1){
            for (Expression exp2:expressions2){
                linkedExpressions.add(new ConcatenateExpression(exp1,exp2));
            }
        }
        return linkedExpressions;
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

    public static void main(String[] args) {
//        String inputString="123-abc-456-zxc";
        String inputString = "Hello World Zsf the Program Synthesis";
        String outputString="HWZPS";
        HashMap<String, String> exampleSet = new HashMap<String, String>();
        exampleSet.put(inputString, outputString);


//        List<Match> matches=buildStringMatches(inputString);

        // TODO : 程序入口，根据examples求得expression
//        generateExpressionByExamples(exampleSet);

        // TODO :每当有新的inputS，利用上面求得的expression将I->O


        // region # testCodeRegion
        generateStr(inputString, outputString);
//        generatePos(inputString,4);
//        generatePos(inputString,7);

        // endregion

    }
}
