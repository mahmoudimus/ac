package com.atlassian.plugin.connect.plugin.iframe.context;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0
 */
public interface ModuleContextParser
{
    ModuleContextParameters parseContextParameters(HttpServletRequest req);
}
