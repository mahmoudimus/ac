package com.atlassian.plugin.connect.plugin.web;

import javax.servlet.Filter;

/**
 * This factory for filters provided by the plugins/products implementing Connect SPI.
 * @since 1.2.0
 */
public interface ConnectRequestFilterFactory
{
    /**
     * Returns all filters for particular phase of Connect HTTP request filtering. This method makes no guarantee
     * about the order of returned filters.
     *
     * The implementations are required to return non null iterable of filters.
     *
     * @param phase for which the filters are returned.
     */
    public Iterable<Filter> getFiltersForPhase(ConnectRequestFilterPhase phase);
}
