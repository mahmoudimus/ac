package com.atlassian.plugin.connect.modules.beans;

import java.util.Map;

import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;

public abstract class ConditionsProvider
{
    public static final String LS = System.getProperty("line.separator");
    protected Map<String, Class<? extends Condition>> conditions;
    
    protected static String getConditionListAsMarkdown(Map<String, Class<? extends Condition>> conditionMap)
    {
        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String,Class<? extends Condition>> entry : conditionMap.entrySet())
        {
            sb.append(LS).append("* ").append(escapeUnderscores(entry.getKey()));
        }

        sb.append(LS).append(LS);
        return sb.toString();
    }

    public Map<String, Class<? extends Condition>> getConditions()
    {
        return ImmutableMap.copyOf(conditions);
    }

    private static String escapeUnderscores(String input)
    {
        return "`" + input + "`";
    }
}
