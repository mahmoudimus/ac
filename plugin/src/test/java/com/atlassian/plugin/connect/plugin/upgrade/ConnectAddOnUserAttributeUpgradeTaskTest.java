package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildAttributeConnectAddOnAttributeName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectAddOnUserAttributeUpgradeTaskTest
{
    private static final String APPLICATION_NAME = "crowd-embedded";
    private ConnectAddOnUserAttributeUpgradeTask upgradeTask;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationManager applicationManager;
    @Mock
    private ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    @Mock
    private Application application;
    @Captor
    private ArgumentCaptor<String> connectAddOnUserCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Set<String>>> userAttributeCaptor;

    @Before
    public void setup()
    {
        upgradeTask = new ConnectAddOnUserAttributeUpgradeTask(applicationService, applicationManager, connectAddOnUserGroupProvisioningService);

    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        when(application.getName()).thenReturn(APPLICATION_NAME);
        when(connectAddOnUserGroupProvisioningService.getCrowdApplicationName()).thenReturn(APPLICATION_NAME);
        when(applicationManager.findByName(APPLICATION_NAME)).thenReturn(application);
        when(applicationService.searchDirectGroupRelationships(eq(application), any(MembershipQuery.class))).thenReturn(ImmutableList.of("connect-addon-one"));

        upgradeTask.doUpgrade();

        verify(applicationService).storeUserAttributes(eq(application), eq("connect-addon-one"), userAttributeCaptor.capture());

        assertThat(userAttributeCaptor.getValue().keySet(), containsInAnyOrder(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)));
    }
}