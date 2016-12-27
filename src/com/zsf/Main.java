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
     * !!!!返回一组对pos函数的引用!!!!
     * @param inputString
     * @param k
     */
    public static Set<Expression> generatePos(String inputString,int k){
        Set<Expression> result=new HashSet<Expression>();
        // 首先把k这个位置(正向数底k个，逆向数第-(inputString.length()-k)个)加到res中
        result.add(new PosExpression(k));
        result.add(new PosExpression(-(inputString.length()-k)));

        // TODO: 2016/11/22 关于token的处理 ，可以降低复杂度？？？(看不懂)
        // pos表示位置的相对值(第几个字符串的位置)
        // 找到r1，r2，找到能匹配的k1，k2位置，然后提取出s[k1，k2]，找到s[k1，k2]匹配r12的位置c，得到新的pos(r1,r2,{c,-(_c-c+1)})
        // FIXME: 2016/12/27 r1和r2的tokens好像是不同的???
        for (Token r1:tokens){
            for (Token r2:tokens){
                // TODO: 2016/12/27 r12 = TokenSeq(tokenSeq1,tokenSeq2); ???

                // TODO: r1'= generateRegex(r1,inputString); r2'=generateRegex(r2,inputString); (不知道是否可以省略)

                // TODO: 2016/12/27  mergeSet(result,new PosExpression(xxxx))
            }
        }

        return result;
    }

    /**
     * TokenSeq??
     * IParts??
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
