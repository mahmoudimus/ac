package com.atlassian.plugin.remotable.spi;

/**
 *
 */
public final class Permissions
{
    // code execution
    public static final String EXECUTE_JAVA = "execute_java";
    public static final String EXECUTE_NATIVE = "execute_native";
    public static final String USE_PRIVATE_API = "use_private_api";
    public static final String USE_PRIVATE_MODULES = "use_private_modules";
    public static final String INSERT_WEB_RESOURCES = "insert_web_resources";

    // services
    public static final String SEND_EMAIL = "send_email";
    public static final String MAKE_HTTP_REQUESTS = "make_http_requests";
    public static final String INTERCEPT_REQUESTS = "intercept_requests";
    public static final String STORE_DATA = "store_data";
    public static final String USE_DB_CONNECTION = "use_db_connection";
    public static final String ACCESS_FILESYSTEM = "access_filesystem";

    // sandbox escaping
    public static final String GENERATE_ANY_HTML = "generate_any_html";
    public static final String USE_REFLECTION = "use_reflection";
    public static final String CREATE_CLASSLOADERS = "create_classloaders";
    public static final String CREATE_OAUTH_LINK = "create_oauth_link";

    private Permissions() {}

}
