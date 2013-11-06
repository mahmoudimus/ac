package com.atlassian.plugin.connect.plugin.imports;

import javax.inject.Inject;

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


/**
 * This class does nothing but is here to centralize the JIRA component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@JiraComponent
public class JiraImports
{
    private final ProjectService projectService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;
    private final TimeZoneService timeZoneService;
    private final InternalWebSudoManager internalWebSudoManager;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final MailQueue mailQueue;
    private final ProjectRoleManager projectRoleManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final UserUtil userUtil;
    private final SubTaskManager subTaskManager;
    private final ApplicationProperties jiraApplicationProperties;
    private final WorklogService worklogService;
    private final UserIssueHistoryManager userIssueHistoryManager;
    private final PermissionManager jiraPermissionManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final UserPreferencesManager userPreferencesManager;
    private final SubTaskToIssueConversionService subTaskToIssueConversionService;
    private final AttachmentService attachmentService;
    private final IssueManager issueManager;
    private final VoteManager voteManager;
    private final WatcherManager watcherManager;
    private final FieldManager fieldManager;
    private final IssueToSubTaskConversionService issueToSubTaskConversionService;
    private final WebFragmentHelper webFragmentHelper;

    @Inject
    public JiraImports(
            @JiraImport ProjectService projectService,
            @JiraImport JiraAuthenticationContext jiraAuthenticationContext,
            @JiraImport("jiraUserManager") UserManager userManager,
            @JiraImport TimeZoneService timeZoneService,
            @JiraImport InternalWebSudoManager internalWebSudoManager,
            @JiraImport SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            @JiraImport MailQueue mailQueue,
            @JiraImport ProjectRoleManager projectRoleManager,
            @JiraImport JiraBaseUrls jiraBaseUrls,
            @JiraImport UserUtil userUtil,
            @JiraImport SubTaskManager subTaskManager,
            @JiraImport("jiraApplicationProperties") ApplicationProperties jiraApplicationProperties,
            @JiraImport WorklogService worklogService,
            @JiraImport UserIssueHistoryManager userIssueHistoryManager,
            @JiraImport PermissionManager jiraPermissionManager,
            @JiraImport FieldVisibilityManager fieldVisibilityManager,
            @JiraImport VelocityRequestContextFactory velocityRequestContextFactory,
            @JiraImport SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory,
            @JiraImport IssueLinkTypeManager issueLinkTypeManager,
            @JiraImport UserPreferencesManager userPreferencesManager,
            @JiraImport SubTaskToIssueConversionService subTaskToIssueConversionService,
            @JiraImport AttachmentService attachmentService,
            @JiraImport IssueManager issueManager,
            @JiraImport VoteManager voteManager,
            @JiraImport WatcherManager watcherManager,
            @JiraImport FieldManager fieldManager,
            @JiraImport IssueToSubTaskConversionService issueToSubTaskConversionService, 
            @JiraImport WebFragmentHelper webFragmentHelper)
    {
        this.projectService = projectService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userManager = userManager;
        this.timeZoneService = timeZoneService;
        this.internalWebSudoManager = internalWebSudoManager;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.mailQueue = mailQueue;
        this.projectRoleManager = projectRoleManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.userUtil = userUtil;
        this.subTaskManager = subTaskManager;
        this.jiraApplicationProperties = jiraApplicationProperties;
        this.worklogService = worklogService;
        this.userIssueHistoryManager = userIssueHistoryManager;
        this.jiraPermissionManager = jiraPermissionManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.userPreferencesManager = userPreferencesManager;
        this.subTaskToIssueConversionService = subTaskToIssueConversionService;
        this.attachmentService = attachmentService;
        this.issueManager = issueManager;
        this.voteManager = voteManager;
        this.watcherManager = watcherManager;
        this.fieldManager = fieldManager;
        this.issueToSubTaskConversionService = issueToSubTaskConversionService;
        this.webFragmentHelper = webFragmentHelper;
    }
}
