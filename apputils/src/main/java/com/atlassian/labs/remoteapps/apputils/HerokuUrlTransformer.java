package com.atlassian.labs.remoteapps.apputils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HerokuUrlTransformer
{
    private static final String HEROKU_POSTGRES_PREFIX = "postgres://";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    String transform(String url)
    {
        if (url == null)
        {
            return null;
        }

        if (url.startsWith("jdbc:"))
        {
            logger.debug("URL '{}' is a JDBC url, not transforming.");
            return url;
        }

        if (url.startsWith(HEROKU_POSTGRES_PREFIX))
        {
            final ParsedUrl parsed = parse(url);
            final String transformed = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(parsed.host)
                    .append("/")
                    .append(parsed.database)
                    .append("?user=")
                    .append(parsed.user)
                    .append("&password=")
                    .append(parsed.pwd)
                    .toString();

            logger.debug("Transformed Heroku URL '{}' into JDBC URL '{}'", url, transformed);
            return transformed;
        }

        logger.warn("Couldn't figure out type of URL, might not be valid. Here it is '{}'", url);
        return url;
    }

    private ParsedUrl parse(String url)
    {
        final String urlWithNoPrefix = url.substring(HEROKU_POSTGRES_PREFIX.length());
        final int colonIndex = urlWithNoPrefix.indexOf(":");
        final int atIndex = urlWithNoPrefix.indexOf("@");
        final int slashIndex = urlWithNoPrefix.indexOf("/");

        final String user = urlWithNoPrefix.substring(0, colonIndex);
        final String pwd = urlWithNoPrefix.substring(colonIndex + 1, atIndex);
        final String host = urlWithNoPrefix.substring(atIndex + 1, slashIndex);
        final String db = urlWithNoPrefix.substring(slashIndex + 1, urlWithNoPrefix.length());
        return new ParsedUrl(host, db, user, pwd);
    }

    private static final class ParsedUrl
    {
        final String host;
        final String database;
        final String user;
        final String pwd;

        private ParsedUrl(String host, String database, String user, String pwd)
        {
            this.host = host;
            this.database = database;
            this.user = user;
            this.pwd = pwd;
        }
    }
}
