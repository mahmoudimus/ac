package com.atlassian.plugin.remotable.api.service.jira;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Permissions for JIRA.  Should all be declared as plugin-permission modules in atlassian-plugin-jira.xml
 */
public class JiraPermissions
{
    private JiraPermissions() {}

    public static final String READ_USERS_AND_GROUPS = "read_users_and_groups";
    public static final String BROWSE_PROJECTS = "browse_projects";
    public static final String CREATE_ISSUES = "create_issues";
    public static final String EDIT_ISSUES = "edit_issues";
    public static final String RESOLVE_ISSUES = "resolve_issues";

    public static Set<String> ALL_REMOTE_PERMISSIONS = ImmutableSet.of(
            READ_USERS_AND_GROUPS, BROWSE_PROJECTS, CREATE_ISSUES, EDIT_ISSUES, RESOLVE_ISSUES);
}
