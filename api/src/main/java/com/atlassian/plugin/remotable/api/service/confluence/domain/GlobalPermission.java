package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public enum GlobalPermission
{
    @RemoteName("USECONFLUENCE")
    USE_CONFLUENCE_PERMISSION,

    @RemoteName("SYSTEMADMINISTRATOR")
    SYSTEM_ADMINISTRATOR_PERMISSION,

    @RemoteName("ADMINISTRATECONFLUENCE")
    CONFLUENCE_ADMINISTRATOR_PERMISSION,

    @RemoteName("PERSONALSPACE")
    PERSONAL_SPACE_PERMISSION,

    @RemoteName("CREATESPACE")
    CREATE_SPACE_PERMISSION,

    @RemoteName("PROFILEATTACHMENTS")
    PROFILE_ATTACHMENT_PERMISSION,

    @RemoteName("UPDATEUSERSTATUS")
    UPDATE_USER_STATUS_PERMISSION
}
