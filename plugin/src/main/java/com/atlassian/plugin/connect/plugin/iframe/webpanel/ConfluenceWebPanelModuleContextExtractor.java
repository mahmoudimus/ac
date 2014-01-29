package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.confluence.ConfluenceModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.confluence.ConfluenceModuleContextParametersImpl;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import java.util.Map;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceWebPanelModuleContextExtractor implements WebPanelModuleContextExtractor
{

    @Override
    public ModuleContextParameters extractParameters(final Map<String, Object> webPanelContext)
    {
        ConfluenceModuleContextParameters moduleContext = new ConfluenceModuleContextParametersImpl();

        @SuppressWarnings("unchecked") // it is what it is
        WebInterfaceContext webInterfaceContext = (WebInterfaceContext) webPanelContext.get("webInterfaceContext");
        if (webInterfaceContext != null)
        {
            moduleContext.addPage(webInterfaceContext.getPage());
            moduleContext.addSpace(webInterfaceContext.getSpace());
        }

        Space space = (Space) webPanelContext.get("space");
        if (space != null)
        {
            moduleContext.addSpace(space);
        }

        return moduleContext;
    }

}
