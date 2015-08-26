package com.twopiradian.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ColumnValuesSet {

    BufferedReader fileName;
    String inputCsvFilePath;
    CSVParser csvParser;
    ArrayList<String> headerNames;

    public  ArrayList<String> readInputCsvFilePathAndColumnIndex(String inputCsvFilePath) throws IOException {
        this.inputCsvFilePath = inputCsvFilePath;
        readInputCsvFile();
        return headerNames;

    }

    private  ArrayList<String> readInputCsvFile() throws FileNotFoundException, IOException {
        csvParser = new CSVParser(new FileReader(inputCsvFilePath), CSVFormat.DEFAULT.withDelimiter(',').withHeader());
        Map<String, Integer> irHeader = csvParser.getHeaderMap();
        headerNames = new ArrayList<>(Arrays.asList((irHeader.keySet()).toArray(new String[0])));
        return headerNames;
    }

    public Set<String> populateSetForColumn(int primaryKeyColumnIndex ) {
        Set<String> populateValuesList = new HashSet<>();
        for (CSVRecord record : csvParser) {
            String valueOfColumn = record.get(primaryKeyColumnIndex);
            populateValuesList.add(valueOfColumn);
        }
        return populateValuesList;
    }

}
