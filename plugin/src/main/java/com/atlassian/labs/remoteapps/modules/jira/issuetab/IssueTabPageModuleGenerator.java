package com.atlassian.labs.remoteapps.modules.jira.issuetab;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class IssueTabPageModuleGenerator implements RemoteModuleGenerator
{
    private final IFrameRenderer iFrameRenderer;
    private final RemoteAppAccessorFactory remoteAppAccessorFactory;
    private final Plugin plugin;

    public IssueTabPageModuleGenerator(final IFrameRenderer iFrameRenderer,
            final RemoteAppAccessorFactory remoteAppAccessorFactory,
            PluginRetrievalService pluginRetrievalService)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "issue-tab-page";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
            "page.xsd",
            "/xsd/page.xsd",
            "PageType",
            "unbounded");
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, final Element element)
    {
        final String moduleKey = "issue-tab-page-" + getRequiredAttribute(element, "key");
        final String url = getRequiredUriAttribute(element, "url").toString();
        final String panelName = getRequiredAttribute(element, "name");

        Element desc = element.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(panelName);
        desc.addAttribute("class", IssueTabPage.class.getName());

        IssueTabPanelModuleDescriptor moduleDescriptor = createDescriptor(ctx,
                desc, moduleKey, url,
                new IFrameParams(element));

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(moduleDescriptor);
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    private IssueTabPanelModuleDescriptor createDescriptor(
            final RemoteAppCreationContext ctx,
            final Element desc,
            final String moduleKey,
            final String url,
            final IFrameParams iFrameParams)
    {
        try
        {
            JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
            IssueTabPanelModuleDescriptor descriptor = new FixedIssueTabPanelModuleDescriptor(
                    jiraAuthenticationContext, new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {

                    return (T) new IssueTabPage(
                            new IFrameContext(ctx.getRemoteAppAccessor() , url, moduleKey, iFrameParams),
                            iFrameRenderer);
                }
            });

            descriptor.init(ctx.getPlugin(), desc);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
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
        return "Issue Tab Page";
    }

    @Override
    public String getDescription()
    {
        return "A remote page decorated as its own JIRA issue tab but not included in All tab" +
                " as it has no individual actions";
    }
}
