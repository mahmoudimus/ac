package at.marketplace;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.Validate;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectAddonRepresentation
{
    private final String descriptorUrl;
    private final String vendorId;
    private final String logoUrl;
    private final String name;
    private final Iterable<Highlight> highlights;
    private final String summary;

    public String getTagline()
    {
        return tagline;
    }

    public String getSummary()
    {
        return summary;
    }

    public Iterable<Highlight> getHighlights()
    {
        return highlights;
    }

    private final String tagline;

    public ConnectAddonRepresentation(Builder builder)
    {
        this.name = Validate.notNull(builder.name);
        this.descriptorUrl = Validate.notNull(builder.descriptorUrl);
        this.vendorId = builder.vendorId;
        this.logoUrl = builder.logo;
        this.highlights = builder.highlights;
        this.summary = builder.summary;
        this.tagline = builder.tagline;
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

    public String getName()
    {
        return name;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public static class Builder
    {
        private String descriptorUrl;
        private String vendorId;
        private String logo;
        private String name;
        private Iterable<Highlight> highlights;
        private String summary;
        private String tagline;

        public Builder withDescriptorUrl(String url)
        {
            this.descriptorUrl = url;
            return this;
        }

        public Builder withVendorId(String vendorId)
        {
            this.vendorId = vendorId;
            return this;
        }

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withLogoUrl(String logoUrl)
        {
            this.logo = logoUrl;
            return this;
        }

        public Builder withHighlights(Highlight first, Highlight second, Highlight third)
        {
            checkNotNull(first);
            checkNotNull(second);
            checkNotNull(third);

            this.highlights = ImmutableList.of(first, second, third);
            return this;
        }

        public Builder withSummary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public Builder withTagline(String tagline)
        {
            this.tagline = tagline;
            return this;
        }

        public ConnectAddonRepresentation build()
        {
            return new ConnectAddonRepresentation(this);
        }
    }

    static class Highlight
    {
        final private String title;
        final private String body;

        public String getTitle()
        {
            return title;
        }

        public String getBody()
        {
            return body;
        }

        public Highlight(String title, String body)
        {
            this.title = title;
            this.body = body;
        }
    }
}
