package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.confluence.ConfluenceModuleContextFilter;
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

        Page page = (Page) webPanelContext.get("page");
        if (page != null)
        {
            moduleContext.addPage(page);
        }

        if (webPanelContext.containsKey("action") && webPanelContext.get("action") instanceof AbstractPageAwareAction)
        {
            AbstractPageAwareAction pageAwareAction = (AbstractPageAwareAction)webPanelContext.get("action");
            if (!moduleContext.containsKey(ConfluenceModuleContextFilter.PAGE_ID))
            {
                moduleContext.addPage(pageAwareAction.getPage());
            }
        }

        return moduleContext;
    }

}
