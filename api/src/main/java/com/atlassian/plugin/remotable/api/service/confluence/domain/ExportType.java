package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
