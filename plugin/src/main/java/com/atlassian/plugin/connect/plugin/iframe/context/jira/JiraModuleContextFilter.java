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
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraModuleContextFilter implements ModuleContextFilter
{
    private static final Logger log = LoggerFactory.getLogger(JiraModuleContextFilter.class);

    public static final String ISSUE_ID             = "issue.id";
    public static final String ISSUE_KEY            = "issue.key";
    public static final String PROJECT_ID           = "project.id";
    public static final String PROJECT_KEY          = "project.key";
    public static final String VERSION_ID           = "version.id";
    public static final String COMPONENT_ID         = "component.id";
    public static final String PROFILE_NAME         = "profileUser.name";
    public static final String PROFILE_KEY          = "profileUser.key";
    public static final String POSTFUNCTION_ID      = "postFunction.id";
    public static final String POSTFUNCTION_CONFIG  = "postFunction.config";

    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final IssueManager issueManager;
    private final VersionManager versionManager;
    private final ProjectComponentManager projectComponentManager;
    private final JiraAuthenticationContext authenticationContext;
    private final Iterable<PermissionCheck> permissionChecks;

    @Autowired
    public JiraModuleContextFilter(final PermissionManager permissionManager, final ProjectService projectService,
            final IssueManager issueManager, final VersionManager versionManager,
            final ProjectComponentManager projectComponentManager,
            final JiraAuthenticationContext authenticationContext)
    {
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.issueManager = issueManager;
        this.versionManager = versionManager;
        this.projectComponentManager = projectComponentManager;
        this.authenticationContext = authenticationContext;
        permissionChecks = constructPermissionChecks();
    }

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters();
        ApplicationUser currentUser = authenticationContext.getUser();
        for (PermissionCheck permissionCheck : permissionChecks)
        {
            String value = unfiltered.get(permissionCheck.getParameterName());
            if (!Strings.isNullOrEmpty(value) && permissionCheck.hasPermission(value, currentUser))
            {
                filtered.put(permissionCheck.getParameterName(), value);
            }
        }
        return filtered;
    }

    private static interface PermissionCheck
    {
        String getParameterName();
        boolean hasPermission(String value, ApplicationUser user);
    }

    private static abstract class LongValuePermissionCheck implements PermissionCheck
    {
        @Override
        public boolean hasPermission(final String value, final ApplicationUser user)
        {
            long longValue;
            try
            {
                longValue = Long.parseLong(value);
            }
            catch (NumberFormatException e)
            {
                log.debug("Failed to parse " + getParameterName(), e);
                return false;
            }
            return hasPermission(longValue, user);
        }

        abstract boolean hasPermission(long value, ApplicationUser user);
    }

    private static class AlwaysAllowedPermissionCheck implements PermissionCheck
    {
        private final String parameterName;

        private AlwaysAllowedPermissionCheck(String parameterName)
        {
            this.parameterName = parameterName;
        }

        @Override
        public String getParameterName()
        {
            return parameterName;
        }

        @Override
        public boolean hasPermission(final String value, final ApplicationUser user)
        {
            return true;
        }
    }

    private Iterable<PermissionCheck> constructPermissionChecks()
    {
        return ImmutableList.of(
                new LongValuePermissionCheck()
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
                new PermissionCheck()
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
                new LongValuePermissionCheck()
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
                new PermissionCheck()
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
                new LongValuePermissionCheck()
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
                new LongValuePermissionCheck()
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
                new PermissionCheck()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PROFILE_NAME;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user)
                    {
                        // TODO determine what permissions are needed to view a user's profile
                        return true;
                    }
                },
                new PermissionCheck()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PROFILE_KEY;
                    }

                    @Override
                    public boolean hasPermission(final String value, final ApplicationUser user)
                    {
                        // TODO determine what permissions are needed to view a user's profile
                        return true;
                    }
                },
                // post-functions are not explicitly protected, the context user will have project admin privileges
                new AlwaysAllowedPermissionCheck(POSTFUNCTION_ID),
                new AlwaysAllowedPermissionCheck(POSTFUNCTION_CONFIG)
        );
    }


}
