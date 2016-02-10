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
import com.atlassian.plugin.connect.jira.field.option.db.CustomFieldManager;
import com.atlassian.plugin.connect.jira.field.option.db.RemoteFieldOptionManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.message.I18nResolver;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;

@JiraComponent
@ExportAsDevService
public class RemoteFieldOptionServiceImpl implements RemoteFieldOptionService
{

    private final RemoteFieldOptionManager remoteFieldOptionManager;
    private final I18nResolver i18n;
    private final CustomFieldManager customFieldManager;

    @Autowired
    public RemoteFieldOptionServiceImpl(final RemoteFieldOptionManager remoteFieldOptionManager, final I18nResolver i18n, final CustomFieldManager customFieldManager)
    {
        this.remoteFieldOptionManager = remoteFieldOptionManager;
        this.i18n = i18n;
        this.customFieldManager = customFieldManager;
    }

    @Override
    public ServiceOutcome<RemoteFieldOption> addOption(final FieldId fieldId, final JsonNode value)
    {
        Either<ErrorCollection, RemoteFieldOption> result = remoteFieldOptionManager.create(fieldId.getAddonKey(), fieldId.getFieldKey(), value.toString());
        return result.fold(
                ServiceOutcomeImpl::new,
                created -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), created)
        );
    }

    @Override
    public ServiceOutcome<List<RemoteFieldOption>> getAllOptions(final FieldId fieldId)
    {
        return new ServiceOutcomeImpl<>(ErrorCollections.empty(), remoteFieldOptionManager.getAll(fieldId.getAddonKey(), fieldId.getFieldKey()));
    }

    @Override
    public ServiceOutcome<RemoteFieldOption> getOption(final FieldId fieldId, final Integer optionId)
    {
        return remoteFieldOptionManager.get(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId)
                .map(val -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), val))
                .orElseGet(this::notFound);
    }

    @Override
    public ServiceResult removeOption(final FieldId fieldId, final Integer optionId)
    {
        Collection<Long> issuesWithTheFieldSet = customFieldManager.findIssues(fieldId, optionId);
        if (issuesWithTheFieldSet.isEmpty())
        {
            remoteFieldOptionManager.delete(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId);
            return new ServiceResultImpl(ErrorCollections.empty());
        }
        else
        {
            return new ServiceResultImpl(ErrorCollections.create(i18n.getText("connect.issue.field.option.delete.used", issuesWithTheFieldSet.toString()), ErrorCollection.Reason.CONFLICT));
        }
    }

    @Override
    public ServiceOutcome<RemoteFieldOption> updateOption(final FieldId fieldId, final RemoteFieldOption option)
    {
        return remoteFieldOptionManager.update(fieldId.getAddonKey(), fieldId.getFieldKey(), option.getId(), option.getValue())
                .map(result -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), result))
                .orElseGet(this::notFound);
    }

    @Override
    public ServiceResult replaceInAllIssues(final FieldId fieldId, final Integer from, final Integer to)
    {
        customFieldManager.replace(fieldId, from, to);
        return new ServiceResultImpl(ErrorCollections.empty());
    }

    private <T> ServiceOutcomeImpl<T> notFound()
    {
        return new ServiceOutcomeImpl<>(ErrorCollections.create(i18n.getText("connect.issue.field.option.not.found"), NOT_FOUND));
    }
}
