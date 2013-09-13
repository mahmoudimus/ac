package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.PageSerializer;
import com.atlassian.user.UserManager;

/**
 * Extracts page parameters that can be included in webpanel's iframe url.
 */
public class PageContextMapParameterExtractor extends AbstractConfluenceContextMapParameterExtractor<AbstractPage>
{
    private static final String PAGE_CONTEXT_PARAMETER = "page";

    public PageContextMapParameterExtractor(PageSerializer pageSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        super(AbstractPage.class, pageSerializer, PAGE_CONTEXT_PARAMETER, permissionManager, userManager);
    }

    @Override
    protected AbstractPage getResource(WebInterfaceContext webInterfaceContext)
    {
        return webInterfaceContext.getPage();
    }
}
