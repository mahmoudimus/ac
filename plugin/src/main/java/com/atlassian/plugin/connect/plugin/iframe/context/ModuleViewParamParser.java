package com.atlassian.plugin.connect.plugin.iframe.context;

import javax.servlet.http.HttpServletRequest;

public interface ModuleViewParamParser
{
    ModuleViewParameters parseViewParameters(HttpServletRequest req);
}
