package at.marketplace;

import org.apache.commons.lang3.Validate;

public class ConnectAddonRepresentation
{
    private final String descriptorUrl;
    private final String vendorId;
    private final String key;
    private final String logoUrl;
    private final String name;

    public ConnectAddonRepresentation(Builder builder)
    {
        this.descriptorUrl = Validate.notNull(builder.descriptorUrl);
        this.vendorId = Validate.notNull(builder.vendorId);
        this.key = Validate.notNull(builder.key);
        this.name = Validate.notNull(builder.name);
        this.logoUrl = Validate.notNull(builder.logo);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public String getDescriptorUrl()
    {
        return descriptorUrl;
    }

    public String getVendorId()
    {
        return vendorId;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    static class Builder
    {
        private String descriptorUrl;
        private String vendorId;
        private String key;
        private String logo;
        private String name;

        Builder withDescriptorUrl(String url)
        {
            this.descriptorUrl = url;
            return this;
        }

        Builder withVendorId(long vendorId)
        {
            this.vendorId = String.valueOf(vendorId);
            return this;
        }

        Builder withKey(String key)
        {
            this.key = key;
            return this;
        }

        Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withLogo(String logoUrl)
        {
            this.logo = logoUrl;
            return this;
        }

        ConnectAddonRepresentation build()
        {
            return new ConnectAddonRepresentation(this);
        }
    }
}
