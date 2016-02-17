package com.atlassian.plugin.connect.jira.field.option;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.plugin.connect.api.auth.scope.AddonKeyExtractor;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.db.ConnectFieldOptionManager;
import com.atlassian.plugin.connect.jira.field.option.db.CustomFieldValueManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;

@JiraComponent
@ExportAsDevService
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
            return result.fold(
                    ServiceOutcomeImpl::new,
                    created -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), created)
            );
        });
    }

    @Override
    public ServiceOutcome<List<ConnectFieldOption>> getAllOptions(AuthenticationData auth, final FieldId fieldId)
    {
        return authenticated(auth, fieldId, () ->
                new ServiceOutcomeImpl<>(ErrorCollections.empty(), connectFieldOptionManager.getAll(fieldId.getAddonKey(), fieldId.getFieldKey())));
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> getOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId)
    {
        return authenticated(auth, fieldId, () ->
                connectFieldOptionManager.get(fieldId.getAddonKey(), fieldId.getFieldKey(), optionId)
                        .map(val -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), val))
                        .orElseGet(this::notFound));
    }

    @Override
    public ServiceResult removeOption(AuthenticationData auth, final FieldId fieldId, final Integer optionId)
    {
        return authenticated(auth, fieldId, () -> {
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
        });
    }

    @Override
    public ServiceOutcome<ConnectFieldOption> putOption(AuthenticationData auth, final FieldId fieldId, final ConnectFieldOption option)
    {
        return authenticated(auth, fieldId, () ->
                connectFieldOptionManager.save(fieldId.getAddonKey(), fieldId.getFieldKey(), option.getId(), option.getValue())
                        .map(result -> new ServiceOutcomeImpl<>(ErrorCollections.empty(), result))
                        .orElseGet(this::notFound));
    }

    @Override
    public ServiceResult replaceInAllIssues(AuthenticationData auth, final FieldId fieldId, final Integer from, final Integer to)
    {
        return authenticated(auth, fieldId, () -> {
            customFieldValueManager.replace(fieldId, from, to);
            return new ServiceResultImpl(ErrorCollections.empty());
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

    private ServiceResult hasAccess(final UserProfile user, final String currentAddOnKey, final FieldId fieldId)
    {
        boolean hasAccess = currentAddOnKey != null && currentAddOnKey.equals(fieldId.getAddonKey()) ||
                user != null && userManager.isSystemAdmin(user.getUserKey());

        if (!hasAccess)
        {
            return new ServiceResultImpl(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.addon", fieldId.getAddonKey()), FORBIDDEN));
        }

        boolean fieldExists = customFieldManager.getCustomFieldType(fieldId.getCustomFieldTypeKey()) != null;

        if (!fieldExists)
        {
            return new ServiceResultImpl(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.field", fieldId.getAddonKey(), fieldId.getFieldKey()), FORBIDDEN));
        }

        return new ServiceResultImpl(ErrorCollections.empty());
    }

    private <T> ServiceOutcomeImpl<T> notFound()
    {
        return new ServiceOutcomeImpl<>(ErrorCollections.create(i18n.getText("connect.issue.field.option.not.found"), NOT_FOUND));
    }
}
