package it.com.atlassian.plugin.connect.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.rule.DevMode;
import it.com.atlassian.plugin.connect.rule.DisableDevMode;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
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

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ApplicationProperties applicationProperties;
    private final I18nResolver i18nResolver;

    @Rule
    public final DisableDevMode disableDevMode = new DisableDevMode(); // TLS validation is disabled in dev mode

    private final AtomicReference<Plugin> installedPlugin = new AtomicReference<Plugin>();

    public AddonValidationTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
            ApplicationProperties applicationProperties, I18nResolver i18nResolver)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.applicationProperties = applicationProperties;
        this.i18nResolver = i18nResolver;
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
                testPluginInstaller.uninstallAddon(installed);
            }
            catch (Exception e)
            {
                log.error("Failed to uninstall test plugin " + installed.getKey() + " during teardown.", e);
            }
        }
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithNoAuthSpecified()
    {
        return new ConnectAddonBeanBuilder()
                .withKey("ac-test-" + System.currentTimeMillis())
                .withModule("webItems", randomWebItemBean())
                .withBaseurl("https://example.com/");
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithAuth(AuthenticationType authenticationType)
    {
        return testBeanBuilderWithNoAuthSpecified().withAuthentication(AuthenticationBean.newAuthenticationBean()
                .withType(authenticationType).build());
    }

    private void install(ConnectAddonBean addonBean) throws Exception
    {
        installedPlugin.set(testPluginInstaller.installAddon(addonBean));
    }

    private void install(String jsonDescriptor) throws Exception
    {
        installedPlugin.set(testPluginInstaller.installAddon(jsonDescriptor));
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
            String actualCode = (e.getCode().isDefined()) ? e.getCode().get() : e.getMessage();
            
            assertEquals(errorCode, actualCode);
        }
    }

    private void installExpectingUpmErrorCode(String jsonDescriptor, String errorCode) throws Exception
    {
        try
        {
            install(jsonDescriptor);
            fail("Expected " + PluginInstallException.class.getSimpleName() + " with code " + errorCode);
        }
        catch (PluginInstallException e)
        {
            String actualCode = (e.getCode().isDefined()) ? e.getCode().get() : e.getMessage();
            
            assertEquals(errorCode, actualCode);
        }
    }

    @Test
    public void testJwtAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwt().build();

        installExpectingUpmErrorCode(bean, "connect.install.error.auth.with.no.installed.callback");
    }

    @Test
    public void testNoAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE).build();
        install(bean);
    }

    @Test
    public void testJwtAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwt()
                .withBaseurl("http://example.com/no-tls")
                .build();

        installExpectingUpmErrorCode(bean, "connect.install.error.base_url.no_tls");
    }

    @Test
    public void testNoAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE)
                .withBaseurl("http://example.com/no-tls")
                .build();

        installExpectingUpmErrorCode(bean, "connect.install.error.base_url.no_tls");
    }

    @Test
    public void testOAuthAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.OAUTH)
                .withBaseurl("http://example.com/no-tls")
                .build();

        installExpectingUpmErrorCode(bean, "connect.install.error.base_url.no_tls");
    }

    @Test
    public void testWebhookRequiringReadScopeWithNoReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE)
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_READ_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        installExpectingUpmErrorCode(bean, missingScopeErrorMessage(ScopeName.READ));
    }

    @Test
    public void testWebhookRequiringReadScopeWithReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE)
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
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE)
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
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE)
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_ADMIN_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        installExpectingUpmErrorCode(bean, missingScopeErrorMessage(ScopeName.ADMIN));
    }

    @Test
    @Ignore("Currently all webhooks require only the READ scope")
    public void testWebhookRequiringAdminScopeWithAdminScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithAuth(AuthenticationType.NONE)
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
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl("example.com").build(), "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testOAuthAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithAuth(AuthenticationType.OAUTH).withBaseurl("example.com").build(), "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testNoneAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithAuth(AuthenticationType.NONE).withBaseurl("example.com").build(), "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testJwtAuthenticationWithMissingBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl(null).build(), schemaValidationErrorMessage());
    }

    @Test
    public void testJwtAuthenticationWithEmptyStringBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl("").build(), schemaValidationErrorMessage());
    }

    @Test
    public void testJwtAuthenticationWithNonUriBaseUrl() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwt().withBaseurl("this is not a URI").build(), schemaValidationErrorMessage());
    }

    @Test
    @DevMode
    public void a404ResponseFromInstalledCallbackResultsInCorrespondingErrorCode() throws Exception
    {
        ConnectAddonBeanBuilder builder = testBeanBuilderWithJwt();
        ConnectAddonBean bean = builder
            .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(builder.getKey()))
            .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/status/404").build())
            .build();
        installExpectingUpmErrorCode(bean, "connect.install.error.remote.host.bad.response.404");
    }

    @Test
    public void aNonExistentDomainNameInInstalledCallbackResultsInCorrespondingErrorCode() throws Exception
    {
        installExpectingUpmErrorCode(testBeanBuilderWithJwtAndInstalledCallback().withBaseurl("https://does.not.exist").build(),
                i18nResolver.getText("connect.install.error.remote.host.bad.domain", "does.not.exist"));
    }

    @Test
    @DevMode
    public void installedCallbackTimingOutResultsInCorrespondingErrorCode() throws Exception
    {
        ConnectAddonBeanBuilder builder = testBeanBuilderWithJwt();
        ConnectAddonBean bean = builder
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(builder.getKey()))
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/timeout/60").build())
                .build();
        installExpectingUpmErrorCode(bean, i18nResolver.getText("connect.install.error.remote.host.timeout", bean.getBaseUrl() + bean.getLifecycle().getInstalled()));
    }

    @Test
    public void installedMalformedJSONDescriptorResultsInCorrespondingErrorCode() throws Exception
    {
        installExpectingUpmErrorCode(TestFileReader.readAddonTestFile("malformedDescriptor.json"), invalidDescriptorErrorMessage());
    }

    private ConnectAddonBeanBuilder testBeanBuilderWithJwtAndInstalledCallback()
    {
        return testBeanBuilderWithJwt()
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build());
    }

    private static ConnectAddonBeanBuilder testBeanBuilderWithJwt()
    {
        return testBeanBuilderWithAuth(AuthenticationType.JWT);
    }

    private String missingScopeErrorMessage(final ScopeName scope)
    {
        return i18nResolver.getText("connect.install.error.missing.scope", scope);
    }

    private String schemaValidationErrorMessage()
    {
        return i18nResolver.getText("connect.install.error.remote.descriptor.validation",
                applicationProperties.getDisplayName());
    }
    
    private String invalidDescriptorErrorMessage()
    {
        return i18nResolver.getText("connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
    }
}
