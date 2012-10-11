package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

/**
 */
public enum SpacePermission
{
    @RemoteName("VIEWSPACE")
    VIEW_SPACE_PERMISSION,

    @RemoteName("COMMENT")
    COMMENT_PERMISSION,

    @RemoteName("EDITSPACE")
    CREATE_EDIT_PAGE_PERMISSION,

    @RemoteName("SETSPACEPERMISSIONS")
    ADMINISTER_SPACE_PERMISSION,

    @RemoteName("REMOVEPAGE")
    REMOVE_PAGE_PERMISSION,

    @RemoteName("REMOVECOMMENT")
    REMOVE_COMMENT_PERMISSION,

    @RemoteName("REMOVEBLOG")
    REMOVE_BLOG_PERMISSION,

    @RemoteName("CREATEATTACHMENT")
    CREATE_ATTACHMENT_PERMISSION,

    @RemoteName("REMOVEATTACHMENT")
    REMOVE_ATTACHMENT_PERMISSION,

    @RemoteName("EDITBLOG")
    EDIT_BLOG_PERMISSION,

    @RemoteName("EXPORTPAGE")
    EXPORT_PAGE_PERMISSION,

    @RemoteName("EXPORTSPACE")
    EXPORT_SPACE_PERMISSION,

    @RemoteName("REMOVEMAIL")
    REMOVE_MAIL_PERMISSION,

    @RemoteName("SETPAGEPERMISSIONS")
    SET_PAGE_PERMISSIONS_PERMISSION
}
