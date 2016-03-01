package com.atlassian.plugin.connect.jira.field.option;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.plugin.connect.api.auth.AddonDataAccessChecker;
import com.atlassian.plugin.connect.api.auth.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.util.ServiceOutcomes;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.errorResult;
import static com.atlassian.plugin.connect.jira.util.ServiceOutcomes.successResult;

@JiraComponent
public class ConnectFieldOptionAuthorizer {

    private final AddonDataAccessChecker addonDataAccessChecker;
    private final I18nResolver i18n;
    private final CustomFieldManager customFieldManager;
    private final UserManager userManager;
    private final PermissionManager jiraPermissionManager;
    private final ProjectManager projectManager;
    private final com.atlassian.jira.user.util.UserManager jiraUserManager;

    @Autowired
    public ConnectFieldOptionAuthorizer(AddonDataAccessChecker addonDataAccessChecker, I18nResolver i18n, CustomFieldManager customFieldManager, UserManager userManager, PermissionManager jiraPermissionManager, ProjectManager projectManager, com.atlassian.jira.user.util.UserManager jiraUserManager) {
        this.addonDataAccessChecker = addonDataAccessChecker;
        this.i18n = i18n;
        this.customFieldManager = customFieldManager;
        this.userManager = userManager;
        this.jiraPermissionManager = jiraPermissionManager;
        this.projectManager = projectManager;
        this.jiraUserManager = jiraUserManager;
    }

    public ServiceResult hasAdminAccess(AuthenticationData auth, final FieldId fieldId) {
        if (!fieldExists(fieldId)) {
            return fieldDoesNotExist(fieldId);
        }

        if (!addonDataAccessChecker.hasAccessToAddon(auth, fieldId.getAddonKey())) {
            return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.addon", fieldId.getAddonKey()), FORBIDDEN));
        }

        return successResult();
    }

    private ServiceResult fieldDoesNotExist(FieldId fieldId) {
        return errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.field", fieldId.getAddonKey(), fieldId.getFieldKey()), FORBIDDEN));
    }


    public ServiceResult hasReadAccess(AuthenticationData authenticationData, FieldId fieldId, ConnectFieldOptionScope scope) {


        if (!fieldExists(fieldId)) {
            return fieldDoesNotExist(fieldId);
        }

        if (addonDataAccessChecker.hasAccessToAddon(authenticationData, fieldId.getAddonKey())) {
            return ServiceOutcomes.successResult();
        } else {
            return authenticationData.accept(new AuthenticationData.AuthenticationDetailsVisitor<ServiceResult>() {
                @Override
                public ServiceResult visit(AuthenticationData.Request authenticationBy) {
                    return hasReadAccess(userManager.getRemoteUser(authenticationBy.getRequest()), fieldId, scope);
                }

                @Override
                public ServiceResult visit(AuthenticationData.AddonKey authenticationBy) {
                    return ServiceOutcomes.successResult(); // sure, all add-ons can read options
                }

                @Override
                public ServiceResult visit(AuthenticationData.User authenticationBy) {
                    return hasReadAccess(authenticationBy.getUser(), fieldId, scope);
                }
            });
        }
    }

    private ServiceResult hasReadAccess(UserProfile remoteUser, FieldId fieldId, ConnectFieldOptionScope scope) {
        return scope.getProjectId().map(projectId -> {
            Project project = projectManager.getProjectObj(projectId);
            if (project == null) {
                return noReadAccess(fieldId);
            } else {
                ApplicationUser jiraUser = jiraUserManager.getUserByName(remoteUser.getUsername());
                boolean hasPermission = Stream.of(ProjectPermissions.CREATE_ISSUES, ProjectPermissions.EDIT_ISSUES)
                        .anyMatch(permission -> jiraPermissionManager.hasPermission(permission, project, jiraUser));

                return hasPermission ? successResult() : noReadAccess(fieldId);
            }
        }).orElse(successResult());
    }

    private ServiceResult noReadAccess(FieldId fieldId) {
        return ServiceOutcomes.errorResult(ErrorCollections.create(i18n.getText("connect.issue.field.option.forbidden.read", fieldId.getCustomFieldName()), FORBIDDEN));
    }

    private boolean fieldExists(final FieldId fieldId) {
        return customFieldManager.getCustomFieldType(fieldId.getCustomFieldTypeKey()) != null;
    }

}
