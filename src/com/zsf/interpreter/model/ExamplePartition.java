package com.zsf.interpreter.model;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.tool.MergeSetTool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hasee on 2017/2/5.
 */
public class ExamplePartition {
    List<ExamplePair> examplePairs=new ArrayList<ExamplePair>();
    Set<Expression> usefulExpression=new HashSet<Expression>();


    public ExamplePartition(ExamplePair examplePair,Set<Expression> usefulExpression) {
        examplePairs.add(examplePair);
        this.usefulExpression = usefulExpression;
    }

    public ExamplePartition(List<ExamplePair> examplePairs, Set<Expression> usefulExpression) {
        this.examplePairs = examplePairs;
        this.usefulExpression = usefulExpression;
    }

    public List<ExamplePair> getExamplePairs() {
        return examplePairs;
    }

    public void setExamplePairs(List<ExamplePair> examplePairs) {
        this.examplePairs = examplePairs;
    }

    public Set<Expression> getUsefulExpression() {
        return usefulExpression;
    }

    public void setUsefulExpression(Set<Expression> usefulExpression) {
        this.usefulExpression = usefulExpression;
    }

    public void showDetails(boolean showExamples, boolean showExpressions) {
        if (showExamples){
            for (ExamplePair pair:examplePairs){
                System.out.println(String.format("Input= '%s' , Output='%s'",pair.getInputString(),pair.getOutputString()));
            }
        }
        if (showExpressions){
            for (Expression expression:usefulExpression){
                System.out.println(expression.toString());
            }
        }

    }
}
