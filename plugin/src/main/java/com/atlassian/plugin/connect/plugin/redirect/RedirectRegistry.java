package com.atlassian.plugin.connect.plugin.redirect;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Registery which store redirecton data for modules.
 * Those data are required by {@link RedirectServlet} to generate signed url to the conenct add-on returned as redirection.
 *
 */
@ParametersAreNonnullByDefault
public interface RedirectRegistry
{
    void register(String addonKey, String moduleKey, RedirectData redirectData);

    RedirectData get(String addonKey, String moduleKey);
}



