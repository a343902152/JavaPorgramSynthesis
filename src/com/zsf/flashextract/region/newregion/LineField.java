package com.zsf.flashextract.region.newregion;

/**
 * Created by hasee on 2017/3/16.
 */
public class LineField implements Field {

    private Field parentField;
    private int beginPos;
    private int endPos;
    private String text;

    public LineField(Field parentField, int beginPos, int endPos, String text) {
        this.parentField = parentField;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.text = text;
    }

    @Override
    public int getBeginPos() {
        return 0;
    }

    @Override
    public int getEndPos() {
        return 0;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public Field getParentField() {
        return null;
    }
}
