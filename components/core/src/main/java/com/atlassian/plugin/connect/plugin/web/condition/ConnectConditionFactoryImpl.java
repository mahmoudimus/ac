package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.web.condition.ConditionElementParserFactory;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import org.dom4j.dom.DOMElement;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.atlassian.plugin.web.baseconditions.AbstractConditionElementParser.CompositeType;

@Named
public class ConnectConditionFactoryImpl implements ConnectConditionFactory {
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConditionElementParser conditionElementParser;
    private final Plugin theConnectPlugin;

    @Inject
    public ConnectConditionFactoryImpl(ConditionElementParserFactory conditionElementParserFactory,
                                       PluginRetrievalService pluginRetrievalService,
                                       ConditionModuleFragmentFactory conditionModuleFragmentFactory) {
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.conditionElementParser = conditionElementParserFactory.getConditionElementParser();
        this.theConnectPlugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public Condition createCondition(final String addonKey, final List<ConditionalBean> conditionalBeans,
                                     final Iterable<Class<? extends Condition>> additionalConditions) {
        DOMElement conditionFragment = conditionModuleFragmentFactory.createFragment(addonKey, conditionalBeans, additionalConditions);
        return conditionElementParser.makeConditions(theConnectPlugin, conditionFragment, CompositeType.AND);
    }

}
