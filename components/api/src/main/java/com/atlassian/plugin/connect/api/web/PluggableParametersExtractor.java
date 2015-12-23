package com.atlassian.plugin.connect.api.web;

import java.util.Map;

/**
 * This component extracts context parameters based on local Connect project extractors as well as extractors loaded from plug-ins.
 */
public interface PluggableParametersExtractor
{

    Map<String, String> extractParameters(Map<String, Object> context);

    Map<String, String> getParametersAccessibleByCurrentUser(Map<String, String> contextParameters);
}
