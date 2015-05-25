package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;

public class ConditionsProviderHelper
{
    public static final String LS = System.getProperty("line.separator");

    protected static String getConditionListAsMarkdown(ConditionClassResolver conditionMap)
    {
        StringBuilder sb = new StringBuilder();

        for (String conditionName : conditionMap.getAllConditionNames())
        {
            sb.append(LS).append("* ").append(escapeUnderscores(conditionName));
        }

        sb.append(LS).append(LS);
        return sb.toString();
    }

    private static String escapeUnderscores(String input)
    {
        return "`" + input + "`";
    }
}
