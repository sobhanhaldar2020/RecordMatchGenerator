package com.twopiradian.services;

import com.twopiradian.generators.RecordMatchGenerator;
import com.twopiradian.model.RuleConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ColumnsNamesGeneratorInOrder {

    RuleConfiguration ruleConfiguration;
    FileWriter orderColumnNamesAndThreshould;
    String outputDirectory;
    String inputCsvFilePath;
    RecordMatchGenerator recordMatchGenerator;
    int totalNumberOfIndexesOfColumnList;
    List<String> orderOfColimnList = new ArrayList<>();
    List<String> orderOfMatchThreshould = new ArrayList<>();
    List<String> ruleNames = new ArrayList<>();
    List<Integer> primaryKeyColumnIndexValueList = new ArrayList<>();

    public ColumnsNamesGeneratorInOrder(RuleConfiguration ruleConfiguration, String outputDirectory, String inputCsvFilePath, RecordMatchGenerator recordMatchGenerator) throws Exception {
        this.ruleConfiguration = ruleConfiguration;
        this.outputDirectory = outputDirectory;
        this.inputCsvFilePath = inputCsvFilePath;
        this.recordMatchGenerator = recordMatchGenerator;

    }

    public void columnsNamesInOrder() throws Exception {
        ColumnValuesSet columnValuesSet = new ColumnValuesSet();

        for (int index = 0; index < ruleConfiguration.getRuleCount(); index++) {
            setColumnNamesAndMatchThreshould(index, columnValuesSet);
        }
        writeTheOrderColumnNamesAndThreshouldAsCSV();

    }

    protected void setColumnNamesAndMatchThreshould(int index, ColumnValuesSet columnValuesSet) throws IOException {
        List<String> columnList;
        List<Float> matchThreshould;
        int primaryKeyColumnIndex;

        recordMatchGenerator.setCoulmnNames(ruleConfiguration.getRuleColumns(index), ruleConfiguration.getRuleName(index));
        recordMatchGenerator.setMatchThresholds(ruleConfiguration.getThresholds(index));
        recordMatchGenerator.setRulePrimaryKeyColumnIndex(Integer.parseInt(ruleConfiguration.getRulePrimaryKeyColumnIndex(index)));
        columnList = recordMatchGenerator.getCoulmnNames();
        matchThreshould = recordMatchGenerator.getMatchThresholds();
        primaryKeyColumnIndex = recordMatchGenerator.getRulePrimaryKeyColumnIndex();
        totalNumberOfIndexesOfColumnList = columnList.size();
        Map<String, Integer> headerNamesAndSize = getHeaderNameAndSizeOfHeader(columnList, columnValuesSet);
        populateNewColumnNamesAndMatchThreshouldValuesInOrder(headerNamesAndSize, columnList, matchThreshould, index, primaryKeyColumnIndex);

    }

    protected void populateNewColumnNamesAndMatchThreshouldValuesInOrder(Map<String, Integer> headerNamesAndSize, List<String> columnList, List<Float> matchThreshould, int index, int primaryKeyColumnIndex) {

        List<Integer> sizeList = new ArrayList<>(headerNamesAndSize.values());
        List<Integer> sortSizeList = new ArrayList<>();
        List<String> newColumnList = new ArrayList<>();
        List<Float> newMatchThreshould = new ArrayList<>();
        sortSizeList.addAll(sizeList);
        Collections.sort(sortSizeList);
        for (Integer sizeOfList : sortSizeList) {
            for (int sortHeaderIndex = 0; sortHeaderIndex < sizeList.size(); sortHeaderIndex++) {
                if (Objects.equals(sizeList.get(sortHeaderIndex), sizeOfList)) {
                    newColumnList.add(columnList.get(sortHeaderIndex));
                    newMatchThreshould.add(matchThreshould.get(sortHeaderIndex));
                    break;
                }
            }
        }
        ruleNames.add(ruleConfiguration.getRuleName(index));
        orderOfColimnList.add(newColumnList.toString());
        orderOfMatchThreshould.add(newMatchThreshould.toString());
        primaryKeyColumnIndexValueList.add(primaryKeyColumnIndex);
    }

    protected Map<String, Integer> getHeaderNameAndSizeOfHeader(List<String> columnList, ColumnValuesSet columnValuesSet) throws IOException {
        Set<String> columnKeys;
        Map<String, Integer> headerNamesAndSize = new LinkedHashMap<>();
        for (int headerIndex = 0; headerIndex < totalNumberOfIndexesOfColumnList; headerIndex++) {
            columnKeys = getSetForColumnIndex(headerIndex, columnValuesSet, inputCsvFilePath, columnList);
            headerNamesAndSize.put(columnList.get(headerIndex), columnKeys.size());

        }
        return headerNamesAndSize;
    }

    protected Set<String> getSetForColumnIndex(int columnIndex, ColumnValuesSet columnValuesSet, String inputCsvFilePath, List<String> columnList) throws IOException {
        Set<String> columnKeys = new HashSet<>();
        ArrayList<String> headerNames = columnValuesSet.readInputCsvFilePathAndColumnIndex(inputCsvFilePath);
        for (int headerIndex = 0; headerIndex < headerNames.size(); headerIndex++) {
            if (columnList.get(columnIndex).equals(headerNames.get(headerIndex))) {
                columnKeys = columnValuesSet.populateSetForColumn(headerIndex);
            }
        }
        return columnKeys;
    }

    protected void writeTheOrderColumnNamesAndThreshouldAsCSV() throws Exception {

        orderColumnNamesAndThreshould = new FileWriter(outputDirectory + File.separatorChar + "ColumnsNamesInOrder.csv");
        orderColumnNamesAndThreshould.append("RuleName" + "," + "ColumnNames" + "," + "MatchThreshould" + "," + "PrimaryKeyColumnIndex" + "\n");
        for (int index = 0; index < orderOfColimnList.size(); index++) {
            orderColumnNamesAndThreshould.append(ruleNames.get(index) + "," + orderOfColimnList.get(index).replaceAll(",", "  ") + "," + orderOfMatchThreshould.get(index).replaceAll(",", "  ") + "," + primaryKeyColumnIndexValueList.get(index) + "\n");
        }
        orderColumnNamesAndThreshould.flush();
        orderColumnNamesAndThreshould.close();
    }
}
