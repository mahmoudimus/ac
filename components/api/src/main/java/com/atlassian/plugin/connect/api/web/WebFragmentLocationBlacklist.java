package com.atlassian.plugin.connect.api.web;

import java.util.Set;

/**
 * Contains a list of locations which are not supported in Connect.
 */
public interface WebFragmentLocationBlacklist
{
    /**
     * @return a set of web-panel locations which are blacklisted.
     */
    Set<String> blacklistedWebPanelLocations();

    /**
     * @return a set of web-item locations which are blacklisted.
     */
    Set<String> blacklistedWebItemLocations();
}
