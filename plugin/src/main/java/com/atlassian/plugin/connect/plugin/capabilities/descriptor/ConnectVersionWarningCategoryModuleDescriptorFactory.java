package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectVersionWarningCategoryModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

@JiraComponent
public class ConnectVersionWarningCategoryModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<ConnectVersionWarningCategoryModuleBean, ConnectVersionWarningCategoryModuleDescriptor>
{
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public ConnectVersionWarningCategoryModuleDescriptorFactory(
            @Nonnull final ConditionModuleFragmentFactory conditionModuleFragmentFactory,
            @Nonnull final ConnectContainerUtil connectContainerUtil)
    {
        this.conditionModuleFragmentFactory = checkNotNull(conditionModuleFragmentFactory);
        this.connectContainerUtil = checkNotNull(connectContainerUtil);
    }

    @Override
    public ConnectVersionWarningCategoryModuleDescriptor createModuleDescriptor(
            ConnectModuleProviderContext moduleProviderContext, 
            Plugin theConnectPlugin, 
            ConnectVersionWarningCategoryModuleBean bean)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        // todo: what should happen here?
        ConnectVersionWarningCategoryModuleDescriptor descriptor = connectContainerUtil.createBean(ConnectVersionWarningCategoryModuleDescriptor.class);
        Element element = createElement(bean, connectAddonBean);
        
        descriptor.init(theConnectPlugin, element);
        return descriptor;
    }


    private Element createElement(ConnectVersionWarningCategoryModuleBean bean, ConnectAddonBean addon)
    {
        DOMElement element = new DOMElement("version-warning-category");

        element.setAttribute("key", bean.getKey(addon));
        element.setAttribute("name", bean.getDisplayName());
        element.setAttribute("state", "enabled");

        element.addElement("order")
                .addText(Integer.toString(bean.getWeight()));

        if (!bean.getConditions().isEmpty())
        {
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions()));
        }
        else
        {
            // JIRA throws an NPE if no conditions are present...
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), Collections.<ConditionalBean>emptyList(),
                    Collections.<Class<? extends Condition>>singletonList(AlwaysDisplayCondition.class)));
        }

        return element;
    }
}
