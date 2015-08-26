package com.twopiradian.generators;

import com.twopiradian.services.RedundantRuleAttributesGenerator;
import com.twopiradian.model.RuleConfiguration;
import com.twopiradian.services.RuleCombinationGenerator;
import com.twopiradian.services.RedundantRuleGenerator;
import com.twopiradian.services.ColumnValuesSet;
import com.twopiradian.services.ColumnsNamesGeneratorInOrder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Launcher {

    private String inputJsonFilePath;
    private String outputDirectory;
    private String inputCsvFilePath;
    private String ruleNameFilePath;
    private String setOfRuleNamesFilePath;
    private String redundantRuleContainerFilePath;
    private String ruleName;
    private String fileName;
    BufferedReader bufferReader;
    Set<String> ruleOneKeys;
    Set<String> ruleTwoKeys;
    FileWriter outputFileName;
    Set<String> ruleSpecificKeysSet;
    Map<String, Set<String>> ruleRuleKeysMap;
    Properties configProperties = new Properties();
    ArrayList<String> headerNames;

    private void readInputPropertiesFile(String propertiesFilePath) {
        try (FileInputStream fileInput = new FileInputStream(new File(propertiesFilePath))) {
            if (fileInput != null) {
                if (configProperties != null) {
                    configProperties.load(fileInput);
                }
            }
            inputJsonFilePath = configProperties.getProperty("inputJsonFilePath");
            inputCsvFilePath = configProperties.getProperty("inputCsvFilePath");
            outputDirectory = configProperties.getProperty("outputDirectory");
            ruleNameFilePath = configProperties.getProperty("ruleNameFilePath");
            setOfRuleNamesFilePath = configProperties.getProperty("setOfRuleNamesFilePath");
            redundantRuleContainerFilePath = configProperties.getProperty("redundantRuleContainerFilePath");

        } catch (IOException | NullPointerException e) {
            System.out.println("The Exception is " + e.getMessage());
        }

    }

    private void populateMapToStoreRuleNameAndIdsList(RuleConfiguration ruleConfiguration) throws IOException {
        ColumnValuesSet valuesForColumn = new ColumnValuesSet();
        for (int index = 0; index < ruleConfiguration.getRuleCount(); index++) {
            ruleName = ruleConfiguration.getRuleName(index);
            fileName = outputDirectory + File.separator + ruleName + ".csv";
            headerNames = valuesForColumn.readInputCsvFilePathAndColumnIndex(fileName);
            ruleSpecificKeysSet = valuesForColumn.populateSetForColumn(0);
            if (ruleRuleKeysMap == null) {
                ruleRuleKeysMap = new HashMap<>();
            }
            ruleRuleKeysMap.put(ruleName, ruleSpecificKeysSet);
        }
    }

    private void writeRedundantRulesAsCSV() throws IOException {

        RedundantRuleGenerator redundantRuleGenerator = new RedundantRuleGenerator();
        bufferReader = new BufferedReader(new FileReader(setOfRuleNamesFilePath));
        outputFileName = new FileWriter(redundantRuleContainerFilePath);
        String partOfLine;
        Set<String> intersectList;
        String[] ruleNames;
        String line;
        outputFileName.append("RuleOne " + "," + "RuleTwo " + "," + "RuleOne Cardinality " + "," + "RuleTwo Cardinality " + "," + "Intersect Count " + "," + "Redundant Rule " + "," + "\n");
        while ((line = bufferReader.readLine()) != null) {

            ruleNames = line.split(":");
            ruleOneKeys = ruleRuleKeysMap.get(ruleNames[0].trim());
            ruleTwoKeys = ruleRuleKeysMap.get(ruleNames[1].trim());
            redundantRuleGenerator.setRuleNames(ruleNames);
            intersectList = redundantRuleGenerator.getIntersectSet(ruleOneKeys, ruleTwoKeys);
            partOfLine = redundantRuleGenerator.getRuleNameIfRedundantRule(ruleOneKeys, ruleTwoKeys);
            outputFileName.append(ruleNames[0].trim() + "," + ruleNames[1].trim() + "," + ruleOneKeys.size() + "," + ruleTwoKeys.size() + "," + intersectList.size() + "," + partOfLine.trim() + "\n");
        }
        outputFileName.flush();
        outputFileName.close();
    }

    public static void main(String[] args) throws Exception {

        Launcher launcher = new Launcher();
//        String proprttiesFilePath = args[0];
//        launcher.readInputPropertiesFile(proprttiesFilePath);
        launcher.readInputPropertiesFile("./src/main/java/resource/config.properties");
        RuleConfiguration ruleConfiguration = new RuleConfiguration(launcher.inputJsonFilePath);
        ruleConfiguration.processRuleFile();
        RecordMatchGenerator recordMatchGenerator = new RecordMatchGenerator(ruleConfiguration, launcher.ruleNameFilePath, launcher.outputDirectory, launcher.inputCsvFilePath);
        recordMatchGenerator.generateOutputFile(recordMatchGenerator);
        RuleCombinationGenerator ruleCombinationGenerator = new RuleCombinationGenerator();
        ruleCombinationGenerator.readInputAndOutputFilePath(launcher.ruleNameFilePath, launcher.setOfRuleNamesFilePath);
        launcher.populateMapToStoreRuleNameAndIdsList(ruleConfiguration);
        launcher.writeRedundantRulesAsCSV();
        RedundantRuleAttributesGenerator identifyRedundantColumnAttribute = new RedundantRuleAttributesGenerator(ruleConfiguration, launcher.outputDirectory);
        identifyRedundantColumnAttribute.deleteRedundanrColumnName(recordMatchGenerator);
        ColumnsNamesGeneratorInOrder columnsNamesGeneratorInOrder = new ColumnsNamesGeneratorInOrder(ruleConfiguration, launcher.outputDirectory, launcher.inputCsvFilePath, recordMatchGenerator);
        columnsNamesGeneratorInOrder.columnsNamesInOrder();
    }
}
