package it.com.atlassian.plugin.connect.installer;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.user.UserManager;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleAuthNoneTest extends AbstractAddonLifecycleTest
{
    protected AddonLifecycleAuthNoneTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults testFilterResults, ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService, UserManager userManager, ApplicationService applicationService, ApplicationManager applicationManager)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, connectApplinkManager, connectAddOnUserService, userManager, applicationService, applicationManager);
    }

    @Override
    protected boolean signCallbacksWithJwt()
    {
        return false;
    }

    @BeforeClass
    public void setup() throws Exception
    {
        testAuthenticator.authenticateUser("admin");
        initBeans(newAuthenticationBean().withType(AuthenticationType.NONE).build());
    }
}
