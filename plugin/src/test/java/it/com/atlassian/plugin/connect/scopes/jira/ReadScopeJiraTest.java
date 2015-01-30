
package it.com.atlassian.plugin.connect.scopes.jira;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.ImmutableMap;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.scopes.ScopeTestBase;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ReadScopeJiraTest extends ScopeTestBase
{
    private static final String ADMIN_USERNAME = "admin";

    private final UserManager userManager;
    private final ProjectService projectService;
    private final CommentService commentService;
    private final CommentPropertyService commentPropertyService;

    public ReadScopeJiraTest(TestPluginInstaller testPluginInstaller,
                             TestAuthenticator testAuthenticator,
                             JwtWriterFactory jwtWriterFactory,
                             ConnectAddonRegistry connectAddonRegistry,
                             ApplicationProperties applicationProperties,
                             UserManager userManager,
                             ProjectService projectService,
                             CommentService commentService,
                             CommentPropertyService commentPropertyService)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry,
                applicationProperties);
        this.userManager = userManager;
        this.projectService = projectService;
        this.commentService = commentService;
        this.commentPropertyService = commentPropertyService;
    }

    @Test
    public void shouldAllowGetGreenhopperRapidview() throws Exception
    {
        assertValidRequest(HttpMethod.GET, "/rest/greenhopper/1.0/rapidview");
    }

    @Test
    public void shouldForbidPutGreenhopperRankBefore() throws Exception
    {
        assertForbiddenRequest(HttpMethod.PUT, "/rest/greenhopper/1.0/api/rank/before");
    }

    @Test
    public void shouldAllowGetSecureProjectAvatar() throws Exception
    {
        Project project = createProject();

        assertValidRequest(HttpMethod.GET, "/secure/projectavatar?pid=" + project.getId());
    }

    @Test
    public void shouldAllowGetSecureUserAvatar() throws Exception
    {
        assertValidRequest(HttpMethod.GET, "/secure/useravatar?ownerId=" + ADMIN_USERNAME);
    }

    @Test
    public void shouldAllowToReadCommentProperties() throws IOException, NoSuchAlgorithmException, JSONException
    {
        ApplicationUser admin = getAdmin();
        Comment comment = createComment();
        EntityPropertyService.PropertyInput property = new EntityPropertyService.PropertyInput("value", "key");
        EntityPropertyService.SetPropertyValidationResult validationResult = commentPropertyService.validateSetProperty(admin, comment.getId(), property);
        EntityPropertyService.PropertyResult propertyResult = commentPropertyService.setProperty(admin, validationResult);

        assertTrue(propertyResult.isValid());
        assertValidRequest(HttpMethod.GET, "/rest/api/2/comment/" + comment.getId() + "/properties/" + property.getPropertyKey());
    }

    private Comment createComment() throws JSONException
    {
        final ApplicationUser admin = getAdmin();
        final CommentService.CommentCreateValidationResult validationResult = commentService.validateCommentCreate(admin, CommentService.CommentParameters.builder()
                .body("comment")
                .build());
        return commentService.create(admin, validationResult, false);
    }

    private Project createProject() throws IOException
    {
        int keyLength = 6;
        String key = RandomStringUtils.randomAlphabetic(keyLength).toUpperCase();
        ApplicationUser user = getAdmin();
        ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(
                user.getDirectoryUser(), key, key, null, ADMIN_USERNAME, null, null);
        return projectService.createProject(result);
    }

    private ApplicationUser getAdmin() {return userManager.getUserByKey(ADMIN_USERNAME);}
}
