package com.zsf.interpreter.model;

/**
 * row col默认等于output.length()
 * resultMap[i][j]存储的是从inputString中产生output.substr(i,j)的expressionGroup
 * Created by hasee on 2017/2/6.
 */
public class ResultMap {
    private int row;
    private int col;
    private ExpressionGroup[][] resultMap;

    public ResultMap(int row, int col) {
        this.row = row;
        this.col = col;
        this.resultMap=new ExpressionGroup[row+1][col+1];
    }

    public void setData(int i, int j, ExpressionGroup expressionGroup) {
        resultMap[i][j]=expressionGroup;
    }

    public ExpressionGroup getData(int i, int j) {
        return resultMap[i][j];
    }
}
