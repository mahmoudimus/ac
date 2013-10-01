package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;

@Component
public class WebItemModuleProvider implements ConnectModuleProvider<WebItemCapabilityBean>
{
    private final WebItemModuleDescriptorFactory webItemFactory;
    private final IFramePageServletDescriptorFactory iframePageFactory;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public WebItemModuleProvider(WebItemModuleDescriptorFactory webItemFactory, IFramePageServletDescriptorFactory iframePageFactory, PluginAccessor pluginAccessor)
    {
        this.webItemFactory = webItemFactory;
        this.iframePageFactory = iframePageFactory;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, List<WebItemCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebItemCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, WebItemCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        if (bean.isAbsolute())
        {
            descriptors.add(webItemFactory.createWebItemDescriptor(plugin, bean));
        }
        else
        {
            String localUrl = createLocalUrl(plugin.getKey(), bean.getKey());

            WebItemCapabilityBean newBean = newWebItemBean(bean).withLink(localUrl).build();
            descriptors.add(webItemFactory.createWebItemDescriptor(plugin, newBean));

            //todo: make sure we do something to actually look up condition and metaTags map
            //ONLY create the servlet if one doesn't already exist!!!
            List<ServletModuleDescriptor> servletDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(ServletModuleDescriptor.class);
            boolean servletExists = false;
            for(ServletModuleDescriptor servletDescriptor : servletDescriptors)
            {
                if(servletDescriptor.getPaths().contains(localUrl))
                {
                    servletExists = true;
                    break;
                }
            }
            
            if(!servletExists)
            {
                descriptors.add(iframePageFactory.createIFrameServletDescriptor(plugin,newBean,localUrl,bean.getLink(),"atl.general","", new AlwaysDisplayCondition(),new HashMap<String, String>()));
            }
        }

        return descriptors;
    }

    //TODO: this should be refactored into a util class
    public static String createLocalUrl(String pluginKey, String pageUrl)
    {
        return URI.create("/atlassian-connect/" + pluginKey + (pageUrl.startsWith("/") ? "" : "/") + pageUrl).toString();
    }

}
