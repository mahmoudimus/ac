package com.atlassian.plugin.connect.spi;

public final class Permissions
{
    // sandbox escaping
    public static final String GENERATE_ANY_HTML = "generate_any_html";
    public static final String CREATE_OAUTH_LINK = "create_oauth_link";

    // x-product
    public static final String READ_APP_LINKS = "read_app_links";
    public static final String MODIFY_APP_LINKS = "modify_app_link";
    // at the moment remotable-plugin, should be UPM
    public static final String READ_LICENSE = "read_license";

    // other
    public static final String DEFINE_PLUGIN_PERMISSION = "define_plugin_permission";

    private Permissions()
    {
    }
}
