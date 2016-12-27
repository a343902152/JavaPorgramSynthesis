package com.zsf;

import com.zsf.interpreter.Expression;
import com.zsf.interpreter.TestExpression;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Set;

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
                // TODO 用mergeSet方法合并constStr和generateSubStr方法得到的结果。
//                W.put(new Pair<Integer, Integer>(i,j),mergeSet(new ConstantStr(outputString.substring(i,j)),
//                        generateSubString(inputString,outputString.substring(i,j))));
            }
        }
        // TODO: 2016/12/27 W->W'，利用循环去重复
//        HashMap<Pair<Integer,Integer>,Set<Expression>> W2=generateLoop(inputString,outputString,W);

        // TODO: 2016/12/27 return dag(....,W2)；
    }

    public static HashMap<Pair<Integer,Integer>,Set<Expression>> mergeSet(Set<Expression> set1,Set<Expression> set2){

        return null;
    }


    /**
     * generateStr()中得到<I,J>->产生式的方法集合，在loop中进行去重复操作
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
        int targetLen=targetString.length();
        for(int k=0;k<inputString.length();k++){
            if(inputString.substring(k,k+targetLen)==targetString){
                // TODO: 2016/11/22 pos1=generatePos(input,k);   pos2=generatePos(input,k+targetLen)
//                int pos1=0;
//                int pos2=0;
//                resulet+=(pos1,pos2);
            }
        }
        return null;
    }

    /**
     * 返回一组能够取得相应位置的'表达式!'，如Pos(r1,r2,c),其中r是正则表达式(在这里是token)，c代表第c个匹配
     *
     * !!!!返回一组对pos函数的引用!!!!
     * @param inputString
     * @param k
     */
    public static void generatePos(String inputString,int k){
        // TODO: 2016/11/22 首先把k这个位置加到res中
        // k表示位置的绝对值(k这个固定位置)
        // resSet+=(k)

        // TODO: 2016/11/22 关于token的处理 ，可以降低复杂度？？？(看不懂)
        // pos表示位置的相对值(第几个字符串的位置)
//        找到r1，r2，找到能匹配的k1，k2位置，然后提取出s[k1，k2]，找到s[k1，k2]匹配r12的位置c，得到新的pos(r1,r2,{c,-(_c-c+1)})


        /* 返回一组对pos函数的引用！！！ */
    }

    public static void generateRegex(){

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
