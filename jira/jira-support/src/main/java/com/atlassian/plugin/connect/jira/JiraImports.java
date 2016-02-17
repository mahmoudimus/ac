package com.atlassian.plugin.connect.jira;

import javax.inject.Inject;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRoleAdminService;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.bc.dashboard.DashboardItemPropertyService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.bc.issuetype.property.IssueTypePropertyService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.project.property.ProjectPropertyService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.bc.subtask.conversion.IssueToSubTaskConversionService;
import com.atlassian.jira.bc.subtask.conversion.SubTaskToIssueConversionService;
import com.atlassian.jira.bc.user.UserPropertyService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.IssueTypeService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
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
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.pocketknife.internal.querydsl.ConnectionPrimerImpl;
import com.atlassian.pocketknife.internal.querydsl.DatabaseAccessorImpl;
import com.atlassian.pocketknife.internal.querydsl.DatabaseCompatibilityKitImpl;
import com.atlassian.pocketknife.internal.querydsl.DatabaseConnectionConverterImpl;
import com.atlassian.pocketknife.internal.querydsl.JiraConnectionProviderImpl;
import com.atlassian.pocketknife.internal.querydsl.SchemaProviderAccessor;
import com.atlassian.pocketknife.internal.querydsl.schema.DatabaseSchemaCreationImpl;
import com.atlassian.pocketknife.internal.querydsl.schema.DefaultSchemaProvider;
import com.atlassian.pocketknife.internal.querydsl.stream.StreamingQueryFactoryImpl;
import com.atlassian.pocketknife.internal.querydsl.util.DatabaseAccessUtil;
import com.atlassian.pocketknife.spi.querydsl.DefaultDialectConfiguration;
import com.atlassian.sal.api.rdbms.TransactionalExecutorFactory;

/**
 * This class does nothing but is here to centralize the JIRA component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings ("ALL")
@Scanned
public class JiraImports
{
    /* QueryDSL components. Can't scan them with spring-scanner automatically because we want them in JIRA ony */
    @JiraComponent
    ConnectionPrimerImpl connectionPrimer;
    @JiraComponent
    DatabaseAccessorImpl databaseAccessor;
    @JiraComponent
    JiraConnectionProviderImpl jiraConnectionProvider;
    @JiraComponent
    DatabaseCompatibilityKitImpl databaseCompatibilityKit;
    @JiraComponent
    DatabaseConnectionConverterImpl databaseConnectionConverter;
    @JiraComponent
    SchemaProviderAccessor schemaProviderAccessor;
    @JiraComponent
    DatabaseSchemaCreationImpl databaseSchemaCreation;
    @JiraComponent
    DefaultSchemaProvider schemaProvider;
    @JiraComponent
    StreamingQueryFactoryImpl streamingQueryFactory;
    @JiraComponent
    DatabaseAccessUtil databaseAccessUtil;
    @JiraComponent
    DefaultDialectConfiguration defaultDialectConfiguration;

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
            @JiraImport JiraLicenseService licenseService,
            @JiraImport ("jiraPermissionManager") PermissionManager jiraPermissionManager,
            @JiraImport ApplicationAuthorizationService applicationAuthorizationService,
            @JiraImport ApplicationRoleManager applicationRoleManager,
            @JiraImport ApplicationRoleAdminService applicationRoleAdminService,
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
            @JiraImport UserPropertyService userPropertyService,
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
            @JiraImport GlobalPermissionManager globalPermissionManager,
            @JiraImport ("beanBuilderFactory") BeanBuilderFactory beanBuilderFactory,
            @JiraImport FeatureManager featureManager,
            @JiraImport DashboardPermissionService dashboardPermissionService,
            @JiraImport CommentPropertyService commentPropertyService,
            @JiraImport IssuePropertyService issuePropertyService,
            @JiraImport IssueTypePropertyService issueTypePropertyService,
            @JiraImport ProjectPropertyService projectPropertyService,
            @JiraImport DashboardItemPropertyService dashboardItemPropertyService,
            @JiraImport IssueTypeService issueTypeService,
            @JiraImport RendererManager rendererManager,
            @JiraImport Encoder encoder,
            @JiraImport CustomFieldValuePersister customFieldValuePersister,
            @JiraImport GenericConfigManager genericConfigManager,
            @JiraImport JqlOperandResolver jqlOperandResolver,
            @JiraImport CustomFieldInputHelper customFieldInputHelper,
            @JiraImport CustomFieldManager customFieldManager,
            @JiraImport ManagedConfigurationItemService managedConfigurationItemService,
            @JiraImport TransactionalExecutorFactory transactionalExecutorFactory)
    {
    }
}
