package com.atlassian.plugin.connect.core.capabilities.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.condition.ConditionElementParserFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConnectConditionElementParserFactoryImpl implements ConditionElementParserFactory
{
    private final ConditionElementParser conditionElementParser;

    @Inject
    public ConnectConditionElementParserFactoryImpl(final WebInterfaceManager webInterfaceManager)
    {
        conditionElementParser = new ConditionElementParser(new WebInterfaceManagerConditionFactory(webInterfaceManager));
    }

    public ConditionElementParser getConditionElementParser()
    {
        return conditionElementParser;
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
