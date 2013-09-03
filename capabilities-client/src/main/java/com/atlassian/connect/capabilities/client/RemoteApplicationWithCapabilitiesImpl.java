package com.atlassian.connect.capabilities.client;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @since version
 */
public class RemoteApplicationWithCapabilitiesImpl implements RemoteApplicationWithCapabilities
{
    @JsonProperty protected final String type;
    @JsonProperty protected final String selfUrl;
    @JsonProperty protected final DateTime buildDate;
    @JsonProperty protected final Map<String, String> capabilities;

    public RemoteApplicationWithCapabilitiesImpl(@Nullable String type, @Nullable DateTime buildDate, final String selfUrl, Map<String, String> capabilities)
    {
        this.type = type;
        this.buildDate = toNonnull(buildDate);
        this.selfUrl = selfUrl;
        this.capabilities = ImmutableMap.copyOf(capabilities);
    }

    private DateTime toNonnull(DateTime buildDate)
    {
        return buildDate != null ? buildDate : UniversalDateFormatter.NULL_DATE;
    }

    public String getType()
    {
        return type;
    }

    /**
     * Build date of the application, or a 'null' date (01/01/1970 UTC) if unknown.
     *
     * @return build date of the application
     */
    public DateTime getBuildDate()
    {
        return buildDate;
    }

    /**
     * The origin url of the remote application's capabilities. If you are interested in requesting the capabilities
     * again, use this url.
     *
     * @return origin url of the remote application's capabilities
     */
    public String getSelfUrl()
    {
        return selfUrl;
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
        return Objects.hashCode(type, buildDate);
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
            return Objects.equal(type, that.type) &&
                    Objects.equal(selfUrl, that.selfUrl) &&
                    Objects.equal(buildDate.secondOfDay().roundFloorCopy(), that.buildDate.secondOfDay().roundFloorCopy());
        }
    }

    @Override
    public String toString()
    {
        return "ApplicationWithCapabilities{" +
                "type='" + type + '\'' +
                ", selfUrl='" + selfUrl + '\'' +
                ", buildDate='" + buildDate.secondOfDay().roundFloorCopy() + '\'' +
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
