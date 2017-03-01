package com.zsf;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.linking.ConcatenateExpression;
import com.zsf.interpreter.expressions.loop.LoopExpression;
import com.zsf.interpreter.expressions.pos.*;
import com.zsf.interpreter.expressions.string.ConstStrExpression;
import com.zsf.interpreter.expressions.string.SubString2Expression;
import com.zsf.interpreter.expressions.string.SubStringExpression;
import com.zsf.interpreter.model.*;
import com.zsf.interpreter.tool.ExpScoreComparator;
import com.zsf.interpreter.tool.ExpressionComparator;
import com.zsf.interpreter.tool.RunTimeMeasurer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by hasee on 2017/3/1.
 */
public class StringProcessor {
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
     */
    public List<ResultMap> generateExpressionsByExamples(List<ExamplePair> examplePairs) {
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        for (ExamplePair pair : examplePairs) {
            String input = pair.getInputString();
            String output = pair.getOutputString();

            ResultMap resultMap= generateStr(input, output);
            resultMaps.add(resultMap);
        }
        return resultMaps;
    }


    /**
     * generate阶段要调用的函数
     * 返回一个能够从input中生产output的expressions集合
     *  @param inputString
     * @param outputString
     */
    private ResultMap generateStr(String inputString, String outputString) {
        // 论文中记作W W指能产生outputString[i，j]的所有方法集合,包括constStr[s[i,j]]以及动态获得子串方法generateSubString().
        int len = outputString.length();
        ResultMap resultMap = new ResultMap(len, len);

        RunTimeMeasurer.startTiming();
        List<Match> matches = buildStringMatches(inputString);

        for (int i = 1; i <= len; i++) {
            for (int j = 0; i + j <= len; j++) {
                String subString = outputString.substring(j, j + i);

                ExpressionGroup expressionGroup = new ExpressionGroup();
                expressionGroup.insert(generateSubString(inputString, subString, matches));
                if (needBeAddedIn(subString, inputString)) {
                    expressionGroup.insert(new ConstStrExpression(subString));
                }
                resultMap.setData(j, i + j, expressionGroup);
            }
        }
        RunTimeMeasurer.endTiming("generateSubString");

        // 上面的resultMap结合DAG就有了跳跃能力，而且无需存储中间结果(只需要存储跳跃边), 再resultMap的基础上再加上一些Loop语句就可实现全局搜索
        // DAG在选择答案时可以结合loss func+bean search极大减小搜索空间。
        // TODO: 2017/3/1 直接更新 resultMap，加入Loop
//        resultMap=newGenerateLoop(inputString,outputString,resultMap);


        // TODO: 2017/3/1 返回的是一个DAG(或者就是resultMap)
        return resultMap;
    }

    private ResultMap newGenerateLoop(String inputString, String outputString, ResultMap resultMap) {
        for (int k1 = 0; k1 < outputString.length(); k1++){
            for (int k2 = k1 + 1; k2 < outputString.length(); k2++){
                for (int k3 = k2 + 1; k3 <= outputString.length(); k3++){
                    ResultMap resultMap1=generateStr(inputString,outputString.substring(k1,k2));
                    ResultMap resultMap2=generateStr(inputString,outputString.substring(k2,k3));

                    ResultMap newResultMap=ResultMap.merge(resultMap1,resultMap2);

                    // TODO: 2017/3/1 产生Loop 暂时不做
                }
            }
        }
        return null;
    }

    /**
     * 已经在concatResExp()中处理过跳跃性的res(如首字母提取)
     * generateLoop()中只要找到是拼接起来的，而且左右表达式一致的exp即可。
     */
    private ExpressionGroup generateLoop(int passbyNode, int endNode, ResultMap resultMap, int outputLen) {
        // TODO: 2017/1/23 效率存在问题，output一旦变长，程序就运行不出来了

        ExpressionGroup outputExpressions = resultMap.getData(passbyNode, endNode);
        ExpressionGroup loopExpressions = new ExpressionGroup();
        for (Expression exp : outputExpressions.getExpressions()) {
            if (exp instanceof ConcatenateExpression) {
                // TODO: 2017/2/16 这里也许可以改为：1. 左做loop 2. 右做loop 3. 合并左右 ，这样可以解决类似“1 2 3 Bank Of China”->"1-2-3 BOC"之类的问题
                if (isSameExpression(((ConcatenateExpression) exp).getLeftExp(), ((ConcatenateExpression) exp).getRightExp())) {
//                    System.out.println("same:");
//                    System.out.println(exp);
                    LoopExpression loop = new LoopExpression(LoopExpression.LINKING_MODE_CONCATENATE, exp, passbyNode, endNode);
//                    System.out.println(loop.toString());
                    loopExpressions.insert(loop);
                    if (endNode == outputLen) {
                        LoopExpression loopToEnd = new LoopExpression(LoopExpression.LINKING_MODE_CONCATENATE, exp, passbyNode, PosExpression.END_POS);
                        loopExpressions.insert(loopToEnd);
                    }
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
     * @param matches
     */
    private ExpressionGroup generateSubString(String inputString, String targetString, List<Match> matches) {
        ExpressionGroup result = new ExpressionGroup();

        ExpressionGroup substr2Expressions = generateSubStr2(inputString, targetString);
        result.insert(substr2Expressions);

        int targetLen = targetString.length();
        for (int k = 0; k <= inputString.length() - targetLen; k++) {
            // 如果input中的某一段能够和target匹配(因为target定长，所以遍历input，每次抽取I中长度为targetLen的一段进行比较)，那么就把此时的posExpression添加到res中
            // TODO: 2017/1/22 这里可能也可以利用matches加速处理
            if (inputString.substring(k, k + targetLen).equals(targetString)) {
                List<PosExpression> res1 = generatePos(inputString, k, matches);
                List<PosExpression> res2 = generatePos(inputString, k + targetLen, matches);

                // 把找到的pos转换为subString
                for (PosExpression expression1 : res1) {
                    for (PosExpression expression2 : res2) {
                        result.insert(new SubStringExpression(expression1, expression2));
                    }
                }
                break;
            }
        }
        return result;
    }

    private ExpressionGroup generateSubStr2(String inputString, String targetString) {
        ExpressionGroup res = new ExpressionGroup();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            Matcher matcher = regex.getPattern().matcher(inputString);
            int count = 0;
            while (matcher.find()) {
                count++;
                if (matcher.group().equals(targetString)) {
                    res.insert(new SubString2Expression(regex, count));
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
     *
     * @param inputString
     * @param k
     * @param matches
     * @return 一个表示能够从input中截取出targetString的位置集合。
     * 如：input=“123-abc-456-zxc" target="abc" 那么一个有效的起点pos(即a的位置)=POS(hyphenTok(即‘-’),letterTok,1||-2)
     * 这个POS表示第一个或倒数第二个左侧为‘-’，右侧为字母的符号的位置
     */
    private List<PosExpression> generatePos(String inputString, int k, List<Match> matches) {
        List<PosExpression> result = new ArrayList<PosExpression>();
        // 首先把k这个位置(正向数底k个，逆向数第-(inputString.length()-k)个)加到res中
        if (k == 0) {
            result.add(new AbsPosExpression(k));
        }
        if (k == inputString.length()) {
            result.add(new AbsPosExpression(PosExpression.END_POS));
        }

        for (Match match:matches){
            if (match.getMatchedIndex()==k){
                result.add(new MatchStartPos(match.getRegex(),match.getCount()));
            }else if ((match.getMatchedIndex()+match.getMatchedString().length())==k){
                result.add(new MatchEndPos(match.getRegex(),match.getCount()));
            }
        }

        /**
         * 新方法：
         * TODO 重构代码
         */
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


    private List<Regex> usefulRegex = initUsefulRegex();

    /**
     * 增加有效的token可以强化匹配能力
     * <p>
     * 但是每添加一个token，答案数就要乘以这个token能产生的结果数
     * token过多会导致结果爆炸增长(很容易伤几千万)
     *
     * @return
     */
    private List<Regex> initUsefulRegex() {
        List<Regex> regexList = new ArrayList<Regex>();
        regexList.add(new Regex("SimpleNumberTok", "[0-9]+"));
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
        regexList.add(new Regex("<", "[<]+"));
        regexList.add(new Regex(">", "[>]+"));
        regexList.add(new Regex("/", "[/]+"));
//        regexList.add(new Regex("SpaceToken", "[ ]+")); // 加上之后就出不了结果？？
        // FIXME: 2017/2/5 如果开启这个SpTok在当前算法下会导致解过于庞大
//        regexList.add(new Regex("SpecialTokens","[ -+()\\[\\],.:]+"));

        return regexList;
    }

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    private List<Match> buildStringMatches(String inputString) {
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

    /**
     * same的定义：
     * constStr要求str相同
     * 普通Expression要求token相同
     * linkingExpression要求左右两边的普通Expression相同(如果linkingExpression左右均为LinkingExpression，)
     * <p>
     * FIXME: 现在只做了substr2的equals
     *
     * @param leftExp
     * @param rightExp
     * @return
     */
    private boolean isSameExpression(Expression leftExp, Expression rightExp) {
        // FIXME: 2017/2/5 现在(包括论文里)不能处理以下 这种LOOP:
        // FIXME concat(subStr2(SimpleNumberTok,1),concat(constStr(-),concat(subStr2(SimpleNumberTok,2),concat(constStr(-),subStr2(SimpleNumberTok,3)))))
        Expression left = leftExp.deepClone();
        Expression right = rightExp.deepClone();
        if (leftExp instanceof ConcatenateExpression) {
            if (isSameExpression(((ConcatenateExpression) leftExp).getLeftExp(),
                    ((ConcatenateExpression) leftExp).getRightExp())) {
                while (((ConcatenateExpression) leftExp).getLeftExp() instanceof ConcatenateExpression) {
                    leftExp = ((ConcatenateExpression) leftExp).getLeftExp();
                }
                left = ((ConcatenateExpression) leftExp).getLeftExp().deepClone();
            } else {
                return false;
            }
        }
        if (rightExp instanceof ConcatenateExpression) {
            if (isSameExpression(((ConcatenateExpression) rightExp).getLeftExp(),
                    ((ConcatenateExpression) rightExp).getRightExp())) {
                while (((ConcatenateExpression) rightExp).getLeftExp() instanceof ConcatenateExpression) {
                    rightExp = ((ConcatenateExpression) rightExp).getLeftExp();
                }
                right = ((ConcatenateExpression) rightExp).getLeftExp().deepClone();
            } else {
                return false;
            }
        }
        if (left instanceof SubString2Expression) {
            return ((SubString2Expression) left).loopEquals(right);
        } else {
            return false;
        }
    }


    private boolean needBeAddedIn(String subString, String inputString) {
        // 如果是原字符串中存在的str，那么就不需要添加(可能会有特例，需要注意一下)
        // TODO: 2017/2/2 (在最终调整之前不修改这个，以防万一)字符串是否存在要修改一下 ，去掉subString的分隔符，然后用LSC比较subString是否全都出现过
        boolean existedString = inputString.indexOf(subString) >= 0;

        // 如果是分界符，那么就添加进去
        // FIXME: 2017/2/3 delimiterReg增大会导致答案急剧增多(比如在[-,]+时为10W个，增大到[-, ]+时可能就会有1000W个)，还不知道怎么解决
        String delimiterReg = "[-,]+";
        return !existedString || subString.matches(delimiterReg);
    }

    /**
     * 找到当前String应该所属的分类(取代了论文中的classifier)
     *
     * @param string
     * @param partitions
     * @return
     */
    private int lookupPartitionIndex(String string, List<ExamplePartition> partitions) {
        int index = -1;
        double maxScore = -1;
        for (int i = 0; i < partitions.size(); i++) {
            Double curScore = partitions.get(i).calculateSimilarity(string);
            if (curScore > maxScore) {
                maxScore = curScore;
                index = i;
            }
        }
        return index;
    }

    /**
     * 根据example得到partitions之后可以开始处理新的输入
     * 首先在partition找到newInput所属的分类
     * 然后再此分类中找出topN的表达式
     * <p>
     * 然后输出执行结果(以及用这个公式的概率)
     *
     * @param newInput
     * @param partitions
     */
    private ExpressionGroup predictOutput(String newInput, List<ExamplePartition> partitions) {
        int partitionIndex = lookupPartitionIndex(newInput, partitions);

        System.out.println("==========所属partition=" + partitionIndex + " ==========");
        ExamplePartition partition = partitions.get(partitionIndex);

        ExpressionGroup topNExpression = getTopNExpressions(partition, newInput, 5000000);

        return topNExpression;
    }

    /**
     * 找出rank得分最高的n个Expression，从前往后排序
     * 【需要一个有效的rank】
     *
     * @param partition
     * @param testString
     * @param n          取出rank前n的结果
     * @return
     */
    private ExpressionGroup getTopNExpressions(ExamplePartition partition, String testString, int n) {
        ExpressionGroup topN = new ExpressionGroup();
        // TODO: 2017/2/6 等待rank算法
        List<Expression> expressions = partition.getUsefulExpression().getExpressions();
        Collections.sort(expressions, new ExpressionComparator());
        int count = 1;
        for (Expression exp : expressions) {
            topN.insert(exp);
            if (count++ >= n) {
                break;
            }
        }
        return topN;
    }

    /**
     * 在得到partitions划分之后，依次处理每一个input并显示
     *
     * @param validationPairs
     */
    public void handleNewInput(List<ValidationPair> validationPairs,ExpressionGroup expressionGroup) {
        for (ValidationPair v : validationPairs) {
            displayOutput(v, expressionGroup);
        }
    }

    /**
     * 显示预测的结果
     * 根据需要可以切换
     *
     * @param v
     * @param topNExpression
     */
    private void displayOutput(ValidationPair v, ExpressionGroup topNExpression) {
        System.out.println("期望输出：" + v.getTargetString());
        for (Expression expression : topNExpression.getExpressions()) {
            if (expression instanceof NonTerminalExpression) {
//                if (v.getTargetString().equals(((NonTerminalExpression) expression).interpret(v.getInputString())))
                    System.out.println(((NonTerminalExpression) expression).interpret(v.getInputString()) + " , " + expression.toString());
            }
        }
    }

    public ExpressionGroup selectTopKExps(List<ResultMap> resultMaps,int k) {
        if (resultMaps==null&&resultMaps.size()<=0){
            return null;
        }
        List<ExpressionGroup> ansList=new ArrayList<ExpressionGroup>();
        for (ResultMap resultMap:resultMaps){
            ExpressionGroup g=doSelectTopKExps(resultMap,0,resultMap.getCol(),k);
            ansList.add(g);
            for (Expression e:g.getExpressions()){
                System.out.println(e.deepth()+"  "+e.score()+"  "+e.toString());
            }
        }
        ExpressionGroup validExpressions=new ExpressionGroup();
        validExpressions=ansList.get(0);
        return validExpressions;
    }

    /**
     * 应对IBM这种跳跃式的output，可以先用Concate把o[0],o[1],o[2]连接起来
     * 每次用beam search保留前k个答案
     * <p>
     * 算法思想：dfs
     */
    private ExpressionGroup doSelectTopKExps(ResultMap resultMap, int start, int end, int k) {
        if (start + 1 == end) {
            return resultMap.getData(start,end);
        }
        ExpressionGroup newExpressions = resultMap.getData(start, end).deepClone();
        for (int j = start + 1; j < end; j++) {
            ExpressionGroup curExpressions = resultMap.getData(start, j);
            if (curExpressions.size() > 0) {
                ExpressionGroup topExpressionGroup=curExpressions.selecTopK(k);
                ExpressionGroup tmpConcatedExps = ConcatenateExpression.concatenateExp(topExpressionGroup, doSelectTopKExps(resultMap, j, end,k));
                newExpressions.insert(tmpConcatedExps);
                newExpressions=newExpressions.selecTopK(k);
            }
        }
        return newExpressions.selecTopK(k);
    }
}
