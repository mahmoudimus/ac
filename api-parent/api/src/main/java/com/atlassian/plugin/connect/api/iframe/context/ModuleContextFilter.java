package com.atlassian.plugin.connect.api.iframe.context;

/**
 * @since 1.0
 */
public interface ModuleContextFilter
{
    /**
     * @param unfiltered the raw context parameters, parsed from the request
     * @return a filtered collection of parameters that will be passed to the remote iframe
     */
    ModuleContextParameters filter(ModuleContextParameters unfiltered);
}
