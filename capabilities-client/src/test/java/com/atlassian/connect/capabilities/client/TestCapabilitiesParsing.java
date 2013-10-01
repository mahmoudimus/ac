package com.atlassian.connect.capabilities.client;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since version
 */
public class TestCapabilitiesParsing
{
    @Test
    public void emptyCapabilities() throws Exception
    {
        DateTime buildDate = new DateTime();
        
        String json = "{\n" +
                "  \"links\": {\n" +
                "    \"self\": \"http://www.example.com/capabilities\"\n" +
                "  },\n" +
                "  \"key\": \"my-app\",\n" +
                "  \"buildDate\": \"" + UniversalDateFormatter.format(buildDate) + "\",\n" +
                "  \"capabilities\": {\n" +
                "  }\n" +
                "}";

        Map<String,String> links = newHashMap();
        links.put("self","http://www.example.com/capabilities");
        
        RemoteApplicationWithCapabilities expectedApp = new RemoteApplicationWithCapabilitiesImpl("my-app", buildDate, links, Collections.EMPTY_MAP);

        ConnectCapabilitiesResponseHandler handler = new ConnectCapabilitiesResponseHandler();
        RemoteApplicationWithCapabilities app = handler.parseBody(new ByteArrayInputStream(json.getBytes()));

        assertEquals(expectedApp, app);
    }

    @Test
    public void noBuildDateWorks() throws Exception
    {
        String json = "{\n" +
                "  \"links\": {\n" +
                "    \"self\": \"http://www.example.com/capabilities\"\n" +
                "  },\n" +
                "  \"key\": \"my-app\",\n" +
                "  \"capabilities\": {\n" +
                "  }\n" +
                "}";

        Map<String,String> links = newHashMap();
        links.put("self","http://www.example.com/capabilities");

        RemoteApplicationWithCapabilities expectedApp = new RemoteApplicationWithCapabilitiesImpl("my-app", null, links, Collections.EMPTY_MAP);

        ConnectCapabilitiesResponseHandler handler = new ConnectCapabilitiesResponseHandler();
        RemoteApplicationWithCapabilities app = handler.parseBody(new ByteArrayInputStream(json.getBytes()));

        assertEquals(expectedApp, app);
    }

    @Test
    public void multipleCapabilities() throws Exception
    {
        String json = "{\n" +
                "  \"links\": {\n" +
                "    \"self\": \"http://www.example.com/capabilities\"\n" +
                "  },\n" +
                "  \"key\": \"my-app\",\n" +
                "  \"capabilities\": {\n" +
                "\"navigation\": \"https://www.example.com/rest/capabilities/navigation\",\n" +
                "\"content-links\": \"https://www.example.com/rest/content-links/1.0/local/\"\n" +
                "  }\n" +
                "}";

        Map<String,String> links = newHashMap();
        links.put("self","http://www.example.com/capabilities");

        Map<String,String> capabilities = newHashMap();
        capabilities.put("navigation","https://www.example.com/rest/capabilities/navigation");
        capabilities.put("content-links","https://www.example.com/rest/content-links/1.0/local/");

        RemoteApplicationWithCapabilities expectedApp = new RemoteApplicationWithCapabilitiesImpl("my-app", null, links, capabilities);

        ConnectCapabilitiesResponseHandler handler = new ConnectCapabilitiesResponseHandler();
        RemoteApplicationWithCapabilities app = handler.parseBody(new ByteArrayInputStream(json.getBytes()));

        assertEquals(expectedApp, app);
        assertNotNull(app.getCapabilities());
        assertTrue(!app.getCapabilities().isEmpty());
        
        assertTrue(app.getCapabilities().containsKey("navigation"));
        assertEquals("https://www.example.com/rest/capabilities/navigation",app.getCapabilities().get("navigation"));

        assertTrue(app.getCapabilities().containsKey("content-links"));
        assertEquals("https://www.example.com/rest/content-links/1.0/local/",app.getCapabilityUrl("content-links"));
    }

}
