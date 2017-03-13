package com.zsf.flashextract.region;

/**
 * Created by zsf on 2017/3/13.
 */
public class SelectedRegion extends Region {

    private int color;

    public SelectedRegion(Region parentRegion, int beginPos, int endPos, String text) {
        super(parentRegion, beginPos, endPos, text);
    }
}
