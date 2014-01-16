package com.atlassian.plugin.connect.plugin.iframe.context.filter;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceModuleContextFilter implements ModuleContextFilter
{
    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        // TODO implement this
        return unfiltered;
    }
}
