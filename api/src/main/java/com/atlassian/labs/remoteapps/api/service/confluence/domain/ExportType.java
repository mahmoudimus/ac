package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

/**
 */
public enum ExportType
{
    @RemoteName("TYPE_XML")
    XML,

    @RemoteName("TYPE_HTML")
    HTML,

    @RemoteName("TYPE_ALL_DATA")
    ALL_DATA
}
