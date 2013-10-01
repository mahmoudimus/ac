package com.atlassian.plugin.connect.plugin.capabilities.http;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.connect.capabilities.client.CapabilitiesGsonFactory;
import com.atlassian.connect.capabilities.client.ConnectCapabilitiesResponseHandler;
import com.atlassian.connect.capabilities.client.RemoteApplicationWithCapabilities;
import com.atlassian.connect.capabilities.client.RemoteApplicationWithCapabilitiesImpl;
import com.atlassian.plugin.connect.api.capabilities.beans.CapabilitySetContainer;
import com.atlassian.plugin.connect.api.capabilities.beans.CapabilitySetTypeAdapter;
import com.atlassian.plugin.connect.api.capabilities.beans.DefaultCapabilitySetContainer;
import com.atlassian.plugin.connect.plugin.capabilities.beans.SomeModule;

import com.google.gson.Gson;

import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.*;

/**
 * @since version
 */
public class TestCapabilitySets
{
    @Test
    public void capabilitySetSingleModuleWorks() throws Exception
    {
        String rootJson = "{\n" +
                "  \"links\": {\n" +
                "    \"self\": \"http://www.example.com/capabilities\"\n" +
                "  },\n" +
                "  \"key\": \"my-app\",\n" +
                "  \"capabilities\": {\n" +
                "      \"some-modules\": \"http://www.example.com/capabilities/some-modules\"\n" +
                "  }\n" +
                "}";

        Map<String, String> links = newHashMap();
        links.put("self", "http://www.example.com/capabilities");

        Map<String, String> moduleSet = new HashMap<String, String>();
        moduleSet.put("some-modules", "http://www.example.com/capabilities/some-modules");

        RemoteApplicationWithCapabilities expectedApp = new RemoteApplicationWithCapabilitiesImpl("my-app", null, links, moduleSet);

        String moduleJson = "{\n" +
                "  \"links\": {\n" +
                "    \"self\": \"http://www.example.com/capabilities\"\n" +
                "  },\n" +
                "  \"some-modules\":[\n" +
                "{\"key\":\"my-module-key\",\"food\":\"pizza\"}\n" +
                "  ]\n" +
                "}";

        ConnectCapabilitiesResponseHandler rootHandler = new ConnectCapabilitiesResponseHandler();
        RemoteApplicationWithCapabilities app = rootHandler.parseBody(new ByteArrayInputStream(rootJson.getBytes()));

        assertEquals(expectedApp, app);

        Gson gson = CapabilitiesGsonFactory.getGsonBuilder().registerTypeAdapter(DefaultCapabilitySetContainer.class, new CapabilitySetTypeAdapter()).create();

        CapabilitySetContainer beanContainer = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(moduleJson.getBytes())), DefaultCapabilitySetContainer.class);

        assertNotNull(beanContainer);

        List<SomeModule> modules = beanContainer.getModules();
        assertNotNull(modules);
        assertFalse(modules.isEmpty());

        SomeModule module = modules.get(0);

        assertEquals("my-module-key", module.getKey());
        assertEquals("pizza", module.getFood());

    }
}
