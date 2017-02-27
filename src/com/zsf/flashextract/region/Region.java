package com.zsf.flashextract.region;

import com.zsf.flashextract.regex.LineSelector;
import com.zsf.interpreter.model.Match;
import com.zsf.interpreter.model.Regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/2/27.
 */
public class Region {
    private Region parentRegion;
    /**
     * beginPos表示当前region在parentRegion中的起始位置
     */
    private int beginPos;
    /**
     * endPos表示当前region在parentRegion中的终止位置
     */
    private int endPos;
    private String text;

    private List<Region> childRegions=new ArrayList<Region>();

    public Region(Region parentRegion, int beginPos, int endPos,String text) {
        this.parentRegion = parentRegion;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.text=text;
    }

    public void selectNewChildRegion(Region newChildRegion){
        insertNewChildRegionToRightPos(newChildRegion);
    }

    private void insertNewChildRegionToRightPos(Region newChildRegion) {
        childRegions.add(newChildRegion);
        // TODO: 2017/2/27 根据endPos从小到大排序的Comparter
//        Collections.sort(childRegions);
    }

    /**
     * 用于Filter，测试selector是否可以选出此行Region
     * @param selector
     * @return
     */
    public boolean canMatch(Regex selector){
        List<Match> matches=selector.doMatch(text);
        if (matches.size()>0){
            return true;
        }
        return false;
    }

    public Region getParentRegion() {
        return parentRegion;
    }

    public void setParentRegion(Region parentRegion) {
        this.parentRegion = parentRegion;
    }

    public List<Region> getChildRegions() {
        return childRegions;
    }

    public void setChildRegions(List<Region> childRegions) {
        this.childRegions = childRegions;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public void setBeginPos(int beginPos) {
        this.beginPos = beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
