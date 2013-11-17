package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.reflect.TypeToken;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticAddOnScopesReaderTest
{
    @Test
    public void readsConfluenceScopes() throws IOException
    {
        assertThat(getTestScopes(), is(AddOnScopeBuilderForTests.buildReadScope()));
    }

    @Test
    public void referencedScopesAreFound() throws IOException
    {
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> scopeReferences = CapabilitiesGsonFactory.getGson().fromJson("[ \"READ\" ]", listType);
        Collection<AddOnScope> scopes = StaticAddOnScopesReader.dereference(getTestScopes(), scopeReferences);

        assertThat(scopes, Is.is(AddOnScopeBuilderForTests.buildReadScope()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badScopeReferenceResultsInException() throws IOException
    {
        StaticAddOnScopesReader.dereference(getTestScopes(), asList("bad"));
    }

    private Collection<AddOnScope> getTestScopes() throws IOException
    {
        return StaticAddOnScopesReader.buildFor("test");
    }
}
