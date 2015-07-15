package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.product.FeatureManager;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import static com.atlassian.plugin.connect.crowd.usermanagement.CrowdServiceLocatorMocks.mockCrowdServiceLocator;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith (Parameterized.class)
public class TestCloudAwareCrowdServiceOnServer
{
    public static final String ADDON_USER_NAME = "addon-user-name";
    public static final String ADDON_DISPLAY_NAME = "Addon Display Name";
    public static final String EMAIL_ADDRESS = "addon@example.com";
    public static final String ADDON_PASSWORD = "addon-password";
    private final boolean isConfluence;
    @Mock private ApplicationService applicationService;
    @Mock private ApplicationManager applicationManager;
    @Mock private HostProperties hostProperties;
    @Mock private CrowdServiceLocator crowdServiceLocator;
    @Mock private ConnectCrowdBase remote;
    @Mock private ConnectCrowdBase embedded;
    @Mock private FeatureManager featureManager;
    @Mock private CrowdClientProvider crowdClientProvider;
    @Mock private UserReconciliation userReconciliation;

    private CloudAwareCrowdService cloudAwareCrowdService;
    public static final ImmutableMap<String, Set<String>> ATTRIBUTES = ImmutableMap.<String, Set<String>>of("attribute-name", newHashSet(singletonList("attribute-value")));

    public TestCloudAwareCrowdServiceOnServer(boolean isConfluence)
    {
        this.isConfluence = isConfluence;
    }

    @Parameterized.Parameters
    public static Collection<Boolean[]> confluenceStatus()
    {
        return asList(new Boolean[] {true}, new Boolean[] {false});
    }

    @Before
    public void beforeEach()
    {
        initMocks(this);

        mockCrowdServiceLocator(crowdServiceLocator, embedded, remote);

        cloudAwareCrowdService = new CloudAwareCrowdService(crowdServiceLocator, applicationService, applicationManager, hostProperties, featureManager, crowdClientProvider, userReconciliation);

        when(featureManager.isOnDemand()).thenReturn(false);
        when(hostProperties.getKey()).thenReturn(isConfluence ? "confluence" : "JIRA");
    }

    @Test
    public void createOrEnableUserUsesEmbedded()
    {
        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PasswordCredential.unencrypted(ADDON_PASSWORD));
        verify(embedded).createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PasswordCredential.unencrypted(ADDON_PASSWORD));
        verifyZeroInteractions(remote);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void createOrEnableUserSetsAttributes()
    {
        cloudAwareCrowdService.createOrEnableUser(
                ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PasswordCredential.unencrypted(ADDON_PASSWORD), ATTRIBUTES);
        verify(embedded).setAttributesOnUser(anyString(), eq(ATTRIBUTES));
        verifyZeroInteractions(remote);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void createOrEnableUserIgnoresEmptyAttributes()
    {
        Map<String, Set<String>> noAttributes = Collections.emptyMap();
        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PasswordCredential.unencrypted(ADDON_PASSWORD), noAttributes);
        verify(embedded, never()).setAttributesOnUser(anyString(), anyMap());
        verifyZeroInteractions(remote);
    }
}