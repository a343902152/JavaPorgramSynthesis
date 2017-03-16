package com.zsf.flashextract.region.newregion;

/**
 * Created by hasee on 2017/3/16.
 */
public class PlainField implements Field {
    private Color color;
    private int beginPos;
    private int endPos;
    private String text;

    public PlainField(Color color, int beginPos, int endPos, String text) {
        this.color = color;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.text = text;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlainField){
            return ((PlainField) obj).getBeginPos()==beginPos&&((PlainField) obj).getEndPos()==endPos&&((PlainField) obj).color==color;
        }
        return false;
    }

    public Color getColor() {
        return color;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public String getText() {
        return text;
    }


}
