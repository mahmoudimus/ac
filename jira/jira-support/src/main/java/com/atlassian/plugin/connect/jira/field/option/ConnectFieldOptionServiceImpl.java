package com.atlassian.plugin.connect.jira.field.option;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.connect.api.auth.AddonDataAccessChecker;
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

import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;
import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.errorOutcome;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.errorResult;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.successOutcome;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.successResult;

@JiraComponent
@ExportAsDevService
@ParametersAreNonnullByDefault
public class ConnectFieldOptionServiceImpl implements ConnectFieldOptionService {

    private final ConnectFieldOptionManager connectFieldOptionManager;
    private final I18nResolver i18n;
    private final CustomFieldValueManager customFieldValueManager;
    private final CustomFieldManager customFieldManager;
    private final AddonDataAccessChecker addonDataAccessChecker;

    @Autowired
    public ConnectFieldOptionServiceImpl(final ConnectFieldOptionManager connectFieldOptionManager, final I18nResolver i18n, final CustomFieldValueManager customFieldValueManager, final CustomFieldManager customFieldManager, final AddonDataAccessChecker addonDataAccessChecker) {
        this.connectFieldOptionManager = connectFieldOptionManager;
        this.i18n = i18n;
        this.customFieldValueManager = customFieldValueManager;
        this.customFieldManager = customFieldManager;
        this.addonDataAccessChecker = addonDataAccessChecker;
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> addOption(AuthenticationData auth, final FieldId fieldId, final JsonNode value, final ConnectFieldOptionScope scope) {
        return authenticated(auth, fieldId, () -> {
            Either<ErrorCollection, ConnectFieldOption> result = connectFieldOptionManager.create(fieldId.getAddonKey(), fieldId.getFieldKey(), value.toString(), scope);
            return result.<ServiceOutcome<ConnectFieldOption>>fold(
                    ServiceOutcomes::errorOutcome,
                    ServiceOutcomes::successOutcome);
        });
    }

    @Override
    public ServiceOutcome<Page<ConnectFieldOption>> getOptions(AuthenticationData auth, final FieldId fieldId, PageRequest pageRequest) {
        return getOptions(auth, fieldId, pageRequest, null);
    }

    @Override
    public ServiceOutcome<Page<ConnectFieldOption>> getOptions(AuthenticationData auth, final FieldId fieldId, PageRequest pageRequest, @Nullable ConnectFieldOptionScope scope) {
        return authenticated(auth, fieldId, () -> {
            Page<ConnectFieldOption> options = connectFieldOptionManager.getAll(fieldId.getAddonKey(), fieldId.getFieldKey(), pageRequest, scope);
            return successOutcome(options);
        });
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> getOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId) {
        return authenticated(auth, fieldId, () -> getOption(fieldId, optionId));
    }

    private ServiceOutcome<ConnectFieldOption> getOption(final FieldId fieldId, final Integer optionId) {
        return connectFieldOptionManager.get(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId)
                .map(ServiceOutcomes::successOutcome)
                .orElseGet(() -> notFound(optionId));
    }

    @Override
    public ServiceResult removeOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId) {
        return authenticated(auth, fieldId, () -> {
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
        return authenticated(auth, fieldId, () ->
                connectFieldOptionManager.save(fieldId.getAddonKey(), fieldId.getFieldKey(), option)
                        .map(ServiceOutcomes::successOutcome)
                        .orElseGet(() -> notFound(option.getId())));
    }

    @Override
    public ServiceOutcome<Boolean> replaceInAllIssues(AuthenticationData auth, final FieldId fieldId, final Integer from, final Integer to) {
        return this.<ServiceOutcome<Boolean>>authenticated(auth, fieldId, () -> {

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
    private <T extends ServiceResult> T authenticated(final AuthenticationData authenticationData, final FieldId fieldId, final Supplier<T> action) {
        ServiceResult hasAccess = hasAccess(authenticationData, fieldId);
        return hasAccess.isValid() ? action.get() : (T) new ServiceOutcomeImpl<>(hasAccess.getErrorCollection());
    }

    private ServiceResult hasAccess(AuthenticationData auth, final FieldId fieldId) {
        if (!addonDataAccessChecker.hasAccessToAddon(auth, fieldId.getAddonKey())) {
            return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.addon", fieldId.getAddonKey()), FORBIDDEN));
        }

        if (!fieldExists(fieldId)) {
            return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.field", fieldId.getAddonKey(), fieldId.getFieldKey()), FORBIDDEN));
        }

        return successResult();
    }

    private boolean fieldExists(final FieldId fieldId) {
        return customFieldManager.getCustomFieldType(fieldId.getCustomFieldTypeKey()) != null;
    }

    private <T> ServiceOutcome<T> notFound(int optionId) {
        return errorOutcome(ErrorCollections.create(i18n.getText("connect.issue.field.option.not.found", optionId), NOT_FOUND));
    }
}
