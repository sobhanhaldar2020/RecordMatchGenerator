package com.twopiradian.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import static org.apache.commons.collections.CollectionUtils.isSubCollection;

public class RedundantRuleGenerator {

    String[] partsOfLine;

    public void setRuleNames(String[] partsOfLine) {
        this.partsOfLine = partsOfLine;
    }

    public <T> Set<T> getUnionSet(Set<T> list1, Set<T> list2) {
        return new HashSet<>(CollectionUtils.union(list1, list2));
    }

    public <T> String getRuleNameIfRedundantRule(Set<T> list1, Set<T> list2) throws IOException {
        boolean isSubset;

        isSubset = isSubCollection(list1, list2);
        if (isSubset) {
            return partsOfLine[0];
        } else {
            isSubset = isSubCollection(list2, list1);
            if (isSubset) {
                return partsOfLine[1];
            }
        }
        return "";
    }

    public <T> boolean isSubset(Set<T> list1, Set<T> list2) throws IOException {
        boolean isSubset;

        isSubset = isSubCollection(list1, list2);
        return isSubset;
    }

    public <T> Set<T> getIntersectSet(Set<T> list1, Set<T> list2) {
        return new HashSet<>(CollectionUtils.intersection(list1, list2));
    }

    public <T> Set<T> getSubtractSet(Set<T> list1, Set<T> list2) {
        return new HashSet<>(CollectionUtils.subtract(list1, list2));
    }
}
