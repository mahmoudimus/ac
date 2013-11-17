package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
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

public class StaticAddOnScopesTest
{
    @Test
    public void readsTestScopes() throws IOException
    {
        assertThat(getTestScopes(), is(AddOnScopeBuilderForTests.buildReadScope()));
    }

    @Test
    public void referencedScopesAreFound() throws IOException
    {
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> scopeReferences = CapabilitiesGsonFactory.getGson().fromJson("[ \"READ\" ]", listType);
        Collection<AddOnScope> scopes = StaticAddOnScopes.dereference(getTestScopes(), scopeReferences);

        assertThat(scopes, Is.is(AddOnScopeBuilderForTests.buildReadScope()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dereferencingANonExistentScopeReferenceResultsInException() throws IOException
    {
        StaticAddOnScopes.dereference(getTestScopes(), asList("bad"));
    }

    @Test(expected = InvalidDescriptorException.class)
    public void readingABadScopeNameResultsInException() throws IOException
    {
        StaticAddOnScopes.buildFor("bad_name");
    }

    private Collection<AddOnScope> getTestScopes() throws IOException
    {
        return StaticAddOnScopes.buildFor("test");
    }
}
