package com.atlassian.plugin.connect.plugin.capabilities.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readCapabilitiesTestFile;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConnectScopesMarshallingTest
{
    @Test
    public void deserializationWorks() throws IOException
    {
        String json = readCapabilitiesTestFile("scopesMarshalling.json");
        Gson gson = CapabilitiesGsonFactory.getGson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> scopes = gson.fromJson(json, listType);

        assertThat(scopes, is(asList("READ")));
    }
}
