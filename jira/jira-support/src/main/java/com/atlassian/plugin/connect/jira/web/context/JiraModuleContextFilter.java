package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.IssueTypeService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.atlassian.plugin.connect.spi.web.context.AbstractModuleContextFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraModuleContextFilter extends AbstractModuleContextFilter<ApplicationUser> {
    public static final String ISSUE_ID = "issue.id";
    public static final String ISSUE_KEY = "issue.key";
    public static final String PROJECT_ID = "project.id";
    public static final String PROJECT_KEY = "project.key";
    public static final String VERSION_ID = "version.id";
    public static final String COMPONENT_ID = "component.id";
    public static final String POSTFUNCTION_ID = "postFunction.id";
    public static final String POSTFUNCTION_CONFIG = "postFunction.config";
    public static final String DASHBOARD_ITEM_ID = "dashboardItem.id";
    public static final String DASHBOARD_ITEM_KEY = "dashboardItem.key";
    public static final String DASHBOARD_ITEM_VIEW_TYPE = "dashboardItem.viewType";
    public static final String DASHBOARD_ID = "dashboard.id";
    public static final String ISSUETYPE_ID = "issuetype.id";

    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final IssueManager issueManager;
    private final VersionManager versionManager;
    private final ProjectComponentManager projectComponentManager;
    private final JiraAuthenticationContext authenticationContext;
    private final DashboardPermissionService dashboardPermissionService;
    private final Iterable<PermissionCheck<ApplicationUser>> permissionChecks;
    private final IssueTypeService issueTypeService;

    @Autowired
    public JiraModuleContextFilter(
            final PluginAccessor pluginAccessor,
            final PermissionManager permissionManager,
            final ProjectService projectService,
            final IssueManager issueManager,
            final VersionManager versionManager,
            final ProjectComponentManager projectComponentManager,
            final JiraAuthenticationContext authenticationContext,
            final DashboardPermissionService dashboardPermissionService,
            final IssueTypeService issueTypeService) {
        super(pluginAccessor, ApplicationUser.class);
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.issueManager = issueManager;
        this.versionManager = versionManager;
        this.projectComponentManager = projectComponentManager;
        this.authenticationContext = authenticationContext;
        this.dashboardPermissionService = dashboardPermissionService;
        this.issueTypeService = issueTypeService;
        this.permissionChecks = constructPermissionChecks();
    }

    @Override
    protected ApplicationUser getCurrentUser() {
        return authenticationContext.getUser();
    }

    @Override
    protected Iterable<PermissionCheck<ApplicationUser>> getPermissionChecks() {
        return permissionChecks;
    }

    @SuppressWarnings("unchecked")
    private Iterable<PermissionCheck<ApplicationUser>> constructPermissionChecks() {
        return ImmutableList.of(
                new PermissionChecks.LongValue<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return ISSUE_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user) {
                        Issue issue = issueManager.getIssueObject(id);
                        return issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, user);
                    }
                },
                new PermissionCheck<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return ISSUE_KEY;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user) {
                        Issue issue = issueManager.getIssueObject(value);
                        return issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, user);
                    }
                },
                new PermissionCheck<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return ISSUETYPE_ID;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user) {
                        return issueTypeService.getIssueType(user, value).isDefined();
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return PROJECT_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user) {
                        return projectService.getProjectById(user, id).isValid();
                    }
                },
                new PermissionCheck<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return PROJECT_KEY;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user) {
                        return projectService.getProjectByKey(user, value).isValid();
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return VERSION_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user) {
                        Version version = versionManager.getVersion(id);
                        return version != null && permissionManager.hasPermission(Permissions.BROWSE,
                                version.getProjectObject(), user);
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>() {
                    @Override
                    public String getParameterName() {
                        return COMPONENT_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user) {
                        ProjectComponent component;
                        try {
                            component = projectComponentManager.find(id);
                        } catch (EntityNotFoundException e) {
                            return false;
                        }
                        return component != null && projectService.getProjectById(user, component.getProjectId()).isValid();
                    }
                },
                new PermissionCheck<ApplicationUser>() {

                    @Override
                    public String getParameterName() {
                        return DASHBOARD_ID;
                    }

                    @Override
                    public boolean hasPermission(final String dashboardId, final ApplicationUser applicationUser) {
                        return dashboardPermissionService.isReadableBy(DashboardId.valueOf(dashboardId), applicationUser.getUsername());
                    }
                },
                // users must be logged in to see another user's profile
                PermissionChecks.<ApplicationUser>mustBeLoggedIn(PROFILE_NAME),
                PermissionChecks.<ApplicationUser>mustBeLoggedIn(PROFILE_KEY),
                // post-functions are not explicitly protected, the context user will have project admin privileges
                PermissionChecks.<ApplicationUser>alwaysAllowed(POSTFUNCTION_ID),
                PermissionChecks.<ApplicationUser>alwaysAllowed(POSTFUNCTION_CONFIG),
                // Dashboard items are always allowed. Permission checking is applied per dashboard already.
                PermissionChecks.<ApplicationUser>alwaysAllowed(DASHBOARD_ITEM_ID),
                PermissionChecks.<ApplicationUser>alwaysAllowed(DASHBOARD_ITEM_KEY),
                PermissionChecks.<ApplicationUser>alwaysAllowed(DASHBOARD_ITEM_VIEW_TYPE)
        );
    }
}
