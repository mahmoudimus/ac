package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.NameToKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.IFramePageRenderer;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.projectconfig.IFrameProjectConfigTabServlet;
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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair.HostUrlPaths;
import static com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils.getService;

/**
 * @since 1.0
 */
@Component
public class IFramePageServletDescriptorFactory
{
    private final IFramePageRenderer iFramePageRenderer;
    private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final BundleContext bundleContext;

    @Autowired
    public IFramePageServletDescriptorFactory(IFramePageRenderer iFramePageRenderer, UserManager userManager,
                                              UrlVariableSubstitutor urlVariableSubstitutor, BundleContext bundleContext)
    {
        this.iFramePageRenderer = iFramePageRenderer;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.bundleContext = bundleContext;
    }

    /**
     * @return a generic iframe module servlet descriptor.
     */
    public ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, final NameToKeyBean bean,
                                                                 final String localUrl, final String path,
                                                                 final String decorator, final String templateSuffix,
                                                                 final Condition condition,
                                                                 final Map<String, String> metaTagsContent)
    {
        return createIFrameServletDescriptor(plugin, bean, localUrl, path, decorator, templateSuffix, condition,
                metaTagsContent, ModuleFactoryType.PAGE);
    }

    /**
     * @return an iframe servlet module descriptor suitable for providing a JIRA Project admin tab.
     */
    public ServletModuleDescriptor createIFrameProjectConfigTabServletDescriptor(final Plugin plugin, final NameToKeyBean bean,
                                                                                 final String localUrl, final String path,
                                                                                 final String decorator, final String templateSuffix,
                                                                                 final Condition condition,
                                                                                 final Map<String, String> metaTagsContent)
    {
        return createIFrameServletDescriptor(plugin, bean, localUrl, path, decorator, templateSuffix, condition,
                metaTagsContent, ModuleFactoryType.JIRA_PROJECT_ADMIN_TAB);
    }

    public ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, IFrameServletBean servletBean)
    {
        AddonUrlTemplatePair urlTemplatePair = servletBean.getUrlTemplatePair();

        // TODO: In ACDEV-498 push the url template into IFramePageContext
        String path = urlTemplatePair.getAddonUrlTemplate().getTemplateString();

        return createIFrameServletDescriptor(plugin, servletBean.getLinkBean(), path,
                servletBean.getPageInfo(),
                ModuleFactoryType.PAGE, urlTemplatePair.getHostUrlPaths().getServletRegistrationPaths(),
                servletBean.getiFrameParams());
    }

    private ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, final NameToKeyBean bean,
                                                                  final String localUrl, final String path,
                                                                  final String decorator, final String templateSuffix,
                                                                  final Condition condition,
                                                                  final Map<String, String> metaTagsContent,
                                                                  final ModuleFactoryType type)
    {
        final String pageName = (!Strings.isNullOrEmpty(bean.getName().getValue()) ? bean.getName().getValue() : bean.getKey());

        return createIFrameServletDescriptor(plugin, bean, path,
                new PageInfo(decorator, templateSuffix, pageName, condition, metaTagsContent),
                type, ImmutableList.<String>of(localUrl + "", localUrl + "/*"),
                new IFrameParamsImpl());
    }

    private ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, final NameToKeyBean bean,
                                                                  final String path,
                                                                  final PageInfo pageInfo,
                                                                  final ModuleFactoryType type,
                                                                  Iterable<String> servletUrlPatterns,
                                                                  IFrameParams iFrameParams)
    {
        final String moduleKey = "servlet-" + bean.getKey();

        final Element servletElement = createServletElement(moduleKey, servletUrlPatterns);

        final Map<String, String> contextParams = urlVariableSubstitutor.getContextVariableMap(path);

        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(
                getModuleFactory(plugin, path, pageInfo, moduleKey, contextParams, iFrameParams, type),
                getService(bundleContext, ServletModuleManager.class)
        );

        descriptor.init(plugin, servletElement);

        return descriptor;
    }

    private Element createServletElement(String moduleKey, Iterable<String> servletUrlPatterns)
    {
        Element root = new DOMElement("servlet");
        root.addAttribute("key", moduleKey);
        root.addAttribute("system", "true");
        root.addAttribute("class", IFramePageServlet.class.getName());
        for (String urlPattern : servletUrlPatterns)
        {
            root.addElement("url-pattern").setText(urlPattern);
        }

        return root;
    }

    /**
     * This enumerates the different types of {@link HttpServlet} that can be returned by
     * {@link ServletModuleDescriptor ServletModuleDescriptors} created by the
     * {@link IFramePageServletDescriptorFactory}.
     */
    private static enum ModuleFactoryType
    {
        /**
         * A generic iframe servlet.
         */
        PAGE,
        /**
         * An iframe servlet that provides JIRA Project admin tabs.
         */
        JIRA_PROJECT_ADMIN_TAB
    }

    private ModuleFactory getModuleFactory(final Plugin plugin, final String path, final PageInfo pageInfo,
                                           final String moduleKey, final Map<String, String> contextParams,
                                           final IFrameParams params, final ModuleFactoryType type)
    {
        switch (type)
        {
            case PAGE:
                return new ModuleFactory()
                {
                    @Override
                    public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws
                            PluginParseException
                    {

                        return (T) new IFramePageServlet(
                                pageInfo,
                                iFramePageRenderer,
                                new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager, urlVariableSubstitutor,
                                contextParams
                        );
                    }
                };

            case JIRA_PROJECT_ADMIN_TAB:
                return new ModuleFactory()
                {
                    @Override
                    public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws
                            PluginParseException
                    {
                        return (T) new IFrameProjectConfigTabServlet(
                                pageInfo,
                                iFramePageRenderer,
                                new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager, urlVariableSubstitutor,
                                contextParams
                        );
                    }
                };

            default:
                throw new IllegalStateException("Unrecognized " + ModuleFactoryType.class + ": " + type);
        }

    }
}
