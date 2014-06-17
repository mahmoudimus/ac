package com.atlassian.plugin.connect.plugin.module.jira.projectconfig;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.*;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.IsProjectAdminCondition;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFramePageServlet;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

import java.net.URI;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Generates a project config tab with a servlet containing an iframe and a web item.
 */
public final class ProjectConfigTabModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    @XmlDescriptor
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
	private final ProjectConfigTabPageBuilder projectConfigTabPageBuilder;
	private final BundleContext bundleContext;
	private final IFramePageRenderer iFramePageRenderer;
	private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    private Element descriptor;

	private WebItemCreator.Builder webItemCreatorBuilder;
    @XmlDescriptor
	private LegacyXmlDynamicDescriptorRegistration.Registration registration;
	private Condition condition;

    public ProjectConfigTabModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            BundleContext bundleContext,
            IFramePageRenderer iFramePageRenderer,
            UserManager userManager,
            WebItemCreator webItemCreator,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ConnectContainerUtil connectContainerUtil)
    {
        super(moduleFactory);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.bundleContext = checkNotNull(bundleContext);
        this.iFramePageRenderer = checkNotNull(iFramePageRenderer);
        this.userManager = checkNotNull(userManager);
        this.webItemCreatorBuilder = checkNotNull(webItemCreator).newBuilder();
        this.condition = connectContainerUtil.createBean(IsProjectAdminCondition.class);

        this.projectConfigTabPageBuilder = new ProjectConfigTabPageBuilder();
    }

    @Override
    public Void getModule()
    {
        return null;
    }

	@Override
	public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
	{
		super.init(plugin, element);
		this.descriptor = element;
	}

	@Override
	public void enabled()
	{
		super.enabled();
		final String key = getRequiredAttribute(descriptor, "key");

		final String location = getRequiredAttribute(descriptor, "location");
		final int weight = Integer.parseInt(getRequiredAttribute(descriptor, "weight"));

		Iterable<DescriptorToRegister> descriptors = projectConfigTabPageBuilder
				.setWebItemContext(new DefaultWebItemContext(
                        "atl.jira.proj.config/" + location,
                        weight,
                        ImmutableMap.of("projectKey", "${project.key}")
                ))
				.setMetaTagContent("adminActiveTab", key)
				.build(getPlugin(), descriptor);
		this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(), descriptors);
	}

	@Override
	public void disabled()
	{
		super.disabled();
		if (registration != null)
		{
			registration.unregister();
		}
	}

	private class ProjectConfigTabPageBuilder
	{
		private Map<String, String> metaTagsContent = newHashMap();

		public ProjectConfigTabPageBuilder setWebItemContext(WebItemContext webItemContext)
		{
			webItemCreatorBuilder.setContextParams(webItemContext.getContextParams())
					.setPreferredSectionKey(webItemContext.getPreferredSectionKey())
					.setPreferredWeight(webItemContext.getPreferredWeight());
			return this;
		}

		public ProjectConfigTabPageBuilder setMetaTagContent(String name, String content)
		{
			metaTagsContent.put(name, content);
			return this;
		}

		public Iterable<DescriptorToRegister> build(Plugin plugin, Element descriptor)
		{
			String key = getRequiredAttribute(descriptor, "key");
			final URI url = getRequiredUriAttribute(descriptor, "url");

            String localUrl = createLocalUrl(plugin.getKey(), key);
			DescriptorToRegister webItemModuleDescriptor = new DescriptorToRegister(webItemCreatorBuilder.build(plugin, key, localUrl, descriptor));

			return ImmutableSet.of(
					createServletDescriptor(plugin, descriptor, key, url, localUrl),
					webItemModuleDescriptor);
		}

		private DescriptorToRegister createServletDescriptor(
				final Plugin plugin,
				Element e,
				String key,
				final URI path,
                String localUrl
		)
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
					final PageInfo pageInfo = new PageInfo("", "-project-admin", pageName, condition, metaTagsContent);
					return (T) new IFrameProjectConfigTabServlet(
                            pageInfo,
                            iFramePageRenderer,
                            new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager,
                            urlVariableSubstitutor,
                            Maps.<String, String>newHashMap()
                    );
				}
			}, getService(bundleContext, ServletModuleManager.class));
			descriptor.init(plugin, config);
			return new DescriptorToRegister(descriptor);
		}
	}

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
