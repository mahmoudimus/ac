package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.VendorBeanBuilder;
import com.google.common.base.Objects;

/**
 * Gives basic information about the plugin vendor
 * <br><br>
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#VENDOR_EXAMPLE}
 * @schemaTitle Plugin Vendor
 * @since 1.0
 */
@SchemaDefinition("vendor")
public class VendorBean extends BaseModuleBean
{
    /**
     * The name of the plugin vendor.
     * Supply your name or the name of the company you work for.
     */
    private String name;

    /**
     * The url for the vendor's website
     */
    @StringSchemaAttributes(format = "uri")
    private String url;

    public VendorBean()
    {
        this.name = "";
        this.url = "";
    }

    public VendorBean(VendorBeanBuilder builder)
    {
        super(builder);

        if (null == name)
        {
            this.name = "";
        }

        if (null == url)
        {
            this.url = "";
        }
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public static VendorBeanBuilder newVendorBean()
    {
        return new VendorBeanBuilder();
    }

    public static VendorBeanBuilder newVendorBean(VendorBean defaultBean)
    {
        return new VendorBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name, url);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof VendorBean))
        {
            return false;
        }
        else
        {
            final VendorBean that = (VendorBean) obj;
            return Objects.equal(name, that.name) &&
                    Objects.equal(url, that.url);
        }
    }
}
