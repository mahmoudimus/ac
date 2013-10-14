package com.atlassian.connect.capabilities.client;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

/**
 * @since version
 */
public class RemoteApplicationWithCapabilitiesImpl implements RemoteApplicationWithCapabilities
{
    protected final String key;

    protected final Map<String, String> links;

    protected final DateTime buildDate;

    protected final Map<String, String> capabilities;

    public RemoteApplicationWithCapabilitiesImpl(@Nullable String key, @Nullable DateTime buildDate, Map<String, String> links, Map<String, String> capabilities)
    {
        this.key = key;
        this.buildDate = toNonnull(buildDate);
        this.links = links;
        this.capabilities = ImmutableMap.copyOf(capabilities);
    }

    private DateTime toNonnull(DateTime buildDate)
    {
        return buildDate != null ? buildDate : UniversalDateFormatter.NULL_DATE;
    }

    /**
     * 
     * @return
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Build date of the application, or a 'null' date (01/01/1970 UTC) if unknown.
     *
     * @return build date of the application
     */
    public DateTime getBuildDate()
    {
        return toNonnull(buildDate);
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    public boolean hasCapabilities()
    {
        return !capabilities.isEmpty();
    }

    @Nonnull
    public Map<String, String> getCapabilities()
    {
        return capabilities;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(key, buildDate);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof RemoteApplicationWithCapabilitiesImpl))
        {
            return false;
        }
        else
        {
            final RemoteApplicationWithCapabilitiesImpl that = (RemoteApplicationWithCapabilitiesImpl) obj;
            return Objects.equal(key, that.key) &&
                    Objects.equal(links, that.links) &&
                    Objects.equal(getBuildDate().secondOfDay().roundFloorCopy(), that.getBuildDate().secondOfDay().roundFloorCopy());
        }
    }

    @Override
    public String toString()
    {
        return "ApplicationWithCapabilities{" +
                "type='" + key + '\'' +
                ", links='" + links + '\'' +
                ", buildDate='" + getBuildDate().secondOfDay().roundFloorCopy() + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }

    @Override
    public boolean hasCapability(String key)
    {
        return capabilities.containsKey(key);
    }

    @Override
    @Nullable
    public String getCapabilityUrl(String key)
    {
        return capabilities.get(key);
    }
}
