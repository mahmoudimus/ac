package com.atlassian.plugin.connect.plugin.capabilities.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.condition.ConditionElementParserFactory;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.Condition;
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
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConditionElementParser conditionElementParser;
    private final Plugin theConnectPlugin;

    @Inject
    public ConnectConditionFactoryImpl(ConditionElementParserFactory conditionElementParserFactory,
            PluginRetrievalService pluginRetrievalService,
            ConditionModuleFragmentFactory conditionModuleFragmentFactory)
    {
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.conditionElementParser = conditionElementParserFactory.getConditionElementParser();
        this.theConnectPlugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public Condition createCondition(final String addOnKey, final List<ConditionalBean> conditionalBeans)
    {
        return createCondition(addOnKey, conditionalBeans, Collections.<Class<? extends Condition>>emptyList());
    }

    @Override
    public Condition createCondition(final String addOnKey, final List<ConditionalBean> conditionalBeans,
            final Class<? extends Condition> additionalCondition)
    {
        List<Class<? extends Condition>> conditionList = Lists.newArrayList();
        conditionList.add(additionalCondition);
        return createCondition(addOnKey, conditionalBeans, conditionList);
    }

    @Override
    public Condition createCondition(final String addOnKey, final List<ConditionalBean> conditionalBeans,
            final Iterable<Class<? extends Condition>> additionalConditions)
    {
        DOMElement conditionFragment = conditionModuleFragmentFactory.createFragment(addOnKey, conditionalBeans, additionalConditions);
        return conditionElementParser.makeConditions(theConnectPlugin, conditionFragment, CompositeType.AND);
    }

}
