package com.atlassian.plugin.connect.plugin.web.context;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @since 1.0
 */
public interface ModuleContextParser
{
    Map<String, String> parseContextParameters(HttpServletRequest req);
}
