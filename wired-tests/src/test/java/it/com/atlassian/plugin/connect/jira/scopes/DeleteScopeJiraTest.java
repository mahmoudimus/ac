package it.com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.plugin.scopes.ScopeTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class DeleteScopeJiraTest extends ScopeTestBase
{
    private final JiraScopeTestUtil scopeTestUtil;

    public DeleteScopeJiraTest(TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            JwtWriterFactory jwtWriterFactory,
            ConnectAddonRegistry connectAddonRegistry,
            ApplicationProperties applicationProperties,
            JiraScopeTestUtil scopeTestUtil)
    {
        super(ScopeName.DELETE, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
        this.scopeTestUtil = scopeTestUtil;
    }

    @Test
    public void shouldAllowToRemoveProperty() throws IOException, JSONException, NoSuchAlgorithmException
    {
        Comment comment = scopeTestUtil.createComment();
        EntityProperty property = scopeTestUtil.createCommentProperty(comment);

        assertResponseCodeForRequest(HttpMethod.DELETE, "/rest/api/2/comment/" + comment.getId() + "/properties/" + property.getKey(), HttpStatus.NO_CONTENT);
    }
}
