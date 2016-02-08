package com.atlassian.plugin.connect.jira.field.option;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class CustomFieldDao
{
    public static final String CF_VALUE_TABLE_NAME = "CustomFieldValue";
    public static final String VALUE_COLUMN = "numbervalue";

    private final OfBizDelegator ofBizDelegator;

    @Autowired
    public CustomFieldDao(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public int remove(final FieldId fieldId, final Integer optionId)
    {
        return ofBizDelegator.removeByAnd(CF_VALUE_TABLE_NAME, getSelectClause(fieldId, optionId));
    }

    public int replace(final FieldId fieldId, final Integer from, final Integer to)
    {
        return ofBizDelegator.bulkUpdateByAnd(
                CF_VALUE_TABLE_NAME,
                ImmutableMap.of(VALUE_COLUMN, to),
                getSelectClause(fieldId, from));
    }

    public Collection<Long> findIssues(final FieldId fieldId, final Integer optionId)
    {
        List<GenericValue> fields = ofBizDelegator.findByAnd(CF_VALUE_TABLE_NAME, getSelectClause(fieldId, optionId));
        return fields.stream().map(gv -> gv.getLong("issue")).collect(Collectors.toCollection(TreeSet::new));
    }

    private Map<String, Object> getSelectClause(FieldId fieldId, Integer optionId)
    {
        return ImmutableMap.of("customfield", fieldId.getFullKey(), VALUE_COLUMN, optionId);
    }
}
