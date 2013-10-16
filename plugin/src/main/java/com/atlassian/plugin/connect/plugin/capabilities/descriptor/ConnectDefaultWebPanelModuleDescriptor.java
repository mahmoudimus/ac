package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.webpanel.IFrameRemoteWebPanel;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ContainerManagedPlugin;
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
    private final String url;
    private final String moduleKey;
    private final IFrameParams iFrameParams;
    private final IFrameRenderer iFrameRenderer;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final UserManager userManager;

    public ConnectDefaultWebPanelModuleDescriptor(ContainerManagedPlugin plugin, WebPanelCapabilityBean bean, Element domElement)
    {
        super(createHostContainer(plugin), createModuleFactory(), findWebInterfaceManager(plugin));
        this.url = bean.getUrl();
        this.moduleKey = bean.getKey();
        this.iFrameParams = new IFrameParamsImpl(domElement);
        this.iFrameRenderer = plugin.getContainerAccessor().createBean(IFrameRenderer.class);
        this.contextMapURLSerializer = plugin.getContainerAccessor().createBean(ContextMapURLSerializer.class);
        this.userManager = plugin.getContainerAccessor().createBean(UserManager.class);
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

    private static HostContainer createHostContainer(ContainerManagedPlugin plugin)
    {
        return plugin.getContainerAccessor().createBean(HostContainer.class);
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

    private static WebInterfaceManager findWebInterfaceManager(ContainerManagedPlugin plugin)
    {
        return ((ContainerManagedPlugin)plugin).getContainerAccessor().createBean(WebInterfaceManager.class);
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