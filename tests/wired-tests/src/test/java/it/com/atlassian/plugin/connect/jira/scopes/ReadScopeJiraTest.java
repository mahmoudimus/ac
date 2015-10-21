
package it.com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ReadScopeJiraTest extends ScopeTestBase
{
    private final JiraTestUtil scopeTestUtil;

    public ReadScopeJiraTest(TestPluginInstaller testPluginInstaller,
                             TestAuthenticator testAuthenticator,
                             JwtWriterFactory jwtWriterFactory,
                             ConnectAddonRegistry connectAddonRegistry,
                             ApplicationProperties applicationProperties,
                             JiraTestUtil scopeTestUtil)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry,
                applicationProperties);
        this.scopeTestUtil = scopeTestUtil;
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
        Project project = scopeTestUtil.createProject();

        assertValidRequest(HttpMethod.GET, "/secure/projectavatar?pid=" + project.getId());
    }

    @Test
    public void shouldAllowGetSecureUserAvatar() throws Exception
    {
        assertValidRequest(HttpMethod.GET, "/secure/useravatar?ownerId=" + JiraTestUtil.ADMIN_USERNAME);
    }

    @Test
    public void shouldAllowGetSecureAvatar() throws Exception
    {
        Project project = scopeTestUtil.createProject();
        String avatarId = project.getIssueTypes().iterator().next().getId();

        assertValidRequest(HttpMethod.GET, "/secure/viewavatar?size=xsmall&avatarType=issuetype&avatarId=" + avatarId);
    }

    @Test
    public void shouldAllowToReadCommentProperty() throws IOException, NoSuchAlgorithmException, JSONException
    {
        Comment comment = scopeTestUtil.createComment();
        EntityProperty property = scopeTestUtil.createCommentProperty(comment);

        assertValidRequest(HttpMethod.GET, "/rest/api/2/comment/" + comment.getId() + "/properties/" + property.getKey());
    }

    @Test
    public void shouldAllowToReadCommentProperties() throws IOException, NoSuchAlgorithmException, JSONException
    {
        Comment comment = scopeTestUtil.createComment();

        assertValidRequest(HttpMethod.GET, "/rest/api/2/comment/" + comment.getId() + "/properties/");
    }

    @Test
    public void shouldAllowUsageOfAutoCompleteSuggestions() throws IOException, NoSuchAlgorithmException
    {
        assertValidRequest(HttpMethod.GET, "/rest/api/2/jql/autocompletedata/suggestions?fieldName=issuetype&fieldValue=");
    }

}
