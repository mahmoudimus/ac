package com.atlassian.labs.remoteapps.plugin.module.jira.searchrequestview;

import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.remoteapps.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.labs.remoteapps.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * Generates a search request where the browser is redirected to a remote url
 */
public class RemoteSearchRequestViewModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final SearchRequestURLHandler searchRequestURLHandler;
    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private Element descriptor;
    private URI url;

    public RemoteSearchRequestViewModuleDescriptor(
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            ApplicationProperties applicationProperties,
            SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            SearchRequestURLHandler searchRequestURLHandler, TemplateRenderer templateRenderer,
            JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.searchRequestURLHandler = searchRequestURLHandler;
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
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
        dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(moduleDescriptor));
    }

    private SearchRequestViewModuleDescriptor createDescriptor(Element element)
    {
        final String title = getName();
        try
        {
            SearchRequestViewModuleDescriptor descriptor = new SearchRequestViewModuleDescriptorImpl(
                    jiraAuthenticationContext, searchRequestURLHandler, new ModuleFactory()
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
            });

            descriptor.init(getPlugin(), element);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }
}
