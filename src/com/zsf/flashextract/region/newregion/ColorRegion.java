package com.zsf.flashextract.region.newregion;

import com.zsf.StringProcessor;
import com.zsf.flashextract.regex.RegexCommomTools;
import com.zsf.flashextract.region.Region;
import com.zsf.flashextract.region.SelectedLineRegion;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.DynamicRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.ExamplePair;
import com.zsf.interpreter.model.ExpressionGroup;
import com.zsf.interpreter.model.Match;
import com.zsf.interpreter.model.ResultMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/3/16.
 */
public class ColorRegion{
    private Color color;
    private String parentDocument;
    private List<String> splitedLineDocument;

    private List<Field> fieldsByUser = new ArrayList<Field>();
    private List<Field> fieldsGenerated = new ArrayList<Field>();

    private List<Integer> positiveLineIndex = new ArrayList<Integer>();
    private List<Integer> negativeLineIndex = new ArrayList<Integer>();
    private List<Integer> needSelectLineIndex = new ArrayList<Integer>();

    private List<Regex> lineSelectors = new ArrayList<Regex>();
    private Regex curLineSelector = null;

    private ExpressionGroup expressionGroup;
    private Expression curExpression;

    public ColorRegion(Color color, String parentDocument) {
        this.color = color;
        this.parentDocument = parentDocument;

        splitedLineDocument = new ArrayList<String>();
        String[] splitedLines = parentDocument.split("\n");
        for (String line : splitedLines) {
            splitedLineDocument.add(line);
        }
    }

    public void selectField(int beginPos, int endPos, String text) {
        int lineIndex = calculateLineIndex(beginPos, endPos);
        Field field = new PlainField(color, beginPos, endPos, text);
        if (!fieldsByUser.contains(field)) {
            fieldsByUser.add(field);
            addPositiveLineIndex(lineIndex);
        }
        if (needGenerateLineSelectors()) {
            doGenerateLineSelectors();
        }
    }

    private boolean needGenerateLineSelectors() {
        // 当选中fields>=2时需要产生selector
        // 即使是已经有selector了，如果又选择了新的field，就可以认为之前的selector不够好，要重新生成
        return fieldsByUser.size() >= 2;
    }

    /**
     * 在selectField()时，如果判断需要产生LineSelectors就会调用此方法，产生LineSelecotr,排序结果后设置curSelector
     */
    private void doGenerateLineSelectors() {
        RegexCommomTools.addDynamicToken(parentDocument,fieldsByUser,MainDocument.usefulRegex);

        List<List<Regex>> startWithReges = new ArrayList<List<Regex>>();
        List<List<Regex>> endWithReges = new ArrayList<List<Regex>>();
        int curDeepth = 1;
        int maxDeepth = 3;

        for (int index:positiveLineIndex) {
            List<Match> matches = RegexCommomTools.buildStringMatches(splitedLineDocument.get(index),MainDocument.usefulRegex);
            startWithReges.add(RegexCommomTools.buildStartWith(curDeepth, maxDeepth, matches, 0, new DynamicRegex("", "")));
            endWithReges.add(RegexCommomTools.buildEndWith(curDeepth, maxDeepth, matches, splitedLineDocument.get(index).length(), new DynamicRegex("", "")));
        }
        System.out.println("start with:");
        System.out.println(startWithReges.get(1));
        System.out.println("end with:");
        System.out.println(endWithReges);

        List<Regex> startWithLineSelector = RegexCommomTools.deDuplication(startWithReges, true);
        List<Regex> endWithLineSelector = RegexCommomTools.deDuplication(endWithReges, false);

        System.out.println(startWithLineSelector);
        System.out.println(endWithLineSelector);

        // 利用positive和negativeExamples对selectors进行筛选
        List<Regex> usefulLineSelector = new ArrayList<Regex>();
        usefulLineSelector.addAll(RegexCommomTools.filterUsefulSelector(startWithLineSelector, splitedLineDocument, positiveLineIndex, getNegativeLineIndex()));
        usefulLineSelector.addAll(RegexCommomTools.filterUsefulSelector(endWithLineSelector, splitedLineDocument,positiveLineIndex, getNegativeLineIndex()));

        this.lineSelectors=usefulLineSelector;
        this.curLineSelector =lineSelectors.get(0);
    }

    private void addPositiveLineIndex(int lineIndex) {
        if (!positiveLineIndex.contains(lineIndex)) {
            positiveLineIndex.add(lineIndex);
        }
    }

    /**
     * 根据当前的selector产生fileds，会在getFileds()之前调用
     */
    private void generateFieldsByCurSelector() {
        needSelectLineIndex=new ArrayList<Integer>();
        for (int i=0;i<splitedLineDocument.size();i++){
            if (RegexCommomTools.canMatch(splitedLineDocument.get(i),curLineSelector)){
                needSelectLineIndex.add(i);
            }
        }

        List<ExamplePair> examplePairs = new ArrayList<ExamplePair>();
        for (Field field:fieldsByUser){
            examplePairs.add(new ExamplePair())
        }
        for (Region region : regions) {
            examplePairs.add(new ExamplePair(region.getParentRegion().getText(), region.getText()));
        }

        StringProcessor stringProcessor = new StringProcessor();
        List<ResultMap> resultMaps = stringProcessor.generateExpressionsByExamples(examplePairs);
        ExpressionGroup expressionGroup = stringProcessor.selectTopKExps(resultMaps, 10);

        if (expressionGroup != null) {
            for (SelectedLineRegion lineRegion : selectedLineRegions) {
                lineRegion.setColorfulRegionExpressions(color, expressionGroup);
            }
        }
    }

    private int calculateLineIndex(int beginPos, int endPos) {
        String textBeforeSelect = parentDocument.substring(0, beginPos);
        int count = 0;
        int index = 0;
        while (true) {
            index = textBeforeSelect.indexOf("\n", index + 1);
            if (index > 0) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public List<Field> getFieldsGenerated() {
        generateFieldsByCurSelector();
        return fieldsGenerated;
    }

    public Color getColor() {
        return color;
    }

    public String getParentDocument() {
        return parentDocument;
    }

    public List<Regex> getLineSelectors() {
        return lineSelectors;
    }

    public Regex getCurLineSelector() {
        return curLineSelector;
    }

    public List<Integer> getNegativeLineIndex() {
        return negativeLineIndex;
    }
}
