package com.twopiradian.services;

import com.twopiradian.generators.RecordMatchGenerator;
import com.twopiradian.model.RuleConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

public class RedundantRuleAttributesGenerator {

    RuleConfiguration ruleConfiguration;
    FileWriter redundantRuleAttributes;
    FileWriter afterDeletedRedundantAttribute;
    String outputDirectory;

    public RedundantRuleAttributesGenerator(RuleConfiguration ruleConfiguration, String outputDirectory) throws Exception {
        this.ruleConfiguration = ruleConfiguration;
        this.outputDirectory = outputDirectory;

    }

    public void deleteRedundanrColumnName(RecordMatchGenerator recordMatchGenerator) throws Exception {
        for (int index = 0; index < ruleConfiguration.getRuleCount(); index++) {
            recordMatchGenerator.readInputCsvFile();
            recordMatchGenerator.setCoulmnNames(ruleConfiguration.getRuleColumns(index), ruleConfiguration.getRuleName(index));
            recordMatchGenerator.setRulePrimaryKeyColumnIndex(Integer.parseInt(ruleConfiguration.getRulePrimaryKeyColumnIndex(index)));
            recordMatchGenerator.setMatchThresholds(ruleConfiguration.getThresholds(index));
            recordMatchGenerator.populateMatchSet(recordMatchGenerator, index, ruleConfiguration.getRuleName(index));
        }
        writeTheRedundantColumnNamesAsCSV(recordMatchGenerator);
    }

    protected void writeTheRedundantColumnNamesAsCSV(RecordMatchGenerator recordMatchGenerator) throws Exception {

        Map<String, String> deleteColumnNamesAndCorrespondingRulaName = recordMatchGenerator.getMapAllRedundantColumnsAndRuleName();
        redundantRuleAttributes = new FileWriter(outputDirectory + File.separatorChar + "RedundantRuleAttributes.csv");
        redundantRuleAttributes.append("Rule" + "," + "Redundant Column/Columns" + "\n");
        for (Map.Entry<String, String> entryDeleteColumnNamesAndCorrespondingRule : deleteColumnNamesAndCorrespondingRulaName.entrySet()) {
            redundantRuleAttributes.append(entryDeleteColumnNamesAndCorrespondingRule.getKey() + "," + entryDeleteColumnNamesAndCorrespondingRule.getValue() + "\n");
        }

        redundantRuleAttributes.flush();
        redundantRuleAttributes.close();
        writeAfterDeletedRedundantAttributesAsCSV(recordMatchGenerator);
    }

    protected void writeAfterDeletedRedundantAttributesAsCSV(RecordMatchGenerator recordMatchGenerator) throws Exception {
        List<String> newColumnsNames = recordMatchGenerator.getNewColumnsNames();
        List<String> newMatchThreshould = recordMatchGenerator.getNewMatchThreshould();
        List<String> rulesNames = recordMatchGenerator.getRules();
        afterDeletedRedundantAttribute = new FileWriter(outputDirectory + File.separatorChar + "AfterDeletedRedundantAttribute.csv");
        afterDeletedRedundantAttribute.append("RuleNames" + "," + "ColumnNames" + "," + "MatchThreshould" + "\n");

        for (int index = 0; index < newColumnsNames.size(); index++) {
            afterDeletedRedundantAttribute.append(rulesNames.get(index).trim() + "," + newColumnsNames.get(index).replaceAll(",", "  ") + "," + newMatchThreshould.get(index).replaceAll(",", "  ") + "\n");
        }
        afterDeletedRedundantAttribute.flush();
        afterDeletedRedundantAttribute.close();
    }

}
