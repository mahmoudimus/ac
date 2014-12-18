package it.com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.threeleggedauth.NoUserAgencyException;
import com.atlassian.plugin.connect.plugin.threeleggedauth.ThreeLeggedAuthService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class ThreeLeggedAuthFilterAddOnSpecificTest extends ThreeLeggedAuthFilterTestBase
{
   public ThreeLeggedAuthFilterAddOnSpecificTest(TestPluginInstaller testPluginInstaller,
                                                 TestAuthenticator testAuthenticator,
                                                 AddonTestFilterResults testFilterResults,
                                                 JwtWriterFactory jwtWriterFactory,
                                                 ConnectAddonRegistry connectAddonRegistry,
                                                 ApplicationProperties applicationProperties,
                                                 ThreeLeggedAuthService threeLeggedAuthService,
                                                 ApplicationService applicationService,
                                                 ApplicationFactory applicationFactory)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, jwtWriterFactory, connectAddonRegistry, applicationProperties, threeLeggedAuthService, applicationService, applicationFactory);
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.READ;
    }

    @Test
    public void authorisedUserAgencyIsAllowed() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        setGlobalImpersonationEnabled(false);
        RequestUtil.Response response = issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void authorisedUserAgencyHasSubjectAsRemoteUser() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(SUBJECT_USERKEY, getCapturedRequest().getRemoteUsername());
    }

    @Test
    public void authorisedUserAgencyHasSubjectAttribute() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(SUBJECT_USERKEY, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    @Test
    public void cannotActForANonExistentUser() throws IOException, NoSuchAlgorithmException, NoUserAgencyException, OperationFailedException, ApplicationPermissionException
    {
        setGlobalImpersonationEnabled(false);
        ensureUserDoesNotExist(NON_EXISTENT_USERKEY);
        RequestUtil.Response response = issueRequest(createRequestUri(NON_EXISTENT_USERKEY));
        assertEquals(401, response.getStatusCode());
    }

    // if the add-on requests the USER_AGENCY scope, specifies a subject and the subject is inactive then the request is rejected
    @Test
    public void cannotActForAnInactiveUser() throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        RequestUtil.Response response = issueRequest(createUriForInactiveSubject());
        assertEquals(401, response.getStatusCode());
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectIsOk() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        RequestUtil.Response response = issueRequest(createRequestUri(null));
        assertEquals(200, response.getStatusCode());
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectImpliesAddOnUser() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUri(null));
        assertEquals(getAddOnUsername(), getCapturedRequest().getRemoteUsername());
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectImpliesNoSubjectAttribute() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUri(null));
        assertEquals(null, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectResultsInAddOnAttribute() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUri(null));
        assertEquals(addOnBean.getKey(), getAddOnIdFromRequestAttribute(getCapturedRequest()));
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsAreOk() throws IOException
    {
        setGlobalImpersonationEnabled(false);
        RequestUtil.Response response = issueRequest(createRequestUriWithoutJwt());
        assertEquals(200, response.getStatusCode());
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHasNoRemoteUser() throws IOException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getCapturedRequest().getRemoteUsername());
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHaveNoSubjectAttribute() throws IOException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHaveNoAddOnAttribute() throws IOException
    {
        setGlobalImpersonationEnabled(false);
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getAddOnIdFromRequestAttribute(getCapturedRequest()));
    }

    // if the specified add-on does not exist then the request is rejected
    @Test
    public void aNonExistentAddOnIsRejected() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        RequestUtil.Response response = issueRequest(createRequestUri(SUBJECT_USERKEY, "non-existent add-on key"));
        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void emptySubjectResultsInError() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(false);
        RequestUtil.Response response = issueRequest(createRequestUri(""));
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void impersonationSetAndNo3LAIsOk() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        RequestUtil.Response response = issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void impersonationSetAndNo3LAImpliesThatTheSubjectIsTheAssignedUser() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(SUBJECT_USERKEY, getCapturedRequest().getRemoteUsername());
    }

    @Test
    public void impersonationSetAndNo3LAImpliesThatTheSubjectAttributeIsSet() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(SUBJECT_USERKEY, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    @Test
    public void impersonationSetAndNo3LAImpliesThatTheAddOnAttributeIsSet() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        issueRequest(createRequestUri(SUBJECT_USERKEY));
        assertEquals(addOnBean.getKey(), getAddOnIdFromRequestAttribute(getCapturedRequest()));
    }

    @Test
    public void impersonationSetButNonExistentAddOnResultsInError() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        RequestUtil.Response response = issueRequest(createRequestUri(SUBJECT_USERKEY, "non-existent-add-on"));
        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void impersonationSetButNoSubjectIsOk() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        RequestUtil.Response response = issueRequest(createRequestUri(null));
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void impersonationSetButNoSubjectResultsInAssignmentToTheAddOnUser() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        issueRequest(createRequestUri(null));
        assertEquals(getAddOnUsername(), getCapturedRequest().getRemoteUsername());
    }

    @Test
    public void impersonationSetButNoSubjectResultsInNoSubjectAttribute() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        issueRequest(createRequestUri(null));
        assertEquals(null, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    @Test
    public void impersonationSetButNoSubjectResultsInAddOnAttribute() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        issueRequest(createRequestUri(null));
        assertEquals(addOnBean.getKey(), getAddOnIdFromRequestAttribute(getCapturedRequest()));
    }

    @Test
    public void impersonationSetButEmptySubjectResultsInError() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        RequestUtil.Response response = issueRequest(createRequestUri(""));
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void impersonationSetButNonExistentSubjectResultsInError() throws IOException, NoSuchAlgorithmException
    {
        setGlobalImpersonationEnabled(true);
        RequestUtil.Response response = issueRequest(createRequestUri("non-existent-user"));
        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void impersonationSetButInactiveSubjectResultsInError() throws IOException, NoSuchAlgorithmException, OperationFailedException, ApplicationPermissionException, InvalidCredentialException, InvalidUserException
    {
        setGlobalImpersonationEnabled(true);
        RequestUtil.Response response = issueRequest(createUriForInactiveSubject());
        assertEquals(401, response.getStatusCode());
    }

    @Override
    protected String getAddonKey()
    {
        return "com.eazybi.atlassian-connect.eazybi-jira";
    }


}
