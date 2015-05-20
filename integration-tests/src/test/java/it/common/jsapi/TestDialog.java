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
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.servlet.condition.ParameterCapturingServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.modules.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDialog extends MultiProductWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AA";

    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_DIALOG_NAME = "BB";

    private static final String ADDON_GENERALPAGE_WEBITEM_DIALOG = "general-page-opening-webitem-dialog";
    private static final String ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG = "CC";

    private static final String ADDON_WEBITEM_DIALOG = "my-webitem-dialog";
    private static final String ADDON_WEBITEM_DIALOG_NAME = "DD";

    private static final String JWT_EXPIRY_DIALOG = "checkDialogJwtExpiry";
    private static final String JWT_EXPIRY_DIALOG_NAME = "JWTD";
    private static final String JWT_EXPIRY_INLINE_DIALOG = "checkInlineDialogJwtExpiry";
    private static final String JWT_EXPIRY_INLINE_DIALOG_NAME = "JWTID";

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_SERVLET = ConnectAppServlets.parameterCapturingDialogServlet();
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();
    public static final String DIALOG_WEB_ITEM_NAME = "EE";
    public static final String SIZE_TO_PARENT_DIALOG_WEB_ITEM_NAME = "SzP";

    private static ConnectRunner runner;

    private long lastIssuedAtTime;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT(INSTALL_HANDLER_SERVLET)
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME, null))
                                .withUrl("/pg")
                                .withKey(ADDON_GENERALPAGE)
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_DIALOG_NAME, null))
                                .withUrl("/my-dialog-url?myuserid={user.id}")
                                .withKey(ADDON_DIALOG)
                                .withLocation("none")
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, null))
                                .withUrl("/general-page")
                                .withKey(ADDON_GENERALPAGE_WEBITEM_DIALOG)
                                .withLocation(getGloballyVisibleLocation())
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
                                .withName(new I18nProperty(DIALOG_WEB_ITEM_NAME, null))
                                .withUrl("/rpd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey("sizeToParentDialog")
                                .withName(new I18nProperty(SIZE_TO_PARENT_DIALOG_WEB_ITEM_NAME, null))
                                .withUrl("/fsd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_DIALOG)
                                .withName(new I18nProperty(JWT_EXPIRY_DIALOG_NAME, null))
                                .withUrl(ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_INLINE_DIALOG)
                                .withName(new I18nProperty(JWT_EXPIRY_INLINE_DIALOG_NAME, null))
                                .withUrl(ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.inlineDialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
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
        login(testUserFactory.basicUser());
        HomePage homePage = product.visit(HomePage.class);
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
        verifyIframeURLHasVersionNumber(closeDialogPage);
    }

    @Test
    public void testLoadGeneralDialog()
    {
        login(testUserFactory.basicUser());
        HomePage homePage = product.visit(HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginDialog", DIALOG_WEB_ITEM_NAME, runner.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertThat(remotePluginTest.getLocation(), endsWith(homePage.getUrl()));

        // Exercise the dialog's submit button.
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest);
        assertFalse(dialog.wasSubmitted());
        dialog.submitAndWaitUntilSubmitted();
        dialog.submitAndWaitUntilHidden();
    }

    @Test
    public void testSizeToParentDoesNotWorkInDialog()
    {
        login(testUserFactory.basicUser());
        product.visit(HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "sizeToParentDialog", SIZE_TO_PARENT_DIALOG_WEB_ITEM_NAME, runner.getAddon().getKey());
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
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_INLINE_DIALOG, JWT_EXPIRY_INLINE_DIALOG_NAME);
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest, true);
        verifyIframeURLHasVersionNumber(dialog);
    }

    /**
     * Open the dialog twice and verify that the value of the iat claim, specified with second precision, is as expected.
     */
    private void verifyJwtIssuedAtTimeForDialog(String moduleKey, String moduleName, final boolean isInlineDialog) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        final JwtReaderFactory jwtReaderFactory = getJwtReaderFactory();
        
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(moduleKey, moduleName);

        // Checking the system time across two JVM's seems unreliable, so allow a considerable discrepancy
        final long timeBeforeClick = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(JwtConstants.TIME_CLAIM_LEEWAY_SECONDS);
        openAndCloseDialog(page, isInlineDialog);
        verifyIssuedAtTime(jwtReaderFactory, newIssuedAtTimeClaimVerifier(timeBeforeClick));

        openAndCloseDialog(page, isInlineDialog);
        verifyIssuedAtTime(jwtReaderFactory, newIssuedAtTimeClaimVerifier(lastIssuedAtTime));
    }

    private void verifyIssuedAtTime(JwtReaderFactory jwtReaderFactory, JwtClaimVerifier issuedAtTimeClaimVerifier) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        final Map<String,String> params = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertTrue("A JWT parameter should have been included in the request for dialog content", params.containsKey(JwtConstants.JWT_PARAM_NAME));
        final String jwt = params.get(JwtConstants.JWT_PARAM_NAME);
        final JwtReader jwtReader = jwtReaderFactory.getReader(jwt);
        Map<String, JwtClaimVerifier> verifiers = new HashMap<>(1);
        verifiers.put("iat", issuedAtTimeClaimVerifier);
        jwtReader.readAndVerify(jwt, verifiers); // will throw if the issued-at-time fails verification
    }

    private RemotePluginAwarePage goToPageWithLink(String dashedModuleKey, String moduleName)
    {
        product.visit(HomePage.class);
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
                    lastIssuedAtTime = claimDate.getTime();
                    if (lastIssuedAtTime < minimumIssueTime)
                    {
                        throw new JwtInvalidClaimException(String.format("Expecting the issued-at claim to have a value greater than or equal to [%d] but it was [%d]", minimumIssueTime, lastIssuedAtTime));
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
            dialog.cancelAndWaitUntilHidden();
        }
    }
}
