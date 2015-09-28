package it.com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import it.com.atlassian.plugin.connect.plugin.scopes.ScopeTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class NoScopeJiraTest extends ScopeTestBase
{
    private final JiraTestUtil scopeTestUtil;

    public NoScopeJiraTest(TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            JwtWriterFactory jwtWriterFactory,
            ConnectAddonRegistry connectAddonRegistry,
            ApplicationProperties applicationProperties,
            JiraTestUtil scopeTestUtil)
    {
        super(null, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
        this.scopeTestUtil = scopeTestUtil;
    }

    @Test
    public void doesNotAllowToReadCommentProperty() throws IOException, JSONException, NoSuchAlgorithmException
    {
        Comment comment = scopeTestUtil.createComment();
        EntityProperty commentProperty = scopeTestUtil.createCommentProperty(comment);

        assertForbiddenRequest(HttpMethod.GET, "/rest/api/2/comment/" + comment.getId() + "/properties/" + commentProperty.getKey());
    }

    @Test
    public void doesNotAllowToDeleteProperty() throws IOException, JSONException, NoSuchAlgorithmException
    {
        Comment comment = scopeTestUtil.createComment();
        EntityProperty commentProperty = scopeTestUtil.createCommentProperty(comment);

        assertForbiddenRequest(HttpMethod.DELETE, "/rest/api/2/comment/" + comment.getId() + "/properties/" + commentProperty.getKey());
    }
}
