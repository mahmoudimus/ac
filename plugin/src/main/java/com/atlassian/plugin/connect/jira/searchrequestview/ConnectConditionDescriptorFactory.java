package com.atlassian.plugin.connect.jira.searchrequestview;

import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.condition.ConnectConditionElementParserFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import org.dom4j.Element;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.atlassian.plugin.web.baseconditions.AbstractConditionElementParser.CompositeType.AND;

/**
 * Implementation of {@link ConditionDescriptorFactory} that wires conditions with beans from the plugin, not just the
 * host. (unlike our friend com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactoryImpl which JIRA
 * uses by default for wiring SearchRequestViewModuleDescriptor conditions).
 *
 * @since 1.0
 */
@JiraComponent
public class ConnectConditionDescriptorFactory implements ConditionDescriptorFactory
{
    private final ConditionElementParser conditionElementParser;

    @Inject
    public ConnectConditionDescriptorFactory(final ConnectConditionElementParserFactory conditionElementParserFactory)
    {
        this.conditionElementParser = conditionElementParserFactory.getConditionElementParser();
    }

    @Nonnull
    @Override
    public Condition retrieveCondition(@Nonnull final Plugin plugin, @Nonnull final Element element)
    {
        return conditionElementParser.makeConditions(plugin, element, AND);
    }
}
