package com.twopiradian.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.FileReader;

public class RuleConfiguration {

    Gson gson = new Gson();
    String ruleFileName = "";
    JsonArray rules;

    public RuleConfiguration(String ruleFileName) {
        this.ruleFileName = ruleFileName;
    }

    public void processRuleFile() throws Exception {
        FileReader reader = new FileReader(ruleFileName);
        rules = gson.fromJson(reader, JsonArray.class);
    }

    public int getRuleCount() {
        return rules.size();
    }

    public String getRuleColumns(int index) {
        return rules.get(index).getAsJsonObject().get("ColumnNames").getAsString();
    }

    public String getThresholds(int index) {
        return rules.get(index).getAsJsonObject().get("MatchThreshould").getAsString();
    }

    public String getRuleName(int index) {
        return rules.get(index).getAsJsonObject().get("Name").getAsString();
    }

    public String getRulePrimaryKeyColumnIndex(int index) {
        return rules.get(index).getAsJsonObject().get("PrimaryKeyColumnIndex").getAsString();
    }

}
