package it.common.jsapi;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.exception.JwtInvalidClaimException;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.jwt.reader.JwtReader;
import com.atlassian.jwt.reader.JwtReaderFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.servlet.condition.ParameterCapturingServlet;
import it.util.TestUser;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.modules.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestDialog extends ConnectWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC General Page";

    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_DIALOG_NAME = "my dialog";

    private static final String ADDON_GENERALPAGE_WEBITEM_DIALOG = "general-page-opening-webitem-dialog";
    private static final String ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG = "WebItem Dialog Opener Page";

    private static final String ADDON_WEBITEM_DIALOG = "my-webitem-dialog";
    private static final String ADDON_WEBITEM_DIALOG_NAME = "my webitem dialog";

    private static final String JWT_EXPIRY_DIALOG = "checkDialogJwtExpiry";
    private static final String JWT_EXPIRY_DIALOG_NAME = "check dialog JWT expiry";
    private static final String JWT_EXPIRY_INLINE_DIALOG = "checkInlineDialogJwtExpiry";
    private static final String JWT_EXPIRY_INLINE_DIALOG_NAME = "check inline dialog JWT expiry";

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_SERVLET = ConnectAppServlets.parameterCapturingDialogServlet();
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();

    private static ConnectRunner runner;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String productContextPath = product.getProductInstance().getContextPath().toLowerCase();
        String globallyVisibleLocation = productContextPath.contains("jira")
                ? "system.top.navigation.bar"
                : productContextPath.contains("wiki") || productContextPath.contains("confluence")
                ? "system.help/pages"
                : null;

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT(INSTALL_HANDLER_SERVLET)
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME, null))
                                .withUrl("/pg")
                                .withKey(ADDON_GENERALPAGE)
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_DIALOG_NAME, null))
                                .withUrl("/my-dialog-url?myuserid={user.id}")
                                .withKey(ADDON_DIALOG)
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, null))
                                .withUrl("/general-page")
                                .withKey(ADDON_GENERALPAGE_WEBITEM_DIALOG)
                                .build()
                )
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(ADDON_WEBITEM_DIALOG_NAME, null))
                                .withUrl("/my-webitem-dialog?myuserid={user.id}")
                                .withKey(ADDON_WEBITEM_DIALOG)
                                .withLocation("none")
                                .withContext(AddOnUrlContext.addon)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .build(),
                        newWebItemBean()
                                .withKey("remotePluginDialog")
                                .withName(new I18nProperty("Remotable Plugin app1 Dialog", null))
                                .withUrl("/rpd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newWebItemBean()
                                .withKey("sizeToParentDialog")
                                .withName(new I18nProperty("Size to parent dialog page", null))
                                .withUrl("/fsd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_DIALOG)
                                .withName(new I18nProperty(JWT_EXPIRY_DIALOG_NAME, null))
                                .withUrl(ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_INLINE_DIALOG)
                                .withName(new I18nProperty(JWT_EXPIRY_INLINE_DIALOG_NAME, null))
                                .withUrl(ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.inlineDialog)
                                        .build())
                                .withLocation(globallyVisibleLocation)
                                .build()
                )

                .addRoute("/pg", ConnectAppServlets.openDialogServlet())
                .addRoute("/my-dialog-url", ConnectAppServlets.closeDialogServlet())
                .addRoute("/general-page", ConnectAppServlets.openDialogServlet(ADDON_WEBITEM_DIALOG))
                .addRoute("/my-webitem-dialog", ConnectAppServlets.closeDialogServlet())
                .addRoute("/rpd", ConnectAppServlets.dialogServlet())
                .addRoute("/fsd", ConnectAppServlets.sizeToParentServlet())
                .addRoute(ParameterCapturingServlet.PARAMETER_CAPTURE_URL, ConnectAppServlets.parameterCapturingDialogServlet(PARAMETER_CAPTURING_SERVLET))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    /**
     * Tests opening a dialog by key from a general page with json descriptor
     */

    @Test
    public void testOpenCloseDialogKeyWithPrependedAddOnKey() throws Exception
    {
        testOpenAndCloseWithPrependedAddOnKey(ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME, ADDON_DIALOG);
    }

    @Test
    public void testOpenCloseDialogKey()
    {
        testOpenAndClose(ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME, ADDON_DIALOG);
    }

    @Test
    public void testWebItemDialogOpenByKeyWithPrependedAddOnKey() throws Exception
    {
        testOpenAndCloseWithPrependedAddOnKey(ADDON_GENERALPAGE_WEBITEM_DIALOG, ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testWebItemDialogOpenByKey() throws Exception
    {
        testOpenAndClose(ADDON_GENERALPAGE_WEBITEM_DIALOG, ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, ADDON_WEBITEM_DIALOG);
    }

    private void testOpenAndCloseWithPrependedAddOnKey(String pageKey, String pageName, String dialogKey)
    {
        testOpenAndClose(pageKey, pageName, ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), dialogKey));
    }

    private void testOpenAndClose(String pageKey, String pageName, String moduleKey)
    {
        loginAndVisit(TestUser.ADMIN, HomePage.class);
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class, pageKey, pageName, runner.getAddon().getKey());
        remotePage.clickAddOnLink();

        RemoteDialogOpeningPage dialogOpeningPage = bindDialogOpeningPage(ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), pageKey));
        RemoteCloseDialogPage closeDialogPage = bindCloseDialogPage(dialogOpeningPage, moduleKey);

        assertThatTheDialogHasTheCorrectProperties(closeDialogPage);
        assertEquals("test dialog close data", closeTheDialog(dialogOpeningPage, closeDialogPage));
    }

    private RemoteCloseDialogPage bindCloseDialogPage(RemoteDialogOpeningPage dialogOpeningPage, String moduleKey)
    {
        return dialogOpeningPage.openKey(moduleKey);
    }

    private RemoteDialogOpeningPage bindDialogOpeningPage(String moduleKey)
    {
        return product.getPageBinder().bind(RemoteDialogOpeningPage.class, moduleKey);
    }

    private String closeTheDialog(RemoteDialogOpeningPage dialogOpeningPage, RemoteCloseDialogPage closeDialogPage)
    {
        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        return dialogOpeningPage.waitForValue("dialog-close-data");
    }

    private void assertThatTheDialogHasTheCorrectProperties(RemoteCloseDialogPage closeDialogPage)
    {
        // check the dimensions are the same as those in the js (mustache file)
        assertThat(closeDialogPage.getIFrameSize().getWidth(), is(231));
        assertThat(closeDialogPage.getIFrameSize().getHeight(), is(356));
        assertTrue(closeDialogPage.getFromQueryString("ui-params").length() > 0);
        assertThat(closeDialogPage.getFromQueryString("user_id"), is("admin"));
        verifyIframeURLHasVersionNumber(closeDialogPage);
    }

    @Test
    public void testLoadGeneralDialog()
    {
        loginAndVisit(TestUser.BETTY, HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginDialog", "Remotable Plugin app1 Dialog", runner.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertNotNull(remotePluginTest.getFullName());
        Assert.assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(TestUser.BETTY.getUsername()));

        // Exercise the dialog's submit button.
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest);
        assertFalse(dialog.wasSubmitted());
        assertEquals(false, dialog.submit());

        assertTrue(dialog.wasSubmitted());
        assertEquals(true, dialog.submit());
    }

    @Test
    public void testSizeToParentDoesNotWorkInDialog()
    {
        loginAndVisit(TestUser.BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "sizeToParentDialog", "Size to parent dialog page", runner.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertTrue(remotePluginTest.isNotFullSize());
    }

    // because we issue a new JWT when it is clicked
    @Test
    public void dialogClickGetsNewJwt() throws JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException
    {
        verifyJwtIssuedAtTimeForDialog(JWT_EXPIRY_DIALOG, JWT_EXPIRY_DIALOG_NAME, false);
    }

    // because we issue a new JWT when it is clicked
    @Test
    public void inlineDialogClickGetsNewJwt() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtVerificationException, JwtParseException
    {
        verifyJwtIssuedAtTimeForDialog(JWT_EXPIRY_INLINE_DIALOG, JWT_EXPIRY_INLINE_DIALOG_NAME, true);
    }

    @Test
    public void verifyInlineDialogHasVersionNumber()
    {
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_INLINE_DIALOG, JWT_EXPIRY_INLINE_DIALOG_NAME);
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest, true);
        verifyIframeURLHasVersionNumber(dialog);
    }

    private void verifyJwtIssuedAtTimeForDialog(String moduleKey, String moduleName, final boolean isInlineDialog) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        final JwtReaderFactory jwtReaderFactory = getJwtReaderFactory();

        RemotePluginAwarePage page = goToPageWithLink(moduleKey, moduleName);

        clickAndVerifyIssuedAtTime(jwtReaderFactory, page, isInlineDialog);
        clickAndVerifyIssuedAtTime(jwtReaderFactory, page, isInlineDialog); // clicking multiple times should result in a new JWT on subsequent clicks
    }

    private void sleepForAtLeast5Seconds()
    {
        sleepUntil(System.currentTimeMillis() + 5000);
    }

    private void sleepUntil(final long wakeTimeMillis)
    {
        final long millisToSleep = wakeTimeMillis - System.currentTimeMillis();

        if (millisToSleep > 0)
        {
            try
            {
                Thread.sleep(millisToSleep);
            }
            catch (InterruptedException e)
            {
                sleepUntil(wakeTimeMillis);
            }
        }
    }

    private void clickAndVerifyIssuedAtTime(JwtReaderFactory jwtReaderFactory, RemotePluginAwarePage page, final boolean isInlineDialog) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        final long timeBeforeClick = System.currentTimeMillis();
        sleepForAtLeast5Seconds(); // because the JWT "iat" claim is specified in seconds there is no way to differentiate between "now" and "now + a few milliseconds"
        openAndCloseDialog(page, isInlineDialog);
        verifyIssuedAtTime(jwtReaderFactory, timeBeforeClick);
    }

    private void verifyIssuedAtTime(JwtReaderFactory jwtReaderFactory, long timeBeforeClick) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        final Map<String,String> params = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertTrue("A JWT parameter should have been included in the request for dialog content", params.containsKey(JwtConstants.JWT_PARAM_NAME));
        final String jwt = params.get(JwtConstants.JWT_PARAM_NAME);
        final JwtReader jwtReader = jwtReaderFactory.getReader(jwt);
        Map<String, JwtClaimVerifier> verifiers = new HashMap<String, JwtClaimVerifier>(1);
        verifiers.put("iat", newIssuedAtTimeClaimVerifier(timeBeforeClick));
        jwtReader.read(jwt, verifiers); // will throw if the issued-at-time fails verification
    }

    private RemotePluginAwarePage goToPageWithLink(String dashedModuleKey, String moduleName)
    {
        loginAndVisit(TestUser.ADMIN, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, dashedModuleKey, moduleName, runner.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());

        return page;
    }

    private JwtReaderFactory getJwtReaderFactory()
    {
        final JwtIssuerSharedSecretService sharedSecretService = new JwtIssuerSharedSecretService()
        {
            @Override
            public String getSharedSecret(String issuer) throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
            {
                return INSTALL_HANDLER_SERVLET.getInstallPayload().getSharedSecret();
            }
        };

        final JwtIssuerValidator jwtIssuerValidator = new JwtIssuerValidator()
        {
            @Override
            public boolean isValid(String issuer)
            {
                return true;
            }
        };

        return new NimbusJwtReaderFactory(jwtIssuerValidator, sharedSecretService);
    }

    private JwtClaimVerifier newIssuedAtTimeClaimVerifier(final long minimumIssueTime)
    {
        return new JwtClaimVerifier()
        {
            @Override
            public void verify(@Nonnull Object claim) throws JwtVerificationException, JwtParseException
            {
                if (claim instanceof Date)
                {
                    Date claimDate = (Date) claim;

                    if (claimDate.getTime() < minimumIssueTime)
                    {
                        throw new JwtInvalidClaimException(String.format("Expecting the issued-at claim to have a value greater than or equal to [%d] but it was [%d]", minimumIssueTime, claimDate.getTime()));
                    }
                }
                else
                {
                    throw new JwtInvalidClaimException(String.format("Expecting the issued-at claim to be a Date but it was a %s: [%s]", claim.getClass().getSimpleName(), claim));
                }
            }
        };
    }

    private void openAndCloseDialog(RemotePluginAwarePage page, final boolean isInlineDialog)
    {
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest, isInlineDialog);

        if (dialog.hasChrome())
        {
            dialog.cancel();
        }
    }
}
