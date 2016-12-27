package com.zsf;

import com.zsf.interpreter.*;
import com.zsf.interpreter.token.Token;
import javafx.util.Pair;
import sun.security.x509.URIName;

import java.util.*;

public class Main {


    /**
     * 根据I->O的examples，利用generateStr()+generatePatrition()...得到能够正确处理I->0转换的表达式
     * 整个过程类似中缀表达式求值：中缀表达式->后缀表达式->求值
     * 对应本程序的inputString->[★根据examples得到的expression★]->outputString
     *
     * 难点：
     * 1. 如何找到能够正确映射I->0的表达式集合？
     * 2. 如何给这些表达式排序找出最优解？
     *
     * generateExpressionByEaxmples求得expression之后返回这个表达式，之后的所有I利用这个E来求得O即可
     *
     * @param exampleSet
     */
    public static void generateExpressionByExamples(HashMap<String,String> exampleSet){

    }

    /**
     * generate阶段要调用的函数
     * 返回一个能够从input中生产output的expressions集合
     * @param inputString
     * @param outputString
     */
    public static void generateStr(String inputString,String outputString){
        // 论文中记作W W指能产生outputString[i，j]的所有方法集合,包括constStr[s[i,j]]以及动态获得子串方法generateSubString().
        HashMap<Pair<Integer,Integer>,Set<Expression>> W=new HashMap<Pair<Integer, Integer>, Set<Expression>>();

        int len=outputString.length();
        for(int i=0;i<len-1;i++){
            for (int j=i+1;j<len;j++){
                W.put(new Pair<Integer, Integer>(i,j),mergeSet(new ConstStrExpression(outputString.substring(i,j)),
                        generateSubString(inputString,outputString.substring(i,j))));
            }
        }
        // TODO: 2016/12/27 W->W'，利用循环去重复
//        HashMap<Pair<Integer,Integer>,Set<Expression>> W2=generateLoop(inputString,outputString,W);

        // TODO: 2016/12/27 return dag(....,W2)；
    }

    /**
     * 把表达式整合起来，能够去重复
     *
     * 要求：
     * 1. 去掉直接重复
     * 2. 去掉间接(值恒相等)重复
     * @param set1
     * @param set2
     * @return 合并后的集合
     */
    public static Set<Expression> mergeSet(Set<Expression> set1,Set<Expression> set2){

        return null;
    }

    /**
     * 合并单个表达式和表达式集合
     * @param expression 新添加的单个表达式
     * @param set 已有的表达式集合
     * @return
     */
    public static Set<Expression> mergeSet(Expression expression,Set<Expression> set){

        return null;
    }

    /**
     * generateStr()中得到<I,J>->产生式的方法集合，在loop中进行整合
     * 返回一个W'
     * @param inputString
     * @param outputString
     * @param W
     */
    public static void generateLoop(String inputString, String outputString,HashMap<Pair<Integer,Integer>,Set<Expression>> W){

    }

    /**
     * 返回从intputString中得到target的所有方式
     * 如：
     * inputString=123-456-123,targetString=123
     * 那么就返回s[0：3]+s[-3：-1]...
     *
     * 返回一组对subStr(s,p1,p2)方法的引用，其中p1,p2则是通过generatePos()得到。
     * @param inputString 输入数据
     * @param targetString 要从intputString中截取的字符串
     */
    public static Set<Expression> generateSubString(String inputString,String targetString){
        Set<Expression> result=new HashSet<Expression>();

        int targetLen=targetString.length();
        for(int k=0;k<inputString.length();k++){
            // 如果input中的某一段能够和target匹配(因为target定长，所以遍历input，每次抽取I中长度为targetLen的一段进行比较)，那么就把此时的posExpression添加到res中
            if(inputString.substring(k, k + targetLen).equals(targetString)){
                Set<Expression> res1=generatePos(inputString,k);
                Set<Expression> res2=generatePos(inputString,k+targetLen);
                mergeSet(result,mergeSet(res1,res2));
            }
        }
        return result;
    }

    /**
     * generatePos()时，用从这个tokens中获得r1 r2来对字符串进行匹配
     */
    private static List<Token> tokens=new ArrayList<Token>();

    /**
     * 返回一组能够取得相应位置的'表达式!'，如Pos(r1,r2,c),其中r是正则表达式(在这里是token)，c代表第c个匹配
     *
     * 注：r=TokenSeq(T1,T2..Tn)表示Str要符合[T1]+[T2]+...[Tn]+这种形式
     * 如：r=TokenSea(num，letter)，那么str必须是123abc或1Abb这种形式才能和r匹配
     *
     * 所以
     * @param inputString
     * @param k
     * @return 一个表示能够从input中截取出targetString的位置集合。
     * 如：input=“123-abc-456-zxc" target="abc" 那么一个有效的起点pos(即a的位置)=POS(hyphenTok(即‘-’),letterTok,1||-2)
     * 这个POS表示第一个或倒数第二个左侧为‘-’，右侧为字母的符号的位置
     */
    public static Set<Expression> generatePos(String inputString,int k){
        Set<Expression> result=new HashSet<Expression>();
        // 首先把k这个位置(正向数底k个，逆向数第-(inputString.length()-k)个)加到res中
        result.add(new PosExpression(k));
        result.add(new PosExpression(-(inputString.length()-k)));

        int len=inputString.length();
        for(int k1=k-1;k1>=0;k1--){
            // TODO 为inputString[k1:k2](含k1，不含k2)寻找一个TokenSeq(如123abc对应{num,letter})
            // TokenSeq tokenSeq1=findTokenSeq(inputString.substring(k1,k2));
            for(int k2=k+1;k2<=len;k2++){
                // TODO：为inputString[k+1:k2](含k+1，不含k2)寻找一个TokenSeq(如123abc对应{num,letter})
                // TokenSeq tokenSeq2=findTokenSeq(inputString.substring(k+1,k2));

                // TODO ：顺序合并r1和r2成为r12
                // TokenSeq tokenSeq12=new TokenSeq(tokenSeq1,tokenSeq1);

                // TODO：查找inputString[k1,k2]是tokenSeq12在inputString中的第几次match，记作c。
                // 记cTotal为r12在inputString中总共match的次数
                // int c = 第几次 inputString.matches(tokenSeq12);
                // int cTotal=总共 inputString.matches(tokenSeq12);

                // TODO: 2016/12/27 在generateRegex中为token去重复
                // TokenSeq resTokenSeq1=generateRegex(r1,inputString);
                // TokenSeq resTokenSeq2=generateRegex(r2,inputString);

                //TODO: 合并结果
                // result.add(new PosExpression(resTokenSeq1,resTokenSeq2,c));
                // result.add(new PosExpression(resTokenSeq1,resTokenSeq2,-(cTotal-c+1)));
            }
        }
        return result;
    }

    /**
     * 注：r=TokenSeq(T1,T2..Tn)表示Str要符合[T1]+[T2]+...[Tn]+这种形式
     * 如：r=TokenSea(num，letter)，那么str必须是123abc或1Abb这种形式才能和r匹配
     * TokenSeq中可能存在的token，如多次123abc456zxc会表示成{numToken，letterToken，numtoken，letterToken}形势，
     * 上面例子得到的经过本方法去重复之后得到{numtoken，letterToken}
     *
     * △ 但是：123abc456->{numToken，letterToken，numtoken}不可以变成{numToken，letterToken}
     *
     * 不太理解，不知道是不是可以略去
     *
     * 对于一个某次字符串s匹配，token1和token2会取得一样的效果，此时token1和token2就没有区别(indistinguishable)
     * @param regExpression
     * @param inputString
     */
    public static void generateRegex(RegExpression regExpression,String inputString){

    }

    public static void main(String[] args) {
        String inputString="123-abc-456-zxc";
        String outputString="abc";
        HashMap<String,String> exampleSet=new HashMap<String, String>();
        exampleSet.put(inputString,outputString);

        // TODO : 程序入口，根据examples求得expression
        generateExpressionByExamples(exampleSet);

        // TODO :每当有新的inputS，利用上面求得的expression将I->O


        // region # testCodeRegion
        generateStr(inputString,outputString);


        // endregion

    }
}
