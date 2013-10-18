package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.webpanel.IFrameRemoteWebPanel;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.AbstractWebFragmentModuleDescriptor;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class ConnectDefaultWebPanelModuleDescriptor extends DefaultWebPanelModuleDescriptor
{
    private String url;
    private String moduleKey;
    private IFrameParams iFrameParams;

    private final IFrameRenderer iFrameRenderer;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final UserManager userManager;
    private final UrlValidator urlValidator;

    public ConnectDefaultWebPanelModuleDescriptor(HostContainer hostContainer, WebInterfaceManager webInterfaceManager, IFrameRenderer iFrameRenderer,
                                                  ContextMapURLSerializer contextMapURLSerializer, UserManager userManager, UrlValidator urlValidator)
    {
        super(hostContainer, createModuleFactory(), webInterfaceManager);
        this.iFrameRenderer = iFrameRenderer;
        this.contextMapURLSerializer = contextMapURLSerializer;
        this.userManager = userManager;
        this.urlValidator = urlValidator;
    }

    @Override
    public void init(Plugin plugin, Element domElement)
    {
        this.url = validateUrl(domElement.attributeValue("url"));
        this.moduleKey = domElement.attributeValue("key");
        this.iFrameParams = new IFrameParamsImpl(domElement);
        super.init(plugin, domElement);
    }

    @Override
    public WebPanel getModule()
    {
        IFrameContext iFrameContext = new IFrameContextImpl(getPluginKey(), url, moduleKey, iFrameParams);
        IFrameRemoteWebPanel delegate = new IFrameRemoteWebPanel(iFrameRenderer, iFrameContext,
                condition != null ? condition : new AlwaysDisplayCondition(),
                contextMapURLSerializer, userManager, new UrlVariableSubstitutor());
        return new ContextAwareWebPanel(delegate, this);
    }

    private String validateUrl(String url)
    {
        urlValidator.validate(url);
        return url;
    }

    private static ModuleFactory createModuleFactory()
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                return null; // never called
            }
        };
    }

    private static class ContextAwareWebPanel implements WebPanel
    {
        private final WebPanel delegate;
        private final AbstractWebFragmentModuleDescriptor<WebPanel> contextProvider;

        private ContextAwareWebPanel(WebPanel delegate, AbstractWebFragmentModuleDescriptor<WebPanel> contextProvider)
        {
            this.delegate = delegate;
            this.contextProvider = contextProvider;
        }

        public String getHtml(final Map<String, Object> context)
        {
            return delegate.getHtml(contextProvider.getContextProvider().getContextMap(new HashMap<String, Object>(context)));
        }

        public void writeHtml(Writer writer, Map<String, Object> context) throws IOException
        {
            delegate.writeHtml(writer, context);
        }
    }
}