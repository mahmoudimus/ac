package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.AbstractPageModuleGenerator;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Module type for admin pages, generating a web item and servlet with iframe
 */
@Component
public class AdminPageModuleGenerator extends AbstractPageModuleGenerator
{
    private final ProductAccessor productAccessor;

    @Autowired
    public AdminPageModuleGenerator(ServletModuleManager servletModuleManager,
                                    TemplateRenderer templateRenderer,
                                    ProductAccessor productAccessor,
                                    WebResourceManager webResourceManager,
                                    ApplicationLinkService applicationLinkService,
                                    OAuthLinkManager oAuthLinkManager,
                                    PermissionManager permissionManager
    )
    {
        super(servletModuleManager, templateRenderer, webResourceManager, applicationLinkService, oAuthLinkManager, permissionManager);
        this.productAccessor = productAccessor;
    }

    @Override
    public String getType()
    {
        return "admin-page";
    }

    @Override
    protected String getDecorator()
    {
        return "atl.admin";
    }

    @Override
    protected int getPreferredWeight()
    {
        return productAccessor.getPreferredAdminWeight();
    }

    @Override
    protected String getPreferredSectionKey()
    {
        return productAccessor.getPreferredAdminSectionKey();
    }
}
