package it.com.atlassian.plugin.connect.plugin.web.condition;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.web.item.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectConditionClassResolverTest extends AbstractConnectAddonTest
{

    private static final String LOCATION = "fake-location";

    private final DynamicWebInterfaceManager webInterfaceManager;

    public ConnectConditionClassResolverTest(WebItemModuleProvider webItemModuleProvider,
            TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            DynamicWebInterfaceManager webInterfaceManager)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
        this.webInterfaceManager = webInterfaceManager;
    }

    @Test
    public void shouldApplyPluginProvidedConditionsToWebItems() throws IOException
    {
        String visibleItemKey = "visible-item";
        String hiddenItemKey = "hidden-item";
        ConnectAddonBean addon = newConnectAddonBean()
                .withKey(AddonUtil.randomPluginKey())
                .withBaseurl("http://example.com")
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems",
                        buildWebItem(visibleItemKey, "Visible", "always-display"),
                        buildWebItem(hiddenItemKey, "Hidden", "never-display"))
                .build();

        plugin = testPluginInstaller.installAddon(addon);

        List<WebItemModuleDescriptor> webItems = webInterfaceManager.getItems(LOCATION);
        List<WebItemModuleDescriptor> displayableWebItems = webInterfaceManager.getDisplayableItems(LOCATION, Collections.emptyMap());

        assertThat(webItems, contains(webItemWithKey(visibleItemKey), webItemWithKey(hiddenItemKey)));
        assertThat(displayableWebItems, contains(webItemWithKey(visibleItemKey)));
    }

    private WebItemModuleBean buildWebItem(String key, String name, String condition)
    {
        return newWebItemBean()
                .withKey(key)
                .withUrl("/")
                .withName(new I18nProperty(name, ""))
                .withConditions(newSingleConditionBean().withCondition(condition).build())
                .withLocation(LOCATION)
                .build();
    }

    private Matcher<WebItemModuleDescriptor> webItemWithKey(String key)
    {
        return new TypeSafeMatcher<WebItemModuleDescriptor>()
        {

            @Override
            protected boolean matchesSafely(WebItemModuleDescriptor item)
            {
                return getWebItemKey(item).equals(getWebItemModuleKey());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("web item with key ");
                description.appendValue(getWebItemModuleKey());
            }

            @Override
            protected void describeMismatchSafely(WebItemModuleDescriptor item, Description mismatchDescription)
            {
                mismatchDescription.appendText("web item with key ");
                mismatchDescription.appendValue(getWebItemKey(item));
            }

            private String getWebItemKey(WebItemModuleDescriptor item)
            {
                return item.getKey();
            }

            private String getWebItemModuleKey()
            {
                return ModuleKeyUtils.addonAndModuleKey(plugin.getKey(), key);
            }
        };
    }
}
