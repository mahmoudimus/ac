package it.com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.util.io.TestFileReader;
import it.com.atlassian.plugin.connect.util.rule.DevMode;
import it.com.atlassian.plugin.connect.util.rule.DisableDevMode;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

@RunWith (AtlassianPluginsTestRunner.class)
public class AddonValidationTest
{
    private static final Logger log = LoggerFactory.getLogger(AddonValidationTest.class);

    private static final String WEBHOOK_REQUIRING_READ_SCOPE = "page_created";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ApplicationProperties applicationProperties;

    @Rule
    public final DisableDevMode disableDevMode = new DisableDevMode(); // TLS validation is disabled in dev mode

    private final AtomicReference<Plugin> installedPlugin = new AtomicReference<Plugin>();

    public AddonValidationTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
            ApplicationProperties applicationProperties)
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
                testPluginInstaller.uninstallAddon(installed);
            }
            catch (Exception e)
            {
                log.error("Failed to uninstall test plugin " + installed.getKey() + " during teardown.", e);
            }
        }
    }

    @Test
    public void testJwtAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwt().build();

        assertInstallationFailsWithMessage(bean, "connect.install.error.auth.with.no.installed.callback");
    }

    @Test
    public void testNoAuthenticationWithNoInstalledCallback() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithoutAuthentication().build();
        install(bean);
    }

    @Test
    public void testJwtAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwtAndInstalledCallback()
                .withBaseurl("http://example.com/no-tls")
                .build();

        assertInstallationFailsWithMessage(bean, "connect.install.error.base_url.no_tls");
    }

    @Test
    public void testNoAuthenticationWithNoTls() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithoutAuthentication()
                .withBaseurl("http://example.com/no-tls")
                .build();

        assertInstallationFailsWithMessage(bean, "connect.install.error.base_url.no_tls");
    }

    @Test
    public void testWebhookRequiringReadScopeWithNoReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithoutAuthentication()
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_READ_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        assertInstallationFailsWithMessage(bean, "connect.install.error.missing.scope", ScopeName.READ);
    }

    @Test
    public void testWebhookRequiringReadScopeWithReadScope() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithoutAuthentication()
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
        ConnectAddonBean bean = testBeanBuilderWithoutAuthentication()
                .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                .withModule("webhooks", WebHookModuleBean.newWebHookBean()
                        .withEvent(WEBHOOK_REQUIRING_READ_SCOPE)
                        .withUrl("/hook")
                        .build())
                .build();

        install(bean);
    }

    @Test
    public void shouldFailInstallationWithGeneralMessageForInvalidConditionParameters() throws Exception
    {
        ConnectAddonBean addon = testBeanBuilderWithoutAuthentication()
                .withModule("webItems", newWebItemBean()
                        .withKey("invalid-condition-item")
                        .withUrl("/")
                        .withName(new I18nProperty("Invalid Condition Item", null))
                        .withConditions(newSingleConditionBean().withCondition("feature_flag").build())
                        .withLocation("some-location")
                        .build())
                .build();
        assertInstallationFailsWithMessage(addon, "connect.install.error.invalid.condition.parameters", "feature_flag", "Parameter 'featureKey' is mandatory.");
    }

    @Test
    public void testJwtAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        ConnectAddonBean bean = testBeanBuilderWithJwtAndInstalledCallback()
                .withBaseurl("example.com")
                .build();
        assertInstallationFailsWithMessage(bean, "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testNoneAuthenticationWithSchemelessBaseUrl() throws Exception
    {
        assertInstallationFailsWithMessage(testBeanBuilderWithoutAuthentication().withBaseurl("example.com").build(), "connect.install.error.base_url.no_scheme");
    }

    @Test
    public void testJwtAuthenticationWithMissingBaseUrl() throws Exception
    {
        assertInstallationFailsWithMessage(testBeanBuilderWithJwt().withBaseurl(null).build(),
                "connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
    }

    @Test
    public void testJwtAuthenticationWithEmptyStringBaseUrl() throws Exception
    {
        assertInstallationFailsWithMessage(testBeanBuilderWithJwt().withBaseurl("").build(),
                "connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
    }

    @Test
    public void testJwtAuthenticationWithNonUriBaseUrl() throws Exception
    {
        assertInstallationFailsWithMessage(testBeanBuilderWithJwt().withBaseurl("this is not a URI").build(),
                "connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
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
        assertInstallationFailsWithMessage(bean, "connect.install.error.remote.host.bad.response.404");
    }

    @Test
    @DevMode
    public void a503ResponseFromInstalledCallbackResultsInCorrespondingErrorCode() throws Exception
    {
        ConnectAddonBeanBuilder builder = testBeanBuilderWithJwt();
        ConnectAddonBean bean = builder
            .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(builder.getKey()))
            .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/status/503").build())
            .build();
        assertInstallationFailsWithMessage(bean, "connect.install.error.remote.host.bad.response.503");
    }

    @Test
    public void aNonExistentDomainNameInInstalledCallbackResultsInCorrespondingErrorCode() throws Exception
    {
        assertInstallationFailsWithMessage(testBeanBuilderWithJwtAndInstalledCallback().withBaseurl("https://does.not.exist").build(),
                "connect.install.error.remote.host.bad.domain", "does.not.exist");
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
        assertInstallationFailsWithMessage(bean, "connect.install.error.remote.host.timeout", bean.getBaseUrl() + bean.getLifecycle().getInstalled());
    }

    @Test
    public void shouldFailInstallationWithDetailedMessageForMalformedDescriptor() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("malformedDescriptor.json"),
                "connect.invalid.descriptor.malformed.json",
                "Unexpected character ('\"' (code 34)): was expecting comma to separate OBJECT entries\n at [Source: ; line: 6, column: 6]");
    }

    @Test
    public void shouldFailInstallationWithGeneralMessageForInvalidShallowDescriptor() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("invalidGenericDescriptor.json"),
                "connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
    }

    @Test
    @DevMode
    public void shouldFailInstallationWithDetailedMessageForInvalidShallowDescriptorInDevMode() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("invalidGenericDescriptor.json"),
                "connect.install.error.remote.descriptor.validation.dev", "<ul><li>: object has missing required properties ([&quot;authentication&quot;])</ul>");
    }

    @Test
    @DevMode
    public void shouldFailInstallationWithGeneralMessageForDescriptorWithNonObjectModuleList() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("descriptorWithNonObjectModuleList.json"),
                "connect.install.error.remote.descriptor.validation.dev", "<ul><li>/modules: instance type (boolean) does not match any allowed primitive type (allowed: [&quot;object&quot;])</ul>");
    }

    @Test
    public void shouldFailInstallationWithGeneralMessageForDescriptorWithInvalidModuleType() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("descriptorWithUnknownModuleType.json"),
                "connect.install.error.unknown.module", "unknownModuleType");
    }

    @Test
    public void shouldFailInstallationWithMessageForInvalidModuleDescriptor() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("webitem/invalidStylesWebItemTest.json"),
                "connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
    }

    @Test
    @DevMode
    public void shouldFailInstallationWithDetailedMessageForInvalidModuleDescriptorInDevMode() throws Exception
    {
        assertInstallationFailsWithMessage(TestFileReader.readAddonTestFile("webitem/invalidStylesWebItemTest.json"),
                "connect.install.error.remote.descriptor.validation.dev",
                "<ul><li>/webItems/0/styleClasses/0: ECMA 262 regex &quot;^[_a-zA-Z]+[_a-zA-Z0-9-]*$&quot;" +
                        " does not match input string &quot;webit%22&quot; &gt;&lt;script&gt;alert(1);&lt;/script&gt;&quot;" +
                        "<li>/webItems/0/styleClasses/1: ECMA 262 regex &quot;^[_a-zA-Z]+[_a-zA-Z0-9-]*$&quot;" +
                        " does not match input string &quot;webit%22%20onerror%22javascript:alert(1);%20&quot;</ul>");
    }

    private ConnectAddonBeanBuilder testBeanBuilderWithNoAuthSpecified()
    {
        return new ConnectAddonBeanBuilder()
                .withKey("ac-test-" + AddonUtil.randomPluginKey())
                .withBaseurl("https://example.com/");
    }

    private ConnectAddonBeanBuilder testBeanBuilderWithAuth(AuthenticationType authenticationType)
    {
        return testBeanBuilderWithNoAuthSpecified().withAuthentication(AuthenticationBean.newAuthenticationBean()
                .withType(authenticationType).build());
    }

    private ConnectAddonBeanBuilder testBeanBuilderWithoutAuthentication()
    {
        return testBeanBuilderWithAuth(AuthenticationType.NONE);
    }

    private ConnectAddonBeanBuilder testBeanBuilderWithJwt()
    {
        return testBeanBuilderWithAuth(AuthenticationType.JWT);
    }

    private ConnectAddonBeanBuilder testBeanBuilderWithJwtAndInstalledCallback()
    {
        return testBeanBuilderWithJwt()
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build());
    }

    private void install(ConnectAddonBean addonBean) throws Exception
    {
        installedPlugin.set(testPluginInstaller.installAddon(addonBean));
    }

    private void install(String jsonDescriptor) throws Exception
    {
        installedPlugin.set(testPluginInstaller.installAddon(jsonDescriptor));
    }

    private void assertInstallationFailsWithMessage(ConnectAddonBean addonBean, String i18nKey, Serializable... i18nParameters) throws Exception
    {
        try
        {
            install(addonBean);
            fail("Expected " + PluginInstallException.class.getSimpleName() + " with code " + i18nKey);
        }
        catch (PluginInstallException e)
        {
            assertPluginInstallExceptionProperties(e, i18nKey, i18nParameters);
        }
    }

    private void assertInstallationFailsWithMessage(String jsonDescriptor, String i18nKey, Serializable... i18nParameters) throws Exception
    {
        try
        {
            install(jsonDescriptor);
            fail("Expected " + PluginInstallException.class.getSimpleName() + " with i18n key " + i18nKey);
        }
        catch (PluginInstallException e)
        {
            assertPluginInstallExceptionProperties(e, i18nKey, i18nParameters);
        }
    }

    private void assertPluginInstallExceptionProperties(PluginInstallException e, String i18nKey, Serializable... i18nParameters)
    {
        assertThat(String.format("No i18n properties defined for exception %s", e), e.getI18nMessageProperties().isDefined());
        Pair<String, Serializable[]> i18nMessageProperties = e.getI18nMessageProperties().get();
        assertThat(i18nMessageProperties.first(), equalTo(i18nKey));
        assertThat(i18nMessageProperties.second(), equalTo(i18nParameters));
    }
}
