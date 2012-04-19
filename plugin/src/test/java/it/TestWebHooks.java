package it;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.waitForEvent;
import static org.junit.Assert.assertEquals;

public class TestWebHooks extends AbstractRemoteAppTest
{
    @Test
	public void testAppStartedWebHookFired() throws IOException, JSONException, InterruptedException
    {
        JSONObject event = waitForEvent(product.getProductInstance(), "remote_app_started");
        assertEquals("app1", event.getString("key"));
	}
}
