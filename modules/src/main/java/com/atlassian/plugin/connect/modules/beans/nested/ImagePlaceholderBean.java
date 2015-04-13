package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ImagePlaceholderBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;


/**
 * Defines a macro image placeholder to display in the Confluence editor
 * <br><br>
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#IMAGE_PLACEHOLDER_EXAMPLE}
 * @schemaTitle Image Placeholder
 * @since 1.0
 */
@SchemaDefinition("imagePlaceholder")
public class ImagePlaceholderBean
{
    /**
     * The width in pixels of the image placeholder. Defaults to the natural image width if not specified.
     */
    private Integer width;
    /**
     * The height in pixels of the image placeholder. Defaults to the natural image height if not specified.
     */
    private Integer height;

    /**
     * The URL of the image placeholder. Your image placeholder needs to be hosted remotely at a web-accessible location.
     * You can specify the URL as an absolute URL or relative to the add-on's base URL.
     */
    @Required
    @StringSchemaAttributes(format = "uri")
    private String url;

    /**
     * Set to true if the image should have the macro placeholder chrome applied to it.
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean applyChrome;

    public ImagePlaceholderBean()
    {
        this.url = "";
        this.applyChrome = false;
    }

    public ImagePlaceholderBean(ImagePlaceholderBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);

        if (null == url)
        {
            this.url = "";
        }
        if (null == applyChrome)
        {
            this.applyChrome = false;
        }
    }

    public Integer getWidth()
    {
        return width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public String getUrl()
    {
        return url;
    }

    public boolean applyChrome()
    {
        return null == applyChrome ? false : applyChrome;
    }

    public static ImagePlaceholderBeanBuilder newImagePlaceholderBean()
    {
        return new ImagePlaceholderBeanBuilder();
    }

    public static ImagePlaceholderBeanBuilder newImagePlaceholderBean(ImagePlaceholderBean defaultBean)
    {
        return new ImagePlaceholderBeanBuilder(defaultBean);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ImagePlaceholderBean))
        {
            return false;
        }

        ImagePlaceholderBean other = (ImagePlaceholderBean) otherObj;

        return new EqualsBuilder()
                .append(width, other.width)
                .append(height, other.height)
                .append(url, other.url)
                .append(applyChrome, other.applyChrome)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(width)
                .append(height)
                .append(url)
                .append(applyChrome)
                .build();
    }
}

