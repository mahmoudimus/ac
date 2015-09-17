package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.product.FeatureManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.plugin.connect.crowd.usermanagement.CrowdServiceLocatorMocks.mockCrowdServiceLocator;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestCloudAwareCrowdServiceInJiraCloud
{
    private static final String ADDON_USER_NAME = "addon-user-name";
    private static final String ADDON_DISPLAY_NAME = "Addon Display Name";
    private static final String EMAIL_ADDRESS = "addon@example.com";
    private static final ImmutableMap<String, Set<String>> ATTRIBUTES = ImmutableMap.<String, Set<String>>of("attribute-name", newHashSet(singletonList("attribute-value")));
    public static final PasswordCredential PASSWORD = PasswordCredential.unencrypted("addon-password");

    @Mock private ApplicationService applicationService;
    @Mock private CrowdApplicationProvider crowdApplicationProvider;
    @Mock private HostProperties hostProperties;
    @Mock private CrowdServiceLocator crowdServiceLocator;
    @Mock private ConnectCrowdBase remote;
    @Mock private ConnectCrowdBase embedded;
    @Mock private ConnectOnDemandCheck connectOnDemandCheck;
    @Mock private CrowdClientProvider crowdClientProvider;
    @Mock private UserReconciliation userReconciliation;

    private CloudAwareCrowdService cloudAwareCrowdService;

    @Before
    public void beforeEach()
    {
        initMocks(this);

        mockCrowdServiceLocator(crowdServiceLocator, embedded, remote);

        when(connectOnDemandCheck.isOnDemand()).thenReturn(true);
        when(hostProperties.getKey()).thenReturn("jira");

        cloudAwareCrowdService = new CloudAwareCrowdService(crowdServiceLocator, applicationService, crowdApplicationProvider, hostProperties, connectOnDemandCheck, crowdClientProvider, userReconciliation);
    }

    @Test
    public void createOrEnableUserUsesEmbedded()
    {
        final Map<String, Set<String>> noAttributes = Collections.emptyMap();

        cloudAwareCrowdService.createOrEnableUser(
                ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS,
                PASSWORD, noAttributes);
        verify(embedded).createOrEnableUser(anyString(), anyString(), anyString(), any(PasswordCredential.class));
        verify(remote, never()).createOrEnableUser(anyString(), anyString(), anyString(), any(PasswordCredential.class));
    }

    @Test
    public void createOrEnableUserSetsAttributesOnBothSides()
    {
        Optional userOption = mock(Optional.class);

        when(embedded.findUserByName(anyString())).thenReturn(userOption);
        when(userOption.isPresent()).thenReturn(true);

        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PASSWORD, ATTRIBUTES);
        cloudAwareCrowdService.handleSync();

        verify(embedded).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
        verify(remote).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
    }
}