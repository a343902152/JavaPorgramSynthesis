package com.zsf.interpreter.tool;

import com.zsf.interpreter.expressions.Expression;

import java.util.List;
import java.util.Set;

/**
 * Created by hasee on 2017/2/5.
 */
public class MergeSetTool {
    /**
     * 把表达式整合起来，能够去重复
     * <p>
     * 要求：
     * 1. 去掉直接重复
     * 2. 去掉间接(值恒相等)重复
     *
     * @param set1
     * @param set2
     * @return 合并后的集合
     */
    public static List<Expression> mergeSet(List<Expression> set1, List<Expression> set2) {
        // TODO: 2017/1/23 null还没彻底检查，现在默认set1不为空，set2有可能为空
        if (set1 != null && set2 != null) {
            set1.addAll(set2);
        }
        return set1;
    }

    /**
     * 合并单个表达式和表达式集合
     *
     * @param expression 新添加的单个表达式
     * @param set        已有的表达式集合
     * @return
     */
    public static List<Expression> mergeSet(Expression expression, List<Expression> set) {
        if (expression != null) {
            set.add(expression);
        }
        return set;
    }
}
