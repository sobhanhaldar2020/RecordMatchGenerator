package com.twopiradian.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordContainer {

    protected int rowNumber;
    protected String record_ID;
    protected boolean processed;
    protected Map<String, String> columnValueMap = new HashMap<String, String>();
    protected List<Integer> matchedRecord = new ArrayList<>();

    public RecordContainer(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public void setRecord_ID(String record_ID) {
        this.record_ID = record_ID;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public void setColumnValueMap(Map<String, String> columnValue) {
        this.columnValueMap = columnValue;
    }

    public void setMatchedRecord(List<Integer> matchedRecord) {
        this.matchedRecord = matchedRecord;
    }

    public String getRecord_ID() {
        return record_ID;
    }

    public boolean isProcessed() {
        return processed;
    }

    public Map<String, String> getColumnValueMap() {
        return columnValueMap;
    }

    public List<Integer> getMatchedRecord() {
        return matchedRecord;
    }

    public void setColumnValueInMap(String columnName, String value) {
        columnValueMap.put(columnName, value);
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }
}
