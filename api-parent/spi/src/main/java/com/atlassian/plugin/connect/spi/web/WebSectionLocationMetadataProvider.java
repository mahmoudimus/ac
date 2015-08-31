package com.atlassian.plugin.connect.spi.web;

import java.util.List;

/**
 * Component which provieds movable sections specific for the product.
 * Movable section is a section which content is moved in the DOM like AUI dropdown.
 * Every time iframe is moved in the DOM it do the request. Because of that web panels in those web section
 * have to point to the redirect servlet which generate from them valid JWT token.
 */
public interface WebSectionLocationMetadataProvider
{
    List<String> getMovableWebSectionLocations();
}
