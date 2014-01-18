package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
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
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
