package it.modules.confluence;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceUserProfilePage;
import it.modules.AbstractPageTestBase;
import it.util.TestUser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import static it.matcher.IsNotBlank.isNotBlank;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of profile page in Confluence
 */
public class TestProfilePage extends AbstractPageTestBase
{
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("profilePages", "/my-awesome-profile?profile_user={profileUser.name}&profile_key={profileUser.key}");
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        RemotePluginEmbeddedTestPage page = runCanClickOnPageLinkAndSeeAddonContents(ConfluenceUserProfilePage.class, Option.<String>none());
        Map<String,String> queryParams = page.getIframeQueryParams();
        assertThat(queryParams.get("profile_user"), is("admin"));
        assertThat(queryParams.get("profile_key"), isNotBlank());
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        runner.setToggleableConditionShouldDisplay(false);

        // web item should not be displayed
        loginAndVisit(TestUser.ADMIN, ConfluenceUserProfilePage.class);
        assertThat("Expected web-item for page to NOT be present", connectPageOperations
                .existsWebItem(MY_AWESOME_PAGE_KEY), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class,
                pluginKey, MY_AWESOME_PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(MY_AWESOME_PAGE));
    }
}
