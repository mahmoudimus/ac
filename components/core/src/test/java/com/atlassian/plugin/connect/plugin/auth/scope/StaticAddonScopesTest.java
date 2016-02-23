package com.atlassian.plugin.connect.plugin.auth.scope;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScopeLoadJsonFileHelper;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;

@ConvertToWiredTest
public class StaticAddonScopesTest
{
    @Test
    public void readsTestScopes() throws IOException
    {
        MatcherAssert.assertThat(getTestScopes(), is(AddonScopeBuilderForTests.buildScopes()));
    }

    @Test
    public void referencedScopesAreFound() throws IOException
    {
        Collection<AddonScope> scopes = StaticAddonScopes.dereference(getTestScopes(), asList(ScopeName.READ, ScopeName.WRITE));
        MatcherAssert.assertThat(scopes, Is.is(AddonScopeBuilderForTests.buildScopes()));
    }

    @Test (expected = IllegalArgumentException.class)
    public void dereferencingANullScopeReferenceResultsInException() throws IOException
    {
        StaticAddonScopes.dereference(getTestScopes(), singletonList((ScopeName) null));
    }

    @Test (expected = IllegalArgumentException.class)
    public void whitelistWithInvalidScopeNameResultsInException() throws IOException
    {
        getTestScopes("bad-test");
    }

    @Test (expected = IllegalArgumentException.class)
    public void scopesWithDuplicateKeysResultsInAnException() throws IOException
    {
        List<AddonScope> scopes = new ArrayList<>(getTestScopes());
        scopes.addAll(getTestScopes());
        StaticAddonScopes.dereference(scopes, singletonList(ScopeName.READ));
    }

    private Collection<AddonScope> getTestScopes() throws IOException
    {
        return getTestScopes("test");
    }

    private Collection<AddonScope> getTestScopes(String whitelistName) throws IOException
    {
        Map<ScopeName, AddonScope> keyToScope = new HashMap<>();
        AddonScopeLoadJsonFileHelper.addProductScopesFromFile(keyToScope, resourceLocation(whitelistName));

        // copy element references into an ArrayList so that equals() comparisons work
        // sort to protect against ordering throwing off ArrayList.equals() and to make toString() look nicer
        ArrayList<AddonScope> addonScopes = new ArrayList<>(keyToScope.values());
        Collections.sort(addonScopes);
        return addonScopes;
    }

    private static URL resourceLocation(String whitelistName)
    {
        return StaticAddonScopesTest.class.getResource(String.format("/scope/%s-whitelist.json", whitelistName));
    }
}
