package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.Filter;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class DefaultConnectRequestFilterFactoryTest
{
    @Mock
    private PluginEventManager pluginEventManager;
    @Mock
    private PluginAccessor pluginAccessor;

    private DefaultConnectRequestFilterFactory requestFilterFactory;

    @Before
    public void setUp()
    {
        this.requestFilterFactory = new DefaultConnectRequestFilterFactory(pluginAccessor, pluginEventManager);
    }

    @Test
    public void testFactoryReturnsRegisteredFilter()
    {
        ConnectRequestFilterModuleDescriptor moduleDescriptor = Mockito.mock(ConnectRequestFilterModuleDescriptor.class);
        Filter filter = Mockito.mock(Filter.class);
        when(moduleDescriptor.getFilterPhase()).thenReturn(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER);
        when(moduleDescriptor.getModule()).thenReturn(filter);

        requestFilterFactory.getTracker().onPluginModuleEnabled(new PluginModuleEnabledEvent(moduleDescriptor));

        Iterable<Filter> filtersForPhase = requestFilterFactory.getFiltersForPhase(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER);

        assertThat(filtersForPhase, Matchers.<Filter>iterableWithSize(1));
        assertThat(filtersForPhase, Matchers.hasItem(filter));
    }

    @Test
    public void testFactoryReactsForPluginModuleDisabled()
    {
        ConnectRequestFilterModuleDescriptor moduleDescriptor = Mockito.mock(ConnectRequestFilterModuleDescriptor.class);
        Filter filter = Mockito.mock(Filter.class);
        when(moduleDescriptor.getFilterPhase()).thenReturn(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER);
        when(moduleDescriptor.getModule()).thenReturn(filter);

        requestFilterFactory.getTracker().onPluginModuleEnabled(new PluginModuleEnabledEvent(moduleDescriptor));

        assertThat(requestFilterFactory.getFiltersForPhase(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER), Matchers.<Filter>iterableWithSize(1));

        requestFilterFactory.getTracker().onPluginModuleDisabled(new PluginModuleDisabledEvent(moduleDescriptor, true));
        assertThat(requestFilterFactory.getFiltersForPhase(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER), Matchers.<Filter>iterableWithSize(0));
    }

    @Test
    public void testFactoryAllowsMultipleRegistrationsForPhase()
    {
        ConnectRequestFilterModuleDescriptor moduleDescriptor1 = Mockito.mock(ConnectRequestFilterModuleDescriptor.class);
        Filter filter1 = Mockito.mock(Filter.class);
        when(moduleDescriptor1.getFilterPhase()).thenReturn(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER);
        when(moduleDescriptor1.getModule()).thenReturn(filter1);
        requestFilterFactory.getTracker().onPluginModuleEnabled(new PluginModuleEnabledEvent(moduleDescriptor1));

        ConnectRequestFilterModuleDescriptor moduleDescriptor2 = Mockito.mock(ConnectRequestFilterModuleDescriptor.class);
        Filter filter2 = Mockito.mock(Filter.class);
        when(moduleDescriptor2.getFilterPhase()).thenReturn(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER);
        when(moduleDescriptor2.getModule()).thenReturn(filter2);
        requestFilterFactory.getTracker().onPluginModuleEnabled(new PluginModuleEnabledEvent(moduleDescriptor2));

        final Iterable<Filter> filtersForPhase = requestFilterFactory.getFiltersForPhase(ConnectRequestFilterPhase.AFTER_API_SCOPING_FILTER);

        assertThat(filtersForPhase, Matchers.<Filter>iterableWithSize(2));
        assertThat(filtersForPhase, Matchers.hasItems(filter1, filter2));
    }

}