package com.atlassian.plugin.connect.core.scopes;

import com.atlassian.plugin.connect.core.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;

@ConvertToWiredTest
public class StaticAddOnScopesTest
{
    @Test
    public void readsTestScopes() throws IOException
    {
        MatcherAssert.assertThat(getTestScopes(), is(AddOnScopeBuilderForTests.buildScopes()));
    }

    @Test
    public void referencedScopesAreFound() throws IOException
    {
        Collection<AddOnScope> scopes = StaticAddOnScopes.dereference(getTestScopes(), asList(ScopeName.READ, ScopeName.WRITE));
        MatcherAssert.assertThat(scopes, Is.is(AddOnScopeBuilderForTests.buildScopes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dereferencingANullScopeReferenceResultsInException() throws IOException
    {
        StaticAddOnScopes.dereference(getTestScopes(), asList((ScopeName) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readingABadResourceNameResultsInException() throws IOException
    {
        StaticAddOnScopes.buildFor(resourceLocation("bad_name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopesWithDuplicateKeysResultsInAnException() throws IOException
    {
        List<AddOnScope> scopes = new ArrayList<>(getTestScopes());
        scopes.addAll(getTestScopes());
        StaticAddOnScopes.dereference(scopes, asList(ScopeName.READ));
    }

    private Collection<AddOnScope> getTestScopes() throws IOException
    {
        return StaticAddOnScopes.buildFor(resourceLocation("test"));
    }

    private static URL resourceLocation(String product)
    {
        return StaticAddOnScopesTest.class.getResource("/com/atlassian/connect/scopes." + product + ".json");
    }
}
