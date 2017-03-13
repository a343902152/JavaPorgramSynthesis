package com.zsf.flashextract.region;

/**
 * 表示被某种规则选中的Region,
 * parent表示它的所属(如果是完整的line那么parent为null)
 * beginPos表示他在parent中的位置，endPos类似
 * text表示这个region的内容
 * color表示他被绘制上的颜色(如果是line的话则是一个特殊的框框)
 *
 * Created by zsf on 2017/3/13.
 */
public class SelectedRegion extends Region {

    private int color;

    public SelectedRegion(Region parentRegion, int beginPos, int endPos, String text, int color) {
        super(parentRegion, beginPos, endPos, text);
        this.color = color;
    }
}
