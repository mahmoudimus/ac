package com.atlassian.plugin.connect.jira.field.option;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.plugin.connect.api.auth.scope.AddonKeyExtractor;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.db.ConnectFieldOptionManager;
import com.atlassian.plugin.connect.jira.field.option.db.CustomFieldValueManager;
import com.atlassian.plugin.connect.jira.util.ServiceOutcomes;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

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
public class ConnectFieldOptionServiceImpl implements ConnectFieldOptionService
{

    private final ConnectFieldOptionManager connectFieldOptionManager;
    private final I18nResolver i18n;
    private final AddonKeyExtractor addonKeyExtractor;
    private final UserManager userManager;
    private final CustomFieldValueManager customFieldValueManager;
    private final CustomFieldManager customFieldManager;

    @Autowired
    public ConnectFieldOptionServiceImpl(final ConnectFieldOptionManager connectFieldOptionManager, final I18nResolver i18n, final AddonKeyExtractor addonKeyExtractor, final UserManager userManager, final CustomFieldValueManager customFieldValueManager, final CustomFieldManager customFieldManager)
    {
        this.connectFieldOptionManager = connectFieldOptionManager;
        this.i18n = i18n;
        this.addonKeyExtractor = addonKeyExtractor;
        this.userManager = userManager;
        this.customFieldValueManager = customFieldValueManager;
        this.customFieldManager = customFieldManager;
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> addOption(AuthenticationData auth, final FieldId fieldId, final JsonNode value)
    {
        return authenticated(auth, fieldId, () -> {
            Either<ErrorCollection, ConnectFieldOption> result = connectFieldOptionManager.create(fieldId.getAddonKey(), fieldId.getFieldKey(), value.toString());
            return result.<ServiceOutcome<ConnectFieldOption>>fold(
                    ServiceOutcomes::errorOutcome,
                    ServiceOutcomes::successOutcome);
        });
    }

    @Override
    public ServiceOutcome<List<ConnectFieldOption>> getAllOptions(AuthenticationData auth, final FieldId fieldId)
    {
        return authenticated(auth, fieldId, () ->
                successOutcome(connectFieldOptionManager.getAll(fieldId.getAddonKey(), fieldId.getFieldKey())));
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> getOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId)
    {
        return authenticated(auth, fieldId, () -> getOption(fieldId, optionId));
    }

    private ServiceOutcome<ConnectFieldOption> getOption(final FieldId fieldId, final Integer optionId)
    {
        return connectFieldOptionManager.get(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId)
                .map(ServiceOutcomes::successOutcome)
                .orElseGet(() -> notFound(optionId));
    }

    @Override
    public ServiceResult removeOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId)
    {
        return authenticated(auth, fieldId, () -> {
            Collection<Long> issuesWithTheFieldSet = customFieldValueManager.findIssues(fieldId, optionId);
            if (issuesWithTheFieldSet.isEmpty())
            {
                connectFieldOptionManager.delete(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId);
                return successResult();
            }
            else
            {
                return ServiceOutcomes.errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.delete.used", issuesWithTheFieldSet.toString()), ErrorCollection.Reason.CONFLICT));
            }
        });
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> putOption(AuthenticationData auth, final FieldId fieldId, final ConnectFieldOption option)
    {
        return authenticated(auth, fieldId, () ->
                connectFieldOptionManager.save(fieldId.getAddonKey(), fieldId.getFieldKey(), option.getId(), option.getValue())
                        .map(ServiceOutcomes::successOutcome)
                        .orElseGet(() -> notFound(option.getId())));
    }

    @Override
    public ServiceResult replaceInAllIssues(AuthenticationData auth, final FieldId fieldId, final Integer from, final Integer to)
    {
        return authenticated(auth, fieldId, () -> {

            if (from.equals(to))
            {
                return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.replace.equal"), VALIDATION_FAILED));
            }

            return ServiceOutcomes.toEither(getOption(fieldId, to))
                    .left().map(ServiceOutcomes::errorResult)
                    .left().on(toOption -> {
                        customFieldValueManager.replace(fieldId, from, to);
                        return successResult();
                    });
        });
    }

    private <T extends ServiceResult> T authenticated(final AuthenticationData authenticationData, final FieldId fieldId, final Supplier<T> action)
    {
        ServiceResult hasAccess = authenticationData.accept(new AuthenticationData.AuthenticationDetailsVisitor<ServiceResult>()
        {
            @Override
            public ServiceResult visit(final AuthenticationData.Request authenticationBy)
            {
                HttpServletRequest request = authenticationBy.getRequest();
                return hasAccess(userManager.getRemoteUser(request), addonKeyExtractor.extractClientKey(request), fieldId);
            }

            @Override
            public ServiceResult visit(final AuthenticationData.AddonKey authenticationBy)
            {
                return hasAccess(null, authenticationBy.getAddonKey(), fieldId);
            }

            @Override
            public ServiceResult visit(final AuthenticationData.User authenticationBy)
            {
                return hasAccess(authenticationBy.getUser(), null, fieldId);
            }
        });
        return hasAccess.isValid() ? action.get() : (T) new ServiceOutcomeImpl<>(hasAccess.getErrorCollection());
    }

    private ServiceResult hasAccess(@Nullable final UserProfile user, @Nullable final String currentAddOnKey, final FieldId fieldId)
    {
        boolean hasAccess = currentAddOnKey != null && currentAddOnKey.equals(fieldId.getAddonKey()) ||
                user != null && userManager.isSystemAdmin(user.getUserKey());

        if (!hasAccess)
        {
            return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.addon", fieldId.getAddonKey()), FORBIDDEN));
        }

        if (!fieldExists(fieldId))
        {
            return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.field", fieldId.getAddonKey(), fieldId.getFieldKey()), FORBIDDEN));
        }

        return successResult();
    }

    private boolean fieldExists(final FieldId fieldId)
    {
        return customFieldManager.getCustomFieldType(fieldId.getCustomFieldTypeKey()) != null;
    }

    private <T> ServiceOutcome<T> notFound(int optionId)
    {
        return errorOutcome(ErrorCollections.create(i18n.getText("connect.issue.field.option.not.found", optionId), NOT_FOUND));
    }
}
