package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
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
        assertThat(getTestScopes(), is(AddOnScopeBuilderForTests.buildScopes()));
    }

    @Test
    public void referencedScopesAreFound() throws IOException
    {
        Collection<AddOnScope> scopes = StaticAddOnScopes.dereference(getTestScopes(), asList(ScopeName.READ, ScopeName.WRITE));
        assertThat(scopes, Is.is(AddOnScopeBuilderForTests.buildScopes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dereferencingANullScopeReferenceResultsInException() throws IOException
    {
        StaticAddOnScopes.dereference(getTestScopes(), asList((ScopeName)null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readingABadResourceNameResultsInException() throws IOException
    {
        StaticAddOnScopes.buildFor("bad_name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopesWithDuplicateKeysResultsInAnException() throws IOException
    {
        List<AddOnScope> scopes = new ArrayList<AddOnScope>(getTestScopes());
        scopes.addAll(getTestScopes());
        StaticAddOnScopes.dereference(scopes, asList(ScopeName.READ));
    }

    private Collection<AddOnScope> getTestScopes() throws IOException
    {
        return StaticAddOnScopes.buildFor("test");
    }
}
