package com.zsf.interpreter.expressions.linking;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.string.ConstStrExpression;
import com.zsf.interpreter.model.ExpressionGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/1/23.
 */
public class ConcatenateExpression extends LinkingExpression {

    private List<Expression> expressionList=new ArrayList<Expression>();


    public ConcatenateExpression(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    public ConcatenateExpression(Expression exp1, Expression exp2) {
        expressionList=new ArrayList<Expression>();
        if (exp1 instanceof ConcatenateExpression && exp2 instanceof ConcatenateExpression){
            // 全都是concat
            expressionList.addAll(((ConcatenateExpression) exp1).getExpressionList());
            expressionList.addAll(((ConcatenateExpression) exp2).getExpressionList());
        }else if (exp1 instanceof ConcatenateExpression){
            // exp1是concat
            expressionList.addAll(((ConcatenateExpression) exp1).getExpressionList());
            expressionList.add(exp2);
        }else if (exp2 instanceof ConcatenateExpression){
            // exp2是concat
            expressionList.add(exp1);
            expressionList.addAll(((ConcatenateExpression) exp2).getExpressionList());
        }else {
            // 全都不是concat
            expressionList.add(exp1);
            expressionList.add(exp2);
        }
    }

    /**
     * 合并两个exp集合的工具函数
     * @param expressions1
     * @param expressions2
     * @return
     */
    public static ExpressionGroup concatenateExp(ExpressionGroup expressions1, ExpressionGroup expressions2) {
        ExpressionGroup linkedExpressions=new ExpressionGroup();
        for(Expression exp1:expressions1.getExpressions()){
            for (Expression exp2:expressions2.getExpressions()){
                if (exp1 instanceof ConstStrExpression && exp2 instanceof ConstStrExpression){
                    linkedExpressions.insert(new ConstStrExpression(((ConstStrExpression) exp1).getConstStr()+((ConstStrExpression) exp2).getConstStr()));
                }else {
                    linkedExpressions.insert(new ConcatenateExpression(exp1,exp2));
                }
                // TODO: 2017/3/2 loop和其他值合併
            }
        }
        return linkedExpressions;
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("concat(");
        for (int i=0;i<expressionList.size();i++){
            if (i==expressionList.size()-1){
                builder.append(expressionList.get(i).toString());
            }else {
                builder.append(expressionList.get(i).toString()+",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public Expression deepClone() {
        return new ConcatenateExpression(expressionList);
    }

    @Override
    public int deepth() {
        return expressionList.size();
    }

    @Override
    public String interpret(String inputString) {
        StringBuilder builder=new StringBuilder();
        for (Expression expression:expressionList){
            if (expression instanceof NonTerminalExpression){
                builder.append(((NonTerminalExpression) expression).interpret(inputString));
            }
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConcatenateExpression){
            if (((ConcatenateExpression) obj).deepth()!=this.deepth()){
                return false;
            }
            List<Expression> expressionList1=this.expressionList;
            List<Expression> expressionList2=((ConcatenateExpression) obj).getExpressionList();
            int deepth=deepth();
            for (int i=0;i<deepth;i++){
                if (!(expressionList1.get(i).equals(expressionList2.get(i)))){
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public double score() {
        double sum=0.0;
        for (Expression expression:expressionList){
            sum+=expression.score();
        }
        double score=(sum/deepth())/Math.pow(1.1,deepth());
        return score;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }
}
