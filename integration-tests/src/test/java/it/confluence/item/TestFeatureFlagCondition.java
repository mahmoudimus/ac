package it.confluence.item;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import it.util.ConnectTestUserFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import it.util.TestUser;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.junit.Assert.assertFalse;

@Ignore
public class TestFeatureFlagCondition extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    private static final String FEATURE_FLAG = "dark-feature";

    private static final String FEATURE_FLAG_KEY = "key-dark-feature";

    protected static final ConfluenceRpc rpc = ConfluenceRpc.newInstance(product.getProductInstance().getBaseUrl(), ConfluenceRpc.Version.V2_WITH_WIKI_MARKUP);

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                    newWebItemBean()
                        .withName(new I18nProperty("dark feature", FEATURE_FLAG))
                        .withKey(FEATURE_FLAG)
                        .withLocation("system.browse")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                            newSingleConditionBean()
                                    .withCondition("feature_flag")
                                    .withParam("key", FEATURE_FLAG_KEY)
                                    .build()
                        )
                        .build())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }

        rpc.logIn(ConnectTestUserFactory.sysadmin(product).confUser());
        rpc.getDarkFeaturesHelper().disableSiteFeature(FEATURE_FLAG_KEY);
    }

    @Test
    public void cannotSeeWithFeatureFlagUnset() throws Exception
    {
        login(ConnectTestUserFactory.basicUser(product));

        assertFalse("Web item should not be visible without feature flag set", connectPageOperations.existsWebItem(getModuleKey(FEATURE_FLAG)));
    }

    @Test
    public void canSeeWithFeatureFlagSet() throws Exception
    {
        TestUser user = ConnectTestUserFactory.basicUser(product);
        login(user);

        rpc.logIn(user.confUser());
        rpc.getDarkFeaturesHelper().enableSiteFeature(FEATURE_FLAG_KEY);

        assertFalse("Web item should be visible with feature flag enabled", connectPageOperations.existsWebItem(getModuleKey(FEATURE_FLAG)));
    }

    @Test
    public void cannotSeeWithFeatureFlagDisabled() throws Exception
    {
        TestUser user = ConnectTestUserFactory.basicUser(product);
        login(user);

        rpc.logIn(user.confUser());
        rpc.getDarkFeaturesHelper().enableSiteFeature(FEATURE_FLAG_KEY);
        rpc.getDarkFeaturesHelper().disableSiteFeature(FEATURE_FLAG_KEY);

        assertFalse("Web item should not be visible with feature flag disabled", connectPageOperations.existsWebItem(getModuleKey(FEATURE_FLAG)));
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }
}
