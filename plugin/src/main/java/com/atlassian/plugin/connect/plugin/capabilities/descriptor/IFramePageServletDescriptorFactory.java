package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.Map;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFramePageServlet;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils.getService;

/**
 * @since version
 */
@Component
public class IFramePageServletDescriptorFactory
{
    private final IFrameRendererImpl iFrameRenderer;
    private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final BundleContext bundleContext;

    @Autowired
    public IFramePageServletDescriptorFactory(IFrameRendererImpl iFrameRenderer, UserManager userManager, UrlVariableSubstitutor urlVariableSubstitutor, BundleContext bundleContext)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.bundleContext = bundleContext;
    }

    public ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, final CapabilityBean bean, final String localUrl, final String path, final String decorator, final String templateSuffix, final Condition condition, final Map<String, String> metaTagsContent)
    {
        //final String pageName = (!Strings.isNullOrEmpty(bean.getName().getValue()) ? bean.getName().getValue() : bean.getKey());
        
        //final String moduleKey = "servlet-" + bean.getKey();

        final String pageName = "";
        final String moduleKey = "";
        
        final Element servletElement = createServletElement(moduleKey,localUrl);

        final Map<String,String> contextParams = urlVariableSubstitutor.getContextVariableMap(path);

        final IFrameParams params = new IFrameParamsImpl();
        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws
                    PluginParseException
            {
                PageInfo pageInfo = new PageInfo(decorator, templateSuffix, pageName, condition, metaTagsContent);

                return (T) new IFramePageServlet(
                        pageInfo,
                        iFrameRenderer,
                        new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager, urlVariableSubstitutor,
                        contextParams
                );
            }
        }, getService(bundleContext, ServletModuleManager.class));
        
       descriptor.init(plugin,servletElement);
        
        return descriptor;
    }
    
    private Element createServletElement(String moduleKey, String localUrl)
    {
        Element root = new DOMElement("servlet");
        root.addAttribute("key",moduleKey);
        root.addAttribute("system","true");
        root.addAttribute("class",IFramePageServlet.class.getName());
        root.addElement("url-pattern").setText(localUrl + "");
        root.addElement("url-pattern").setText(localUrl + "/*");
        
        return root;
    }
}
