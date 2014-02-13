package it.com.atlassian.plugin.connect.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.rule.DisableDevMode;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
@RunWith (AtlassianPluginsTestRunner.class)
public class AddonValidationTest
{
    private static final Logger log = LoggerFactory.getLogger(AddonValidationTest.class);

    private static final String WEBHOOK_REQUIRING_READ_SCOPE = "page_created";
    private static final String WEBHOOK_REQUIRING_ADMIN_SCOPE = "user_created";

    @ClassRule
    public static final DisableDevMode disableDevMode = new DisableDevMode(); // TLS validation is disabled in dev mode

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ApplicationProperties applicationProperties;

    private final AtomicReference<Plugin> installedPlugin = new AtomicReference<Plugin>();

    public AddonValidationTest(TestPluginInstaller testPluginInstaller, final TestAuthenticator testAuthenticator, ApplicationProperties applicationProperties)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.applicationProperties = applicationProperties;
    }

    @BeforeClass
    public void oneTimeSetup() throws Exception
    {
        testAuthenticator.authenticateUser("admin");
    }

    @After
    public void tearDown() throws Exception
    {
        Plugin installed = installedPlugin.getAndSet(null);
        if (installed != null)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(installed);
            }
            catch (Exception e)
            {
                log.error("Failed to uninstall test plugin " + installed.getKey() + " during teardown.", e);
            }
        }
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithDefaultAuth()
    {
        return new ConnectAddonBeanBuilder()
                .withKey("ac-test-" + System.currentTimeMillis())
                .withBaseurl("https://example.com/");
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithAuth(AuthenticationType authenticationType)
    {
        return testBeanBuilderWithDefaultAuth().withAuthentication(AuthenticationBean.newAuthenticationBean()
                .withType(authenticationType).build());
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithAuthNone()
    {
        return testBeanBuilderWithDefaultAuth()
                .withAuthentication(AuthenticationBean.newAuthenticationBean()
                        .withType(AuthenticationType.NONE).build());
    }

    private void install(ConnectAddonBean addonBean) throws Exception
    {
        installedPlugin.set(testPluginInstaller.installPlugin(addonBean));
    }

    private void installExpectingUpmErrorCode(ConnectAddonBean addonBean, String errorCode) throws Exception
    {
        try
        {
            install(addonBean);
            fail("Expected " + PluginInstallException.class.getSimpleName() + " with code " + errorCode);
        }
        catch (PluginInstallException e)
        {
            assertEquals(errorCode, e.getCode().get());
        }
    }

    @Test
    public void testJwtAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwt().build();

        installExpectingUpmErrorCode(bean, "connect.install.error.auth.with.no.installed.callback");
    }

    @Test
    public void testDefaultAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithDefaultAuth().build(); // auth defaults to JWT

        installExpectingUpmErrorCode(bean, "connect.install.error.auth.with.no.installed.callback");
    }

    @Test
    public void testNoAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithNoAuth().build();
        install(bean);
    }

    @Test
    public void testJwtAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwt()
                .withBaseurl("http://example.com/no-tls")
                .build();

        installExpectingUpmErrorCode(bean, "connect.install.error.auth.with.no.tls");
    }

    @Test
    public void testNoAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithNoAuth()
                .withBaseurl("http://example.com/no-tls")
                .build();

        install(bean);
    }

    @Test
    public void testWebhookRequiringReadScopeWithNoReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuthNone()
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_READ_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        installExpectingUpmErrorCode(bean, "connect.install.error.missing.scope.READ");
    }

    @Test
    public void testWebhookRequiringReadScopeWithReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuthNone()
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_READ_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        install(bean);
    }

    @Test
    public void testWebhookRequiringReadScopeWithImpliedReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuthNone()
                .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_READ_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        install(bean);
    }

    @Test
    @Ignore("Currently all webhooks require only the READ scope")
    public void testWebhookRequiringAdminScopeWithNoAdminScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuthNone()
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_ADMIN_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        installExpectingUpmErrorCode(bean, "connect.install.error.missing.scope.ADMIN");
    }

    @Test
    @Ignore("Currently all webhooks require only the READ scope")
    public void testWebhookRequiringAdminScopeWithAdminScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuthNone()
                .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_ADMIN_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        install(bean);
    }

    @Test
    public void testJwtAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl("example.com").build(), "connect.install.error.auth.with.no.tls");
    }

    @Test
    public void testOAuthAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithAuth(AuthenticationType.OAUTH).withBaseurl("example.com").build(), "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testNoneAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithNoAuth().withBaseurl("example.com").build(), "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testJwtAuthenticationWithMissingBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl(null).build(), schemaValidationErrorCode());
    }

    @Test
    public void testJwtAuthenticationWithEmptyStringBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl("").build(), schemaValidationErrorCode());
    }

    @Test
    public void testJwtAuthenticationWithNonUriBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl("this is not a URI").build(), schemaValidationErrorCode());
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithNoAuth()
    {
        return testBeanBuilderWithAuth(AuthenticationType.NONE);
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithJwt()
    {
        return testBeanBuilderWithAuth(AuthenticationType.JWT);
    }

    private String schemaValidationErrorCode()
    {
        return "connect.install.error.remote.descriptor.validation." + applicationProperties.getDisplayName().toLowerCase();
    }
}
