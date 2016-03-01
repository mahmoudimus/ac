package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionScope;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.pocketknife.api.querydsl.DatabaseConnection;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLExpressions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
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

    /**
     * This method replaces value in issues according to the scope.
     *
     * @param fieldId field id
     * @param from option to replace
     * @param to option to replace with
     * @param newValueScope scope of the to-option
     * @return at most ten issues for which the replacement could not be performed (due to scope incompatibility)
     */
    public Collection<Long> replace(final FieldId fieldId, final Integer from, final Integer to, ConnectFieldOptionScope newValueScope) {
        QCustomFieldValue CUSTOM_FIELD_VALUE = connectFieldTables.customFieldValue("CFV");
        QCustomFieldValue CUSTOM_FIELD_VALUE_2 = connectFieldTables.customFieldValue("CFV_2");
        QIssue ISSUE = connectFieldTables.issue("I");

        BooleanExpression scopePredicate = newValueScope.getProjectId()
                .map(projectId ->
                        SQLExpressions.select(ISSUE.id)
                                .join(CUSTOM_FIELD_VALUE_2).on(ISSUE.id.eq(CUSTOM_FIELD_VALUE_2.issue))
                                .where(ISSUE.project.eq(newValueScope.getProjectId().get()))
                                .exists())
                .orElse(Expressions.TRUE);

        return databaseAccessor.runInTransaction(databaseConnection -> {
                databaseConnection
                        .update(CUSTOM_FIELD_VALUE)
                        .where(isValue(fieldId, from).and(scopePredicate))
                        .set(CUSTOM_FIELD_VALUE.stringvalue, to.toString())
                        .execute();

            return findIssues(databaseConnection, fieldId, from, PageRequests.request(0L, 10));
        });
    }

    public Collection<Long> findIssues(final FieldId fieldId, final Integer optionId, PageRequest pageRequest) {
        return databaseAccessor.runInTransaction(databaseConnection -> findIssues(databaseConnection, fieldId, optionId, pageRequest)
        );
    }

    private List<Long> findIssues(DatabaseConnection databaseConnection, FieldId fieldId, Integer optionId, PageRequest pageRequest) {
        QCustomFieldValue CUSTOM_FIELD_VALUE = connectFieldTables.customFieldValue("CFV");
        return databaseConnection
                .select(CUSTOM_FIELD_VALUE.issue)
                .from(CUSTOM_FIELD_VALUE)
                .where(isValue(fieldId, optionId))
                .limit(pageRequest.getLimit())
                .offset(pageRequest.getStart())
                .distinct()
                .orderBy(CUSTOM_FIELD_VALUE.issue.asc())
                .fetch();
    }

    private BooleanExpression isValue(final FieldId fieldId, final Integer optionId) {
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
