package it.com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jwt.JwtConstants;
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
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.threeleggedauth.ThreeLeggedAuthService;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.google.common.collect.ImmutableSet;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.assertNotNull;

public abstract class ThreeLeggedAuthFilterTestBase
{
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final AddonTestFilterResults testFilterResults;
    private final JwtWriterFactory jwtWriterFactory;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final ApplicationProperties applicationProperties;
    protected final ThreeLeggedAuthService threeLeggedAuthService;
    private final ApplicationManager applicationManager;
    private final ApplicationService applicationService;
    private final AtomicReference<Plugin> installedPlugin = new AtomicReference<Plugin>();
    protected final RequestUtil requestUtil;

    protected ConnectAddonBean addOnBean;
    private boolean globalImpersonationWasEnabled;

    private final static Logger log = LoggerFactory.getLogger(ThreeLeggedAuthFilterTestBase.class);
    private final static String ADMIN_USERNAME = "admin";
    protected final static String SUBJECT_USERKEY = "barney";
    protected static final String INACTIVE_USERKEY = "inactive_user";
    protected static final String NON_EXISTENT_USERKEY = "non_existent_user";
    private static final String REQUEST_PATH = "/path";

    public ThreeLeggedAuthFilterTestBase(TestPluginInstaller testPluginInstaller,
                                         TestAuthenticator testAuthenticator,
                                         AddonTestFilterResults testFilterResults,
                                         JwtWriterFactory jwtWriterFactory,
                                         ConnectAddonRegistry connectAddonRegistry,
                                         ApplicationProperties applicationProperties,
                                         ThreeLeggedAuthService threeLeggedAuthService,
                                         ApplicationService applicationService,
                                         ApplicationManager applicationManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.jwtWriterFactory = jwtWriterFactory;
        this.connectAddonRegistry = connectAddonRegistry;
        this.applicationProperties = applicationProperties;
        this.threeLeggedAuthService = threeLeggedAuthService;
        this.applicationManager = applicationManager;
        this.applicationService = applicationService;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    protected abstract ScopeName getScope();

    @BeforeClass
    public void oneTimeSetup() throws IOException
    {
        testAuthenticator.authenticateUser(ADMIN_USERNAME);
        addOnBean = createAddOnBean(getScope());
        installedPlugin.set(testPluginInstaller.installAddon(addOnBean));
    }

    @Before
    public void beforeEachTest()
    {
        globalImpersonationWasEnabled = isGlobalImpersonationEnabled();
    }

    @After
    public void afterEachTest()
    {
        setGlobalImpersonationEnabled(globalImpersonationWasEnabled);
    }

    protected boolean isGlobalImpersonationEnabled()
    {
        return Boolean.getBoolean(JwtConstants.AppLinks.SYS_PROP_ALLOW_IMPERSONATION);
    }

    protected void setGlobalImpersonationEnabled(boolean value)
    {
        System.setProperty(JwtConstants.AppLinks.SYS_PROP_ALLOW_IMPERSONATION, Boolean.toString(value));
    }

    @AfterClass
    public void oneTimeTearDown()
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

    protected URI createUriForInactiveSubject() throws OperationFailedException, ApplicationPermissionException, InvalidUserException, InvalidCredentialException, UnsupportedEncodingException, NoSuchAlgorithmException
    {
        final UserTemplate userTemplate = new UserTemplate(INACTIVE_USERKEY);
        userTemplate.setActive(false);
        ensureUserDoesNotExist(userTemplate.getName());
        User user = applicationService.addUser(applicationManager.findAll().iterator().next(), userTemplate, PasswordCredential.NONE);
        return createRequestUri(user.getName());
    }

    protected String getAddOnUsername()
    {
        return "addon_" + addOnBean.getKey().toLowerCase();
    }

    protected void ensureUserDoesNotExist(String username) throws OperationFailedException, ApplicationPermissionException
    {
        // precondition: the user should not exist
        try
        {
            applicationService.removeUser(applicationManager.findAll().iterator().next(), username);
        }
        catch (UserNotFoundException e)
        {
            // ignore the fact that we could not delete a non-existent user; it makes sense
        }
    }

    protected ServletRequestSnapshot getCapturedRequest()
    {
        ServletRequestSnapshot request = testFilterResults.getRequest(addOnBean.getKey(), REQUEST_PATH);
        assertNotNull(request);
        return request;
    }

    protected RequestUtil.Response issueRequest(URI uri) throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
            .setMethod(HttpMethod.GET)
            .setUri(uri)
            .build();

        return requestUtil.makeRequest(request);
    }

    protected URI createRequestUri(String subject) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        return createRequestUri(subject, addOnBean.getKey());
    }

    protected URI createRequestUri(String subject, String issuer) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        URI uri = createRequestUriWithoutJwt();

        JwtWriter jwtWriter = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, connectAddonRegistry.getSecret(addOnBean.getKey()));
        final String contextPath = URI.create(applicationProperties.getBaseUrl(UrlMode.CANONICAL)).getPath();
        final JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuer(issuer)
                .queryHash(HttpRequestCanonicalizer.computeCanonicalRequestHash(new CanonicalHttpUriRequest("GET", uri.getPath(), contextPath)));

        if (null != subject)
        {
            jsonBuilder.subject(subject);
        }

        String jwtToken = jwtWriter.jsonToJwt(jsonBuilder.build());
        uri = URI.create(uri.toString() + "?jwt=" + jwtToken);
        return uri;
    }

    protected URI createRequestUriWithoutJwt()
    {
        final URI internalAddonBaseUrl = URI.create(testPluginInstaller.getInternalAddonBaseUrl(addOnBean.getKey()));
        return URI.create(internalAddonBaseUrl + REQUEST_PATH);
    }

    protected Object getSubjectFromRequestAttribute(ServletRequestSnapshot request)
    {
        return getRequestAttribute(request, JwtConstants.HttpRequests.JWT_SUBJECT_ATTRIBUTE_NAME);
    }

    protected Object getAddOnIdFromRequestAttribute(ServletRequestSnapshot request)
    {
        return getRequestAttribute(request, JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME);
    }

    private Object getRequestAttribute(ServletRequestSnapshot request, String attributeName)
    {
        final Map<String, Object> attributes = request.getAttributes();
        return null == attributes ? null : attributes.get(attributeName);
    }

    private ConnectAddonBean createAddOnBean(ScopeName scope)
    {
        final String addonKey = getAddonKey();
        return newConnectAddonBean()
                .withKey(addonKey)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withName(getClass().getSimpleName())
                .withScopes(ImmutableSet.of(scope))
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
                        .build())
                .build();
    }

    protected String getAddonKey()
    {
        return getClass().getSimpleName() + '-' + System.currentTimeMillis();
    }
}
