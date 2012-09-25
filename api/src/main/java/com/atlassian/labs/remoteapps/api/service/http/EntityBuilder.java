package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.util.Map;

/**
 * A basic contract to be implemented by all entity builders.
 */
public interface EntityBuilder
{
    /**
     * Builds an {@link Entity}.
     *
     * @return The built entity
     */
    Entity build();

    /**
     * Represents a built entity consisting of a set of HTTP headers and an {@link InputStream}.
     */
    static interface Entity
    {
        /**
         * Gets all HTTP headers for the represented entity.  At a minimum, this should include
         * an appropriate "Content-Type" header.
         *
         * @return A map of all HTTP headers for the entity
         */
        public Map<String, String> getHeaders();

        /**
         * Gets the input stream for the built entity.
         *
         * @return An entity input stream
         */
        public InputStream getInputStream();
    }
}
