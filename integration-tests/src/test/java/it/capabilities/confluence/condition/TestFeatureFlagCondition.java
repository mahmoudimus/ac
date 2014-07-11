package it.capabilities.confluence.condition;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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

        rpc.logIn(User.ADMIN);
        rpc.getDarkFeaturesHelper().disableSiteFeature(FEATURE_FLAG_KEY);
    }

    @Test
    public void cannotSeeWithFeatureFlagUnset() throws Exception
    {
        loginAsAdmin();

        assertFalse("Web item should not be visible without feature flag set", connectPageOperations.existsWebItem(getModuleKey(FEATURE_FLAG)));
    }

    @Test
    public void canSeeWithFeatureFlagSet() throws Exception
    {
        loginAsAdmin();

        rpc.logIn(User.ADMIN);
        rpc.getDarkFeaturesHelper().enableSiteFeature(FEATURE_FLAG_KEY);

        assertFalse("Web item should be visible with feature flag enabled", connectPageOperations.existsWebItem(getModuleKey(FEATURE_FLAG)));
    }

    @Test
    public void cannotSeeWithFeatureFlagDisabled() throws Exception
    {
        loginAsAdmin();

        rpc.logIn(User.ADMIN);
        rpc.getDarkFeaturesHelper().enableSiteFeature(FEATURE_FLAG_KEY);
        rpc.getDarkFeaturesHelper().disableSiteFeature(FEATURE_FLAG_KEY);

        assertFalse("Web item should not be visible with feature flag disabled", connectPageOperations.existsWebItem(getModuleKey(FEATURE_FLAG)));
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }
}
