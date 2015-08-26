package com.twopiradian.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RuleCombinationGenerator {

    private List<String> ruleNames = new ArrayList<>();
    List<String> outputRuleNames = new ArrayList<>();
    String inputFilePath;
    String outputFilePath;
    BufferedReader filename;
    FileWriter outputFile;

    public void readInputAndOutputFilePath(String inputFilePath, String outputFilePath) throws Exception {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        readInputFile();
    }

    protected final void readInputFile() throws Exception {
        filename = new BufferedReader(new FileReader(inputFilePath));
        addListValues();
    }

    protected void addListValues() throws Exception {
        String line;
        while ((line = filename.readLine()) != null) {
            ruleNames.add(line);
        }

        getRuleCombination();

    }

    protected void getRuleCombination() throws IOException {

        int numberOfRules = ruleNames.size();

        for (int index = 0; index < numberOfRules; index++) {
            for (int nextIndex = index + 1; nextIndex < numberOfRules; nextIndex++) {
                outputRuleNames.add((ruleNames.get(index) + " : " + ruleNames.get(nextIndex)));
            }
        }
        writeInFile();
    }

    protected void writeInFile() throws IOException {
        outputFile = new FileWriter(outputFilePath);
        for (String outputRuleName : outputRuleNames) {
            outputFile.append(outputRuleName + "\n");
        }
        outputFile.flush();
        outputFile.close();
    }
}
