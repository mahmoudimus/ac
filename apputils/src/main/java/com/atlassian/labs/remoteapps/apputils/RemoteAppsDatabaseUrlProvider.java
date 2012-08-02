package com.atlassian.labs.remoteapps.apputils;

import com.atlassian.labs.remoteapps.api.DatabaseUrlProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RemoteAppsDatabaseUrlProvider implements DatabaseUrlProvider
{
    private static final String DATABASE_URL_KEY = "DATABASE_URL";
    private static final String DEFAULT_DATABASE_URL = "jdbc:postgresql://localhost/ra?user=ra_user&password=ra_pwd";

    private final Environment env;

    public RemoteAppsDatabaseUrlProvider(Environment env)
    {
        this.env = checkNotNull(env);
    }

    @Override
    public String getUrl()
    {
        return new HerokuUrlTransformer().transform(env.getOptionalEnv(DATABASE_URL_KEY, DEFAULT_DATABASE_URL));
    }
}
