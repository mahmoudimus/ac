package it.common.iframe;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

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
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteInlineDialog;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static org.junit.Assert.assertTrue;

public class TestWebItemJwtReissue extends MultiProductWebDriverTestBase
{

    private static final String JWT_EXPIRY_PAGE_KEY = "checkPageJwtExpiry";
    private static final String JWT_EXPIRY_DIALOG_KEY = "checkDialogJwtExpiry";
    private static final String JWT_EXPIRY_INLINE_DIALOG_KEY = "checkInlineDialogJwtExpiry";

    private static final String PARAMETER_CAPTURE_PAGE_PATH = "/pcp";
    private static final String PARAMETER_CAPTURE_DIALOG_PATH = "/pcd";
    private static final String PARAMETER_CAPTURE_INLINE_DIALOG_PATH = "/pcid";

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_PAGE_SERVLET = ConnectAppServlets.parameterCapturingPageServlet();
    private static final ParameterCapturingServlet PARAMETER_CAPTURING_DIALOG_SERVLET = ConnectAppServlets.parameterCapturingDialogServlet();
    private static final ParameterCapturingServlet PARAMETER_CAPTURING_INLINE_DIALOG_SERVLET = ConnectAppServlets.parameterCapturingInlineDialogServlet();
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();

    private static ConnectRunner runner;

    private NimbusJwtReaderFactory jwtReaderFactory;
    private long lastIssuedAtTime;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT(INSTALL_HANDLER_SERVLET)
                .addModules("webItems",
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_PAGE_KEY)
                                .withName(new I18nProperty("JWTP", null))
                                .withUrl(PARAMETER_CAPTURE_PAGE_PATH)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.page)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_DIALOG_KEY)
                                .withName(new I18nProperty("JWTD", null))
                                .withUrl(PARAMETER_CAPTURE_DIALOG_PATH)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_INLINE_DIALOG_KEY)
                                .withName(new I18nProperty("JWTID", null))
                                .withUrl(PARAMETER_CAPTURE_INLINE_DIALOG_PATH)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.inlineDialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )
                .addRoute(PARAMETER_CAPTURE_PAGE_PATH, ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_PAGE_SERVLET))
                .addRoute(PARAMETER_CAPTURE_DIALOG_PATH, ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_DIALOG_SERVLET))
                .addRoute(PARAMETER_CAPTURE_INLINE_DIALOG_PATH, ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_INLINE_DIALOG_SERVLET))
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

    @Before
    public void createJwtReaderFactory()
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

        jwtReaderFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, sharedSecretService);
    }

    // because we issue a new JWT when it is clicked
    @Test
    public void dialogClickGetsNewJwt() throws JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException
    {
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_DIALOG_KEY);

        final long timeBeforeClick = getSystemTimeBeforeJwtIssue();
        openAndCloseDialog(page);
        verifyIssuedAtTime(timeBeforeClick, PARAMETER_CAPTURING_DIALOG_SERVLET);

        openAndCloseDialog(page);
        verifyIssuedAtTime(lastIssuedAtTime, PARAMETER_CAPTURING_DIALOG_SERVLET);
    }

    @Test
    public void pageClicksGetsNewJwt()
            throws JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException
    {
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_PAGE_KEY);

        final long timeBeforeClick = getSystemTimeBeforeJwtIssue();
        openAndClosePage(page);
        verifyIssuedAtTime(timeBeforeClick, PARAMETER_CAPTURING_PAGE_SERVLET);

        openAndClosePage(page);
        verifyIssuedAtTime(lastIssuedAtTime, PARAMETER_CAPTURING_PAGE_SERVLET);
    }

    // because we issue a new JWT when it is clicked
    @Test
    @Ignore
    public void inlineDialogClickGetsNewJwt() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtVerificationException, JwtParseException
    {
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_INLINE_DIALOG_KEY);

        final long timeBeforeClick = getSystemTimeBeforeJwtIssue();
        openAndCloseInlineDialog(page);
        verifyIssuedAtTime(timeBeforeClick, PARAMETER_CAPTURING_INLINE_DIALOG_SERVLET);

        openAndCloseInlineDialog(page);
        verifyIssuedAtTime(lastIssuedAtTime, PARAMETER_CAPTURING_INLINE_DIALOG_SERVLET);
    }

    @Test
    public void verifyInlineDialogHasVersionNumber()
    {
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_INLINE_DIALOG_KEY);
        page.clickAddOnLink();
        RemoteInlineDialog inlineDialog = product.getPageBinder().bind(RemoteInlineDialog.class);
        verifyIframeURLHasVersionNumber(inlineDialog);
    }

    private String getJwtFromParameterCapturingServlet(ParameterCapturingServlet parameterCapturingServlet)
    {
        final Map<String,String> params = parameterCapturingServlet.getParamsFromLastRequest();
        assertTrue("A JWT parameter should have been included in the request for dialog content", params.containsKey(JwtConstants.JWT_PARAM_NAME));
        return params.get(JwtConstants.JWT_PARAM_NAME);
    }

    private void verifyIssuedAtTime(long minimumIssuedTime, ParameterCapturingServlet parameterCapturingServlet) throws JwtUnknownIssuerException, JwtParseException, JwtIssuerLacksSharedSecretException, JwtVerificationException
    {
        String jwt = getJwtFromParameterCapturingServlet(parameterCapturingServlet);
        JwtClaimVerifier issuedAtTimeClaimVerifier = newIssuedAtTimeClaimVerifier(minimumIssuedTime);
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

    private long getSystemTimeBeforeJwtIssue()
    {
        // Checking the system time across two JVM's seems unreliable, so allow a considerable discrepancy
        return System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(JwtConstants.TIME_CLAIM_LEEWAY_SECONDS);
    }

    private void openAndCloseDialog(RemotePluginAwarePage page)
    {
        page.clickAddOnLink();
        RemoteDialog dialog = product.getPageBinder().bind(RemoteDialog.class);
        dialog.cancelAndWaitUntilHidden();
    }

    private void openAndCloseInlineDialog(RemotePluginAwarePage page)
    {
        page.clickAddOnLink();
        RemoteInlineDialog inlineDialog = product.getPageBinder().bind(RemoteInlineDialog.class);
        inlineDialog.hideAndWaitUntilHidden();
    }

    private void openAndClosePage(RemotePluginAwarePage page)
    {
        page.clickAddOnLinkWithoutBinding();
        login(testUserFactory.basicUser());
    }
}
