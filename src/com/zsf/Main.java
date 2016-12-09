package com.zsf;

public class Main {

    public static void main(String[] args) {
	// write your code here
    }


    /**
     * generate阶段要调用的函数
     * 返回一个能够从input中生产output的expressions集合
     * @param inputString
     * @param outputString
     */
    public void generateStr(String inputString,String outputString){

    }

    public void generateLppo(String inputString,String outputString){

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
    public void generateSubString(String inputString,String targetString){
        int targetLen=targetString.length();
        for(int k=0;k<inputString.length();k++){
            if(inputString.substring(k,k+targetLen)==targetString){
                // TODO: 2016/11/22 pos1=generatePos(input,k);   pos2=generatePos(input,k+targetLen)
//                int pos1=0;
//                int pos2=0;
//                resulet+=(pos1,pos2);
            }
        }

    }

    /**
     * 返回一组能够取得相应位置的'表达式!'，如Pos(r1,r2,c),其中r是正则表达式(在这里是token)，c代表第c个匹配
     *
     * !!!!返回一组对pos函数的引用!!!!
     * @param inputString
     * @param k
     */
    public void generatePos(String inputString,int k){
        // TODO: 2016/11/22 首先把k这个位置加到res中
        // k表示位置的绝对值(k这个固定位置)
        // resSet+=(k)

        // TODO: 2016/11/22 关于token的处理 ，可以降低复杂度？？？(看不懂)
        // pos表示位置的相对值(第几个字符串的位置)
//        找到r1，r2，找到能匹配的k1，k2位置，然后提取出s[k1，k2]，找到s[k1，k2]匹配r12的位置c，得到新的pos(r1,r2,{c,-(_c-c+1)})


        /* 返回一组对pos函数的引用！！！ */
    }

    public void generateRegex(){

    }
}
