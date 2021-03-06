package it.com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectCrowdService;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.auth.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleAuthNoneTest extends AbstractAddonLifecycleTest {
    protected AddonLifecycleAuthNoneTest(TestPluginInstaller testPluginInstaller,
                                         TestAuthenticator testAuthenticator,
                                         AddonTestFilterResults testFilterResults,
                                         ConnectApplinkManager connectApplinkManager,
                                         ConnectCrowdService connectCrowdService,
                                         UserManager userManager,
                                         ApplicationService applicationService,
                                         ApplicationManager applicationManager,
                                         DarkFeatureManager darkFeatureManager,
                                         ConnectAddonRegistry connectAddonRegistry) {
        super(testPluginInstaller, testAuthenticator, testFilterResults, connectApplinkManager, connectCrowdService, userManager, applicationService, applicationManager, darkFeatureManager, connectAddonRegistry);
    }

    @Override
    protected boolean signCallbacksWithJwt() {
        return false;
    }

    @Before
    public void setup() throws Exception {
        testAuthenticator.authenticateUser("admin");
        initBeans(newAuthenticationBean().withType(AuthenticationType.NONE).build());
    }
}
