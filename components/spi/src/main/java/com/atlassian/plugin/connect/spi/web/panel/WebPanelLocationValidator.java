package com.atlassian.plugin.connect.spi.web.panel;

/**
 * Implementations of this validator are responsible for checking, if the location to which a web-panel
 * wants to plug itself is supported.
 */
public interface WebPanelLocationValidator
{
    /**
     * This method is supposed to return false, if the product doesn't support this location
     * for Connect add-ons.
     *
     * @param location the location in which Connect add-ons wants to add a web-panel
     * @return true, if this is ok for the add-ons to add a web-panel in the {@param location}.
     */
    boolean validateLocation(String location);

}
