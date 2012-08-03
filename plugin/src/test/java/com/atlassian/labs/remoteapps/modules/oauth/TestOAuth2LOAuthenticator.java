package com.atlassian.labs.remoteapps.modules.oauth;

import net.oauth.OAuthMessage;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestOAuth2LOAuthenticator
{
    @Test
    public void testExtractPluginKey() throws IOException
    {
        OAuthMessage message = new OAuthMessage("GET", "http://localhost", Collections.<Map.Entry>emptyList());
        message.addParameter("foo", "bar");
        String auth = message.getAuthorizationHeader("baz");
        assertEquals("baz", OAuth2LOAuthenticator.extractPluginKey(auth));
    }
}
