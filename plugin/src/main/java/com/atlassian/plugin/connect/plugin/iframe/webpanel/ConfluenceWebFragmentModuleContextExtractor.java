package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.confluence.pages.AbstractPage;
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
public class ConfluenceWebFragmentModuleContextExtractor implements WebFragmentModuleContextExtractor
{

    @Override
    public ModuleContextParameters extractParameters(final Map<String, ? extends Object> webFragmentContext)
    {
        if(ModuleContextParameters.class.isAssignableFrom(webFragmentContext.getClass()))
        {
            return (ModuleContextParameters) webFragmentContext;
        }
        
        ConfluenceModuleContextParameters moduleContext = new ConfluenceModuleContextParametersImpl();

        @SuppressWarnings("unchecked") // it is what it is
        WebInterfaceContext webInterfaceContext = (WebInterfaceContext) webFragmentContext.get("webInterfaceContext");
        if (webInterfaceContext != null)
        {
            moduleContext.addPage(webInterfaceContext.getPage());
            moduleContext.addSpace(webInterfaceContext.getSpace());
        }

        Space space = (Space) webFragmentContext.get("space");
        if (space != null)
        {
            moduleContext.addSpace(space);
        }

        AbstractPage page = (AbstractPage) webFragmentContext.get("page");
        if (page != null)
        {
            moduleContext.addPage(page);
        }

        if (webFragmentContext.containsKey("action") && webFragmentContext.get("action") instanceof AbstractPageAwareAction)
        {
            AbstractPageAwareAction pageAwareAction = (AbstractPageAwareAction) webFragmentContext.get("action");
            if (!moduleContext.containsKey(ConfluenceModuleContextFilter.PAGE_ID))
            {
                moduleContext.addPage(pageAwareAction.getPage());
            }
        }

        return moduleContext;
    }

}
