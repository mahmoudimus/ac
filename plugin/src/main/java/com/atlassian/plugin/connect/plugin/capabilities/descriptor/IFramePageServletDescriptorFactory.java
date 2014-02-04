package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.Map;

import javax.servlet.http.HttpServlet;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.GeneratedKeyBean;
import com.atlassian.plugin.connect.modules.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.IFramePageRenderer;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.projectconfig.IFrameProjectConfigTabServlet;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFramePageServlet;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
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
     * @return an iframe servlet module descriptor suitable for providing a JIRA Project admin tab.
     */
    public ServletModuleDescriptor createIFrameProjectConfigTabServletDescriptor(final Plugin plugin, final GeneratedKeyBean bean,
                                                                                 final String localUrl, final String path,
                                                                                 final String decorator, final String templateSuffix,
                                                                                 final Condition condition,
                                                                                 final Map<String, String> metaTagsContent)
    {
        return createIFrameServletDescriptor(plugin, bean, localUrl, path, decorator, templateSuffix, condition,
                metaTagsContent, ModuleFactoryType.JIRA_PROJECT_ADMIN_TAB);
    }

    /**
     * @return iframe module servlet descriptor suitable for a generic page or panel.
     */
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

    private ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, final GeneratedKeyBean bean,
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

    private ServletModuleDescriptor createIFrameServletDescriptor(final Plugin plugin, final GeneratedKeyBean bean,
                                                                  final String path,
                                                                  final PageInfo pageInfo,
                                                                  final ModuleFactoryType type,
                                                                  Iterable<String> servletUrlPatterns,
                                                                  IFrameParams iFrameParams)
    {
        final String moduleKey = "servlet-" + bean.getKey();
        final Element servletElement = createServletElement(moduleKey, servletUrlPatterns);
        final Map<String, String> contextParams = urlVariableSubstitutor.getContextVariableMap(path);

        // In order to control the construction of the IFrame servlet from our dynamic module descriptor, we provide
        // our own implementation of ModuleFactory.
        ModuleFactory moduleFactory = new IFrameServletModuleFactoryBuilder()
                .withPageInfo(pageInfo)
                .withIFramePageRenderer(iFramePageRenderer)
                .withIFrameContext(new IFrameContextImpl(plugin.getKey(), path, moduleKey, iFrameParams))
                .withUserManager(userManager)
                .withUrlVariableSubstitutor(urlVariableSubstitutor)
                .withContextParams(contextParams)
                .withModuleFactoryType(type)
                .build();

        ServletModuleManager service = getService(bundleContext, ServletModuleManager.class);

        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(moduleFactory, service);
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

    private static class IFrameServletModuleFactoryBuilder
    {
        private PageInfo pageInfo;
        private IFramePageRenderer iFramePageRenderer;
        private IFrameContext iFrameContext;
        private UserManager userManager;
        private UrlVariableSubstitutor urlVariableSubstitutor;
        private Map<String, String> contextParams;
        private ModuleFactoryType moduleFactoryType;

        public IFrameServletModuleFactoryBuilder withPageInfo(PageInfo pageInfo)
        {
            this.pageInfo = pageInfo;
            return this;
        }

        public IFrameServletModuleFactoryBuilder withIFramePageRenderer(IFramePageRenderer iFramePageRenderer)
        {
            this.iFramePageRenderer = iFramePageRenderer;
            return this;
        }

        public IFrameServletModuleFactoryBuilder withIFrameContext(IFrameContext iFrameContext)
        {
            this.iFrameContext = iFrameContext;
            return this;
        }

        public IFrameServletModuleFactoryBuilder withUserManager(UserManager userManager)
        {
            this.userManager = userManager;
            return this;
        }

        public IFrameServletModuleFactoryBuilder withUrlVariableSubstitutor(UrlVariableSubstitutor urlVariableSubstitutor)
        {
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            return this;
        }

        public IFrameServletModuleFactoryBuilder withContextParams(Map<String, String> contextParams)
        {
            this.contextParams = contextParams;
            return this;
        }

        public IFrameServletModuleFactoryBuilder withModuleFactoryType(ModuleFactoryType moduleFactoryType)
        {
            this.moduleFactoryType = moduleFactoryType;
            return this;
        }

        public ModuleFactory build()
        {
            final HttpServlet servlet;

            switch (moduleFactoryType) {
                case PAGE:
                    servlet = new IFramePageServlet(pageInfo, iFramePageRenderer, iFrameContext, userManager,
                            urlVariableSubstitutor, contextParams);
                    break;
                case JIRA_PROJECT_ADMIN_TAB:
                    servlet = new IFrameProjectConfigTabServlet(pageInfo, iFramePageRenderer, iFrameContext,
                            userManager, urlVariableSubstitutor, contextParams);
                    break;
                default:
                    throw new IllegalStateException("Unknown " + ModuleFactoryType.class.getSimpleName() + " " + moduleFactoryType);
            }

            return new ModuleFactory()
            {
                @Override
                public <T> T createModule(final String s, final ModuleDescriptor<T> tModuleDescriptor)
                        throws PluginParseException
                {
                    // This looks horrific, but in practice the ModuleFactory will only ever be called with a
                    // ServletModuleDescriptor.
                    @SuppressWarnings("unchecked")
                    T module = (T) servlet;

                    return module;
                }
            };
        }
    }

}
