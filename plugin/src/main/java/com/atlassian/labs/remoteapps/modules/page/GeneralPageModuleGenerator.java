package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.DefaultWebItemContext;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for general pages, generating a web item and servlet with iframe
 */
@Component
public class GeneralPageModuleGenerator extends AbstractPageModuleGenerator
{
    @Autowired
    public GeneralPageModuleGenerator(ServletModuleManager servletModuleManager,
                                    ProductAccessor productAccessor,
                                    ApplicationLinkOperationsFactory applicationLinkSignerFactory,
                                    IFrameRenderer iFrameRenderer,
                                    UserManager userManager,
                                    PluginRetrievalService pluginRetrievalService
    )
    {
        super(servletModuleManager, applicationLinkSignerFactory, iFrameRenderer,
              new DefaultWebItemContext(
                      productAccessor.getPreferredGeneralSectionKey(),
                      productAccessor.getPreferredGeneralWeight(),
                      productAccessor.getLinkContextParams()
              ), userManager, productAccessor, pluginRetrievalService);
    }

    @Override
    public String getType()
    {
        return "general-page";
    }

    @Override
    public String getName()
    {
        return "General Page";
    }

    @Override
    public String getDescription()
    {
        return "A non-admin general page decorated by the application, with a link in a globally-accessible place";
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    protected String getDecorator()
    {
        return "atl.general";
    }
}
