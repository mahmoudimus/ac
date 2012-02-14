package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.DefaultWebItemContext;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for user profile pages, generating a web item and servlet with iframe
 */
public class ProfilePageModuleGenerator extends AbstractPageModuleGenerator
{
    public ProfilePageModuleGenerator(ServletModuleManager servletModuleManager,
                                    TemplateRenderer templateRenderer,
                                    ProductAccessor productAccessor,
                                    ApplicationLinkOperationsFactory applicationLinkSignerFactory,
                                    IFrameRenderer iFrameRenderer,
            UserManager userManager
    )
    {
        super(servletModuleManager, templateRenderer, applicationLinkSignerFactory, iFrameRenderer,
              new DefaultWebItemContext(
                      productAccessor.getPreferredProfileSectionKey(),
                      productAccessor.getPreferredProfileWeight(),
                      productAccessor.getLinkContextParams()
              ), userManager, productAccessor);
    }

    @Override
    public String getType()
    {
        return "profile-page";
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    protected String getDecorator()
    {
        return "atl.userprofile";
    }

    @Override
    protected String getTemplateSuffix()
    {
        return "-profile";
    }
}
