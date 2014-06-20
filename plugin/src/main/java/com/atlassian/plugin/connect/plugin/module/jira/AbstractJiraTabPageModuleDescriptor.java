package com.atlassian.plugin.connect.plugin.module.jira;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.util.VelocityKiller;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Common part of module descriptor for all JIRA tab panels
 */
public abstract class AbstractJiraTabPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    @XmlDescriptor
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConditionProcessor conditionProcessor;
    private final UrlValidator urlValidator;

    private Element descriptor;
    @XmlDescriptor
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    protected String url;

    public AbstractJiraTabPageModuleDescriptor(
            final ModuleFactory moduleFactory,
            final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            final ConditionProcessor conditionProcessor,
            final UrlValidator urlValidator)
    {
        super(moduleFactory);
        this.urlValidator = checkNotNull(urlValidator);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.conditionProcessor = checkNotNull(conditionProcessor);
    }

    /**
     * Subclass should return prefix used to specify module key, for instance issue-tab-, project-tab- etc.
     * @return module key prefix
     */
    protected abstract String getModulePrefix();

    /**
     * Subclass should create and return the module descriptor which creates a module for this tab, for instance IssueTabPanelModuleDescriptor.
     * @param key plugin key
     * @param iFrameParams plugin params
     * @param condition plugin condition
     * @return plugin module descriptor
     */
    protected abstract JiraResourcedModuleDescriptor createTabPanelModuleDescriptor(final String key, final IFrameParams iFrameParams, final Condition condition);

    /**
     * Subclass should return the class which displays the tab as an iframe.
     * @return implementation class of the component
     */
    protected abstract Class<?> getIFrameTabClass();

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
        this.url = getRequiredAttribute(element, "url");
        urlValidator.validate(this.url);
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void enabled()
    {
        super.enabled();

        final String tabName = getRequiredAttribute(descriptor, "name");

        final Element desc = descriptor.createCopy();
        String moduleKey = getModulePrefix() + getRequiredAttribute(descriptor, "key");
        final String weight = getOptionalAttribute(descriptor, "weight", null);

        final Condition condition = conditionProcessor.process(descriptor, desc, getPluginKey(), "#" + moduleKey + "-remote-condition-panel");
        if (condition instanceof ContainingRemoteCondition)
        {
            moduleKey += "-remote-condition";
        }
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(VelocityKiller.attack(tabName));
        desc.addAttribute("class", getIFrameTabClass().getName());
        if (weight != null)
        {
            desc.addElement("order").setText(weight);
        }

        final JiraResourcedModuleDescriptor moduleDescriptor = createDescriptor(moduleKey,
                desc, new IFrameParamsImpl(descriptor), condition);

        XmlDescriptorExploder.notifyAndExplode(getPluginKey());
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(moduleDescriptor));
    }

    @Override
    public void disabled()
    {
        XmlDescriptorExploder.notifyAndExplode(getPluginKey());

        super.disabled();
        if (registration != null)
        {
            registration.unregister();
        }
    }

    private JiraResourcedModuleDescriptor createDescriptor(
            final String key,
            final Element desc,
            final IFrameParams iFrameParams, final Condition condition)
    {
        try
        {
            desc.addAttribute("system", "true");
            final JiraResourcedModuleDescriptor descriptor = createTabPanelModuleDescriptor(key, iFrameParams, condition);

            descriptor.init(conditionProcessor.getLoadablePlugin(getPlugin()), desc);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
