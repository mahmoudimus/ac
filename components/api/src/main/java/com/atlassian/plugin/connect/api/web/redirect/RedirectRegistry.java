package com.atlassian.plugin.connect.api.web.redirect;

/**
 * The registry which store redirection data for modules.
 * These data are required by RedirectServlet to generate signed url
 * to the connect add-on returned as redirection.
 */
public interface RedirectRegistry
{
    void register(String addonKey, String moduleKey, RedirectData redirectData);

    RedirectData get(String addonKey, String moduleKey);
}