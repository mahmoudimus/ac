package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.AbstractPageModuleGenerator;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for admin pages, generating a web item and servlet with iframe
 */
public class AdminPageModuleGenerator extends AbstractPageModuleGenerator
{
    private final ProductAccessor productAccessor;

    public AdminPageModuleGenerator(ServletModuleManager servletModuleManager,
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
        return "admin-page";
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
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

    @Override
    protected Map<String, String> getContextParams()
    {
        return productAccessor.getLinkContextParams();
    }
}
