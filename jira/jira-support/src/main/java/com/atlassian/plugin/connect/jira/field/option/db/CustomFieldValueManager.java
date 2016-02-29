package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Optional;

@JiraComponent
public class CustomFieldValueManager {
    private final DatabaseAccessor databaseAccessor;
    private final CustomFieldManager customFieldManager;
    private final ConnectFieldTables connectFieldTables;

    @Autowired
    public CustomFieldValueManager(final DatabaseAccessor databaseAccessor, final CustomFieldManager customFieldManager, final ConnectFieldTables connectFieldTables) {
        this.databaseAccessor = databaseAccessor;
        this.customFieldManager = customFieldManager;
        this.connectFieldTables = connectFieldTables;
    }

    public long remove(final FieldId fieldId, final Integer optionId) {
        QCustomFieldValue CUSTOM_FIELD_VALUE = connectFieldTables.customFieldValue("CFV");
        return databaseAccessor.runInTransaction(databaseConnection ->
                databaseConnection.delete(CUSTOM_FIELD_VALUE).where(isValue(fieldId, optionId)).execute());
    }

    public long replace(final FieldId fieldId, final Integer from, final Integer to) {
        QCustomFieldValue CUSTOM_FIELD_VALUE = connectFieldTables.customFieldValue("CFV");
        return databaseAccessor.runInTransaction(databaseConnection ->
                databaseConnection
                        .update(CUSTOM_FIELD_VALUE)
                        .where(isValue(fieldId, from))
                        .set(CUSTOM_FIELD_VALUE.stringvalue, to.toString())
                        .execute());
    }

    public Collection<Long> findIssues(final FieldId fieldId, final Integer optionId) {
        QCustomFieldValue CUSTOM_FIELD_VALUE = connectFieldTables.customFieldValue("CFV");
        return databaseAccessor.runInTransaction(databaseConnection ->
                        databaseConnection
                                .select(CUSTOM_FIELD_VALUE.issue)
                                .from(CUSTOM_FIELD_VALUE)
                                .where(isValue(fieldId, optionId))
                                .distinct()
                                .orderBy(CUSTOM_FIELD_VALUE.issue.asc())
                                .fetch()
        );
    }

    private Predicate isValue(final FieldId fieldId, final Integer optionId) {
        QCustomFieldValue CUSTOM_FIELD_VALUE = connectFieldTables.customFieldValue("CFV");
        return getCustomFieldId(fieldId)
                .map(id -> CUSTOM_FIELD_VALUE.customfield.eq(id).and(CUSTOM_FIELD_VALUE.stringvalue.eq(optionId.toString())))
                .orElse(Expressions.FALSE);
    }

    private Optional<Long> getCustomFieldId(final FieldId fieldId) {
        return customFieldManager.getCustomFieldObjects().stream()
                .filter(field -> field.getCustomFieldType().getKey().equals(fieldId.getCustomFieldTypeKey()))
                .map(CustomField::getIdAsLong)
                .findFirst();
    }
}
