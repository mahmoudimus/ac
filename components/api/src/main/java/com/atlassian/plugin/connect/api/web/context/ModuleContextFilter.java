package com.atlassian.plugin.connect.api.web.context;

import java.util.Map;

/**
 * @since 1.0
 */
public interface ModuleContextFilter
{
    /**
     * @param unfilteredContext the raw context parameters, parsed from the request
     * @return a filtered collection of parameters that will be passed to the remote iframe
     */
    Map<String, String> filter(Map<String, String> unfilteredContext);
}
