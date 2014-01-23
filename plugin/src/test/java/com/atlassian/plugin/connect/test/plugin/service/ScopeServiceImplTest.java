package com.atlassian.plugin.connect.test.plugin.service;

import com.atlassian.plugin.connect.plugin.scopes.StaticAddOnScopes;
import com.atlassian.plugin.connect.plugin.service.ScopeService;
import com.atlassian.plugin.connect.plugin.service.ScopeServiceImpl;
import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

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
        assertThat(scopeService.build(), is(StaticAddOnScopes.buildForJira()));
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
