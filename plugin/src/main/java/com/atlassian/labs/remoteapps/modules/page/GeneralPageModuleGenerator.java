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
 * Module type for general pages, generating a web item and servlet with iframe
 */
@Component
public class GeneralPageModuleGenerator extends AbstractPageModuleGenerator
{
    private final ProductAccessor productAccessor;

    @Autowired
    public GeneralPageModuleGenerator(ServletModuleManager servletModuleManager,
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
        return "general-page";
    }

    @Override
    protected String getDecorator()
    {
        return "atl.general";
    }

    @Override
    protected int getPreferredWeight()
    {
        return productAccessor.getPreferredGeneralWeight();
    }

    @Override
    protected String getPreferredSectionKey()
    {
        return productAccessor.getPreferredGeneralSectionKey();
    }
}
