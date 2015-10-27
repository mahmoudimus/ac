package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0
 */
public interface ModuleContextParser
{
    ModuleContextParameters parseContextParameters(HttpServletRequest req);
}
