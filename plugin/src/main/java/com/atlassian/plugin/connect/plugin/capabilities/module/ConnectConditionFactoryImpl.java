package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import com.google.common.collect.Lists;
import org.dom4j.dom.DOMElement;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import static com.atlassian.plugin.web.baseconditions.AbstractConditionElementParser.CompositeType;

@Named
public class ConnectConditionFactoryImpl implements ConnectConditionFactory
{
    private final PluginRetrievalService pluginRetrievalService;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConditionElementParser conditionElementParser;

    @Inject
    public ConnectConditionFactoryImpl(WebInterfaceManager webInterfaceManager,
            PluginRetrievalService pluginRetrievalService,
            ConditionModuleFragmentFactory conditionModuleFragmentFactory)
    {
        this.pluginRetrievalService = pluginRetrievalService;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.conditionElementParser = constructConditionParser(webInterfaceManager);
    }

    @Override
    public Condition createCondition(final String addOnKey, final List<ConditionalBean> conditionalBeans)
    {
        return createCondition(addOnKey, conditionalBeans, Collections.<Class<? extends Condition>>emptyList());
    }

    @Override
    public Condition createCondition(final String addOnKey, final List<ConditionalBean> conditionalBeans,
            final Class<? extends Condition> additionalConditions)
    {
        List<Class<? extends Condition>> conditionList = Lists.newArrayList();
        conditionList.add(additionalConditions);
        return createCondition(addOnKey, conditionalBeans, conditionList);
    }

    @Override
    public Condition createCondition(final String addOnKey, final List<ConditionalBean> conditionalBeans,
            final Iterable<Class<? extends Condition>> additionalConditions)
    {
        DOMElement conditionFragment = conditionModuleFragmentFactory
                .createFragment(addOnKey, conditionalBeans, additionalConditions);
        return conditionElementParser.makeConditions(pluginRetrievalService.getPlugin(), conditionFragment, CompositeType.AND);
    }

    private static ConditionElementParser constructConditionParser(final WebInterfaceManager webInterfaceManager)
    {
        return new ConditionElementParser(new WebInterfaceManagerConditionFactory(webInterfaceManager));
    }

    private static class WebInterfaceManagerConditionFactory implements ConditionElementParser.ConditionFactory
    {
        private final WebInterfaceManager webInterfaceManager;

        private WebInterfaceManagerConditionFactory(final WebInterfaceManager webInterfaceManager)
        {
            this.webInterfaceManager = webInterfaceManager;
        }

        public Condition create(String className, Plugin plugin) throws ConditionLoadingException
        {
            return webInterfaceManager.getWebFragmentHelper().loadCondition(className, plugin);
        }
    }
}
