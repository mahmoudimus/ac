package com.atlassian.plugin.connect.plugin.module.jira;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.IFrameComponentTab;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Common part of module descriptor for all JIRA tab panels
 *
 * @since v6.1
 */
public abstract class AbstractJiraTabPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConditionProcessor conditionProcessor;

    private Element descriptor;
    private DynamicDescriptorRegistration.Registration registration;

    protected  String url;


    public AbstractJiraTabPageModuleDescriptor(
            final ModuleFactory moduleFactory,
            final DynamicDescriptorRegistration dynamicDescriptorRegistration,
            final ConditionProcessor conditionProcessor)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.conditionProcessor = checkNotNull(conditionProcessor);
    }

    /**
     * Prefix used to specify module key
     * @return
     */
    protected abstract String getModulePrefix();

    /**
     * Creating plugin module descriptor
     * @param key
     * @param iFrameParams
     * @param condition
     * @return
     */
    protected abstract JiraResourcedModuleDescriptor createTabPanelModuleDescriptor(final String key, final IFrameParams iFrameParams, final Condition condition);

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
        this.url = getRequiredAttribute(element, "url");
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

        final Condition condition = conditionProcessor.process(descriptor, desc, getPluginKey(), "#" + moduleKey + "-remote-condition-panel");
        if (condition instanceof ContainingRemoteCondition)
        {
            moduleKey += "-remote-condition";
        }
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(tabName);
        desc.addAttribute("class", IFrameComponentTab.class.getName());

        final JiraResourcedModuleDescriptor moduleDescriptor = createDescriptor(moduleKey,
                desc, new IFrameParamsImpl(descriptor), condition);
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
}
