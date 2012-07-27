package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.modules.DefaultWebItemContext;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for admin pages, generating a web item and servlet with iframe
 */
@Component
public class AdminPageModuleGenerator extends AbstractPageModuleGenerator
{
    private final UserManager userManager;

    @Autowired
    public AdminPageModuleGenerator(ServletModuleManager servletModuleManager,
                                    ProductAccessor productAccessor,
                                    IFrameRenderer iFrameRenderer,
            PluginRetrievalService pluginRetrievalService,
                                    UserManager userManager
    )
    {
        super(servletModuleManager, iFrameRenderer,
              new DefaultWebItemContext(
                      productAccessor.getPreferredAdminSectionKey(),
                      productAccessor.getPreferredAdminWeight(),
                      productAccessor.getLinkContextParams()
              ), userManager, productAccessor, pluginRetrievalService);
        this.userManager = userManager;
    }

    @Override
    public String getType()
    {
        return "admin-page";
    }

    @Override
    public String getName()
    {
        return "Administration Page";
    }

    @Override
    public String getDescription()
    {
        return "An admin page decorated in the admin section, with a link in the admin menu";
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
