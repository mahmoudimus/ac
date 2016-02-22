package it.common.iframe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_PAGE_SERVLET = ConnectAppServlets.parameterCapturingServlet(ConnectAppServlets.simplePageServlet());
    private static final ParameterCapturingServlet PARAMETER_CAPTURING_DIALOG_SERVLET = ConnectAppServlets.parameterCapturingServlet(ConnectAppServlets.simpleDialogServlet());
    private static final ParameterCapturingServlet PARAMETER_CAPTURING_INLINE_DIALOG_SERVLET = ConnectAppServlets.parameterCapturingServlet(ConnectAppServlets.simpleInlineDialogServlet());
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();

    private static ConnectRunner runner;

    private NimbusJwtReaderFactory jwtReaderFactory;
    private long lastIssuedAtTime;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
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
    public static void stopConnectAddon() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Before
    public void createJwtReaderFactory()
    {
        final JwtIssuerSharedSecretService sharedSecretService = issuer -> INSTALL_HANDLER_SERVLET.getInstallPayload().getSharedSecret();

        final JwtIssuerValidator jwtIssuerValidator = issuer -> true;

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
    public void pageClicksGetsNewJwt() throws Exception
    {
        login(testUserFactory.basicUser());
        RemotePluginAwarePage page = goToPageWithLink(JWT_EXPIRY_PAGE_KEY);
        URL webItemUrl = new URL(page.findLinkElement().getAttribute("href"));

        long timeBeforeClick = getSystemTimeBeforeJwtIssue();
        doRequest(webItemUrl);

        verifyIssuedAtTime(timeBeforeClick, PARAMETER_CAPTURING_PAGE_SERVLET);

        // JWT token's sign time has one second precision.
        // We have to wait one second to be sure that token from next request is new.
        Thread.sleep(1000);

        doRequest(webItemUrl);
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
        page.clickAddonLink();
        RemoteInlineDialog inlineDialog = product.getPageBinder().bind(RemoteInlineDialog.class);
        verifyIframeURLHasVersionNumber(inlineDialog);
    }

    private void doRequest(final URL url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.connect();
        connection.getResponseCode(); // it waits for response
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
        return product.getPageBinder().bind(GeneralPage.class, dashedModuleKey, runner.getAddon().getKey());
    }

    private JwtClaimVerifier newIssuedAtTimeClaimVerifier(final long minimumIssueTime)
    {
        return claim -> {
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
        };
    }

    private long getSystemTimeBeforeJwtIssue()
    {
        // Checking the system time across two JVM's seems unreliable, so allow a considerable discrepancy
        return System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(JwtConstants.TIME_CLAIM_LEEWAY_SECONDS);
    }

    private void openAndCloseDialog(RemotePluginAwarePage page)
    {
        page.clickAddonLink();
        RemoteDialog dialog = product.getPageBinder().bind(RemoteDialog.class);
        dialog.cancelAndWaitUntilHidden();
    }

    private void openAndCloseInlineDialog(RemotePluginAwarePage page)
    {
        page.clickAddonLink();
        RemoteInlineDialog inlineDialog = product.getPageBinder().bind(RemoteInlineDialog.class);
        inlineDialog.hideAndWaitUntilHidden();
    }
}
