package it.com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.request.RequestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;

import com.google.common.collect.Sets;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.plugin.auth.jwt.JwtAuthorizationGenerator.constructParameterMap;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * Send a bunch of requests from an addon with certain scopes and assert that the requests are accepted or rejected as expected.
 */
public abstract class ScopeTestBase
{
    private final ScopeName addonScope;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final JwtWriterFactory jwtWriterFactory;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final ApplicationProperties applicationProperties;
    protected final RequestUtil requestUtil;

    private ConnectAddonBean addonBean;
    private Plugin addon;

    private static final Logger log = LoggerFactory.getLogger(ScopeTestBase.class);

    public ScopeTestBase(@Nullable ScopeName addonScope,
                         TestPluginInstaller testPluginInstaller,
                         TestAuthenticator testAuthenticator,
                         JwtWriterFactory jwtWriterFactory,
                         ConnectAddonRegistry connectAddonRegistry,
                         ApplicationProperties applicationProperties)
    {
        this.addonScope = addonScope;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.jwtWriterFactory = jwtWriterFactory;
        this.connectAddonRegistry = connectAddonRegistry;
        this.applicationProperties = applicationProperties;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    @BeforeClass
    public void setup() throws IOException
    {
        final String key = getClass().getSimpleName() + '-' + AddonUtil.randomPluginKey();
        ConnectAddonBeanBuilder connectAddonBeanBuilder = newConnectAddonBean()
                .withKey(key)
                .withName(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withLicensing(true)
                .withAuthentication(newAuthenticationBean()
                        .withType(AuthenticationType.JWT)
                        .build())
                .withLifecycle(newLifecycleBean()
                        .withInstalled("/installed")
                        .build())
                .withModule("generalPages", newPageBean()
                        .withUrl("/hello-world.html")
                        .withKey("general")
                        .withName(new I18nProperty("Greeting", "greeting"))
                        .build());

        // scopes are optional so that we can have "no scopes" test classes
        if (null != addonScope)
        {
            connectAddonBeanBuilder = connectAddonBeanBuilder.withScopes(Sets.newHashSet(addonScope));
        }

        addonBean = connectAddonBeanBuilder.build();

        testAuthenticator.authenticateUser("admin");
        addon = testPluginInstaller.installAddon(addonBean);
    }

    @AfterClass
    public void tearDown()
    {
        if (null != addon)
        {
            try
            {
                testPluginInstaller.uninstallAddon(addon);
            }
            catch (IOException e)
            {
                log.error(String.format("Unable to uninstall add-on '%s'", addon.getKey()), e);
            }
        }

        testAuthenticator.unauthenticate();
    }

    protected void assertValidRequest(HttpMethod method, String path) throws IOException, NoSuchAlgorithmException
    {
        assertResponseCodeForRequest(method, path, HttpStatus.OK);
    }

    protected void assertForbiddenRequest(HttpMethod method, String path) throws IOException, NoSuchAlgorithmException
    {
        assertResponseCodeForRequest(method, path, HttpStatus.FORBIDDEN);
    }

    protected void assertResponseCodeForRequest(HttpMethod httpMethod, String uriSuffix, HttpStatus status)
            throws IOException, NoSuchAlgorithmException
    {
        URI uri = constructUri(httpMethod, uriSuffix);
        RequestUtil.Response response = issueRequest(httpMethod, uri);
        String message = String.format("Expecting HTTP response code %d from %s %s but was %d.",
                status.code, httpMethod, uri, response.getStatusCode());
        assertEquals(message, status.code, response.getStatusCode());
    }

    private URI constructUri(HttpMethod httpMethod, String uriSuffix) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        final URI hostProductBaseUrl = URI.create(applicationProperties.getBaseUrl(UrlMode.CANONICAL));
        URI uri = URI.create(hostProductBaseUrl + uriSuffix);

        // add JWT query param (request from add-on to host product)
        {
            JwtWriter jwtWriter = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, connectAddonRegistry.getSecret(addonBean.getKey()));
            final String contextPath = hostProductBaseUrl.getPath();
            final JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                    .issuer(addonBean.getKey())
                    .queryHash(HttpRequestCanonicalizer.computeCanonicalRequestHash(new CanonicalHttpUriRequest(httpMethod.toString(), uri.getPath(), contextPath, constructParameterMap(uri))));
            String jwtToken = jwtWriter.jsonToJwt(jsonBuilder.build());
            final char queryStringSeparator = uriSuffix.contains("?") ? '&' : '?';
            uri = URI.create(uri.toString() + queryStringSeparator + "jwt=" + jwtToken);
        }
        return uri;
    }

    private RequestUtil.Response issueRequest(HttpMethod httpMethod, URI uri) throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(httpMethod)
                .setUri(uri)
                .build();

        return requestUtil.makeRequest(request);
    }
}
