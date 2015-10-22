package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import java.util.Map;
import javax.inject.Inject;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceWebFragmentModuleContextExtractor implements WebFragmentModuleContextExtractor
{
    private final UserManager userManager;

    @Inject
    public ConfluenceWebFragmentModuleContextExtractor(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public ModuleContextParameters extractParameters(final Map<String, ? extends Object> webFragmentContext)
    {
        if(ModuleContextParameters.class.isAssignableFrom(webFragmentContext.getClass()))
        {
            return (ModuleContextParameters) webFragmentContext;
        }
        
        ConfluenceModuleContextParameters moduleContext = new ConfluenceModuleContextParametersImpl();

        {
            @SuppressWarnings("unchecked") // it is what it is
                    WebInterfaceContext webInterfaceContext = (WebInterfaceContext) webFragmentContext.get("webInterfaceContext");
            if (webInterfaceContext != null)
            {
                moduleContext.addPage(webInterfaceContext.getPage());
                moduleContext.addContent(webInterfaceContext.getPage());
                moduleContext.addSpace(webInterfaceContext.getSpace());
            }
        }

        final Object action = webFragmentContext.get("action");

        if (action instanceof ConfluenceActionSupport)
        {
            WebInterfaceContext webInterfaceContext = ((ConfluenceActionSupport)action).getWebInterfaceContext();

            if (webInterfaceContext != null)
            {
                moduleContext.addPage(webInterfaceContext.getPage());
                moduleContext.addContent(webInterfaceContext.getPage());
                moduleContext.addSpace(webInterfaceContext.getSpace());
            }
        }

        Space space = (Space) webFragmentContext.get("space");
        if (space != null)
        {
            moduleContext.addSpace(space);
        }

        AbstractPage page = (AbstractPage) webFragmentContext.get("page");
        if (page != null)
        {
            moduleContext.addContent(page);
            moduleContext.addPage(page);
        }

        if (action instanceof AbstractPageAwareAction)
        {
            AbstractPageAwareAction pageAwareAction = (AbstractPageAwareAction) action;
            if (!moduleContext.containsKey(ConfluenceModuleContextFilter.PAGE_ID))
            {
                moduleContext.addContent(pageAwareAction.getPage());
                moduleContext.addPage(pageAwareAction.getPage());
            }
        }

        Object content = webFragmentContext.get("content");
        if (content != null && content instanceof ContentEntityObject)
        {
            moduleContext.addContent((ContentEntityObject)content);
        }

        ConfluenceUser profileUser = (ConfluenceUser) webFragmentContext.get("targetUser");
        if (profileUser != null)
        {
            UserProfile profile = userManager.getUserProfile(profileUser.getKey());
            moduleContext.addProfileUser(profile);
        }

        ModuleContextParameters nestedContext = (ModuleContextParameters) webFragmentContext.get(MODULE_CONTEXT_KEY);
        if (nestedContext != null)
        {
            moduleContext.putAll(nestedContext);
        }

        return moduleContext;
    }

}
