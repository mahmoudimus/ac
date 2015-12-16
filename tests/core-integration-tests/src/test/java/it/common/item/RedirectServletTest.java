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
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.AfterClass;
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
    private static final String WEB_ITEM_KEY = "checkPageJwtExpiry";
    private static final String ABSOLUTE_PAGE_KEY = "absolutePage";
    private static final String WEB_ITEM_ON_URL = "/pcp";
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
                                .withKey(WEB_ITEM_KEY)
                                .withName(new I18nProperty("JWTP", null))
                                .withUrl(WEB_ITEM_ON_URL)
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

    @AfterClass
    public static void tearDownUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(true);
    }

    @Test
    public void shouldReturnRedirectionToAddOnServer() throws Exception
    {
        String addOnPageUrl = runner.getAddon().getBaseUrl() + WEB_ITEM_ON_URL;

        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, WEB_ITEM_KEY));
        String url =  response.getHeaderField("Location");

        assertThat(url, Matchers.startsWith(addOnPageUrl));
    }

    @Test
    public void shouldSignRedirectionWithFreshJwtToken() throws Exception
    {
        HttpURLConnection response1 = doRedirectRequest(getPathToRedirectServlet(addOnKey, WEB_ITEM_KEY));

        long timeBeforeClick = getSystemTimeBeforeJwtIssue();
        long claimDate1 = getClaimDate(response1.getHeaderField("Location"));
        assertThat(claimDate1, greaterThan(timeBeforeClick));

        // JWT token's sign time has one second precision.
        // We have to wait one second to be sure that token from next request is new.
        Thread.sleep(1000);

        HttpURLConnection response2 = doRedirectRequest(getPathToRedirectServlet(addOnKey, WEB_ITEM_KEY));
        long claimDate2 = getClaimDate(response2.getHeaderField("Location"));
        assertThat(claimDate2, greaterThan(claimDate1));
    }

    @Test
    public void shouldReturnCachedResponseWithTemporaryRedirect() throws Exception
    {
        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, WEB_ITEM_KEY));
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
        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, WEB_ITEM_KEY));
        assertThat(response.getResponseCode(), is(HttpStatus.SC_NOT_FOUND));
    }

    @Test
    public void shouldReturnNotFoundIfAddonHasBeenUninstalled() throws Exception
    {
        runner.stopAndUninstall();

        HttpURLConnection response = doRedirectRequest(getPathToRedirectServlet(addOnKey, WEB_ITEM_KEY));
        assertThat(response.getResponseCode(), Is.is(HttpStatus.SC_NOT_FOUND));
    }

    private HttpURLConnection doRedirectRequest(URI uri) throws IOException
    {
        return (HttpURLConnection) uri.toURL().openConnection();
    }

    private URI getPathToRedirectServlet(String addOnKey, String moduleKey)
    {
        return UriBuilder.fromPath(baseUrl).path(RedirectServletPath.forModule(addOnKey, moduleKey)).build();
    }

    private long getClaimDate(String urlToAddOn) throws Exception
    {
        JwtIssuerSharedSecretService sharedSecretService = issuer -> INSTALL_HANDLER_SERVLET.getInstallPayload().getSharedSecret();
        JwtIssuerValidator jwtIssuerValidator = issuer -> true;
        NimbusJwtReaderFactory jwtReaderFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, sharedSecretService);

        String jwt = readJwt(urlToAddOn);
        JwtDateReader jwtDateReader = new JwtDateReader();
        JwtReader jwtReader = jwtReaderFactory.getReader(jwt);
        jwtReader.readAndVerify(jwt, ImmutableMap.of("iat", jwtDateReader));
        return jwtDateReader.getClaimDate();
    }

    private String getQueryParam(String key, String url)
    {
        return RemotePageUtil.findInContext(url, key);
    }

    private String readJwt(String urlToAddOn)
    {
        return getQueryParam(JwtConstants.JWT_PARAM_NAME, urlToAddOn);
    }

    private static class JwtDateReader implements JwtClaimVerifier
    {

        private long claimDate = 0;

        @Override
        public void verify(@Nonnull Object claim) throws JwtVerificationException, JwtParseException
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
