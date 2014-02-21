package com.atlassian.plugin.connect.plugin.capabilities.condition;

import com.atlassian.plugin.web.descriptors.ConditionElementParser;

/**
 * @since 1.0
 */
public interface ConditionElementParserFactory
{
    ConditionElementParser getConditionElementParser();
}
