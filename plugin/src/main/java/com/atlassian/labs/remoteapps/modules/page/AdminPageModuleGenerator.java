package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.DefaultWebItemContext;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for admin pages, generating a web item and servlet with iframe
 */
public class AdminPageModuleGenerator extends AbstractPageModuleGenerator
{
    private final UserManager userManager;

    public AdminPageModuleGenerator(ServletModuleManager servletModuleManager,
                                    TemplateRenderer templateRenderer,
                                    ProductAccessor productAccessor,
                                    ApplicationLinkOperationsFactory applicationLinkSignerFactory,
                                    IFrameRenderer iFrameRenderer,
                                    UserManager userManager
    )
    {
        super(servletModuleManager, templateRenderer, applicationLinkSignerFactory, iFrameRenderer,
              new DefaultWebItemContext(
                      productAccessor.getPreferredAdminSectionKey(),
                      productAccessor.getPreferredAdminWeight(),
                      productAccessor.getLinkContextParams()
              ), userManager);
        this.userManager = userManager;
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
    protected Condition getCondition()
    {
        return new Condition()
        {
            @Override
            public void init(Map<String, String> params) throws PluginParseException
            {
            }

            @Override
            public boolean shouldDisplay(Map<String, Object> context)
            {
                return userManager.isAdmin(userManager.getRemoteUsername());
            }
        };
    }

    @Override
    protected String getDecorator()
    {
        return "atl.admin";
    }
}
