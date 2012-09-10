package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

/**
 */
public enum ContentType
{
    @RemoteName("page")
    PAGE,

    @RemoteName("comment")
    COMMENT,

    @RemoteName("globaldescription")
    GLOBAL_DESCRIPTION,

    @RemoteName("spacedesc")
    SPACE_DESCRIPTION,

    @RemoteName("personalspacedesc")
    PERSONAL_SPACE_DESCRIPTION,

    @RemoteName("attachment")
    ATTACHMENT,

    @RemoteName("userinfo")
    PERSONAL_DESCRIPTION,

    @RemoteName("blogpost")
    BLOG_POST,

    @RemoteName("status")
    USER_STATUS,

    @RemoteName("draft")
    DRAFT,

    @RemoteName("custom")
    CUSTOM,

    @RemoteName("mail")
    MAIL
}
