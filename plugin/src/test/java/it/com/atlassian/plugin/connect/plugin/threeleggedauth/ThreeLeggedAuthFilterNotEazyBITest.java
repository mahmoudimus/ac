package it.com.atlassian.plugin.connect.plugin.threeleggedauth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.threeleggedauth.NoUserAgencyException;
import com.atlassian.plugin.connect.plugin.threeleggedauth.ThreeLeggedAuthService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ThreeLeggedAuthFilterNotEazyBITest extends ThreeLeggedAuthFilterTestBase
{
    private static final String ADD_ON_KEYS_SYS_PROP = "com.atlassian.connect.3la.authorised_add_on_keys";

    public ThreeLeggedAuthFilterNotEazyBITest(TestPluginInstaller testPluginInstaller,
                                              TestAuthenticator testAuthenticator,
                                              AddonTestFilterResults testFilterResults,
                                              JwtWriterFactory jwtWriterFactory,
                                              ConnectAddonRegistry connectAddonRegistry,
                                              ApplicationProperties applicationProperties,
                                              ThreeLeggedAuthService threeLeggedAuthService,
                                              ApplicationService applicationService,
                                              ApplicationManager applicationManager)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, jwtWriterFactory, connectAddonRegistry, applicationProperties, threeLeggedAuthService, applicationService, applicationManager);
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.READ;
    }

//    @Test
    public void pleaseImplementMe()
    {
        /* suggested tests:
            - EazyBI can impersonate a valid user (200 response code, correct add-on and subject attributes, request is assigned to subject)
            - EazyBI cannot impersonate a non-existent user (401 or 403 response code)
            - EazyBI cannot impersonate an inactive user (same response code as above)
            - EazyBI can omit the subject claim and the request goes through ok but without impersonation (200 response code, correct add-on and subject attributes, request is assigned to add-on user)
                (for these see ThreeLeggedAuthFilterWithUserAgency in feature/ACDEV-1228-3-legged-auth)
            - A random other add-on can specify a subject for impersonation and the request goes through ok but without impersonation
            - A random other add-on can omit the subject claim and the request goes through ok but without impersonation (200 response code, correct add-on and subject attributes, request is assigned to add-on user)
                (see ThreeLeggedAuthFilterWithoutUserAgency in feature/ACDEV-1228-3-legged-auth)

            - only READ scope allowed
         */
        throw new RuntimeException("not implemented!");
    }

    @Test
    public void specifyingSubjectIsAllowed() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        grant3LA();
        assertEquals(200, issueRequest(createRequestUri(SUBJECT_USERNAME)));
    }

    @Test
    public void specifiedSubjectIsIgnoredAndAddonUserIsUsed() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        grant3LA();
        issueRequest(createRequestUri(SUBJECT_USERNAME));
        assertEquals(getAddOnUsername(), getCapturedRequest().getRemoteUserKey());
    }

    @Test
    public void authorisedUserAgencyHasSubjectAttribute() throws IOException, NoSuchAlgorithmException, NoUserAgencyException
    {
        grant3LA();
        issueRequest(createRequestUri(SUBJECT_USERNAME));
        assertEquals(SUBJECT_USERNAME, getSubjectFromRequestAttribute(getCapturedRequest()));
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectIsOk() throws IOException, NoSuchAlgorithmException
    {
        assertEquals(200, issueRequest(createRequestUri(null)));
    }

    // if the add-on does not specify a subject then the add-on user is assigned to the request, whether or not it also requests the USER_AGENCY scope
    @Test
    public void noSubjectImpliesAddOnUser() throws IOException, NoSuchAlgorithmException
    {
        issueRequest(createRequestUri(null));
        assertEquals(getAddOnUsername(), getCapturedRequest().getRemoteUserKey());
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
    public void noSubjectResultsInAddOnAttribute() throws IOException, NoSuchAlgorithmException
    {
        issueRequest(createRequestUri(null));
        assertEquals(addOnBean.getKey(), getAddOnIdFromRequestAttribute(getCapturedRequest()));
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsAreOk() throws IOException
    {
        assertEquals(200, issueRequest(createRequestUriWithoutJwt()));
    }

    // if this is not a request from a JWT add-on then the request proceeds through the filter chain
    @Test
    public void nonJwtRequestsHasNoRemoteUser() throws IOException
    {
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getCapturedRequest().getRemoteUserKey());
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
    public void nonJwtRequestshaveNoAddOnAttribute() throws IOException
    {
        issueRequest(createRequestUriWithoutJwt());
        assertEquals(null, getAddOnIdFromRequestAttribute(getCapturedRequest()));
    }

    // if the specified add-on does not exist then the request is rejected
    @Test
    public void aNonExistentAddOnIsRejected() throws IOException, NoSuchAlgorithmException
    {
        assertEquals(401, issueRequest(createRequestUri(SUBJECT_USERNAME, "non-existent add-on key")));
    }

    @Test
    public void emptySubjectResultsInError() throws IOException, NoSuchAlgorithmException
    {
        assertEquals(400, issueRequest(createRequestUri("")));
    }

    private void grant3LA()
    {
        // this doesn't work as system property only checked on start up so setting it here is too late
//        System.setProperty(ADD_ON_KEYS_SYS_PROP, addOnBean.getKey());
    }

}
