package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.web.Condition;

public class PageInfo
{
    private final String decorator;
    private final String templateSuffix;
    private final String title;
    private final Condition condition;

    public PageInfo(String decorator, String templateSuffix, String title,
            Condition condition)
    {
        this.decorator = decorator;
        this.templateSuffix = templateSuffix;
        this.title = title;
        this.condition = condition;
    }

    public String getDecorator()
    {
        return decorator;
    }

    public String getTemplateSuffix()
    {
        return templateSuffix;
    }

    public String getTitle()
    {
        return title;
    }

    public Condition getCondition()
    {
        return condition;
    }
}
