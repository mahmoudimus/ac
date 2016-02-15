package it.com.atlassian.plugin.connect.plugin.web.condition;

import java.io.IOException;
import java.util.Collections;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.crowd.usermanagement.EmbeddedCrowd;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.web.condition.UserIsInGroupCondition;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static it.com.atlassian.plugin.connect.plugin.web.condition.WebTestMatchers.webItemWithKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@RunWith(AtlassianPluginsTestRunner.class)
public class UserIsInGroupConditionTest
{
    public static final String LOCATION = "example-web-fragment-location";
    public static final String ADDON_MODULE_KEY = "test-user-in-group-condition-addon-module-key";
    public static final String ADDON_KEY = "test-addon-condition-user-in-group";
    public static final String TEST_GROUP_NAME = "test-group";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final WebInterfaceManager webInterfaceManager;
    private final EmbeddedCrowd embeddedCrowd;

    public UserIsInGroupConditionTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                      WebInterfaceManager webInterfaceManager, EmbeddedCrowd embeddedCrowd)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.webInterfaceManager = webInterfaceManager;
        this.embeddedCrowd = embeddedCrowd;
    }

    private Plugin installJsonAddon(final String groupName) throws IOException
    {
        final WebItemModuleBean userInGroupConditionWebItem = WebItemModuleBean.newWebItemBean()
            .withKey(ADDON_MODULE_KEY)
            .withName(new I18nProperty(ADDON_MODULE_KEY, "blah"))
            .withLocation(LOCATION)
            .withUrl("/nowhere")
            .withConditions(ImmutableList.of(
                SingleConditionBean.newSingleConditionBean().withCondition(UserIsInGroupCondition.CONDITION_NAME).withParam("groupName", groupName).build()
            ))
            .build();

        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
            .withKey(ADDON_KEY)
            .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
            .withDescription(getClass().getCanonicalName())
            .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
            .withScopes(Sets.newHashSet(ScopeName.READ))
            .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
            .withModule("webItems", userInGroupConditionWebItem)
            .withLicensing(true)
            .build();

        return testPluginInstaller.installAddon(addonBean);
    }

    @After
    public void tearDown() throws IOException
    {
        for (String key : testPluginInstaller.getInstalledAddonKeys())
        {
            testPluginInstaller.uninstallAddon(key);
        }
    }

    @Test
    public void webItemsDisplayedWhenUserExistsInGroup()
        throws IOException, ApplicationPermissionException, OperationFailedException, ApplicationNotFoundException,
        InvalidAuthenticationException, UserNotFoundException, GroupNotFoundException
    {
        final String username = "admin";
        final String groupName = TEST_GROUP_NAME + "-user-in-group";

        testAuthenticator.authenticateUser(username);
        final Plugin plugin = installJsonAddon(groupName);

        assertTrue(embeddedCrowd.ensureGroupExists(groupName));
        embeddedCrowd.ensureUserIsInGroup(username, groupName);

        final Iterable<WebItemModuleDescriptor> displayableWebItems = webInterfaceManager.getDisplayableItems(LOCATION, Collections.emptyMap());
        assertThat(displayableWebItems, contains(webItemWithKey(plugin, ADDON_MODULE_KEY)));
    }

    @Test
    public void webItemsNotDisplayedWhenUserExistsButNotInGroup()
        throws IOException, ApplicationPermissionException, OperationFailedException, ApplicationNotFoundException,
        InvalidAuthenticationException, UserNotFoundException, GroupNotFoundException
    {
        final String username = "admin";
        final String groupName = TEST_GROUP_NAME + "-user-not-in-group";

        testAuthenticator.authenticateUser(username);
        final Plugin plugin = installJsonAddon(groupName);

        assertTrue(embeddedCrowd.ensureGroupExists(groupName));

        final Iterable<WebItemModuleDescriptor> displayableWebItems = webInterfaceManager.getDisplayableItems(LOCATION, Collections.emptyMap());
        assertThat(displayableWebItems, not(contains(webItemWithKey(plugin, ADDON_MODULE_KEY))));
    }

    @Test
    public void webItemsNotDisplayedWhenUserExistsButGroupDoesNot()
        throws IOException, ApplicationPermissionException, OperationFailedException, ApplicationNotFoundException,
        InvalidAuthenticationException, UserNotFoundException, GroupNotFoundException
    {
        final String username = "admin";
        final String groupName = TEST_GROUP_NAME + "-group-that-does-not-exist";

        testAuthenticator.authenticateUser(username);
        final Plugin plugin = installJsonAddon(groupName);

        final Iterable<WebItemModuleDescriptor> displayableWebItems = webInterfaceManager.getDisplayableItems(LOCATION, Collections.emptyMap());
        assertThat(displayableWebItems, not(contains(webItemWithKey(plugin, ADDON_MODULE_KEY))));
    }
}
