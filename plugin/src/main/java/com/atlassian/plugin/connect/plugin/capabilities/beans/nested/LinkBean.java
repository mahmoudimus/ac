package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.LinkBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleBeanUtils.copyFieldsByNameAndType;

/**
 * Represents a link, its optional title and alternative text
 *
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#LINK_EXAMPLE}
 * @schemaTitle Link
 * @since 1.0
 */
public class LinkBean
{
    /**
     *  The URL of the link. It can be absolute, or relative to the Add-On base URL.
     */
    @Required
    @StringSchemaAttributes(format = "uri")
    private String url;

    /**
     * The title of the link.
     */
    private String title;

    /**
     * Alternative text that is shown when the element cannot be rendered.
     */
    private String altText;

    public LinkBean(LinkBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);

        if (null == url)
        {
            url = "";
        }
        if (null == title)
        {
            title = "";
        }
        if (null == altText)
        {
            altText = "";
        }
    }

    public String getUrl()
    {
        return url;
    }

    public String getTitle()
    {
        return title;
    }

    public String getAltText()
    {
        return altText;
    }

    public static LinkBeanBuilder newLinkBean()
    {
        return new LinkBeanBuilder();
    }

    public static LinkBeanBuilder newLinkBean(LinkBean defaultBean)
    {
        return new LinkBeanBuilder(defaultBean);
    }
}
