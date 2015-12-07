package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.spi.FeatureManager;
import com.atlassian.plugin.connect.spi.HostProperties;
import com.atlassian.plugin.connect.api.ConnectAddonInitException;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

public class TestCloudAwareCrowdServiceInConfluenceCloud
{
    public static final PasswordCredential PASSWORD = PasswordCredential.unencrypted("addon-password");
    @Mock private ApplicationService applicationService;
    @Mock private CrowdApplicationProvider crowdApplicationProvider;
    @Mock private HostProperties hostProperties;
    @Mock private CrowdServiceLocator crowdServiceLocator;
    @Mock private ConnectCrowdBase remote;
    @Mock private ConnectCrowdBase embedded;
    @Mock private FeatureManager featureManager;
    @Mock private CrowdClientProvider crowdClientProvider;
    @Mock private UserReconciliation userReconciliation;

    private CloudAwareCrowdService cloudAwareCrowdService;

    @Rule public ExpectedException thrown = ExpectedException.none();

    private static final String ADDON_USER_NAME = "addon-user-name";
    private static final String ADDON_DISPLAY_NAME = "Addon Display Name";
    private static final String EMAIL_ADDRESS = "addon@example.com";
    private static final ImmutableMap<String, Set<String>> ATTRIBUTES = ImmutableMap.<String, Set<String>>of("attribute-name", newHashSet(singletonList("attribute-value")));

    @Before
    public void beforeEach()
    {
        initMocks(this);

        mockCrowdServiceLocator(crowdServiceLocator, embedded, remote);

        when(featureManager.isOnDemand()).thenReturn(true);
        when(hostProperties.getKey()).thenReturn("confluence");

        cloudAwareCrowdService = new CloudAwareCrowdService(crowdServiceLocator, applicationService, crowdApplicationProvider, hostProperties, featureManager, crowdClientProvider, userReconciliation);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void createOrEnableUserUsesRemote()
    {
        final User mockUser = mock(User.class);
        when(embedded.findUserByName(anyString())).thenReturn((Optional) Optional.of(mockUser));
        Map<String, Set<String>> noAttributes = Collections.emptyMap();

        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PASSWORD, noAttributes);
        verify(remote).createOrEnableUser(anyString(), anyString(), anyString(), any(PasswordCredential.class));
        verify(embedded, never()).createOrEnableUser(anyString(), anyString(), anyString(), any(PasswordCredential.class));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void createOrEnableUserSetsAttributesOnBothSidesAfterSync()
            throws InterruptedException
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn(ADDON_USER_NAME);

        when(embedded.findUserByName(anyString())).thenReturn(Optional.empty(), (Optional) Optional.of(user));

        ScheduledThreadPoolExecutor simulatedCrowdSyncEvent = new ScheduledThreadPoolExecutor(1);
        simulatedCrowdSyncEvent.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                cloudAwareCrowdService.handleSync();
            }
        }, 1, TimeUnit.SECONDS);
        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PASSWORD, ATTRIBUTES);

        verify(remote).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
        verify(embedded).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
    }

    @Test
    public void createOrEnableUserSetsAttributesOnSecondSyncEvent()
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn(ADDON_USER_NAME);

        when(embedded.findUserByName(anyString())).thenReturn(
            Optional.empty(),               // No user is found when we search for an existing user
            Optional.empty(),               // The user still doesn't show up after a directory sync
            (Optional) Optional.of(user),   // The user shows up after a second directory sync
            (Optional) Optional.of(user)    // The user is still there when we double-check at the end
        );

        cloudAwareCrowdService.setSyncTimeout(1);

        ScheduledThreadPoolExecutor simulatedCrowdSyncEvent = new ScheduledThreadPoolExecutor(2);

        // The first sync event
        simulatedCrowdSyncEvent.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                cloudAwareCrowdService.handleSync();
            }
        }, 300, TimeUnit.MILLISECONDS);

        // The second sync event
        simulatedCrowdSyncEvent.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                cloudAwareCrowdService.handleSync();
            }
        }, 600, TimeUnit.MILLISECONDS);

        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PASSWORD, ATTRIBUTES);

        verify(remote).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
        verify(embedded).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
    }


    @Test
    public void createOrEnableUserSetsAttributesOnBothSidesAfterMissedSyncEvent()
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn(ADDON_USER_NAME);

        when(embedded.findUserByName(anyString())).thenReturn(
            Optional.empty(),               // No user is found when we search for an existing user
            (Optional) Optional.of(user)    // The user shows up after the timeout has elapsed
        );

        cloudAwareCrowdService.setSyncTimeout(1);
        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PASSWORD, ATTRIBUTES);

        verify(remote).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
        verify(embedded).setAttributesOnUser(ADDON_USER_NAME, ATTRIBUTES);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void createOrEnableUserThrowsWhenUserFailsToSync()
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn(ADDON_USER_NAME);
        when(embedded.findUserByName(anyString())).thenReturn(Optional.empty());
        cloudAwareCrowdService.setSyncTimeout(1);

        thrown.expect(ConnectAddonInitException.class);
        cloudAwareCrowdService.createOrEnableUser(ADDON_USER_NAME, ADDON_DISPLAY_NAME, EMAIL_ADDRESS, PASSWORD, ATTRIBUTES);
    }
}