package com.twopiradian.generators;

import com.twopiradian.model.RuleConfiguration;
import com.twopiradian.services.RedundantRuleGenerator;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import com.twopiradian.model.RecordContainer;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RecordMatchGenerator {

    protected String fileName;
    private int primaryKeyColumnIndex;
    CSVParser csvParser;
    ArrayList<String> headerNames;
    FileWriter outputFile;
    FileWriter ruleNameFile;
    FileWriter errorFileName;
    FileWriter deleteColumnFileName;
    String outputDirectory;
    String ruleNameFilePath;
    String inputCsvFilePath;
    boolean writeHeader = true;
    JaroWinklerDistance JaroWinklerDistance;
    RuleConfiguration ruleConfiguration;
    List<RecordContainer> recordsContainer;
    Set<String> matchSetList;
    List<String> columnsList;
    List<Integer> inputColumnIndexesForMatching;
    List<Float> matchThreshould;
    Map<String, String> deleteColumnNamesAndCorrespondingRulaName;
    List<Integer> dropColumnsIndexes;
    List<String> newColumnsNames;
    List<String> newMatchThreshould;
    List<String> rulesNames;

    public RecordMatchGenerator(RuleConfiguration ruleConfiguration, String ruleNameFilePath, String outputDirectory, String inputCsvFilePath) {
        this.ruleConfiguration = ruleConfiguration;
        this.ruleNameFilePath = ruleNameFilePath;
        this.outputDirectory = outputDirectory;
        this.inputCsvFilePath = inputCsvFilePath;
        JaroWinklerDistance = new JaroWinklerDistance();
    }

    public void setCoulmnNames(String columns, String ruleName) throws IOException {
        if (inputColumnIndexesForMatching != null) {
            inputColumnIndexesForMatching.clear();
        }
        if (columnsList != null) {
            columnsList.clear();
        }
        String[] partsOfColumnNames = columns.split(",");
        int checkHeaderName;
        for (String partOfColumnNames : partsOfColumnNames) {
            checkHeaderName = getColumnIndexFromName(partOfColumnNames);
            if (checkHeaderName == -1) {
                errorFileName = new FileWriter(outputDirectory + File.separatorChar + "ErrorFile.csv");
                errorFileName.append("This " + partOfColumnNames + " ColumnName does not exist in the InputCsv File " + " Please remove : " + partOfColumnNames + " From " + ruleName);
                errorFileName.flush();
                errorFileName.close();
                System.exit(0);

            }
            if (inputColumnIndexesForMatching == null) {
                inputColumnIndexesForMatching = new ArrayList<>();
            }
            inputColumnIndexesForMatching.add(checkHeaderName);
            if (columnsList == null) {
                columnsList = new ArrayList<>();
            }
            columnsList.add(partOfColumnNames);
        }
    }

    public void setRulePrimaryKeyColumnIndex(int primaryKeyColumnIndex) {
        this.primaryKeyColumnIndex = primaryKeyColumnIndex;
    }

    public void setMatchThresholds(String thresholds) {
        if (matchThreshould != null) {
            matchThreshould.clear();
        }
        String[] partsOfThresholds = thresholds.split(",");
        for (String threshold : partsOfThresholds) {
            if (matchThreshould == null) {
                matchThreshould = new ArrayList<>();
            }
            matchThreshould.add(Float.parseFloat(threshold));
        }
    }

    protected void setOutputFile(String ruleName) throws Exception {
        fileName = outputDirectory + File.separator + ruleName + ".csv";
        writeRuleNameInFile(ruleName);
        outputFile = new FileWriter(fileName);
    }

    protected void createRuleNameFile() throws Exception {
        ruleNameFile = new FileWriter(ruleNameFilePath);

    }

    protected void writeRuleNameInFile(String ruleName) throws IOException {
        ruleNameFile.append(ruleName + "\n");
    }

    public void readInputCsvFile() throws IOException {
        csvParser = new CSVParser(new FileReader(inputCsvFilePath), CSVFormat.DEFAULT.withDelimiter(',').withHeader());
        Map<String, Integer> irHeader = csvParser.getHeaderMap();
        headerNames = new ArrayList<>(Arrays.asList((irHeader.keySet()).toArray(new String[0])));
    }

    public int getColumnIndexFromName(String columnName) {
        for (int headerIndex = 0; headerIndex < headerNames.size(); headerIndex++) {
            if (columnName.equalsIgnoreCase(headerNames.get(headerIndex))) {
                return headerIndex;
            }
        }

        return -1;
    }

    protected void populateColumnValuesInList() {
        int rowNumber = 0;
        if (recordsContainer != null) {
            recordsContainer.clear();
        }
        if (csvParser != null) {
            for (CSVRecord record : csvParser) {
                RecordContainer recordContainer = new RecordContainer(rowNumber);
                rowNumber++;
                recordContainer.setRecord_ID(record.get(primaryKeyColumnIndex));
                recordContainer.setProcessed(false);

                for (int inputColumnIndex : inputColumnIndexesForMatching) {
                    recordContainer.setColumnValueInMap(headerNames.get(inputColumnIndex), record.get(inputColumnIndex));
                }
                if (recordsContainer == null) {
                    recordsContainer = new ArrayList<>();
                }
                recordsContainer.add(recordContainer);
            }
        } else {
            System.out.println("Could not read file. Aborting");
        }

    }

    protected void populateDropColumnValuesInList(int inputColumnIndex) {
        int rowNumber = 0;
        recordsContainer.clear();
        if (csvParser != null) {
            for (CSVRecord record : csvParser) {
                RecordContainer recordContainer = new RecordContainer(rowNumber);
                rowNumber++;
                recordContainer.setRecord_ID(record.get(primaryKeyColumnIndex));
                recordContainer.setProcessed(false);
                for (int index = 0; index <= inputColumnIndex; index++) {

                    if (dropColumnsIndexes != null && dropColumnsIndexes.size() > 0) {
                        dropColumnValuesInList(index, recordContainer, record);

                    } else {
                        recordContainer.setColumnValueInMap(headerNames.get(inputColumnIndexesForMatching.get(index)), record.get(inputColumnIndexesForMatching.get(index)));
                    }
                }
                if (recordsContainer == null) {
                    recordsContainer = new ArrayList<>();
                }
                recordsContainer.add(recordContainer);
            }
        } else {
            System.out.println("Could not read file. Aborting");
        }
    }

    protected void dropColumnValuesInList(int index, RecordContainer recordContainer, CSVRecord record) {
        boolean matchIndex = true;
        for (int dropColumnsIndex : dropColumnsIndexes) {
            if (index == dropColumnsIndex) {
                matchIndex = false;
                break;
            }
        }
        if (matchIndex) {
            recordContainer.setColumnValueInMap(headerNames.get(inputColumnIndexesForMatching.get(index)), record.get(inputColumnIndexesForMatching.get(index)));
        }
    }

    protected List<Integer> getMatchedRecordIds(RecordContainer recordContainerToMatch, int matchStartIndex, String outputDirectory, String ruleName) throws IOException {
        List<Integer> matchedRecordIds = new ArrayList<>();
        int count = recordsContainer.size();
        float distance;
        float getMatchThreshold;

        for (int index = matchStartIndex; index < count; index++) {
            RecordContainer recordContainer = recordsContainer.get(index);
            if (!recordContainer.isProcessed()) {
                Map<String, String> columnValueMap = recordContainer.getColumnValueMap();
                boolean recordMatched = true;
                for (String key : columnValueMap.keySet()) {
                    distance = JaroWinklerDistance.getDistance(columnValueMap.get(key), recordContainerToMatch.getColumnValueMap().get(key));
                    getMatchThreshold = getThresholdValue(key, outputDirectory, ruleName);
                    if (distance < getMatchThreshold) {
                        return matchedRecordIds;
                    }
                }
                if (recordMatched) {
                    matchedRecordIds.add(recordContainer.getRowNumber());
                    recordContainer.setProcessed(true);
                }

            }
        }
        return matchedRecordIds;
    }

    public float getThresholdValue(String column, String outputDirectory, String ruleName) throws IOException {
        int index = 0;
        int lengthOfPartsOfColumnNames = columnsList.size();
        if (lengthOfPartsOfColumnNames != matchThreshould.size()) {
            errorFileName = new FileWriter(outputDirectory + File.separatorChar + "ErrorFile.csv");
            errorFileName.append("Number Of ColumnNames and Threshould value not same for ::" + ruleName);
            errorFileName.flush();
            errorFileName.close();
            System.exit(0);
        }
        while (index < lengthOfPartsOfColumnNames) {
            if (column.equals(columnsList.get(index))) {
                return (matchThreshould.get(index));
            } else {
                index++;
            }
        }
        return -1;
    }

    protected void matchString(String ruleName) throws IOException {

        int sizeOfRecordsContainer = recordsContainer.size();
        for (int count = 0; count < sizeOfRecordsContainer; count++) {
            RecordContainer recordContainer = recordsContainer.get(count);
            List<Integer> matchRecordIds = getMatchedRecordIds(recordContainer, count + 1, outputDirectory, ruleName);
            recordContainer.setMatchedRecord(matchRecordIds);
            recordContainer.setProcessed(true);
        }
    }

    protected void writeInFile() throws IOException {
        int groupNumber = 0;
        for (RecordContainer recordContainer : recordsContainer) {
            if (recordContainer.getMatchedRecord().size() > 0) {
                groupNumber++;
                String line = getOutputLineForRecord(recordContainer.getRowNumber(), groupNumber);
                outputFile.append(line);
                List<Integer> matchedRowNumbers = recordContainer.getMatchedRecord();
                for (Integer rowNumber : matchedRowNumbers) {
                    line = getOutputLineForRecord(rowNumber, groupNumber);
                    outputFile.append(line);
                }
            }
        }
        outputFile.flush();
        outputFile.close();

    }

    protected String getOutputLineForRecord(int record_id, int groupNumber) {
        RecordContainer recordContainer = recordsContainer.get(record_id);
        StringBuilder sb = new StringBuilder();
        sb.append(recordContainer.getRecord_ID());
        for (String columnName : recordContainer.getColumnValueMap().keySet()) {
            sb.append(",").append(recordContainer.getColumnValueMap().get(columnName));
        }
        sb.append(",").append(groupNumber).append(System.lineSeparator());
        return sb.toString();
    }

    public void generateOutputFile(RecordMatchGenerator recordMatchGenerator) throws Exception {
        recordMatchGenerator.createRuleNameFile();
        for (int index = 0; index < ruleConfiguration.getRuleCount(); index++) {
            recordMatchGenerator.readInputCsvFile();
            recordMatchGenerator.setOutputFile(ruleConfiguration.getRuleName(index));
            recordMatchGenerator.setCoulmnNames(ruleConfiguration.getRuleColumns(index), ruleConfiguration.getRuleName(index));
            recordMatchGenerator.setRulePrimaryKeyColumnIndex(Integer.parseInt(ruleConfiguration.getRulePrimaryKeyColumnIndex(index)));
            recordMatchGenerator.setMatchThresholds(ruleConfiguration.getThresholds(index));
            recordMatchGenerator.populateColumnValuesInList();
            recordMatchGenerator.matchString(ruleConfiguration.getRuleName(index));
            recordMatchGenerator.writeInFile();
        }
        ruleNameFile.flush();
        ruleNameFile.close();

    }

    public void populateMatchSet(RecordMatchGenerator recordMatchGenerator, int index, String ruleName) throws Exception {
        boolean checkCompareListsFlag;
        if (matchSetList != null) {
            matchSetList.clear();
        }
        for (int count = 0; count <= inputColumnIndexesForMatching.size() - 1; count++) {
            recordMatchGenerator.readInputCsvFile();
            recordMatchGenerator.populateDropColumnValuesInList(count);
            recordMatchGenerator.matchString(ruleConfiguration.getRuleName(index));
            Set<String> toBeCompareList = recordMatchGenerator.getSetUptoColumnIndex(count);
            if (toBeCompareList.size() > 0) {
                checkCompareListsFlag = recordMatchGenerator.compareSets(matchSetList, toBeCompareList);
                if (checkCompareListsFlag) {
                    populateDropColumnsIndex(count);
                } else {
                    matchSetList.clear();
                    matchSetList = toBeCompareList;
                }
            }
        }
        if (dropColumnsIndexes.size() > 0) {
            dropColumnNamesAndMatchThreshould(dropColumnsIndexes, ruleName);
        }
        dropColumnsIndexes.clear();
        if (newColumnsNames == null) {
            newColumnsNames = new ArrayList<>();
        }
        newColumnsNames.add(columnsList.toString());
        if (newMatchThreshould == null) {
            newMatchThreshould = new ArrayList<>();
        }
        newMatchThreshould.add(matchThreshould.toString());
        if (rulesNames == null) {
            rulesNames = new ArrayList<>();
        }
        rulesNames.add(ruleName);
    }

    public Set<String> getSetUptoColumnIndex(int index) throws IOException {
        Set<String> toBeCompareList = new HashSet<>();

        for (RecordContainer recordContainer : recordsContainer) {
            if (recordContainer.getMatchedRecord().size() > 0) {
                if (index == 0) {
                    if (matchSetList == null) {
                        matchSetList = new HashSet<>();
                    }
                    matchSetList.add(recordContainer.getRecord_ID());
                } else {
                    toBeCompareList.add(recordContainer.getRecord_ID());
                }
            }
        }
        return toBeCompareList;
    }

    public boolean compareSets(Set<String> matchSetList, Set<String> toBeCompareList) throws IOException {
        RedundantRuleGenerator getLists = new RedundantRuleGenerator();
        boolean isSubset;
        isSubset = getLists.isSubset(toBeCompareList, matchSetList);
        if (isSubset) {
            if (matchSetList.size() == toBeCompareList.size()) {
                return true;
            }
        }
        return isSubset;

    }

    public void populateDropColumnsIndex(int deleteColumnIndex) throws Exception {
        if (dropColumnsIndexes == null) {
            dropColumnsIndexes = new ArrayList<>();
        }
        dropColumnsIndexes.add(deleteColumnIndex);
    }

    public void dropColumnNamesAndMatchThreshould(List<Integer> deleteColumnIndexes, String ruleName) throws Exception {

        List<String> deleteColumnNames = new ArrayList<>();
        for (int index : deleteColumnIndexes) {
            deleteColumnNames.add(columnsList.get(index));
        }
        for (int index = deleteColumnIndexes.size() - 1; index >= 0; index--) {
            matchThreshould.remove((int) deleteColumnIndexes.get(index));
        }
        for (String deleteColumnName : deleteColumnNames) {
            columnsList.remove(deleteColumnName);
        }
        if (deleteColumnNamesAndCorrespondingRulaName == null) {
            deleteColumnNamesAndCorrespondingRulaName = new HashMap<>();
        }
        deleteColumnNamesAndCorrespondingRulaName.put(ruleName, deleteColumnNames.toString().replaceAll(",", "  "));
    }

    public Map<String, String> getMapAllRedundantColumnsAndRuleName() {
        return deleteColumnNamesAndCorrespondingRulaName;
    }

    public List<String> getNewColumnsNames() {
        return newColumnsNames;
    }

    public List<String> getNewMatchThreshould() {
        return newMatchThreshould;
    }

    public List<String> getRules() {
        return rulesNames;
    }

    public List<String> getCoulmnNames() {
        return columnsList;
    }

    public List<Float> getMatchThresholds() {
        return matchThreshould;
    }

    public int getRulePrimaryKeyColumnIndex() {
        return primaryKeyColumnIndex;
    }

}
