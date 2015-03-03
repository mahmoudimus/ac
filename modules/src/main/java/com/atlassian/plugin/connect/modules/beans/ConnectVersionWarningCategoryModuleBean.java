package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectVersionWarningCategoryModuleBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

@SchemaDefinition ("versionWarningCategory")
public class ConnectVersionWarningCategoryModuleBean extends BeanWithKeyAndParamsAndConditions
{
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;

    private Integer weight;

    public ConnectVersionWarningCategoryModuleBean()
    {
        this("", 100);
    }

    public ConnectVersionWarningCategoryModuleBean(String url, Integer weight)
    {
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
    }

    public ConnectVersionWarningCategoryModuleBean(ConnectVersionWarningCategoryModuleBeanBuilder builder)
    {
        super(builder);

        if (null == weight)
        {
            this.weight = 100;
        }
        if (null == url)
        {
            this.url = "";
        }
    }

    /**
     * Specifies the URL targeted by the tab panel. The URL is relative to the add-on's base URL.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Determines the order in which the tab panel's link appears in the menu or list.
     * <p/>
     * The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the menu or list.
     * <p/>
     * Built-in web items have weights that are incremented by numbers that leave room for additional
     * items, such as by 10 or 100. Be mindful of the weight you choose for your item, so that it appears
     * in a sensible order given existing items.
     */
    public int getWeight()
    {
        return weight;
    }

    public static ConnectVersionWarningCategoryModuleBeanBuilder newWarningCategoryBean()
    {
        return new ConnectVersionWarningCategoryModuleBeanBuilder();
    }

    public static ConnectVersionWarningCategoryModuleBeanBuilder newWarningCategoryBean(ConnectVersionWarningCategoryModuleBean defaultBean)
    {
        return new ConnectVersionWarningCategoryModuleBeanBuilder(defaultBean);
    }
}
