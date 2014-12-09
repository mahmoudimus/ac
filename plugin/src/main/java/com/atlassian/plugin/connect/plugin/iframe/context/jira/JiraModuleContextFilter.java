package com.atlassian.plugin.connect.plugin.iframe.context.jira;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.AbstractModuleContextFilter;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraModuleContextFilter extends AbstractModuleContextFilter<ApplicationUser>
{
    public static final String ISSUE_ID = "issue.id";
    public static final String ISSUE_KEY = "issue.key";
    public static final String PROJECT_ID = "project.id";
    public static final String PROJECT_KEY = "project.key";
    public static final String VERSION_ID = "version.id";
    public static final String COMPONENT_ID = "component.id";
    public static final String POSTFUNCTION_ID = "postFunction.id";
    public static final String POSTFUNCTION_CONFIG = "postFunction.config";

    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final IssueManager issueManager;
    private final VersionManager versionManager;
    private final ProjectComponentManager projectComponentManager;
    private final JiraAuthenticationContext authenticationContext;
    private final Iterable<PermissionCheck<ApplicationUser>> permissionChecks;

    @Autowired
    public JiraModuleContextFilter(
            final PluginAccessor pluginAccessor,
            final PluginEventManager pluginEventManager,
            final PermissionManager permissionManager,
            final ProjectService projectService,
            final IssueManager issueManager,
            final VersionManager versionManager,
            final ProjectComponentManager projectComponentManager,
            final JiraAuthenticationContext authenticationContext)
    {
        super(pluginAccessor, pluginEventManager, ApplicationUser.class);
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.issueManager = issueManager;
        this.versionManager = versionManager;
        this.projectComponentManager = projectComponentManager;
        this.authenticationContext = authenticationContext;
        this.permissionChecks = constructPermissionChecks();
    }

    @Override
    protected ApplicationUser getCurrentUser()
    {
        return authenticationContext.getUser();
    }

    @Override
    protected Iterable<PermissionCheck<ApplicationUser>> getPermissionChecks()
    {
        return permissionChecks;
    }

    private Iterable<PermissionCheck<ApplicationUser>> constructPermissionChecks()
    {
        return ImmutableList.of(
                new PermissionChecks.LongValue<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return ISSUE_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user)
                    {
                        Issue issue = issueManager.getIssueObject(id);
                        return issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, user);
                    }
                },
                new PermissionCheck<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return ISSUE_KEY;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user)
                    {
                        Issue issue = issueManager.getIssueObject(value);
                        return issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, user);
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PROJECT_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user)
                    {
                        return projectService.getProjectById(user, id).isValid();
                    }
                },
                new PermissionCheck<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PROJECT_KEY;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user)
                    {
                        return projectService.getProjectByKey(user, value).isValid();
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return VERSION_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user)
                    {
                        Version version = versionManager.getVersion(id);
                        return version != null && permissionManager.hasPermission(Permissions.BROWSE,
                                version.getProjectObject(), user);
                    }
                },
                new PermissionChecks.LongValue<ApplicationUser>()
                {
                    @Override
                    public String getParameterName()
                    {
                        return COMPONENT_ID;
                    }

                    @Override
                    public boolean hasPermission(final long id, final ApplicationUser user)
                    {
                        ProjectComponent component;
                        try
                        {
                            component = projectComponentManager.find(id);
                        }
                        catch (EntityNotFoundException e)
                        {
                            return false;
                        }
                        return component != null && projectService.getProjectById(user, component.getProjectId()).isValid();
                    }
                },
                // users must be logged in to see another user's profile
                PermissionChecks.<ApplicationUser>mustBeLoggedIn(PROFILE_NAME),
                PermissionChecks.<ApplicationUser>mustBeLoggedIn(PROFILE_KEY),
                // post-functions are not explicitly protected, the context user will have project admin privileges
                PermissionChecks.<ApplicationUser>alwaysAllowed(POSTFUNCTION_ID),
                PermissionChecks.<ApplicationUser>alwaysAllowed(POSTFUNCTION_CONFIG)
        );
    }
}
