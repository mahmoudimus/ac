package com.atlassian.plugin.remotable.container.ao;

import org.junit.Test;

import static org.junit.Assert.*;

public final class HerokuUrlTransformerTest
{
    private HerokuUrlTransformer hut = new HerokuUrlTransformer();

    @Test
    public void transformJdbcUrlDoesNotTransform()
    {
        final String url = "jdbc:some-url";
        assertEquals(url, hut.transform(url));
    }

    @Test
    public void transformHerokuUrlToJdbc()
    {
        assertEquals(
                "jdbc:postgresql://host/database?user=db-user&password=db-pwd",
                hut.transform("postgres://db-user:db-pwd@host/database")
        );
    }
}
