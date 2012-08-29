package com.atlassian.labs.remoteapps.plugin.module.page;

import com.atlassian.labs.remoteapps.plugin.module.IFrameParams;
import com.atlassian.labs.remoteapps.plugin.module.IFrameRenderer;
import com.atlassian.labs.remoteapps.plugin.module.WebItemContext;
import com.atlassian.labs.remoteapps.plugin.module.WebItemCreator;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a builder for remote page descriptor generation.  Builder instances meant to be shared
 * across threads.
 */
@Component
public class RemotePageDescriptorCreator
{
    private final ServletModuleManager servletModuleManager;
    private final UserManager userManager;
    private final WebItemCreator webItemCreator;
    private final IFrameRenderer iFrameRenderer;
    private final ProductAccessor productAccessor;

    @Autowired
    public RemotePageDescriptorCreator(
            ServletModuleManager servletModuleManager, UserManager userManager,
            WebItemCreator webItemCreator, IFrameRenderer iFrameRenderer,
            ProductAccessor productAccessor)
    {
        this.servletModuleManager = servletModuleManager;
        this.userManager = userManager;
        this.webItemCreator = webItemCreator;
        this.iFrameRenderer = iFrameRenderer;
        this.productAccessor = productAccessor;
    }

    public Builder newBuilder()
    {
        return new Builder();
    }

    public static String createLocalUrl(String pluginKey, String pageKey)
    {
        return "/remoteapps/" + pluginKey + "/" + pageKey;
    }

    public class Builder
    {
        private WebItemCreator.Builder webItemCreatorBuilder;
        private String decorator = "";
        private String templateSuffix = "";
        private Condition condition = new AlwaysDisplayCondition();

        public Builder()
        {
            this.webItemCreatorBuilder = webItemCreator.newBuilder();
            this.webItemCreatorBuilder.setPreferredWeight(productAccessor.getPreferredGeneralWeight());
            this.webItemCreatorBuilder.setPreferredSectionKey(productAccessor.getPreferredGeneralSectionKey());
            this.webItemCreatorBuilder.setContextParams(productAccessor.getLinkContextParams());
            this.webItemCreatorBuilder.setCondition(condition.getClass());
        }
        public Iterable<ModuleDescriptor> build(Plugin plugin, Element descriptor)
        {
            checkNotNull(decorator);
            String key = getRequiredAttribute(descriptor, "key");
            final String url = getRequiredUriAttribute(descriptor, "url").toString();

            String localUrl = createLocalUrl(plugin.getKey(), key);
            WebItemModuleDescriptor webItemModuleDescriptor = webItemCreatorBuilder.build(plugin, key, localUrl, descriptor);

            return ImmutableSet.<ModuleDescriptor>of(
                    createServletDescriptor(plugin, descriptor, key, url, localUrl),
                    webItemModuleDescriptor);
        }

        private ServletModuleDescriptor createServletDescriptor(
                final Plugin plugin,
                Element e,
                String key,
                final String path,
                String localUrl
        )
        {
            final String pageName = getRequiredAttribute(e, "name");
            Element config = e.createCopy();
            final String moduleKey = "servlet-" + key;
            config.addAttribute("key", moduleKey);
            config.addAttribute("class", IFramePageServlet.class.getName());
            config.addElement("url-pattern").setText(localUrl + "");
            config.addElement("url-pattern").setText(localUrl + "/*");

            final IFrameParams params = new IFrameParams(e);
            final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws
                        PluginParseException
                {
                    PageInfo pageInfo = new PageInfo(decorator, templateSuffix, pageName, condition);

                    return (T) new IFramePageServlet(
                            pageInfo,
                            iFrameRenderer,
                            new IFrameContext(plugin.getKey(), path, moduleKey, params), userManager
                    );
                }
            }, servletModuleManager);
            descriptor.init(plugin, config);
            return descriptor;
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
    }
}
