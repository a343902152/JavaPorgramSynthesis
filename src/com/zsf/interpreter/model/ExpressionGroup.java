package com.zsf.interpreter.model;

import com.zsf.interpreter.expressions.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/2/6.
 */
public class ExpressionGroup {
    private List<Expression> expressions = new ArrayList<Expression>();


    public ExpressionGroup() {
        this.expressions = new ArrayList<Expression>();
    }

    /**
     * 添加exp1
     *
     * @param expression
     */
    public void insert(Expression expression) {
        expressions.add(expression);
    }

    /**
     * 添加exp2
     *
     * @param expressionGroup
     */
    public void insert(ExpressionGroup expressionGroup) {
        // TODO: 2017/2/6 去重复
        expressions.addAll(expressionGroup.getExpressions());
    }

    /**
     * insert 3
     *
     * @param expressions
     */
    private void insert(List<Expression> expressions) {
        // TODO: 2017/2/6 去重复
        this.expressions.addAll(expressions);
    }

    public int size() {
        if (expressions != null) {
            return this.expressions.size();
        } else {
            return 0;
        }
    }

    public ExpressionGroup deepClone() {
        ExpressionGroup expressionGroup = new ExpressionGroup();
        expressionGroup.insert(expressions);

        return expressionGroup;
    }


    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }


}
