package com.zsf.interpreter.expressions.loop;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.linking.ConcatenateExpression;
import com.zsf.interpreter.expressions.linking.LinkingExpression;
import com.zsf.interpreter.expressions.string.SubString2Expression;

/**
 * Created by hasee on 2017/1/23.
 */
public class LoopExpression extends NonTerminalExpression {

    public static final String LINKING_MODE_CONCATENATE="concat";

    /**
     * 要通过某种方式吧Loop的结果连接起来
     * 默认为concatenate连接字符串
     * 如果有需要也可以拓展出AddExpression或SbuExpression等
     */
    private String linkingMode;
    /**
     * btotalExpressions表示当前这个Loop要代替的完整的表达式
     * 如IBM中totalExpressions就是一串SbuStr2(vi,UppderToken,w)；
     */
    private Expression totalExpressions;
    /**
     * baseExpression表示for的循环主体
     * 在IBM中就是SbuStr2(UppderToken,w)
     */
    private Expression baseExpression;

    // 因为for的index通常是一个等差数列，下面3个就代表了begin:stepsize:end
    private int beginNode=0;
    private int stepSize=0;
    private int endNode=0;

    public LoopExpression(String linkingMode, Expression totalExpressions, int beginNode, int endNode) {
        this.linkingMode = linkingMode;
        this.totalExpressions = totalExpressions;
        this.beginNode = beginNode;
        this.endNode = endNode;

        // TODO: 2017/1/23 根据totalExpressions得到baseExp
        // TODO: 2017/1/23 下面的假设TotalExp全都是Concat，而且.leftExp()全都是substr2

        // FIXME: 2017/1/25 现在的问题是，左右都有可能是concat，要用while判断，但是如果不实现clone，就会导致指针乱更新。
        if (totalExpressions instanceof ConcatenateExpression){

            Expression leftExp=((ConcatenateExpression) totalExpressions).getLeftExp().deepClone();
            while (leftExp instanceof ConcatenateExpression){
                leftExp=((ConcatenateExpression) leftExp).getLeftExp().deepClone();
            }
            baseExpression=leftExp;
        }
        // TODO: 2017/1/23 step
    }

    @Override
    public String toString() {
        // Loop(concat(subStr2(UpperTok,0,1,3)))
        // TODO: 2017/1/23 Loop是否只适用于substr2？如果不是的话，这个toString还有待斟酌
        String ans="";
        if (baseExpression == null){
            System.out.println("Loop(error)");
        }
        if (baseExpression instanceof SubString2Expression){
            ans=String.format("Loop(%s(%s(%s,%d,%d,%d)))",linkingMode,
                    "subStr2",((SubString2Expression) baseExpression).getRegex().getRegexName(),
                    beginNode,stepSize,endNode);
        }else {
            ans=String.format("Loop(%s(%s))",linkingMode,baseExpression.toString());
        }
        return ans;
    }

    @Override
    public Expression deepClone() {
        return new LoopExpression(linkingMode,totalExpressions.deepClone(),beginNode,endNode);
    }

    @Override
    public int deepth() {
        return 1;
    }


    public String getLinkingMode() {
        return linkingMode;
    }

    public void setLinkingMode(String linkingMode) {
        this.linkingMode = linkingMode;
    }

    public static String getLinkingModeConcatenate() {
        return LINKING_MODE_CONCATENATE;
    }

    public Expression getTotalExpressions() {
        return totalExpressions;
    }

    public void setTotalExpressions(LinkingExpression totalExpressions) {
        this.totalExpressions = totalExpressions;
    }

    public int getBeginNode() {
        return beginNode;
    }

    public void setBeginNode(int beginNode) {
        this.beginNode = beginNode;
    }

    public int getEndNode() {
        return endNode;
    }

    public void setEndNode(int endNode) {
        this.endNode = endNode;
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    @Override
    public String interpret(String inputString) {
        System.out.println("LOOP_UNHANDLED");
        return "Loop_unhandled";
    }
}
