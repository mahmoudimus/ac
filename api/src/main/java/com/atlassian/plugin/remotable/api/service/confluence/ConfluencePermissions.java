package com.atlassian.plugin.remotable.api.service.confluence;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Permissions for Confluence.  Should all be declared as plugin-permission modules in atlassian-plugin-confluence.xml
 */
public final class ConfluencePermissions
{
    private ConfluencePermissions()
    {}

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

    public static Set<String> ALL_REMOTE_PERMISSIONS = ImmutableSet.of(
            MANAGE_INDEX, READ_CONTENT, MODIFY_ATTACHMENTS, READ_USERS_AND_GROUPS, MODIFY_SPACES,
            READ_SERVER_INFORMATION, MODIFY_USERS, MANAGE_WATCHERS, LABEL_CONTENT, RENDER_CONTENT,
            MODIFY_CONTENT, MANAGE_ANONYMOUS_PERMISSIONS);
}
