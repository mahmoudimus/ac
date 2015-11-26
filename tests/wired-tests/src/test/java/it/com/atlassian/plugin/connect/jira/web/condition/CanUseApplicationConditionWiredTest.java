package it.com.atlassian.plugin.connect.jira.web.condition;

import java.util.Collections;

import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.jira.web.condition.CanUseApplicationCondition;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith (AtlassianPluginsTestRunner.class)
public class CanUseApplicationConditionWiredTest
{

    private final WebFragmentHelper webFragmentHelper;
    private final PluginAccessor pluginAccessor;
    private final UserSearchService userSearchService;
    private final JiraAuthenticationContext authenticationContext;

    private Condition condition;

    public CanUseApplicationConditionWiredTest(WebFragmentHelper webFragmentHelper, PluginAccessor pluginAccessor, final UserSearchService userSearchService, final JiraAuthenticationContext authenticationContext)
    {
        this.webFragmentHelper = webFragmentHelper;
        this.pluginAccessor = pluginAccessor;
        this.userSearchService = userSearchService;
        this.authenticationContext = authenticationContext;
    }

    @Before
    public void setUp() throws ConditionLoadingException
    {
        condition = webFragmentHelper.loadCondition(CanUseApplicationCondition.class.getName(), getConnectPlugin());
    }

    @Test
    public void conditionEvaluatesToFalseForUninstalledApps()
    {
        ApplicationUser aUser = userSearchService.findUsers("", UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY).iterator().next();
        authenticationContext.setLoggedInUser(aUser);
        condition.init(ImmutableMap.of("applicationKey", "jira-software"));
        assertFalse(condition.shouldDisplay(Collections.emptyMap()));
    }

    @Test
    public void conditionEvaluatesToTrueForAvailableApps()
    {
        ApplicationUser aUser = userSearchService.findUsers("", UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY).iterator().next();
        authenticationContext.setLoggedInUser(aUser);
        condition.init(ImmutableMap.of("applicationKey", "jira-core"));
        assertTrue(condition.shouldDisplay(Collections.emptyMap()));
    }

    private Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }
}
