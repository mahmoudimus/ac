package com.atlassian.plugin.connect.api.jira;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

/**
 * Permissions for JIRA.  Should all be declared as plugin-permission modules in atlassian-plugin-jira.xml
 */
@XmlDescriptor
public final class JiraPermissions
{
    public static final String READ_USERS_AND_GROUPS = "read_users_and_groups";
    public static final String BROWSE_PROJECTS = "browse_projects";
    public static final String CREATE_ISSUES = "create_issues";
    public static final String EDIT_ISSUES = "edit_issues";
    public static final String RESOLVE_ISSUES = "resolve_issues";
    public static final String READ_METADATA = "read_metadata";
    public static final String READ_USER_SESSION_DATA = "read_user_session_data";
    public static final String DOWNLOAD_ATTACHMENTS = "download_attachments";

    private JiraPermissions()
    {
    }
}
