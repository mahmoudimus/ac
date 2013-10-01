package com.atlassian.plugin.connect.api.capabilities.beans;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * represents the document returned from a child capabilities url.
 * It contains all of the common and/or required fields as well as a list of
 * the {@link CapabilityBean}s for this set.
 */
public interface CapabilitySetContainer<T extends CapabilityBean>
{
    @Nonnull
    DateTime getBuildDate();

    Map<String,String> getLinks();
    
    List<T> getModules();

}
