package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith (MockitoJUnitRunner.class)
public class IsLicensedConditionTest
{
    @Mock
    private LicenseRetriever licenseRetriever;

    @InjectMocks
    private IsLicensedCondition isLicensedCondition;

    @Test
    public void shouldNotDisplayIfNoLicenseCanBeFound() {
        Mockito.when(licenseRetriever.getLicense(Mockito.anyString())).thenReturn(Option.<PluginLicense>none());
        assertFalse("The condition should not return true if there is no license.", isLicensedCondition.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void shouldNotDisplayIfTheAddonIsNotLicensed() {
        final PluginLicense mockPluginLicense = Mockito.mock(PluginLicense.class);
        Mockito.when(mockPluginLicense.isActive()).thenReturn(false);
        Mockito.when(licenseRetriever.getLicense(Mockito.anyString())).thenReturn(Option.some(mockPluginLicense));
        assertFalse("The condition should not return true if the license is not active.", isLicensedCondition.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void shouldDisplayIfTheAddonIsLicensed() {
        final PluginLicense mockPluginLicense = Mockito.mock(PluginLicense.class);
        Mockito.when(mockPluginLicense.isActive()).thenReturn(true);
        Mockito.when(licenseRetriever.getLicense(Mockito.anyString())).thenReturn(Option.some(mockPluginLicense));
        assertTrue("The condition should not return false if the license is active.", isLicensedCondition.shouldDisplay(ImmutableMap.of()));
    }
}