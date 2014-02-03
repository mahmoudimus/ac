package com.atlassian.plugin.connect.plugin.imports;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.bc.project.ProjectService;
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
import com.atlassian.mail.queue.MailQueue;
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
    private final ApplicationProperties jiraApplicationProperties;
    private final AttachmentService attachmentService;
    private final FieldManager fieldManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final InternalWebSudoManager internalWebSudoManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueManager issueManager;
    private final IssueToSubTaskConversionService issueToSubTaskConversionService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraBaseUrls jiraBaseUrls;
    private final MailQueue mailQueue;
    private final PermissionManager jiraPermissionManager;
    private final ProjectRoleManager projectRoleManager;
    private final ProjectService projectService;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private final SubTaskManager subTaskManager;
    private final SubTaskToIssueConversionService subTaskToIssueConversionService;
    private final TimeZoneService timeZoneService;
    private final UserIssueHistoryManager userIssueHistoryManager;
    private final UserManager userManager;
    private final UserPreferencesManager userPreferencesManager;
    private final UserUtil userUtil;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final VoteManager voteManager;
    private final WatcherManager watcherManager;
    private final WebFragmentHelper webFragmentHelper;
    private final WorklogService worklogService;
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;

    @Inject
    public JiraImports(
            @JiraImport("jiraApplicationProperties") ApplicationProperties jiraApplicationProperties,
            @JiraImport AttachmentService attachmentService,
            @JiraImport FieldManager fieldManager,
            @JiraImport FieldVisibilityManager fieldVisibilityManager,
            @JiraImport InternalWebSudoManager internalWebSudoManager,
            @JiraImport IssueLinkTypeManager issueLinkTypeManager,
            @JiraImport IssueManager issueManager,
            @JiraImport IssueToSubTaskConversionService issueToSubTaskConversionService,
            @JiraImport JiraAuthenticationContext jiraAuthenticationContext,
            @JiraImport JiraBaseUrls jiraBaseUrls,
            @JiraImport MailQueue mailQueue,
            @JiraImport PermissionManager jiraPermissionManager,
            @JiraImport ProjectRoleManager projectRoleManager,
            @JiraImport ProjectService projectService,
            @JiraImport SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            @JiraImport SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory,
            @JiraImport SubTaskManager subTaskManager,
            @JiraImport SubTaskToIssueConversionService subTaskToIssueConversionService,
            @JiraImport TimeZoneService timeZoneService,
            @JiraImport UserIssueHistoryManager userIssueHistoryManager,
            @JiraImport UserPreferencesManager userPreferencesManager,
            @JiraImport("jiraUserManager") UserManager userManager,
            @JiraImport UserUtil userUtil,
            @JiraImport VelocityRequestContextFactory velocityRequestContextFactory,
            @JiraImport VoteManager voteManager,
            @JiraImport WatcherManager watcherManager,
            @JiraImport WebFragmentHelper webFragmentHelper,
            @JiraImport WorklogService worklogService,
            @JiraImport ApplicationService applicationService,
            @JiraImport ApplicationManager applicationManager)
    {
        this.attachmentService = attachmentService;
        this.fieldManager = fieldManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.internalWebSudoManager = internalWebSudoManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueManager = issueManager;
        this.issueToSubTaskConversionService = issueToSubTaskConversionService;
        this.jiraApplicationProperties = jiraApplicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jiraBaseUrls = jiraBaseUrls;
        this.jiraPermissionManager = jiraPermissionManager;
        this.mailQueue = mailQueue;
        this.projectRoleManager = projectRoleManager;
        this.projectService = projectService;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
        this.subTaskManager = subTaskManager;
        this.subTaskToIssueConversionService = subTaskToIssueConversionService;
        this.timeZoneService = timeZoneService;
        this.userIssueHistoryManager = userIssueHistoryManager;
        this.userManager = userManager;
        this.userPreferencesManager = userPreferencesManager;
        this.userUtil = userUtil;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.voteManager = voteManager;
        this.watcherManager = watcherManager;
        this.webFragmentHelper = webFragmentHelper;
        this.worklogService = worklogService;
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
    }
}
