package com.atlassian.plugin.connect.plugin.module.page;

import java.net.URI;
import java.util.Map;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.WebItemContext;
import com.atlassian.plugin.connect.plugin.module.WebItemCreator;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a builder for remote page descriptor generation.  Builder instances meant to be shared
 * across threads.
 */
@Component
public final class RemotePageDescriptorCreator
{
    private final BundleContext bundleContext;
    private final UserManager userManager;
    private final WebItemCreator webItemCreator;
    private final IFrameRendererImpl iFrameRenderer;
    private final ProductAccessor productAccessor;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public RemotePageDescriptorCreator(
            BundleContext bundleContext, UserManager userManager,
            WebItemCreator webItemCreator, IFrameRendererImpl iFrameRenderer,
            ProductAccessor productAccessor, UrlValidator urlValidator,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.bundleContext = bundleContext;
        this.userManager = userManager;
        this.webItemCreator = webItemCreator;
        this.iFrameRenderer = iFrameRenderer;
        this.productAccessor = productAccessor;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    public Builder newBuilder()
    {
        return new Builder();
    }

    public static String createLocalUrl(String pluginKey, String pageUrl)
    {
        return URI.create("/atlassian-connect/" + pluginKey + (pageUrl.startsWith("/") ? "" : "/") + pageUrl).toString();
    }

    public class Builder
    {
        private WebItemCreator.Builder webItemCreatorBuilder;
        private String decorator = "";
        private String templateSuffix = "";
        private Condition condition = new AlwaysDisplayCondition();
        private Map<String, String> metaTagsContent = Maps.newHashMap();

        public Builder()
        {
            this.webItemCreatorBuilder = webItemCreator.newBuilder();
            this.webItemCreatorBuilder.setPreferredWeight(productAccessor.getPreferredGeneralWeight());
            this.webItemCreatorBuilder.setPreferredSectionKey(productAccessor.getPreferredGeneralSectionKey());
            this.webItemCreatorBuilder.setCondition(condition.getClass());
        }
        public Iterable<DescriptorToRegister> build(Plugin plugin, Element descriptor)
        {
            checkNotNull(decorator);
            String key = getRequiredAttribute(descriptor, "key");
            final String url = getRequiredAttribute(descriptor, "url");
            this.webItemCreatorBuilder.setContextParams(urlVariableSubstitutor.getContextVariables(url));

            String localUrl = createLocalUrl(plugin.getKey(), key);
            DescriptorToRegister webItemModuleDescriptor = new DescriptorToRegister(webItemCreatorBuilder.build(plugin, key, localUrl, descriptor));

            return ImmutableSet.of(
                    createServletDescriptor(plugin, descriptor, key, url, localUrl),
                    webItemModuleDescriptor);
        }

        public DescriptorToRegister createServletDescriptor(
                final Plugin plugin,
                Element e,
                String key,
                final String path,
                String localUrl)
        {
            final String pageName = getRequiredAttribute(e, "name");
            Element config = e.createCopy();
            final String moduleKey = "servlet-" + key;
            config.addAttribute("key", moduleKey);
            config.addAttribute("system", "true");
            config.addAttribute("class", IFramePageServlet.class.getName());
            config.addElement("url-pattern").setText(localUrl + "");
            config.addElement("url-pattern").setText(localUrl + "/*");

            final IFrameParams params = new IFrameParamsImpl(e);
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
                            new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager, urlVariableSubstitutor
                    );
                }
            }, getService(bundleContext, ServletModuleManager.class));
            descriptor.init(plugin, config);
            return new DescriptorToRegister(descriptor);
        }

        public Builder setDecorator(String decorator)
        {
            this.decorator = decorator;
            return this;
        }

        public Builder setTemplateSuffix(String templateSuffix)
        {
            this.templateSuffix = templateSuffix;
            return this;
        }

        public Builder setCondition(Condition condition)
        {
            this.condition = condition;
            webItemCreatorBuilder.setCondition(condition.getClass());
            return this;
        }

        public Builder setWebItemStyleClass(String webItemStyleClass)
        {
            webItemCreatorBuilder.setAdditionalStyleClass(webItemStyleClass);
            return this;
        }

        public Builder setWebItemContext(WebItemContext webItemContext)
        {
            webItemCreatorBuilder.setContextParams(webItemContext.getContextParams())
                    .setPreferredSectionKey(webItemContext.getPreferredSectionKey())
                    .setPreferredWeight(webItemContext.getPreferredWeight());
            return this;
        }

		public Builder setMetaTagContent(String name, String content)
		{
			metaTagsContent.put(name, content);
			return this;
		}
	}
}
