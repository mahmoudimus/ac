package com.atlassian.plugin.connect.bitbucket.web.condition;

import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class BitbucketConditionClassResolver implements ConnectConditionClassResolver {
    @Override
    public List<Entry> getEntries() {
        return ImmutableList.of();
    }
}
