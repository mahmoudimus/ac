package com.atlassian.plugin.connect.jira.field.option;

import java.util.Map;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
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

    private Map<String, Object> getSelectClause(FieldId fieldId, Integer optionId)
    {
        return ImmutableMap.of("customfield", fieldId.getFullKey(), VALUE_COLUMN, optionId);
    }
}
