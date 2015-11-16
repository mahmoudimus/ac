package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddOnScope;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddOnScopeLoadJsonFileHelper;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void whitelistWithInvalidScopeNameResultsInException() throws IOException
    {
        getTestScopes("bad-test");
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
        return getTestScopes("test");
    }

    private Collection<AddOnScope> getTestScopes(String whitelistName) throws IOException
    {
        Map<ScopeName, AddOnScope> keyToScope = new HashMap<>();
        AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(keyToScope, resourceLocation(whitelistName));

        // copy element references into an ArrayList so that equals() comparisons work
        // sort to protect against ordering throwing off ArrayList.equals() and to make toString() look nicer
        ArrayList<AddOnScope> addOnScopes = new ArrayList<>(keyToScope.values());
        Collections.sort(addOnScopes);
        return addOnScopes;
    }

    private static URL resourceLocation(String whitelistName)
    {
        return StaticAddOnScopesTest.class.getResource(String.format("/scope/%s-whitelist.json", whitelistName));
    }
}
