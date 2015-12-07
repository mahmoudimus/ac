package it.common.item;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.exception.JwtInvalidClaimException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.jwt.reader.JwtReader;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePageUtil;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.google.common.collect.ImmutableMap;
import it.common.MultiProductWebDriverTestBase;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.ws.rs.core.UriBuilder;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class RedirectServletTest extends MultiProductWebDriverTestBase
{
    private static final String JWT_EXPIRY_PAGE_KEY = "checkPageJwtExpiry";
    private static final String ABSOLUTE_PAGE_KEY = "absolutePage";
    private static final String PARAMETER_CAPTURE_PAGE_PATH = "/pcp";
    private static final String ABSOLUTE_URL = "http://example.com";
    private static final InstallHandlerServlet INSTALL_HANDLER_SERVLET = ConnectAppServlets.installHandlerServlet();

    private final String baseUrl = product.getProductInstance().getBaseUrl();
    private final String addOnKey = AddonTestUtils.randomAddOnKey();
    private ConnectRunner runner;

    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Before
    public void setUp() throws Exception
    {
        WebItemTargetBean pageTarget = newWebItemTargetBean()
                .withType(WebItemTargetType.page)
                .build();
        runner = new ConnectRunner(baseUrl, addOnKey)
                .addJWT(INSTALL_HANDLER_SERVLET)
                .addModules("webItems",
                        newWebItemBean()
                                .withKey(JWT_EXPIRY_PAGE_KEY)
                                .withName(new I18nProperty("JWTP", null))
                                .withUrl(PARAMETER_CAPTURE_PAGE_PATH + "?test-value={testParam}")
                                .withTarget(pageTarget)
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(ABSOLUTE_PAGE_KEY)
                                .withName(new I18nProperty("Absolute", null))
                                .withUrl(ABSOLUTE_URL)
                                .withTarget(pageTarget)
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )
                .start();
    }

    @After
    public void tearDown() throws Exception
    {
        runner.stopAndUninstall();
    }

    @Test
    public void shouldReturnRedirectionToAddOnServer() throws Exception
    {
        String addOnPageUrl = runner.getAddon().getBaseUrl() + PARAMETER_CAPTURE_PAGE_PATH;

        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, JWT_EXPIRY_PAGE_KEY));
        String url =  response.getHeaderField("Location");

        assertThat(url, Matchers.startsWith(addOnPageUrl));
    }

    @Test
    public void shouldSignRedirectionWithFreshJwtToken() throws Exception
    {
        HttpURLConnection response1 = doRedirectRequest(getPathToRedirectServlet(addOnKey, JWT_EXPIRY_PAGE_KEY));

        long timeBeforeClick = getSystemTimeBeforeJwtIssue();
        long claimDate1 = getClaimDate(response1);
        assertThat(claimDate1, greaterThan(timeBeforeClick));

        // JWT token's sign time has one second precision.
        // We have to wait one second to be sure that token from next request is new.
        Thread.sleep(1000);

        HttpURLConnection response2 = doRedirectRequest(getPathToRedirectServlet(addOnKey, JWT_EXPIRY_PAGE_KEY));
        long claimDate2 = getClaimDate(response2);
        assertThat(claimDate2, greaterThan(claimDate1));
    }

    @Test
    public void shouldResolveUriParamsForRedirection() throws Exception
    {
        String testValue = "10000";
        URI redirectUri = UriBuilder.fromUri(getPathToRedirectServlet(addOnKey, JWT_EXPIRY_PAGE_KEY)).queryParam("testParam", testValue).build();
        HttpURLConnection response = doRedirectRequest(redirectUri);
        String testIdFromUrlParam = getQueryParam(response, "test-value");
        assertThat(testIdFromUrlParam, is(testValue));
    }

    @Test
    public void shouldReturnCachedResponseWithTemporaryRedirect() throws Exception
    {
        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, JWT_EXPIRY_PAGE_KEY));
        assertThat(response.getResponseCode(), is(HttpStatus.SC_TEMPORARY_REDIRECT));

        assertThat(response.getHeaderField("cache-control"), allOf(
                not(isEmptyOrNullString()),
                not(containsString("no-cache")),
                not(containsString("no-store")),
                not(containsString("max-age=0"))
        ));
    }

    @Test
    public void shouldReturnNotFoundIfModuleKeyDoesNotBelongsToModuleThatNeedsRedirection() throws Exception
    {
        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, ABSOLUTE_PAGE_KEY));
        assertThat(response.getResponseCode(), is(HttpStatus.SC_NOT_FOUND));
    }

    @Test
    public void shouldReturnNotFoundIfAddonKeyIsNotValid() throws Exception
    {
        String addOnKey = "not-existing-add-onn";
        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, JWT_EXPIRY_PAGE_KEY));
        assertThat(response.getResponseCode(), is(HttpStatus.SC_NOT_FOUND));
    }

    private HttpURLConnection doRedirectRequest(final URI uri) throws IOException
    {
        return (HttpURLConnection) uri.toURL().openConnection();
    }

    private URI getPathToRedirectServlet(final String addOnKey, final String jwtExpiryPageKey)
    {
        return UriBuilder.fromPath(baseUrl).path(RedirectServletPath.forModule(addOnKey, jwtExpiryPageKey)).build();
    }

    private long getClaimDate(HttpURLConnection response) throws Exception
    {
        final JwtIssuerSharedSecretService sharedSecretService = issuer -> INSTALL_HANDLER_SERVLET.getInstallPayload().getSharedSecret();
        final JwtIssuerValidator jwtIssuerValidator = issuer -> true;
        NimbusJwtReaderFactory jwtReaderFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, sharedSecretService);

        String jwt = readJwt(response);
        JwtDateReader jwtDateReader = new JwtDateReader();
        JwtReader jwtReader = jwtReaderFactory.getReader(jwt);
        jwtReader.readAndVerify(jwt, ImmutableMap.of("iat", jwtDateReader));
        return jwtDateReader.getClaimDate();
    }

    private String getQueryParam(HttpURLConnection response, String key)
    {
        return RemotePageUtil.findInContext(response.getHeaderField("Location"), key);
    }

    private String readJwt(HttpURLConnection response)
    {
        return getQueryParam(response, JwtConstants.JWT_PARAM_NAME);
    }

    private static class JwtDateReader implements JwtClaimVerifier
    {

        private long claimDate = 0;

        @Override
        public void verify(@Nonnull final Object claim) throws JwtVerificationException, JwtParseException
        {
            if (claim instanceof Date)
            {
                claimDate = ((Date) claim).getTime();
            }
            else
            {
                throw new JwtInvalidClaimException(String.format("Expecting the issued-at claim to be a Date but it was a %s: [%s]", claim.getClass().getSimpleName(), claim));
            }
        }

        public long getClaimDate()
        {
            return claimDate;
        }
    }

    private long getSystemTimeBeforeJwtIssue()
    {
        // Checking the system time across two JVM's seems unreliable, so allow a considerable discrepancy
        return System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(JwtConstants.TIME_CLAIM_LEEWAY_SECONDS);
    }
}
