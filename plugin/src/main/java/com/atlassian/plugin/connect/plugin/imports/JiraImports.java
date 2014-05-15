package com.atlassian.plugin.connect.plugin.imports;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.bc.subtask.conversion.IssueToSubTaskConversionService;
import com.atlassian.jira.bc.subtask.conversion.SubTaskToIssueConversionService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.plugin.web.WebFragmentHelper;

import javax.inject.Inject;


/**
 * This class does nothing but is here to centralize the JIRA component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings ("ALL")
@JiraComponent
public class JiraImports
{
    @Inject
    public JiraImports(
            @JiraImport ("jiraApplicationProperties") ApplicationProperties jiraApplicationProperties,
            @JiraImport AttachmentService attachmentService,
            @JiraImport FieldManager fieldManager,
            @JiraImport FieldVisibilityManager fieldVisibilityManager,
            @JiraImport InternalWebSudoManager internalWebSudoManager,
            @JiraImport IssueLinkTypeManager issueLinkTypeManager,
            @JiraImport IssueManager issueManager,
            @JiraImport IssueToSubTaskConversionService issueToSubTaskConversionService,
            @JiraImport JiraAuthenticationContext jiraAuthenticationContext,
            @JiraImport JiraBaseUrls jiraBaseUrls,
            @JiraImport ("jiraPermissionManager") PermissionManager jiraPermissionManager,
            @JiraImport PermissionSchemeManager permissionSchemeManager,
            @JiraImport ProjectComponentManager projectComponentManager,
            @JiraImport ProjectRoleManager projectRoleManager,
            @JiraImport ProjectRoleService projectRoleService,
            @JiraImport ProjectManager projectManager,
            @JiraImport ProjectService projectService,
            @JiraImport SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            @JiraImport SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory,
            @JiraImport SubTaskManager subTaskManager,
            @JiraImport SubTaskToIssueConversionService subTaskToIssueConversionService,
            @JiraImport TimeZoneService timeZoneService,
            @JiraImport UserIssueHistoryManager userIssueHistoryManager,
            @JiraImport UserPreferencesManager userPreferencesManager,
            @JiraImport ("jiraUserManager") UserManager userManager,
            @JiraImport UserUtil userUtil,
            @JiraImport VelocityRequestContextFactory velocityRequestContextFactory,
            @JiraImport ("jiraVersionManager") VersionManager versionManager,
            @JiraImport VoteManager voteManager,
            @JiraImport WatcherManager watcherManager,
            @JiraImport WebFragmentHelper webFragmentHelper,
            @JiraImport WorklogService worklogService,
            @JiraImport ApplicationService applicationService,
            @JiraImport ApplicationManager applicationManager,
            @JiraImport GlobalPermissionManager globalPermissionManager)
    {
    }
}
