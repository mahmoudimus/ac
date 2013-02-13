package com.atlassian.plugin.remotable.plugin.module.jira.projectconfig;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.DefaultWebItemContext;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.WebItemContext;
import com.atlassian.plugin.remotable.plugin.module.WebItemCreator;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFramePageServlet;
import com.atlassian.plugin.remotable.plugin.module.page.PageInfo;
import com.atlassian.plugin.remotable.plugin.module.permission.jira.IsProjectAdminCondition;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
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

import static com.atlassian.plugin.remotable.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.atlassian.plugin.remotable.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a project config tab with a servlet containing an iframe and a web item.
 */
public final class ProjectConfigTabModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
	private final ProjectConfigTabPageBuilder projectConfigTabPageBuilder;
	private final BundleContext bundleContext;
	private final IFrameRendererImpl iFrameRenderer;
	private final UserManager userManager;
	private Element descriptor;

	private WebItemCreator.Builder webItemCreatorBuilder;
	private DynamicDescriptorRegistration.Registration registration;
	private Condition condition;

    public ProjectConfigTabModuleDescriptor(
            ModuleFactory moduleFactory,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            BundleContext bundleContext,
            IFrameRendererImpl iFrameRenderer,
            UserManager userManager,
            WebItemCreator webItemCreator,
            JiraAuthenticationContext authenticationContext)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.bundleContext = checkNotNull(bundleContext);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.userManager = checkNotNull(userManager);
        this.webItemCreatorBuilder = checkNotNull(webItemCreator).newBuilder();
        this.condition = new IsProjectAdminCondition(checkNotNull(authenticationContext));

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
                        ImmutableMap.of("projectKey", "$!helper.project.key")
                ))
				.setMetaTagContent("adminActiveTab", "webitem-".concat(key))
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
		private Map<String, String> metaTagsContent = Maps.newHashMap();

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

			URI localUrl = createLocalUrl(plugin.getKey(), key);
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
				URI localUrl
		)
		{
			final String pageName = getRequiredAttribute(e, "name");
			Element config = e.createCopy();
			final String moduleKey = "servlet-" + key;
			config.addAttribute("key", moduleKey);
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
							iFrameRenderer,
							new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager);
				}
			}, getService(bundleContext, ServletModuleManager.class));
			descriptor.init(plugin, config);
			return new DescriptorToRegister(descriptor);
		}
	}
}
