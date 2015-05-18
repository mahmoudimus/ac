package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;

public abstract class ConditionsProvider
{
    public static final String LS = System.getProperty("line.separator");
    private final ConditionClassResolver conditions;

    public ConditionsProvider(final ConditionClassResolver conditions)
    {
        this.conditions = conditions;
    }

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

    public ConditionClassResolver getConditions()
    {
        return conditions;
    }

    private static String escapeUnderscores(String input)
    {
        return "`" + input + "`";
    }
}
