package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.AbstractPageModuleGenerator;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Module type for user profile pages, generating a web item and servlet with iframe
 *
 * todo: make this use the velocity template for profiles
 */
@Component
public class ProfilePageModuleGenerator extends AbstractPageModuleGenerator
{
    private final ProductAccessor productAccessor;

    @Autowired
    public ProfilePageModuleGenerator(ServletModuleManager servletModuleManager,
                                      TemplateRenderer templateRenderer,
                                      ProductAccessor productAccessor,
                                      WebResourceManager webResourceManager,
                                      ApplicationLinkOperationsFactory applicationLinkSignerFactory
    )
    {
        super(servletModuleManager, templateRenderer, webResourceManager, applicationLinkSignerFactory);
        this.productAccessor = productAccessor;
    }

    @Override
    public String getType()
    {
        return "profile-page";
    }

    @Override
    protected String getDecorator()
    {
        return "atl.userprofile";
    }

    @Override
    protected int getPreferredWeight()
    {
        return productAccessor.getPreferredProfileWeight();
    }

    @Override
    protected String getPreferredSectionKey()
    {
        return productAccessor.getPreferredProfileSectionKey();
    }
}
