package com.atlassian.plugin.connect.jira.field.option;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.connect.api.auth.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.db.ConnectFieldOptionManager;
import com.atlassian.plugin.connect.jira.field.option.db.CustomFieldValueManager;
import com.atlassian.plugin.connect.jira.util.ServiceOutcomes;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.message.I18nResolver;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Supplier;

import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;
import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.errorOutcome;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.successOutcome;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.successResult;
import static com.google.common.base.Preconditions.checkNotNull;

@JiraComponent
@ExportAsDevService
@ParametersAreNonnullByDefault
public class ConnectFieldOptionServiceImpl implements ConnectFieldOptionService {

    private final ConnectFieldOptionManager connectFieldOptionManager;
    private final I18nResolver i18n;
    private final CustomFieldValueManager customFieldValueManager;
    private final ConnectFieldOptionAuthorizer connectFieldOptionAuthorizer;

    @Autowired
    public ConnectFieldOptionServiceImpl(final ConnectFieldOptionManager connectFieldOptionManager, final I18nResolver i18n, final CustomFieldValueManager customFieldValueManager, ConnectFieldOptionAuthorizer connectFieldOptionAuthorizer) {
        this.connectFieldOptionManager = connectFieldOptionManager;
        this.i18n = i18n;
        this.customFieldValueManager = customFieldValueManager;
        this.connectFieldOptionAuthorizer = connectFieldOptionAuthorizer;
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> addOption(AuthenticationData auth, final FieldId fieldId, final JsonNode value, final ConnectFieldOptionScope scope) {
        return withAdminAccess(auth, fieldId, () -> {
            Either<ErrorCollection, ConnectFieldOption> result = connectFieldOptionManager.create(fieldId.getAddonKey(), fieldId.getFieldKey(), value.toString(), scope);
            return result.<ServiceOutcome<ConnectFieldOption>>fold(
                    ServiceOutcomes::errorOutcome,
                    ServiceOutcomes::successOutcome);
        });
    }

    @Override
    public ServiceOutcome<Page<ConnectFieldOption>> getOptions(AuthenticationData auth, final FieldId fieldId, PageRequest pageRequest) {
        return withAdminAccess(auth, fieldId, () -> getOptions(fieldId, pageRequest, null));
    }

    @Override
    public ServiceOutcome<Page<ConnectFieldOption>> getOptions(AuthenticationData auth, final FieldId fieldId, PageRequest pageRequest, ConnectFieldOptionScope scope) {
        return withReadAccess(auth, fieldId, checkNotNull(scope), () -> getOptions(fieldId, pageRequest, scope));
    }

    private ServiceOutcome<Page<ConnectFieldOption>> getOptions(FieldId fieldId, PageRequest pageRequest, @Nullable ConnectFieldOptionScope scope) {
        Page<ConnectFieldOption> options = connectFieldOptionManager.getAll(fieldId.getAddonKey(), fieldId.getFieldKey(), pageRequest, scope);
        return successOutcome(options);
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> getOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId) {
        return withAdminAccess(auth, fieldId, () -> getOption(fieldId, optionId));
    }

    private ServiceOutcome<ConnectFieldOption> getOption(final FieldId fieldId, final Integer optionId) {
        return connectFieldOptionManager.get(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId)
                .map(ServiceOutcomes::successOutcome)
                .orElseGet(() -> notFound(optionId));
    }

    @Override
    public ServiceResult removeOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId) {
        return withAdminAccess(auth, fieldId, () -> {
            Collection<Long> issuesWithTheFieldSet = customFieldValueManager.findIssues(fieldId, optionId, PageRequests.request(0L, 10));
            if (issuesWithTheFieldSet.isEmpty()) {
                connectFieldOptionManager.delete(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId);
                return successResult();
            } else {
                return ServiceOutcomes.errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.delete.used", issuesWithTheFieldSet.toString()), ErrorCollection.Reason.CONFLICT));
            }
        });
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> putOption(AuthenticationData auth, final FieldId fieldId, final ConnectFieldOption option) {
        return withAdminAccess(auth, fieldId, () ->
                connectFieldOptionManager.save(fieldId.getAddonKey(), fieldId.getFieldKey(), option)
                        .map(ServiceOutcomes::successOutcome)
                        .orElseGet(() -> notFound(option.getId())));
    }

    @Override
    public ServiceOutcome<Boolean> replaceInAllIssues(AuthenticationData auth, final FieldId fieldId, final Integer from, final Integer to) {
        return this.<ServiceOutcome<Boolean>>withAdminAccess(auth, fieldId, () -> {

            if (from.equals(to)) {
                return errorOutcome(ErrorCollections.create(i18n.getText("connect.issue.field.option.replace.equal"), VALIDATION_FAILED));
            }

            return ServiceOutcomes.toEither(getOption(fieldId, to))
                    .left().<ServiceOutcome<Boolean>>map(ServiceOutcomes::errorOutcome)
                    .left().on(newValue -> {
                        Collection<Long> leftIssues = customFieldValueManager.replace(fieldId, from, to, newValue.getScope());
                        return successOutcome(leftIssues.isEmpty());
                    });
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends ServiceResult> T withAdminAccess(final AuthenticationData authenticationData, final FieldId fieldId, final Supplier<T> action) {
        ServiceResult hasAccess = connectFieldOptionAuthorizer.hasAdminAccess(authenticationData, fieldId);
        return hasAccess.isValid() ? action.get() : (T) new ServiceOutcomeImpl<>(hasAccess.getErrorCollection());
    }

    @SuppressWarnings("unchecked")
    private <T extends ServiceResult> T withReadAccess(final AuthenticationData authenticationData, FieldId fieldId, ConnectFieldOptionScope scope, final Supplier<T> action) {
        ServiceResult hasAccess = connectFieldOptionAuthorizer.hasReadAccess(authenticationData, fieldId, scope);
        return hasAccess.isValid() ?
                action.get() :
                (T) new ServiceOutcomeImpl<>(hasAccess.getErrorCollection());
    }


    private <T> ServiceOutcome<T> notFound(int optionId) {
        return errorOutcome(ErrorCollections.create(i18n.getText("connect.issue.field.option.not.found", optionId), NOT_FOUND));
    }
}
