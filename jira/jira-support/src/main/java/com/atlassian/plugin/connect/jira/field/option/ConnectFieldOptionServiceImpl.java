package com.atlassian.plugin.connect.jira.field.option;

import java.util.Collection;
import java.util.List;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.db.CustomFieldValueManager;
import com.atlassian.plugin.connect.jira.field.option.db.ConnectFieldOptionManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.message.I18nResolver;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;

@JiraComponent
@ExportAsDevService
public class ConnectFieldOptionServiceImpl implements ConnectFieldOptionService
{

    private final ConnectFieldOptionManager connectFieldOptionManager;
    private final I18nResolver i18n;
    private final CustomFieldValueManager customFieldValueManager;

    @Autowired
    public ConnectFieldOptionServiceImpl(final ConnectFieldOptionManager connectFieldOptionManager, final I18nResolver i18n, final CustomFieldValueManager customFieldValueManager)
    {
        this.connectFieldOptionManager = connectFieldOptionManager;
        this.i18n = i18n;
        this.customFieldValueManager = customFieldValueManager;
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> addOption(final FieldId fieldId, final JsonNode value)
    {
        Either<ErrorCollection, ConnectFieldOption> result = connectFieldOptionManager.create(fieldId.getAddonKey(), fieldId.getFieldKey(), value.toString());
        return result.fold(
                ServiceOutcomeImpl::new,
                created -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), created)
        );
    }

    @Override
    public ServiceOutcome<List<ConnectFieldOption>> getAllOptions(final FieldId fieldId)
    {
        return new ServiceOutcomeImpl<>(ErrorCollections.empty(), connectFieldOptionManager.getAll(fieldId.getAddonKey(), fieldId.getFieldKey()));
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> getOption(final FieldId fieldId, final Integer optionId)
    {
        return connectFieldOptionManager.get(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId)
                .map(val -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), val))
                .orElseGet(this::notFound);
    }

    @Override
    public ServiceResult removeOption(final FieldId fieldId, final Integer optionId)
    {
        Collection<Long> issuesWithTheFieldSet = customFieldValueManager.findIssues(fieldId, optionId);
        if (issuesWithTheFieldSet.isEmpty())
        {
            connectFieldOptionManager.delete(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId);
            return new ServiceResultImpl(ErrorCollections.empty());
        }
        else
        {
            return new ServiceResultImpl(ErrorCollections.create(i18n.getText("connect.issue.field.option.delete.used", issuesWithTheFieldSet.toString()), ErrorCollection.Reason.CONFLICT));
        }
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> updateOption(final FieldId fieldId, final ConnectFieldOption option)
    {
        return connectFieldOptionManager.update(fieldId.getAddonKey(), fieldId.getFieldKey(), option.getId(), option.getValue())
                .map(result -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), result))
                .orElseGet(this::notFound);
    }

    @Override
    public ServiceResult replaceInAllIssues(final FieldId fieldId, final Integer from, final Integer to)
    {
        customFieldValueManager.replace(fieldId, from, to);
        return new ServiceResultImpl(ErrorCollections.empty());
    }

    private <T> ServiceOutcomeImpl<T> notFound()
    {
        return new ServiceOutcomeImpl<>(ErrorCollections.create(i18n.getText("connect.issue.field.option.not.found"), NOT_FOUND));
    }
}
