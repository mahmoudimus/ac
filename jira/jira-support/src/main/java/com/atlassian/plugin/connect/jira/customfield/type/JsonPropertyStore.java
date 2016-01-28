package com.atlassian.plugin.connect.jira.customfield.type;

import com.atlassian.jira.issue.Issue;

public interface JsonPropertyStore
{
    void storeValue(final String fieldTypeId, String fieldId, Issue issue, String value);

    String loadValue(final String fieldTypeId, String fieldId, Issue issue);
}
