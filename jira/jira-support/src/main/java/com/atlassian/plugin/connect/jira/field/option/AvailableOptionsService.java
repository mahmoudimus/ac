package com.atlassian.plugin.connect.jira.field.option;

import java.util.List;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.plugin.connect.jira.field.FieldId;

public interface AvailableOptionsService
{
    ServiceOutcome<AvailableOption> create(FieldId fieldId, JsonValue value);

    ServiceOutcome<List<AvailableOption>> get(FieldId fieldId);

    ServiceOutcome<AvailableOption> get(FieldId fieldId, Integer optionId);

    ServiceResult delete(FieldId fieldId, Integer valueId);

    ServiceOutcome<AvailableOption> update(FieldId fieldId, AvailableOption value);

    ServiceResult replace(FieldId fieldId, Integer from, Integer to);
}
