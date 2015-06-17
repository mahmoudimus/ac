package com.atlassian.plugin.connect.spi.module.page;

import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class PageInfo
{
    private final String decorator;
    private final String templateSuffix;
    private final String title;
    private final Condition condition;
	private final Map<String, String> metaTagsContent;

	public PageInfo(String decorator, String templateSuffix, String title,
            Condition condition, Map<String, String> metaTagsContent)
    {
        this.decorator = decorator;
        this.templateSuffix = templateSuffix;
        this.title = title;
        this.condition = condition;
		this.metaTagsContent = ImmutableMap.copyOf(metaTagsContent);
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

	public Map<String, String> getMetaTagsContent()
	{
		return metaTagsContent;
	}
}
