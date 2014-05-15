package com.atlassian.plugin.connect.plugin.iframe.context;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0
 */
public interface ModuleContextParser
{
    ModuleContextParameters parseContextParameters(HttpServletRequest req);
}
