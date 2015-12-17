package it.com.atlassian.plugin.connect.plugin.auth.user;

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
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableSet;
import it.com.atlassian.plugin.connect.util.request.RequestUtil;
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
    private final ApplicationManager applicationManager;
    private final ApplicationService applicationService;
    private final UserManager userManager;
    private final AtomicReference<Plugin> installedPlugin = new AtomicReference<Plugin>();

    protected final RequestUtil requestUtil;

    protected ConnectAddonBean addonBean;
    private boolean globalImpersonationWasEnabled;

    private static final Logger log = LoggerFactory.getLogger(ThreeLeggedAuthFilterTestBase.class);
    private static final String ADMIN_USERNAME = "admin";
    protected String SUBJECT_USERNAME = "admin";
    protected String SUBJECT_USERKEY;
    protected String INACTIVE_USERNAME = "inactive_user";
    protected String NON_EXISTENT_USERKEY = "non_existent_user";

    private static final String REQUEST_PATH = "/path";

    public ThreeLeggedAuthFilterTestBase(TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            AddonTestFilterResults testFilterResults,
            JwtWriterFactory jwtWriterFactory,
            ConnectAddonRegistry connectAddonRegistry,
            ApplicationProperties applicationProperties,
            ApplicationService applicationService,
            ApplicationManager applicationManager, final UserManager userManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.jwtWriterFactory = jwtWriterFactory;
        this.connectAddonRegistry = connectAddonRegistry;
        this.applicationProperties = applicationProperties;
        this.applicationManager = applicationManager;
        this.applicationService = applicationService;
        this.userManager = userManager;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    protected abstract ScopeName getScope();

    @BeforeClass
    public void oneTimeSetup() throws IOException
    {
        testAuthenticator.authenticateUser(ADMIN_USERNAME);
        addonBean = createAddonBean(getScope());
        installedPlugin.set(testPluginInstaller.installAddon(addonBean));
    }

    @Before
    public void beforeEachTest()
    {
        globalImpersonationWasEnabled = isGlobalImpersonationEnabled();
        SUBJECT_USERKEY = getUserKeyForUserName(SUBJECT_USERNAME);
    }

    @After
    public void afterEachTest()
    {
        setGlobalImpersonationEnabled(globalImpersonationWasEnabled);
    }

    protected String getUserKeyForUserName(String username)
    {
        UserProfile userProfile = userManager.getUserProfile(username);
        if (userProfile != null)
        {
            log.warn("User key for user " + username + ":" + userProfile.getUserKey().getStringValue());
            return userProfile.getUserKey().getStringValue();
        }
        return null;
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
        final UserTemplate userTemplate = new UserTemplate(INACTIVE_USERNAME);
        userTemplate.setActive(false);
        ensureUserDoesNotExist(userTemplate.getName());
        User user = applicationService.addUser(applicationManager.findAll().iterator().next(), userTemplate, PasswordCredential.NONE);
        return createRequestUri(user.getName());
    }

    protected String getAddonUsername()
    {
        return "addon_" + addonBean.getKey().toLowerCase();
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
        ServletRequestSnapshot request = testFilterResults.getRequest(addonBean.getKey(), REQUEST_PATH);
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
        return createRequestUri(subject, addonBean.getKey());
    }

    protected URI createRequestUri(String subject, String issuer) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        URI uri = createRequestUriWithoutJwt();

        JwtWriter jwtWriter = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, connectAddonRegistry.getSecret(addonBean.getKey()));
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
        final URI internalAddonBaseUrl = URI.create(testPluginInstaller.getInternalAddonBaseUrl(addonBean.getKey()));
        return URI.create(internalAddonBaseUrl + REQUEST_PATH);
    }

    protected Object getSubjectFromRequestAttribute(ServletRequestSnapshot request)
    {
        return getRequestAttribute(request, JwtConstants.HttpRequests.JWT_SUBJECT_ATTRIBUTE_NAME);
    }

    protected Object getAddonIdFromRequestAttribute(ServletRequestSnapshot request)
    {
        return getRequestAttribute(request, JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME);
    }

    private Object getRequestAttribute(ServletRequestSnapshot request, String attributeName)
    {
        final Map<String, Object> attributes = request.getAttributes();
        return null == attributes ? null : attributes.get(attributeName);
    }

    private ConnectAddonBean createAddonBean(ScopeName scope)
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
        return getClass().getSimpleName() + '-' + AddonUtil.randomPluginKey();
    }
}
