package com.zsf.flashextract.region.newregion;

/**
 * Created by hasee on 2017/3/16.
 */
public interface Field {
    int getBeginPos();
    int getEndPos();
    String getText();
    Field getParentField();
}
