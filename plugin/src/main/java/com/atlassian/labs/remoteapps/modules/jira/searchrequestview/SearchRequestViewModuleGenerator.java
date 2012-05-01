package com.atlassian.labs.remoteapps.modules.jira.searchrequestview;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static java.util.Collections.emptyMap;

/**
 * A module that maps the search-request-view plugin module to remote apps
 */
public class SearchRequestViewModuleGenerator implements RemoteModuleGenerator
{
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final SearchRequestURLHandler searchRequestURLHandler;
    private final Plugin plugin;
    private final TemplateRenderer templateRenderer;

    public SearchRequestViewModuleGenerator(
            final ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            PluginRetrievalService pluginRetrievalService,
            ApplicationProperties applicationProperties,
            SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            SearchRequestURLHandler searchRequestURLHandler, TemplateRenderer templateRenderer)
    {
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.searchRequestURLHandler = searchRequestURLHandler;
        this.templateRenderer = templateRenderer;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "search-request-view";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
                "search-request-view.xsd",
                "/xsd/search-request-view.xsd",
                "SearchRequestViewType",
                "unbounded");
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, final Element element)
    {
        final String moduleKey = "search-request-view-" + getRequiredAttribute(element, "key");
        final String url = getRequiredAttribute(element, "url");

        Element desc = element.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addAttribute("class", SearchRequestView.class.getName());
        desc.addAttribute("order", getOptionalAttribute(element, "weight", 1000));
        desc.addAttribute("contentType", "text/html");
        desc.addAttribute("fileExtension", "html");

        SearchRequestViewModuleDescriptor moduleDescriptor = createDescriptor(ctx,
                desc, url);

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(
                moduleDescriptor);
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    private SearchRequestViewModuleDescriptor createDescriptor(
            final RemoteAppCreationContext ctx,
            final Element element,
            final String url)
    {
        final String title = getRequiredAttribute(element, "name");
        try
        {
            JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
            SearchRequestViewModuleDescriptor descriptor = new SearchRequestViewModuleDescriptorImpl(
                    jiraAuthenticationContext, searchRequestURLHandler, new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws
                        PluginParseException
                {
                    ApplicationLinkOperationsFactory.LinkOperations linkOps =
                            applicationLinkOperationsFactory.create(
                            ctx.getApplicationType());

                    return (T) new RemoteSearchRequestView(applicationProperties,
                            searchRequestViewBodyWriterUtil,
                            templateRenderer,
                            ctx.getApplicationType().getId().get(),
                            URI.create(url),
                                    title);
                }
            });

            descriptor.init(ctx.getPlugin(), element);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }

    @Override
    public void validate(Element element, String registrationUrl, String username) throws
            PluginParseException
    {
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public String getName()
    {
        return "Search Request View";
    }

    @Override
    public String getDescription()
    {
        return "A search request view that redirects to the Remote App's url with found issue" +
                " keys as the 'issues' query parameter";
    }

}
