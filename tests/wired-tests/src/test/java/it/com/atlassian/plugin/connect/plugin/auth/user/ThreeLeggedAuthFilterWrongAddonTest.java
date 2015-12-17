package it.com.atlassian.plugin.connect.plugin.auth.user;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.user.NoUserAgencyException;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import it.com.atlassian.plugin.connect.util.request.RequestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils.lowerCase;
import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class ThreeLeggedAuthFilterWrongAddonTest extends ThreeLeggedAuthFilterTestBase
{

    public ThreeLeggedAuthFilterWrongAddonTest(TestPluginInstaller testPluginInstaller,
                                               TestAuthenticator testAuthenticator,
                                               AddonTestFilterResults testFilterResults,
                                               JwtWriterFactory jwtWriterFactory,
                                               ConnectAddonRegistry connectAddonRegistry,
                                               ApplicationProperties applicationProperties,
                                               ApplicationService applicationService,
                                               ApplicationManager applicationManager,
                                               UserManager userManager)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, jwtWriterFactory, connectAddonRegistry, applicationProperties, applicationService, applicationManager, userManager);
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.READ;
    }

    @Test
    public void specifyingSubjectIsAllowed() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        RequestUtil.Response response = issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void specifiedSubjectIsIgnoredAndAddonUserIsUsed() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(lowerCase(getAddonUsername()), lowerCase(getCapturedRequest().getRemoteUsername()));
    }

    @Test
    public void authorisedUserAgencyHasSubjectAttribute() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(SUBJECT_USERKEY, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectIsOk() throws IOException, NoSuchAlgorithmException
    {
        RequestUtil.Response response = issueRequest(createRequestUri(null));
        assertEquals(200, response.getStatusCode());
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectImpliesAddonUser() throws IOException, NoSuchAlgorithmException
    {
        issueRequest(createRequestUri(null));
        assertEquals(lowerCase(getAddonUsername()), lowerCase(getCapturedRequest().getRemoteUsername()));
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectImpliesNoSubjectAttribute() throws IOException, NoSuchAlgorithmException
    {
        issueRequest(createRequestUri(null));
        assertEquals(null, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectResultsInAddonAttribute() throws IOException, NoSuchAlgorithmException
    {
        issueRequest(createRequestUri(null));
        assertEquals(addonBean.getKey(), getAddonIdFromRequestAttribute(getCapturedRequest()));
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsAreOk() throws IOException
    {
        RequestUtil.Response response = issueRequest(createRequestUriWithoutJwt());
        assertEquals(200, response.getStatusCode());
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHasNoRemoteUser() throws IOException
    {
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getCapturedRequest().getRemoteUsername());
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHaveNoSubjectAttribute() throws IOException
    {
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHaveNoAddonAttribute() throws IOException
    {
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getAddonIdFromRequestAttribute(getCapturedRequest()));
    }

    // if the specified add-on does not exist then the request is rejected
    @Test
    public void aNonExistentAddonIsRejected() throws IOException, NoSuchAlgorithmException
    {
        RequestUtil.Response response = issueRequest(createRequestUri(SUBJECT_USERKEY, "non-existent add-on key"));
        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void emptySubjectResultsInError() throws IOException, NoSuchAlgorithmException
    {
        RequestUtil.Response response = issueRequest(createRequestUri(""));
        assertEquals(400, response.getStatusCode());
    }

}
