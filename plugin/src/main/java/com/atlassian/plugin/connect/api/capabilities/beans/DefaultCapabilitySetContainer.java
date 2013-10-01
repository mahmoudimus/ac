package com.atlassian.plugin.connect.api.capabilities.beans;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.connect.capabilities.client.UniversalDateFormatter;

import com.google.common.base.Objects;

import org.joda.time.DateTime;

/**
 * This class represents the document returned from a child capabilities url.
 * It contains all of the common and/or required fields as well as a list of
 * the {@link CapabilityBean}s for this set.
 */
public class DefaultCapabilitySetContainer<T extends CapabilityBean> implements CapabilitySetContainer<T>
{
    protected final Map<String, String> links;

    protected final DateTime buildDate;
    
    protected List<T> modules;

    public DefaultCapabilitySetContainer(@Nullable DateTime buildDate, Map<String, String> links, List<T> modules)
    {
        this.buildDate = toNonnull(buildDate);
        this.links = links;
        this.modules = modules;
    }

    private DateTime toNonnull(DateTime buildDate)
    {
        return buildDate != null ? buildDate : UniversalDateFormatter.NULL_DATE;
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

    public List<T> getModules()
    {
        return modules;
    }

    public void setModules(List<T> modules)
    {
        this.modules = modules;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(buildDate);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof DefaultCapabilitySetContainer))
        {
            return false;
        }
        else
        {
            final DefaultCapabilitySetContainer that = (DefaultCapabilitySetContainer) obj;
            return Objects.equal(links, that.links) &&
                    Objects.equal(getBuildDate().secondOfDay().roundFloorCopy(), that.getBuildDate().secondOfDay().roundFloorCopy());
        }
    }

    @Override
    public String toString()
    {
        return "CapabilityBean{" +
                ", links='" + links + '\'' +
                ", buildDate='" + getBuildDate().secondOfDay().roundFloorCopy() + '\'' +
                '}';
    }
}
