package com.atlassian.plugin.connect.plugin.capabilities.gson;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
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
        Type listType = new TypeToken<List<ScopeName>>() {}.getType();
        List<ScopeName> scopeReferences = CapabilitiesGsonFactory.getGson().fromJson(json, listType);

        assertThat(scopeReferences, is(asList(ScopeName.READ)));
    }

    @Test
    public void readingABadScopeNameResultsInAnException()
    {
        String json = "[ \"fubar\" ]";
        Type listType = new TypeToken<List<ScopeName>>() {}.getType();
        CapabilitiesGsonFactory.getGson().fromJson(json, listType);
    }
}
