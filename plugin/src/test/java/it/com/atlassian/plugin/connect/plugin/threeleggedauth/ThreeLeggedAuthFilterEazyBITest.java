package it.com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.threeleggedauth.ThreeLeggedAuthService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AtlassianPluginsTestRunner.class)
public class ThreeLeggedAuthFilterEazyBITest extends ThreeLeggedAuthFilterTestBase
{
    public ThreeLeggedAuthFilterEazyBITest(TestPluginInstaller testPluginInstaller,
                                           TestAuthenticator testAuthenticator,
                                           AddonTestFilterResults testFilterResults,
                                           JwtWriterFactory jwtWriterFactory,
                                           ConnectAddonRegistry connectAddonRegistry,
                                           ApplicationProperties applicationProperties,
                                           ThreeLeggedAuthService threeLeggedAuthService,
                                           ApplicationService applicationService,
                                           ApplicationManager applicationManager)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, jwtWriterFactory, connectAddonRegistry, applicationProperties, threeLeggedAuthService, applicationService, applicationManager);
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.READ;
    }

    @Test
    public void pleaseImplementMe()
    {
        /* suggested tests:
            - EazyBI can impersonate a valid user (200 response code, correct add-on and subject attributes, request is assigned to subject)
            - EazyBI cannot impersonate a non-existent user (401 or 403 response code)
            - EazyBI cannot impersonate an inactive user (same response code as above)
            - EazyBI can omit the subject claim and the request goes through ok but without impersonation (200 response code, correct add-on and subject attributes, request is assigned to add-on user)
                (for these see ThreeLeggedAuthFilterWithUserAgency in feature/ACDEV-1228-3-legged-auth)
            - A random other add-on can specify a subject for impersonation and the request goes through ok but without impersonation
            - A random other add-on can omit the subject claim and the request goes through ok but without impersonation (200 response code, correct add-on and subject attributes, request is assigned to add-on user)
                (see ThreeLeggedAuthFilterWithoutUserAgency in feature/ACDEV-1228-3-legged-auth)
         */
        throw new RuntimeException("not implemented!");
    }
}
