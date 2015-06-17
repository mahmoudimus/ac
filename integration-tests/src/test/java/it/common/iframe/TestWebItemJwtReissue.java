package it.common.iframe;

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
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
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

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.modules.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static org.junit.Assert.assertTrue;

public class TestWebItemJwtReissue extends MultiProductWebDriverTestBase
{

    private static final String JWT_EXPIRY_DIALOG = "checkDialogJwtExpiry";
    private static final String JWT_EXPIRY_INLINE_DIALOG = "checkInlineDialogJwtExpiry";

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_SERVLET = ConnectAppServlets.parameterCapturingDialogServlet();
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();

    private static ConnectRunner runner;

    private long lastIssuedAtTime;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT(INSTALL_HANDLER_SERVLET)
                .addModules("webItems",
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_DIALOG)
                                .withName(new I18nProperty("JWTD", null))
                                .withUrl(ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_INLINE_DIALOG)
                                .withName(new I18nProperty("JWTID", null))
                                .withUrl(ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.inlineDialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )
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

    // because we issue a new JWT when it is clicked
    @Test
    public void dialogClickGetsNewJwt() throws JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException
    {
        verifyJwtIssuedAtTimeForDialog(JWT_EXPIRY_DIALOG, false);
    }

    // because we issue a new JWT when it is clicked
    @Test
    public void inlineDialogClickGetsNewJwt() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtVerificationException, JwtParseException
    {
        verifyJwtIssuedAtTimeForDialog(JWT_EXPIRY_INLINE_DIALOG, true);
    }

    @Test
    public void verifyInlineDialogHasVersionNumber()
    {
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_INLINE_DIALOG);
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest, true);
        verifyIframeURLHasVersionNumber(dialog);
    }

    /**
     * Open the dialog twice and verify that the value of the iat claim, specified with second precision, is as expected.
     */
    private void verifyJwtIssuedAtTimeForDialog(String moduleKey, final boolean isInlineDialog) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        final JwtReaderFactory jwtReaderFactory = getJwtReaderFactory();

        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(moduleKey);

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

    private RemotePluginAwarePage goToPageWithLink(String dashedModuleKey)
    {
        product.visit(HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, dashedModuleKey, runner.getAddon().getKey());

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
