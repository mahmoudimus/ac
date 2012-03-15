package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.module.ClassPrefixModuleFactory;
import com.atlassian.plugin.module.LegacyModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.component.ComponentLocator;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.net.URI;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 15/03/12 Time: 11:18 PM To change this template use
 * File | Settings | File Templates.
 */
public class ProxyServletLoader
{
    private final ServletModuleManager servletModuleManager;
    private final BundleContext bundleContext;
    private final Plugin plugin;

    public ProxyServletLoader(BundleContext bundleContext,
            ServletModuleManager servletModuleManager,
            PluginRetrievalService pluginRetrievalService)
    {
        this.bundleContext = bundleContext;
        this.servletModuleManager = servletModuleManager;
        this.plugin = pluginRetrievalService.getPlugin();
    }
    
    public String getAppProxyPrefix()
    {
        return "/plugins/servlet/" + plugin.getKey() + "/proxy";
    }
    
    public void start(int httpPort)
    {
        String proxyPrefix = getAppProxyPrefix();

        // this isn't great but if the servlet module manager is a proxy, who gets destroyed
        // when the plugin goes down, it cases a proxy destroyed exception in servlet module maanager
        // when the servlet is unregistered
        ServletModuleDescriptor descriptor = new ServletModuleDescriptor(
                new ClassPrefixModuleFactory(new DefaultHostContainer()),
                ComponentLocator.getComponent(ServletModuleManager.class));
        Element root = DocumentHelper.createElement("servlet");
        root.addAttribute("key", "__servlet_proxy");
        root.addAttribute("class", ProxyServlet.class.getName());
        root.addElement("url-pattern").setText("/" + plugin.getKey() + "/proxy/*");
        root.addElement("init-param").
                addElement("param-name").addText("remotePort").getParent().
                addElement("param-value").addText(String.valueOf(httpPort)).getParent().getParent().
            addElement("init-param").
                addElement("param-name").addText("remoteServer").getParent().
                addElement("param-value").addText("localhost").getParent().getParent().
             addElement("init-param").
                addElement("param-name").addText("remotePath").getParent().
                addElement("param-value").addText("");
        descriptor.init(plugin, root);
        bundleContext.registerService(ModuleDescriptor.class.getName(), descriptor, null);
    }
}
