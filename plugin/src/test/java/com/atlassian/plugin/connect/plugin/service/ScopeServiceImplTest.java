package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;
import com.atlassian.plugin.connect.plugin.scopes.StaticAddOnScopes;
import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScopeServiceImplTest
{
    private @Mock ApplicationProperties applicationProperties;
    private ScopeService scopeService;

    @Before
    public void beforeEachTest()
    {
        scopeService = new ScopeServiceImpl(applicationProperties);
    }

    @Test
    public void loadsForJira() throws IOException
    {
        when(applicationProperties.getDisplayName()).thenReturn("jira");
        Collection<AddOnScope> actual = scopeService.build();
        Collection<AddOnScope> expected = StaticAddOnScopes.buildForJira();
        AddOnScope actualFirst = actual.iterator().next();
        AddOnScope expectedFirst = expected.iterator().next();
        assertThat(actualFirst, is(expectedFirst));
        assertThat(actual, is(expected));
    }

    @Test
    public void loadsForConfluence() throws IOException
    {
        when(applicationProperties.getDisplayName()).thenReturn("confluence");
        assertThat(scopeService.build(), is(StaticAddOnScopes.buildForConfluence()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void willNotLoadForGarbledApplicationName() throws IOException
    {
        when(applicationProperties.getDisplayName()).thenReturn("fubar");
        scopeService.build();
    }
}
