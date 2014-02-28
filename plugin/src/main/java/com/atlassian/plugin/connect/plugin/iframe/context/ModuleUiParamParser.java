package com.atlassian.plugin.connect.plugin.iframe.context;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.fugue.Option;

public interface ModuleUiParamParser
{
    Option<String> parseUiParameters(HttpServletRequest req);
}
