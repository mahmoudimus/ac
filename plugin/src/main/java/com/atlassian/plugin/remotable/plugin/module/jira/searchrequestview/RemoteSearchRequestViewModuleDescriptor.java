package com.atlassian.plugin.remotable.plugin.module.jira.searchrequestview;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.jira.ComponentManager.getComponent;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a search request where the browser is redirected to a remote url
 */
public final class RemoteSearchRequestViewModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final TemplateRenderer templateRenderer;
    private final ConditionDescriptorFactory conditionDescriptorFactory;
    private Element descriptor;
    private URI url;
    private DynamicDescriptorRegistration.Registration registration;

    public RemoteSearchRequestViewModuleDescriptor(
            ModuleFactory moduleFactory,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            ApplicationProperties applicationProperties,
            SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            TemplateRenderer templateRenderer, ConditionDescriptorFactory conditionDescriptorFactory)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.searchRequestViewBodyWriterUtil = checkNotNull(searchRequestViewBodyWriterUtil);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.conditionDescriptorFactory = checkNotNull(conditionDescriptorFactory);
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
        this.url = getRequiredUriAttribute(element, "url");
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final String moduleKey = "search-request-view-" + getKey();

        Element desc = descriptor.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addAttribute("class", SearchRequestView.class.getName());
        desc.addAttribute("order", getOptionalAttribute(descriptor, "weight", 1000));
        desc.addAttribute("contentType", "text/html");
        desc.addAttribute("fileExtension", "html");

        SearchRequestViewModuleDescriptor moduleDescriptor = createDescriptor(desc);
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(moduleDescriptor));
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

    private SearchRequestViewModuleDescriptor createDescriptor(Element element)
    {
        final String title = getName();
        try
        {
            ModuleFactory moduleFactory = new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws
                    PluginParseException
                {
                    return (T) new RemoteSearchRequestView(applicationProperties,
                        searchRequestViewBodyWriterUtil,
                        templateRenderer,
                        getPluginKey(),
                        url,
                        title);
                }
            };

            SearchRequestViewModuleDescriptor descriptor = new SearchRequestViewModuleDescriptorImpl(
                    getComponent(JiraAuthenticationContext.class),
                    getComponent(SearchRequestURLHandler.class),
                    moduleFactory,
                    conditionDescriptorFactory);

            descriptor.init(getPlugin(), element);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }
}
