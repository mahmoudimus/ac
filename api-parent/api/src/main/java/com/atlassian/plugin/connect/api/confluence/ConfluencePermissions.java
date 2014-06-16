package com.atlassian.plugin.connect.api.confluence;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

/**
 * Permissions for Confluence.  Should all be declared as plugin-permission modules in atlassian-plugin-confluence.xml
 */
@XmlDescriptor
public final class ConfluencePermissions
{
    public static final String MANAGE_INDEX = "manage_index";
    public static final String READ_CONTENT = "read_content";
    public static final String MODIFY_ATTACHMENTS = "modify_attachments";
    public static final String READ_USERS_AND_GROUPS = "read_users_and_groups";
    public static final String MODIFY_SPACES = "modify_spaces";
    public static final String READ_SERVER_INFORMATION = "read_server_information";
    public static final String MODIFY_USERS = "modify_users";
    public static final String MANAGE_WATCHERS = "manage_watchers";
    public static final String LABEL_CONTENT = "label_content";
    public static final String RENDER_CONTENT = "render_content";
    public static final String MODIFY_CONTENT = "modify_content";
    public static final String MANAGE_ANONYMOUS_PERMISSIONS = "manage_anonymous_permissions";

    private ConfluencePermissions()
    {
    }
}
