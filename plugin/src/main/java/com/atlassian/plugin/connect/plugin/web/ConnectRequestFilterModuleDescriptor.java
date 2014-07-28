package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.ModuleDescriptor;

import javax.servlet.Filter;

/**
 * Module descriptor for filters which can be implemented by products/plugins and are managed by Connect plugin. This
 * filters are executed on a choose phase of Connect request filtering {@link com.atlassian.plugin.connect.plugin.web.ConnectRequestFilterPhase}.
 *
 * If multiple filters are registers per single phase, Connect makes no guarantee on the order in which filters will be executed. 
 */
public interface ConnectRequestFilterModuleDescriptor extends ModuleDescriptor<Filter>
{
    ConnectRequestFilterPhase getFilterPhase();
}
