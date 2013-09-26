package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;

import java.util.Map;

/**
 * A factory for creating UrlTemplateInstance's
 */
public interface UrlTemplateInstanceFactory
{
    /**
     * Creates a UrlTemplateInstance for the given template string context and username.
     * @param urlTemplateString
     * @param context
     * @param username
     * @return
     */
    UrlTemplateInstance create(String urlTemplateString, Map<String, Object> context, String username)
            throws MalformedRequestException, UnauthorisedException, ResourceNotFoundException;
}
